package com.theanimalmap.eventtracker.security;

import com.theanimalmap.eventtracker.service.KafkaEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises the real Spring Security filter chain (see SecurityConfig) with a tiny
 * rate-limit budget so the 429 path is reachable without firing 100+ requests.
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "tam.security.rate-limit.capacity=2",
        "tam.security.rate-limit.refill-period-seconds=60"
})
class RateLimitIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    KafkaEventPublisher eventProducer;

    private static final String BODY = "{\"type\":\"search_animal\",\"payload\":{\"animal\":\"lion\"}}";

    @Test
    void thirdRequestWithinWindow_isRateLimited() throws Exception {
        mockMvc.perform(post("/events")
                        .header("X-API-KEY", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/events")
                        .header("X-API-KEY", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/events")
                        .header("X-API-KEY", "test-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"));
    }

    @Test
    void rateLimitAppliesAfterAuth_invalidKeyStillGets401() throws Exception {
        mockMvc.perform(post("/events")
                        .header("X-API-KEY", "wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }
}
