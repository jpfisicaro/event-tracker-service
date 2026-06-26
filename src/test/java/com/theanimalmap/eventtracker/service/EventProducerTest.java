package com.theanimalmap.eventtracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theanimalmap.eventtracker.dto.EventRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class EventProducerTest {

    @Test
    void publish_shouldSendSerializedEventToKafkaTemplate() throws Exception {
        KafkaTemplate<String, String> kafkaTemplate = Mockito.mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(eq("tam-events"), anyString())).thenReturn(future);

        KafkaEventPublisher producer = new KafkaEventPublisher(kafkaTemplate, objectMapper, "tam-events");

        EventRequest event = new EventRequest("search_animal", objectMapper.readTree("{\"animal\":\"lion\"}"));
        producer.publish(event);

        verify(kafkaTemplate).send(eq("tam-events"), anyString());
        verifyNoMoreInteractions(kafkaTemplate);
    }
}
