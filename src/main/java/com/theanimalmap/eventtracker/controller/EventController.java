package com.theanimalmap.eventtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theanimalmap.eventtracker.dto.EventRequest;
import com.theanimalmap.eventtracker.service.EventProducer;
import com.theanimalmap.eventtracker.validation.EventValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventProducer producer;
    private final ObjectMapper objectMapper;
    private final EventValidator validator;

    public EventController(EventProducer producer, ObjectMapper objectMapper, EventValidator validator) {
        this.producer = producer;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostMapping
    public ResponseEntity<Void> postEvent(@Valid @RequestBody EventRequest req) {
        validator.validate(req.type(), req.payload());
        log.debug("Received event type={}", req.type());

        try {
            String eventJson = objectMapper.writeValueAsString(req);
            producer.publish("interactions", eventJson);
        } catch (JsonProcessingException e) {
            // Should never happen for a simple DTO, but fail fast if it does
            throw new IllegalStateException("Failed to serialize event request", e);
        }

        return ResponseEntity.accepted().build();
    }
}