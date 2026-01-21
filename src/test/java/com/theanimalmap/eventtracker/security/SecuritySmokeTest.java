package com.theanimalmap.eventtracker.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class SecuritySmokeTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void postEvents_withApiKey_isAllowed() throws Exception {
        mockMvc.perform(post("/events")
                        .header("X-API-KEY", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"search_animal\",\"payload\":{\"animal\":\"lion\"}}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void postEvents_withoutApiKey_isUnauthorized() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"search_animal\",\"payload\":{\"animal\":\"lion\"}}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void otherEndpoints_areUnauthorized() throws Exception {
        mockMvc.perform(get("/whatever"))
                .andExpect(status().isUnauthorized());
    }
}