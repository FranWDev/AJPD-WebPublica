package org.dubini.frontend_api.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.dubini.frontend_api.dto.PublicationDTO;
import org.dubini.frontend_api.service.SupabaseStorageService;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PersistentCaffeineCacheManager implements CacheManager {

    private static final String NEWS_CACHE_KEY = "newsKey";
    
    private final Map<String, CaffeineCache> caches = new ConcurrentHashMap<>();
    private final SupabaseStorageService storageService;

    public PersistentCaffeineCacheManager(SupabaseStorageService storageService) {
        this.storageService = storageService;
    }

    @PostConstruct
    public void warmUpCachesAsync() {
        List<String> cacheNames = List.of("news");
        log.info("[CACHE] Iniciando carga asíncrona de cachés persistentes...");
        for (String name : cacheNames) {
            CaffeineCache cache = (CaffeineCache) getCache(name);
            CompletableFuture.runAsync(() -> tryLoadCache(name, cache.getNativeCache()));
        }
    }

    private void tryLoadCache(String name, Cache<Object, Object> nativeCache) {
        String fileName = name + ".json";
        try {
            if (storageService.exists(fileName)) {
                // Carga específica para cache de noticias
                if ("news".equals(name)) {
                    List<PublicationDTO> newsList = storageService.downloadJson(
                            fileName, new TypeReference<List<PublicationDTO>>() {}
                    );
                    if (newsList != null && !newsList.isEmpty()) {
                        nativeCache.put(NEWS_CACHE_KEY, newsList);
                        log.info("[CACHE] Caché '{}' cargada exitosamente desde Supabase ({} noticias)", name, newsList.size());
                    }
                } else {
                    // Carga genérica para otras caches
                    Map<Object, Object> data = storageService.downloadJson(
                            fileName, new TypeReference<Map<Object, Object>>() {}
                    );
                    if (data != null && !data.isEmpty()) {
                        data.forEach(nativeCache::put);
                        log.info("[CACHE] Caché '{}' cargada exitosamente desde Supabase ({} entradas)", name, data.size());
                    }
                }
            }
        } catch (Exception e) {
            log.error("[CACHE] Error cargando caché '{}' desde Supabase: {}", name, e.getMessage(), e);
        }
    }

    @PreDestroy
    public void saveCaches() {
        log.info("[CACHE] Persistiendo cachés en Supabase...");
        caches.keySet().forEach(this::saveCache);
    }

    public void saveCache(String name) {
        CaffeineCache cache = caches.get(name);
        if (cache == null)
            return;

        String fileName = name + ".json";
        try {
            if ("news".equals(name)) {
                @SuppressWarnings("unchecked")
                List<PublicationDTO> newsList = (List<PublicationDTO>) cache.getNativeCache()
                        .getIfPresent(NEWS_CACHE_KEY);
                
                if (newsList != null && !newsList.isEmpty()) {
                    storageService.uploadJson(fileName, newsList);
                    log.info("[CACHE] Caché '{}' persistida en Supabase ({} noticias)", name, newsList.size());
                }
            } else {
                Map<Object, Object> cacheMap = cache.getNativeCache().asMap();
                if (!cacheMap.isEmpty()) {
                    storageService.uploadJson(fileName, cacheMap);
                    log.info("[CACHE] Caché '{}' persistida en Supabase ({} entradas)", name, cacheMap.size());
                }
            }
        } catch (Exception e) {
            log.error("[CACHE] Error persistiendo caché '{}' en Supabase: {}", name, e.getMessage(), e);
        }
    }

    @Override
    public org.springframework.cache.Cache getCache(String name) {
        return caches.computeIfAbsent(name, cacheName -> {
            Cache<Object, Object> nativeCache = Caffeine.newBuilder().build();
            return new CaffeineCache(cacheName, nativeCache);
        });
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }

    public boolean isEmpty(String name) {
        CaffeineCache cache = caches.get(name);
        if (cache == null) {
            return true;
        }
        
        if ("news".equals(name)) {
            Object cached = cache.getNativeCache().getIfPresent(NEWS_CACHE_KEY);
            return cached == null;
        }
        
        return cache.getNativeCache().asMap().isEmpty();
    }

    public void reloadCache(String name) {
        CaffeineCache cache = (CaffeineCache) getCache(name);
        cache.clear(); 
        CompletableFuture.runAsync(() -> tryLoadCache(name, cache.getNativeCache()))
                .exceptionally(ex -> {
                    log.error("[CACHE] Falló la recarga asíncrona de caché '{}': {}", name, ex.getMessage(), ex);
                    return null;
                });
    }
}