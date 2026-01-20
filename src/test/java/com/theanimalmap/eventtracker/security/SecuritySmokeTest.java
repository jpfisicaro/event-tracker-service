package com.theanimalmap.eventtracker.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecuritySmokeTest {

    @Autowired MockMvc mockMvc;

    @Test
    void postEvents_isAllowed() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"search_animal\",\"payload\":{\"animal\":\"lion\"}}"))
                .andExpect(status().isAccepted());
    }

    @Test
    void otherEndpoints_areDenied() throws Exception {
        mockMvc.perform(get("/whatever"))
                .andExpect(status().isForbidden());
    }
}