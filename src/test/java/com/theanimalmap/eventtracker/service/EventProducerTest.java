package com.theanimalmap.eventtracker.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class EventProducerTest {

    @Test
    void publish_shouldSendEventToKafkaTemplate() {
        KafkaTemplate<String, String> kafkaTemplate = Mockito.mock(KafkaTemplate.class);

        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);

        when(kafkaTemplate.send("tam-events", "{\"x\":1}")).thenReturn(future);

        EventProducer producer = new EventProducer(kafkaTemplate);

        producer.publish("tam-events", "{\"x\":1}");

        verify(kafkaTemplate).send("tam-events", "{\"x\":1}");
        verifyNoMoreInteractions(kafkaTemplate);
    }
}