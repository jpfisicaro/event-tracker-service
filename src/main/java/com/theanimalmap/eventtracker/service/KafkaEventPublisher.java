package com.theanimalmap.eventtracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theanimalmap.eventtracker.dto.EventRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${tam.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(EventRequest event) {
        String eventJson;
        try {
            eventJson = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event", e);
        }

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