package org.dubini.frontend_api.service;

import java.time.LocalDate;

import org.dubini.frontend_api.dto.MuseoVisitanteRegistroRequest;
import org.dubini.frontend_api.dto.MuseoVisitanteRegistroResponse;
import org.dubini.frontend_api.exception.MuseoRegistroException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MuseoRegistroService {

    private final ResendEmailService resendEmailService;
    private final RateLimiterService rateLimiterService;

    public MuseoRegistroService(ResendEmailService resendEmailService, RateLimiterService rateLimiterService) {
        this.resendEmailService = resendEmailService;
        this.rateLimiterService = rateLimiterService;
    }

    public MuseoVisitanteRegistroResponse registrarVisitante(MuseoVisitanteRegistroRequest solicitud, String clientIp) {
        log.info("[MUSEO] Procesando registro de visita para '{}' ({}) desde IP '{}'",
                solicitud.getNombre(), solicitud.getEmail(), clientIp);
        validarSolicitudCompleta(solicitud);
        validarRateLimit(clientIp);
        resendEmailService.enviarRegistroMuseo(solicitud);
        rateLimiterService.recordRequest(clientIp);
        log.info("[MUSEO] Visita registrada exitosamente para '{}' en fecha {}", solicitud.getNombre(), solicitud.getFecha());
        return new MuseoVisitanteRegistroResponse(
                solicitud.getNombre().trim(),
                solicitud.getEmail().trim(),
                solicitud.getTelefono().trim(),
                solicitud.getTipoCaridad().trim(),
                solicitud.getNumPersonas(),
                solicitud.getFecha().toString(),
                solicitud.getHoraRango().trim());
    }

    private void validarSolicitudCompleta(MuseoVisitanteRegistroRequest solicitud) {
        if (solicitud == null) {
            log.warn("[MUSEO] Validación fallida: solicitud de visita nula");
            throw new MuseoRegistroException("No se recibió información para registrar la visita.");
        }

        validarTexto(solicitud.getEmail(), "email");
        validarTexto(solicitud.getTelefono(), "teléfono");
        validarTexto(solicitud.getHoraRango(), "rango horario");

        LocalDate fecha = solicitud.getFecha();
        if (fecha == null) {
            log.warn("[MUSEO] Validación fallida para '{}': fecha de visita es nula", solicitud.getEmail());
            throw new MuseoRegistroException("La fecha de visita es obligatoria.");
        }
        if (fecha.isBefore(LocalDate.now())) {
            log.warn("[MUSEO] Validación fallida para '{}': fecha {} es anterior a hoy", solicitud.getEmail(), fecha);
            throw new MuseoRegistroException("La fecha de visita no puede ser anterior al día actual.");
        }
    }

    private void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new MuseoRegistroException("El campo " + campo + " es obligatorio.");
        }
    }

    private void validarRateLimit(String ip) {
        if (!rateLimiterService.canMakeRequest(ip)) {
            log.warn("[MUSEO] Solicitud rechazada por exceso de tasa para IP '{}'", ip);
            throw new MuseoRegistroException(rateLimiterService.getRateLimitMessage());
        }
    }
}
