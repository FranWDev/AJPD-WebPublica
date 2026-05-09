package org.dubini.frontend_api.service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class RateLimiterService {

    private static final String RATE_LIMIT_MESSAGE = "Demasiadas solicitudes desde tu dirección IP. Por favor, intenta más tarde.";
    private static final boolean ENABLED = true;

    private final Map<String, Cache<String, Instant>> caches = new ConcurrentHashMap<>();

    public RateLimiterService() {
        // Inicializar caché por defecto para el museo (24h como estaba antes)
        createCache("museo", 24, TimeUnit.HOURS);
        // Inicializar caché para contacto (1h recomendado)
        createCache("contacto", 1, TimeUnit.HOURS);
        // Inicializar caché para inscripción (24h)
        createCache("inscripcion", 24, TimeUnit.HOURS);
    }

    private void createCache(String context, long duration, TimeUnit unit) {
        Cache<String, Instant> cache = Caffeine.newBuilder()
                .expireAfterWrite(duration, unit)
                .maximumSize(10000)
                .build();
        caches.put(context, cache);
    }

    public boolean canMakeRequest(String ip) {
        return canMakeRequest(ip, "museo");
    }

    public boolean canMakeRequest(String ip, String context) {
        if (!ENABLED) {
            return true;
        }
        Cache<String, Instant> cache = caches.get(context);
        return cache == null || cache.getIfPresent(ip) == null;
    }

    public void recordRequest(String ip) {
        recordRequest(ip, "museo");
    }

    public void recordRequest(String ip, String context) {
        Cache<String, Instant> cache = caches.get(context);
        if (cache != null) {
            cache.put(ip, Instant.now());
        }
    }

    public String getRateLimitMessage() {
        return RATE_LIMIT_MESSAGE;
    }

    public Instant getLastRequestTime(String ip) {
        return getLastRequestTime(ip, "museo");
    }

    public Instant getLastRequestTime(String ip, String context) {
        Cache<String, Instant> cache = caches.get(context);
        return cache != null ? cache.getIfPresent(ip) : null;
    }
}
