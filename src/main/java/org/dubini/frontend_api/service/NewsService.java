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
                    log.info("Cache warmed up with {} news items", data.size());
                })
                .doOnSuccess(v -> {
                    if (cacheManager instanceof PersistentCaffeineCacheManager pcm) {
                        pcm.saveCache(CACHE_NAME);
                    }
                })
                .then()
                .onErrorResume(e -> {
                    log.warn("Backoffice failed during warmup, attempting fallback from disk: {}", e.getMessage());
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
            log.error("Error converting cache value to List<PublicationDTO>: {}", e.getMessage());
            return null;
        }
    }

    public Mono<List<PublicationDTO>> get() {
        Cache cache = cacheManager.getCache(CACHE_NAME);
        if (cache == null) {
            log.error("✗ Cache {} no encontrada en CacheManager", CACHE_NAME);
            return Mono.error(new CacheException("Cache no inicializada"));
        }

        List<PublicationDTO> cached = getFromCache(cache);
        if (cached != null && !cached.isEmpty()) {
            log.info("✓ Returning {} news from in-memory cache", cached.size());
            if (newsByTitleCache.isEmpty()) {
                for (PublicationDTO pub : cached) {
                    if (pub.getTitle() != null) {
                        newsByTitleCache.put(normalizeTitle(pub.getTitle()), pub);
                    }
                }
            }
            return Mono.just(cached);
        }

        log.warn("Cache empty, calling backoffice to populate it...");

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
                    log.info("Cache updated with {} news items", news.size());
                    if (cacheManager instanceof PersistentCaffeineCacheManager pcm) {
                        pcm.saveCache(CACHE_NAME);
                    }
                })
                .onErrorResume(e -> {
                    log.warn("Backoffice failed, attempting fallback from disk: {}", e.getMessage());
                    return fallbackFromDisk(cache);
                });
    }

    private Mono<List<PublicationDTO>> fallbackFromDisk(Cache cache) {
        if (cacheManager instanceof PersistentCaffeineCacheManager pcm) {
            log.info("In-memory cache empty, attempting to load from disk...");
            pcm.reloadCache(CACHE_NAME);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            List<PublicationDTO> cached = getFromCache(cache);
            if (cached != null && !cached.isEmpty()) {
                log.info("✓ Returning {} news from disk cache", cached.size());

                cache.put(CACHE_KEY, cached);
                newsByTitleCache.clear();
                for (PublicationDTO pub : cached) {
                    if (pub.getTitle() != null) {
                        newsByTitleCache.put(normalizeTitle(pub.getTitle()), pub);
                    }
                }
                log.info("In-memory cache repopulated with {} news items from disk", cached.size());

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
        log.debug("Searching for news with normalized title: {}", normalizedRequestTitle);

        PublicationDTO found = newsByTitleCache.get(normalizedRequestTitle);
        if (found != null) {
            log.info("✓ Found news with title in O(1): {} (normalized: {})", found.getTitle(),
                    normalizedRequestTitle);
            return Mono.just(found);
        }

        return get()
                .flatMap(newsList -> {
                    PublicationDTO retryFound = newsByTitleCache.get(normalizedRequestTitle);
                    if (retryFound != null) {
                        log.info("✓ Found news with title after refresh in O(1): {} (normalized: {})", retryFound.getTitle(),
                                normalizedRequestTitle);
                        return Mono.just(retryFound);
                    } else {
                        log.warn("✗ News with normalized title '{}' not found", normalizedRequestTitle);
                        return Mono.error(new BackofficeException(
                                String.format("News with title '%s' not found", title)));
                    }
                });
    }

    public void clear() {
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