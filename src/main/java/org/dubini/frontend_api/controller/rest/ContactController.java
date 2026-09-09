package org.dubini.frontend_api.controller.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dubini.frontend_api.dto.ContactRequest;
import org.dubini.frontend_api.dto.HttpResponse;
import org.dubini.frontend_api.service.RateLimiterService;
import org.dubini.frontend_api.service.ResendEmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/contacto")
@RequiredArgsConstructor
public class ContactController {

    private final ResendEmailService resendEmailService;
    private final RateLimiterService rateLimiterService;

    @PostMapping
    public ResponseEntity<HttpResponse> enviarMensaje(@Valid @RequestBody ContactRequest request, HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        log.info("[API] [CONTACTO] Petición de contacto recibida desde IP '{}' ({}) - Asunto: '{}'",
                clientIp, request.getEmail(), request.getAsunto());

        if (!rateLimiterService.canMakeRequest(clientIp, "contacto")) {
            log.warn("[API] [CONTACTO] Solicitud bloqueada por límite de tasa desde IP '{}'", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(HttpResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.TOO_MANY_REQUESTS.value())
                            .message("Has enviado demasiados mensajes. Por favor, espera una hora.")
                            .error("Rate limit exceeded")
                            .path("/api/contacto")
                            .build());
        }

        try {
            // Sanitizar entrada para prevenir XSS antes de procesar el email
            request.sanitize();
            
            resendEmailService.enviarEmailContacto(request);
            rateLimiterService.recordRequest(clientIp, "contacto");

            log.info("[API] [CONTACTO] Mensaje de contacto enviado con éxito para '{}'", request.getEmail());
            return ResponseEntity.ok(HttpResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.OK.value())
                    .message("¡Mensaje enviado con éxito! Nos pondremos en contacto contigo pronto.")
                    .path("/api/contacto")
                    .build());
        } catch (Exception e) {
            log.error("[API] [CONTACTO] Error al procesar mensaje de contacto para '{}': {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(HttpResponse.builder()
                            .timestamp(LocalDateTime.now())
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .message("No se pudo enviar el mensaje. Por favor, inténtalo de nuevo más tarde.")
                            .error(e.getMessage())
                            .path("/api/contacto")
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
