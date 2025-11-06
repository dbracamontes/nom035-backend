package com.example.nom035.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;

/**
 * Public endpoint to collect lightweight frontend network logs / diagnostics.
 * Permitted by security config via /api/public/** matcher.
 */
@RestController
@RequestMapping("/api/public/logs")
public class LoggingController {
    private static final Logger log = LoggerFactory.getLogger(LoggingController.class);

    @PostMapping
    public ResponseEntity<Void> ingest(@RequestBody(required = false) Map<String, Object> payload,
                                       HttpServletRequest request) {
        try {
            log.info("[FE_LOG] ts={} remote={} ua={} payload={}",
                    Instant.now().toString(),
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    payload);
        } catch (Exception e) {
            log.warn("Failed to ingest frontend log: {}", e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }
}
