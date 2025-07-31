package com.theanimalmap.eventtracker.controller;

import com.theanimalmap.eventtracker.service.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventProducer producer;

    @PostMapping
    public ResponseEntity<Void> postEvent(@RequestBody String rawEventJson) {
        producer.publish("interactions", rawEventJson);
        return ResponseEntity.accepted().build();
    }
}
