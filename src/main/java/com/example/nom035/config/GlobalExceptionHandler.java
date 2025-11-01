package com.example.nom035.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.security.access.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("[GlobalExceptionHandler] Excepción no controlada:", ex);
        return new ResponseEntity<>("Error interno del servidor: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
    // Log at warn level and return 403 with JSON so frontend can handle authorization errors correctly
    logger.warn("[GlobalExceptionHandler] Acceso denegado: {}", ex.getMessage());
    String body = "{\"error\":\"Acceso denegado: no tienes permisos suficientes para esta acción.\"}";
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .header("Content-Type", "application/json")
        .body(body);
    }
}
