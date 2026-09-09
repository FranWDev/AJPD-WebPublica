package org.dubini.frontend_api.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.dubini.frontend_api.dto.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CacheException.class)
    public ResponseEntity<HttpResponse> handleCacheException(CacheException ex, WebRequest request) {
        log.error("[EXCEPTION] [CACHE] Error de caché en '{}': {}", request.getDescription(false), ex.getMessage());
        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Cache Error")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(BackofficeException.class)
    public ResponseEntity<HttpResponse> handleBackofficeException(BackofficeException ex, WebRequest request) {
        log.error("[EXCEPTION] [BACKOFFICE] Error de comunicación con backoffice en '{}': {}", request.getDescription(false), ex.getMessage());
        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Backoffice Service Error")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<HttpResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        log.warn("[EXCEPTION] Recurso no encontrado en '{}': {}", request.getDescription(false), ex.getMessage());
        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<HttpResponse> handleNoResourceFound(NoResourceFoundException ex, WebRequest request) {
        log.debug("[EXCEPTION] Recurso estático no encontrado en '{}': {}", request.getDescription(false), ex.getMessage());
        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Not Found")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        log.warn("[EXCEPTION] Método HTTP no soportado en '{}': {}", request.getDescription(false), ex.getMessage());
        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("Method Not Allowed")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MuseoRegistroException.class)
    public ResponseEntity<HttpResponse> handleMuseoRegistro(MuseoRegistroException ex, WebRequest request) {
        log.warn("[EXCEPTION] [MUSEO] Error en registro de visitante en '{}': {}", request.getDescription(false), ex.getMessage());
        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Museo Registration Error")
                .message(ex.getMessage())
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<HttpResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            fieldErrors.put(fieldName, error.getDefaultMessage());
        });

        log.warn("[EXCEPTION] Error de validación en '{}': {}", request.getDescription(false), fieldErrors);

        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("La solicitud contiene datos inválidos.")
                .path(request.getDescription(false))
                .data(fieldErrors)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponse> handleGlobal(Exception ex, WebRequest request) {
        log.error("[EXCEPTION] Excepción no controlada en '{}': {}", request.getDescription(false), ex.getMessage(), ex);
        HttpResponse response = HttpResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message("Ocurrió un error al procesar tu solicitud. Por favor, intenta más tarde.")
                .path(request.getDescription(false))
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
