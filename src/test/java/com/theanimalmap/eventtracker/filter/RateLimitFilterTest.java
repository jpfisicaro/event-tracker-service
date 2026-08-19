package com.theanimalmap.eventtracker.filter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter(3, 60, new SimpleMeterRegistry());
    }

    /*
        this test uses a for loop for the first 3 requests .The 4th one is called outside the for and it should fail
        due to the bucket being full.
     */
    @Test
    void allowsRequestsUpToCapacityThenRejectsWith429() throws Exception {
        for (int i = 1; i <= 3; i++) {
            FilterChain chain = mock(FilterChain.class);
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilterInternal(postEventsRequest("some-key"), res, chain);

            verify(chain, times(1)).doFilter(any(), any());
            assertThat(res.getStatus()).isEqualTo(200);
        }

        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilterInternal(postEventsRequest("some-key"), res, chain);

        verifyNoInteractions(chain);
        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getHeader("Retry-After")).isEqualTo("60");
        assertThat(res.getContentAsString()).contains("rate limit exceeded");
    }

    /*
        This should never happen since I only use one API KEY that is used by the frontend to send the requests. Still is a useful test.
        It basically tests that each api key has its own bucket, so if you fill one and then send a request with another api key, then another bucket
        should be used and the response is still 200.
     */
    @Test
    void tracksBucketsIndependentlyPerApiKey() throws Exception {
        for (int i = 1; i <= 3; i++) {
            filter.doFilterInternal(postEventsRequest("key-a"), new MockHttpServletResponse(), mock(FilterChain.class));
        }
        // key-a is now exhausted, but a different key should still have its own budget.
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilterInternal(postEventsRequest("key-b"), res, chain);

        verify(chain, times(1)).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(200);
    }

    /*
        ok, this test check that other paths are not being limited by the rate limiter.
     */
    @Test
    void doesNotThrottleRequestsToOtherPaths() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/actuator/health");
        req.addHeader("X-API-KEY", "some-key");

        assertThat(filter.shouldNotFilter(req)).isTrue();
    }

    /*
        This test doesn't limit a message sent to /events without api key . It will fail either way because of the authentication
     */
    @Test
    void doesNotThrottleRequestsWithoutAnApiKeyHeader() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilterInternal(postEventsRequest(null), res, chain);

        verify(chain, times(1)).doFilter(any(), any());
        assertThat(res.getStatus()).isEqualTo(200);
    }

    /*
        method used for the rest of the tests in this class, in order to send a post message to /events
     */
    private static MockHttpServletRequest postEventsRequest(String apiKey) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/events");
        if (apiKey != null) {
            req.addHeader("X-API-KEY", apiKey);
        }
        return req;
    }
}
