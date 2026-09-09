package org.dubini.frontend_api.controller.rest;

import java.util.List;

import org.dubini.frontend_api.dto.PublicationDTO;
import org.dubini.frontend_api.service.CacheEtagService;
import org.dubini.frontend_api.service.NewsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NewsRestController {

    private final NewsService newsService;
    private final CacheEtagService cacheEtagService;

    @GetMapping("/api/news")
    public Mono<ResponseEntity<List<PublicationDTO>>> getNews() {
        return newsService.get()
                .map(newsList -> {
                    String etag = cacheEtagService.calculateEtag(newsList);
                    log.debug("[API] [NEWS] Retornando {} noticias con ETag: {}", newsList.size(), etag);

                    return ResponseEntity.ok()
                            .header("ETag", etag)
                            .body(newsList);
                })
                .onErrorResume(e -> {
                    log.error("[API] [NEWS] Error al consultar listado de noticias: {}", e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @GetMapping("/api/news/title/{title}")
    public Mono<ResponseEntity<PublicationDTO>> getNewsByTitle(@PathVariable String title) {
        return newsService.getByTitle(title)
                .map(news -> {
                    String etag = cacheEtagService.calculateEtag(news);
                    log.debug("[API] [NEWS] Retornando noticia '{}' con ETag: {}", title, etag);

                    return ResponseEntity.ok()
                            .header("ETag", etag)
                            .body(news);
                })
                .onErrorResume(e -> {
                    log.warn("[API] [NEWS] Error al consultar noticia por título '{}': {}", title, e.getMessage());

                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.badRequest().build());
                    }

                    if (e.getMessage().contains("not found")) {
                        return Mono.just(ResponseEntity.notFound().build());
                    }

                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}