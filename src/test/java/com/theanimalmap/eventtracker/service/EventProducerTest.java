package com.theanimalmap.eventtracker.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class EventProducerTest {

    @Test
    void publish_shouldSendEventToKafkaTemplate() {
        KafkaTemplate<String, String> kafkaTemplate = Mockito.mock(KafkaTemplate.class);

        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send("tam-events", "{\"x\":1}")).thenReturn(future);

        KafkaEventPublisher producer = new KafkaEventPublisher(kafkaTemplate);

        producer.publish("tam-events", "{\"x\":1}");

        verify(kafkaTemplate).send("tam-events", "{\"x\":1}");
        verifyNoMoreInteractions(kafkaTemplate);
    }
}