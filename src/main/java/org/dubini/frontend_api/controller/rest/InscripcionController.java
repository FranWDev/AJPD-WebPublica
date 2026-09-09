package org.dubini.frontend_api.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.dubini.frontend_api.dto.HttpResponse;
import org.dubini.frontend_api.dto.InscripcionRequest;
import org.dubini.frontend_api.service.RateLimiterService;
import org.dubini.frontend_api.service.ResendEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/inscripcion")
@RequiredArgsConstructor
public class InscripcionController {

    private final ResendEmailService resendEmailService;
    private final RateLimiterService rateLimiterService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HttpResponse> registrarInscripcion(@ModelAttribute InscripcionRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        log.info("[API] [INSCRIPCION] Petición de inscripción recibida desde IP '{}' para aspirante '{}' ({})",
                clientIp, request.getNombre(), request.getEmail());

        if (!rateLimiterService.canMakeRequest(clientIp, "inscripcion")) {
            log.warn("[API] [INSCRIPCION] Solicitud bloqueada por límite de tasa desde IP '{}'", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(HttpResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.TOO_MANY_REQUESTS.value())
                            .message("Ya hemos recibido una solicitud desde tu dirección IP hoy. Por favor, espera 24 horas.")
                            .error("Rate limit exceeded")
                            .path("/api/inscripcion")
                            .build());
        }

        try {
            resendEmailService.enviarInscripcion(request);
            rateLimiterService.recordRequest(clientIp, "inscripcion");

            log.info("[API] [INSCRIPCION] Solicitud de inscripción tramitada exitosamente para '{}'", request.getEmail());
            return ResponseEntity.ok(HttpResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .message("¡Inscripción recibida con éxito! Revisa tu correo electrónico para la confirmación.")
                    .path("/api/inscripcion")
                    .build());
        } catch (Exception e) {
            log.error("[API] [INSCRIPCION] Error al procesar inscripción para '{}': {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(HttpResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("No se pudo procesar tu inscripción. Por favor, inténtalo de nuevo más tarde.")
                            .error(e.getMessage())
                            .path("/api/inscripcion")
                            .build());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
