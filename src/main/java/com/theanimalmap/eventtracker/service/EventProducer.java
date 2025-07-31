package com.theanimalmap.eventtracker.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public EventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String eventJson) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, eventJson);

        future.thenAccept(result -> {
            System.out.println("Enviado OK: " + result.getRecordMetadata().offset());
        });

    }
}