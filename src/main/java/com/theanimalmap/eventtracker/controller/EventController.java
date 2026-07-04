package com.theanimalmap.eventtracker.controller;

import com.theanimalmap.eventtracker.dto.EventRequest;
import com.theanimalmap.eventtracker.service.EventPublisher;
import com.theanimalmap.eventtracker.validation.EventValidator;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger log = LoggerFactory.getLogger(EventController.class);

    private final EventPublisher publisher;
    private final EventValidator validator;

    private final MeterRegistry meterRegistry;

    public EventController(EventPublisher publisher, EventValidator validator, MeterRegistry meterRegistry) {
        this.publisher = publisher;
        this.validator = validator;
        this.meterRegistry = meterRegistry;
    }

    @Operation(
            summary = "Track a user event",
            description = "Receives a click, search, or filter event and publishes it to Kafka for downstream analytics processing."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event accepted and published to Kafka"),
            @ApiResponse(responseCode = "400", description = "Invalid event payload"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
    @PostMapping
    public ResponseEntity<Void> postEvent(@Valid @RequestBody EventRequest req) {
        validator.validate(req.type(), req.payload());
        log.debug("Received event type={}, payload={}", req.type(), req.payload());
        publisher.publish(req);
        Counter.builder("tam.events.received")
                .tag("type", req.type())
                .register(meterRegistry)
                .increment();
        return ResponseEntity.accepted().build();
    }
}