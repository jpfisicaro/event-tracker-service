package com.theanimalmap.eventtracker.controller;

import com.theanimalmap.eventtracker.service.EventProducer;
import com.theanimalmap.eventtracker.validation.EventValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@Import(ApiExceptionHandler.class) // para que se apliquen los 400 custom
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventProducer producer;

    @MockitoBean
    private EventValidator validator;

    @Test
    void postEvent_validEvent_shouldReturn202_andPublish() throws Exception {
        // validator.validate(...) no hace nada (OK)
        String body = """
                {"type":"search_animal","payload":{"animal":"lion"}}
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isAccepted());

        verify(validator).validate(eq("search_animal"), any());
        verify(producer).publish(eq("interactions"), any(String.class));
    }

    @Test
    void postEvent_unknownType_shouldReturn400_andNotPublish() throws Exception {
        doThrow(new IllegalArgumentException("unknown type: whatever"))
                .when(validator).validate(eq("whatever"), any());

        String body = """
                {"type":"whatever","payload":{"animal":"lion"}}
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("unknown type: whatever"));

        verify(producer, never()).publish(anyString(), anyString());
    }

    @Test
    void postEvent_missingRequiredFieldInPayload_shouldReturn400_andNotPublish() throws Exception {
        doThrow(new IllegalArgumentException("missing field: animal"))
                .when(validator).validate(eq("search_animal"), any());

        String body = """
                {"type":"search_animal","payload":{}}
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("missing field: animal"));

        verify(producer, never()).publish(anyString(), anyString());
    }

    @Test
    void postEvent_invalidJson_shouldReturn400_andNotPublish() throws Exception {
        String body = """
                {"type":"search_animal","payload":
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(producer, never()).publish(anyString(), anyString());
    }

    @Test
    void postEvent_missingType_shouldReturn400_andNotPublish() throws Exception {
        // Si tenés @Valid en el controller + starter validation/provider,
        // esto debería dar 400 por Bean Validation.
        String body = """
                {"payload":{"animal":"lion"}}
                """;

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(producer, never()).publish(anyString(), anyString());
    }
}