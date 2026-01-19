package com.theanimalmap.eventtracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    // ✅ JSON inválido / body mal formado => 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> unreadable(HttpMessageNotReadableException ex) {
        log.warn("Invalid JSON body: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", "invalid json"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", "invalid request body"));
    }

    // ✅ bugs reales / cosas no previstas => 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> unhandled(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.internalServerError().body(Map.of("error", "internal error"));
    }
}