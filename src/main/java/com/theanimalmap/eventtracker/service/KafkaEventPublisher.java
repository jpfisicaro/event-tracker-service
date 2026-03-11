package com.theanimalmap.eventtracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String eventJson) {
        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(topic, eventJson);

        future.thenAccept(result ->
                log.debug(
                        "Event sent to Kafka topic={}, partition={}, offset={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                )
        ).exceptionally(ex -> {
            log.error("Failed to send event to Kafka topic={}", topic, ex);
            return null;
        });
    }
}