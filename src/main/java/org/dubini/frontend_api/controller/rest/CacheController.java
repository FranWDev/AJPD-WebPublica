package org.dubini.frontend_api.controller.rest;

import org.dubini.frontend_api.dto.HttpResponse;
import org.dubini.frontend_api.service.CacheEtagService;
import org.dubini.frontend_api.service.NewsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CacheController {

    private final NewsService newsService;
    private final CacheEtagService cacheEtagService;

    @GetMapping("/api/news/last")
    public Mono<ResponseEntity<Void>> checkNewsUpdateGet(
            @RequestHeader(value = "If-None-Match", required = false) String clientEtag) {

        return newsService.get()
                .map(newsList -> {
                    String serverEtag = cacheEtagService.calculateEtag(newsList);
                    boolean hasChanged = cacheEtagService.hasChanged(clientEtag, serverEtag);

                    log.debug("[API] [CACHE] Comprobación de ETag (GET) - Cliente: '{}', Servidor: '{}', Modificado: {}",
                            clientEtag, serverEtag, hasChanged);

                    HttpHeaders headers = new HttpHeaders();
                    headers.set("ETag", serverEtag);

                    HttpStatus status = hasChanged ? HttpStatus.OK : HttpStatus.NOT_MODIFIED;

                    return new ResponseEntity<Void>(headers, status);
                })
                .onErrorResume(e -> {
                    log.error("[API] [CACHE] Error al verificar actualización de noticias (GET): {}", e.getMessage());
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @RequestMapping(value = "/api/news/last", method = RequestMethod.HEAD)
    public Mono<ResponseEntity<Void>> checkNewsUpdateHead(
            @RequestHeader(value = "If-None-Match", required = false) String clientEtag) {

        return newsService.get()
                .map(newsList -> {
                    String serverEtag = cacheEtagService.calculateEtag(newsList);
                    boolean hasChanged = cacheEtagService.hasChanged(clientEtag, serverEtag);

                    log.debug("[API] [CACHE] Comprobación de ETag (HEAD) - Cliente: '{}', Servidor: '{}', Modificado: {}",
                            clientEtag, serverEtag, hasChanged);

                    HttpHeaders headers = new HttpHeaders();
                    headers.set("ETag", serverEtag);

                    HttpStatus status = hasChanged ? HttpStatus.OK : HttpStatus.NOT_MODIFIED;

                    return new ResponseEntity<Void>(headers, status);
                })
                .onErrorResume(e -> {
                    log.error("[API] [CACHE] Error al verificar actualización de noticias (HEAD): {}", e.getMessage());
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    @GetMapping("/api/cache/news/clear")
    public Mono<ResponseEntity<HttpResponse>> clearNewsCache() {
        log.info("[API] [CACHE] Petición de purga y recarga de caché de noticias recibida");
        cacheEtagService.clearEtagCache();

        return newsService.warmUpCache()
                .thenReturn(ResponseEntity.ok(HttpResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .message("News and ETag cache cleared")
                        .build()))
                .onErrorResume(e -> handleError(e));
    }

    private Mono<ResponseEntity<HttpResponse>> handleError(Throwable e) {
        log.error("[API] [CACHE] Error durante la recarga de caché de noticias: {}", e.getMessage(), e);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(HttpResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Internal Server Error")
                        .message(e.getMessage())
                        .build()));
    }
}