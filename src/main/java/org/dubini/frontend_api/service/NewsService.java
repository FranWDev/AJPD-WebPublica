package org.dubini.frontend_api.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dubini.frontend_api.cache.CacheWarmable;
import org.dubini.frontend_api.cache.PersistentCaffeineCacheManager;
import org.dubini.frontend_api.client.NewsClient;
import org.dubini.frontend_api.dto.PublicationDTO;
import org.dubini.frontend_api.exception.BackofficeException;
import org.dubini.frontend_api.exception.CacheException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService implements CacheWarmable {

    private static final String CACHE_NAME = "news";
    private static final String CACHE_KEY = "newsKey";

    private final NewsClient newsClient;
    private final CacheManager cacheManager;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final ObjectMapper objectMapper;

    // Mapa para consultas rápidas O(1) en memoria
    private final Map<String, PublicationDTO> newsByTitleCache = new ConcurrentHashMap<>();

    /**
     * Registra y devuelve un CircuitBreaker con configuración explícita
     */
    private CircuitBreaker getNewsCircuitBreaker() {
        return circuitBreakerRegistry.circuitBreaker("newsCircuitBreaker",
                () -> CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .slidingWindowSize(10)
                        .build());
    }

    @Override
    public String getCacheName() {
        return CACHE_NAME;
    }

    @Override
    public Mono<Void> warmUpCache() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null)
            return Mono.empty();

        cache.clear();

        return newsClient.get()
                .transformDeferred(CircuitBreakerOperator.of(getNewsCircuitBreaker()))
                .doOnNext(data -> {
                    cache.put(CACHE_KEY, data);
                    newsByTitleCache.clear();
                    for (PublicationDTO pub : data) {
                        if (pub.getTitle() != null) {
                            newsByTitleCache.put(normalizeTitle(pub.getTitle()), pub);
                        }
                    }
                    log.info("[CACHE] [NEWS] Precalentamiento completado con {} noticias", data.size());
                })
                .doOnSuccess(v -> {
                    if (cacheManager instanceof PersistentCaffeineCacheManager pcm) {
                        pcm.saveCache(CACHE_NAME);
                    }
                })
                .then()
                .onErrorResume(e -> {
                    log.warn("[CACHE] [NEWS] Falló conexión con backoffice durante precalentamiento, usando fallback de disco: {}", e.getMessage());
                    return fallbackFromDisk(cache).then();
                });
    }

    @SuppressWarnings("unchecked")
    private List<PublicationDTO> getFromCache(Cache cache) {
        Object cached = cache.get(CACHE_KEY, Object.class);
        if (cached == null) {
            return null;
        }

        if (cached instanceof List<?> list && !list.isEmpty()
                && list.get(0) instanceof PublicationDTO) {
            return (List<PublicationDTO>) cached;
        }

        try {
            return objectMapper.convertValue(cached,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, PublicationDTO.class));
        } catch (Exception e) {
            log.error("[CACHE] [NEWS] Error al convertir valor de caché a lista de noticias: {}", e.getMessage());
            return null;
        }
    }

    public Mono<List<PublicationDTO>> get() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.error("[CACHE] [NEWS] Caché '{}' no encontrada en CacheManager", CACHE_NAME);
            return Mono.error(new CacheException("Cache no inicializada"));
        }

        List<PublicationDTO> cached = getFromCache(cache);
        if (cached != null && !cached.isEmpty()) {
            log.info("[CACHE] [NEWS] Retornando {} noticias desde memoria", cached.size());
            if (newsByTitleCache.isEmpty()) {
                for (PublicationDTO pub : cached) {
                    if (pub.getTitle() != null) {
                        newsByTitleCache.put(normalizeTitle(pub.getTitle()), pub);
                    }
                }
            }
            return Mono.just(cached);
        }

        log.info("[CACHE] [NEWS] Memoria vacía, solicitando noticias a backoffice...");

        return newsClient.get()
                .transformDeferred(CircuitBreakerOperator.of(getNewsCircuitBreaker()))
                .doOnNext(news -> {
                    cache.put(CACHE_KEY, news);
                    newsByTitleCache.clear();
                    for (PublicationDTO pub : news) {
                        if (pub.getTitle() != null) {
                            newsByTitleCache.put(normalizeTitle(pub.getTitle()), pub);
                        }
                    }
                    log.info("[CACHE] [NEWS] Caché actualizada exitosamente con {} noticias", news.size());
                    if (cacheManager instanceof PersistentCaffeineCacheManager pcm) {
                        pcm.saveCache(CACHE_NAME);
                    }
                })
                .onErrorResume(e -> {
                    log.warn("[CACHE] [NEWS] Falló consulta al backoffice, usando fallback de disco: {}", e.getMessage());
                    return fallbackFromDisk(cache);
                });
    }

    private Mono<List<PublicationDTO>> fallbackFromDisk(Cache cache) {
        if (cacheManager instanceof PersistentCaffeineCacheManager pcm) {
            log.info("[CACHE] [NEWS] Recargando noticias desde almacenamiento persistente...");
            pcm.reloadCache(CACHE_NAME);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<PublicationDTO> cached = getFromCache(cache);
            if (cached != null && !cached.isEmpty()) {
                log.info("[CACHE] [NEWS] Retornando {} noticias desde almacenamiento en disco", cached.size());

                cache.put(CACHE_KEY, cached);
                newsByTitleCache.clear();
                for (PublicationDTO pub : cached) {
                    if (pub.getTitle() != null) {
                        newsByTitleCache.put(normalizeTitle(pub.getTitle()), pub);
                    }
                }
                log.info("[CACHE] [NEWS] Memoria reabastecida con {} noticias desde disco", cached.size());

                return Mono.just(cached);
            }
        }

        return Mono.error(new BackofficeException("Backoffice unavailable and no cache available"));
    }

    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }

        return title.toLowerCase()
                .replaceAll("[áàäâ]", "a")
                .replaceAll("[éèëê]", "e")
                .replaceAll("[íìïî]", "i")
                .replaceAll("[óòöô]", "o")
                .replaceAll("[úùüû]", "u")
                .replaceAll("[ñ]", "n")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .trim();
    }

    public Mono<PublicationDTO> getByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Title cannot be null or empty"));
        }

        String normalizedRequestTitle = normalizeTitle(title);
        log.debug("[NEWS] Buscando noticia por título normalizado: '{}'", normalizedRequestTitle);

        PublicationDTO found = newsByTitleCache.get(normalizedRequestTitle);
        if (found != null) {
            log.info("[NEWS] Noticia localizada en caché: '{}' (normalizado: '{}')", found.getTitle(),
                    normalizedRequestTitle);
            return Mono.just(found);
        }

        return get()
                .flatMap(newsList -> {
                    PublicationDTO retryFound = newsByTitleCache.get(normalizedRequestTitle);
                    if (retryFound != null) {
                        log.info("[NEWS] Noticia localizada tras refresco de caché: '{}' (normalizado: '{}')", retryFound.getTitle(),
                                normalizedRequestTitle);
                        return Mono.just(retryFound);
                    } else {
                        log.warn("[NEWS] Noticia con título normalizado '{}' no encontrada", normalizedRequestTitle);
                        return Mono.error(new BackofficeException(
                                String.format("News with title '%s' not found", title)));
                    }
                });
    }

    public void clear() {
        log.info("[CACHE] [NEWS] Petición de purga de caché recibida");
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache != null)
            cache.clear();

        newsByTitleCache.clear();

        if (cacheManager instanceof PersistentCaffeineCacheManager pcm) {
            pcm.saveCache(CACHE_NAME);
        }

        warmUpCache().subscribe();
    }
}