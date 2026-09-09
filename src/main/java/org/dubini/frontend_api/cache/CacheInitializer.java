package org.dubini.frontend_api.cache;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInitializer {

    private final List<CacheWarmable> warmables;

    @PostConstruct
    public void init() {
        for (CacheWarmable w : warmables) {
            w.warmUpCache()
                    .doOnSubscribe(sub -> log.info("[CACHE] Inicializando precalentamiento de caché '{}'", w.getCacheName()))
                    .doOnError(err -> log.error("[CACHE] Falló el precalentamiento de caché '{}': {}", w.getCacheName(), err.getMessage()))
                    .doOnSuccess(v -> log.info("[CACHE] Precalentamiento completado para caché '{}'", w.getCacheName()))
                    .subscribe();
        }
    }
}
