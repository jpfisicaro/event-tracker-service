package com.theanimalmap.eventtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theanimalmap.eventtracker.dto.EventRequest;
import com.theanimalmap.eventtracker.service.EventPublisher;
import com.theanimalmap.eventtracker.validation.EventValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final String topic;
    private final EventPublisher publisher;
    private final ObjectMapper objectMapper;
    private final EventValidator validator;

    public EventController(EventPublisher publisher, ObjectMapper objectMapper, EventValidator validator, @Value("${tam.kafka.topic}") String topic) {
        this.publisher = publisher;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.topic = topic;
    }

    @PostMapping
    public ResponseEntity<Void> postEvent(@Valid @RequestBody EventRequest req) {
        validator.validate(req.type(), req.payload());
        log.debug("Received event type={}, event payload={}", req.type(), req.payload());

        try {
            String eventJson = objectMapper.writeValueAsString(req);
            publisher.publish(topic, eventJson);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event request", e);
        }

        return ResponseEntity.accepted().build();
    }
}