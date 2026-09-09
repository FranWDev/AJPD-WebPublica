package org.dubini.frontend_api.controller;

import java.time.LocalDateTime;

import org.dubini.frontend_api.dto.HttpResponse;
import org.dubini.frontend_api.dto.MuseoVisitanteRegistroRequest;
import org.dubini.frontend_api.dto.MuseoVisitanteRegistroResponse;
import org.dubini.frontend_api.exception.MuseoRegistroException;
import org.dubini.frontend_api.service.MuseoRegistroService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/museo")
@Validated
public class MuseoController {

    private final MuseoRegistroService museoRegistroService;

    public MuseoController(MuseoRegistroService museoRegistroService) {
        this.museoRegistroService = museoRegistroService;
    }

    @PostMapping("/visitantes")
    public ResponseEntity<HttpResponse> registrarVisitante(@Valid @RequestBody MuseoVisitanteRegistroRequest solicitud,
            HttpServletRequest request) {
        
        // Sanitizar entrada para prevenir XSS
        solicitud.sanitize();

        if (envioSolicitudesDeshabilitado()) {
            log.warn("[API] [MUSEO] Solicitud rechazada: el envío de solicitudes está deshabilitado");
            throw new MuseoRegistroException("El envío de solicitudes no está habilitado en este momento.");
        }

        String clientIp = extractClientIp(request);
        log.info("[API] [MUSEO] Petición de registro de visitante recibida desde IP '{}' ({})", clientIp, solicitud.getEmail());
        MuseoVisitanteRegistroResponse registro = museoRegistroService.registrarVisitante(solicitud, clientIp);

        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .message("Inscripción recibida. Se ha enviado confirmación por correo.")
                .path(request.getRequestURI())
                .data(registro)
                .build();

        log.info("[API] [MUSEO] Registro completado exitosamente para '{}'", solicitud.getEmail());
        return ResponseEntity.ok(response);
    }

    private boolean envioSolicitudesDeshabilitado() {
        return false;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
