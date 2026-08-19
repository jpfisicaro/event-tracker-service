package com.theanimalmap.eventtracker.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs after {@link ApiKeyAuthFilter}, so it only ever sees requests that already
 * carried a valid X-API-KEY. Throttles POST /events to a fixed number of requests
 * per period, tracked per API key value so distinct keys don't share a budget.
 *
 * Exposes tam.ratelimit.available_tokens (gauge, tagged by a non-reversible key
 * fingerprint so the raw API key never ends up in Prometheus) so the bucket state
 * can be watched in Grafana over time, not just inferred from 429s after the fact.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";

    private final long capacity;
    private final long refillPeriodSeconds;
    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${tam.security.rate-limit.capacity:100}") long capacity,
            @Value("${tam.security.rate-limit.refill-period-seconds:60}") long refillPeriodSeconds,
            MeterRegistry meterRegistry) {
        this.capacity = capacity;
        this.refillPeriodSeconds = refillPeriodSeconds;
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(HttpMethod.POST.matches(request.getMethod()) && "/events".equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String key = req.getHeader(HEADER);
        if (key == null || key.isBlank()) {
            // ApiKeyAuthFilter already rejects this request before it reaches here in the real
            // filter chain; skip rate limiting rather than throttle on an empty bucket key.
            chain.doFilter(req, res);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(key, this::newTrackedBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(req, res);
            return;
        }

        res.setStatus(429);
        res.setHeader("Retry-After", String.valueOf(refillPeriodSeconds));
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write("{\"error\":\"rate limit exceeded\"}");
    }

    private Bucket newTrackedBucket(String key) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofSeconds(refillPeriodSeconds)));
        Bucket bucket = Bucket.builder().addLimit(limit).build();

        Gauge.builder("tam.ratelimit.available_tokens", bucket, Bucket::getAvailableTokens)
                .description("Tokens left in the rate-limit bucket for this API key")
                .tag("key_fingerprint", fingerprint(key))
                .register(meterRegistry);

        return bucket;
    }

    // Short, non-reversible label so the raw API key never shows up in metrics/Grafana.
    private static String fingerprint(String key) {
        return String.format("%08x", key.hashCode());
    }
}
