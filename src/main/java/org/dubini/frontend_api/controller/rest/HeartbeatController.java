package org.dubini.frontend_api.controller.rest;

import org.dubini.frontend_api.dto.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@RestController
public class HeartbeatController {

    @GetMapping("/api/heartbeat")
    public Mono<ResponseEntity<HttpResponse>> heartbeat() {
        log.debug("[API] [HEARTBEAT] Comprobación de salud recibida");
        return Mono.just(ResponseEntity.ok(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.OK.value())
                        .message("Heartbeat OK - service is active")
                        .build()
        ));
    }
}
