package com.theanimalmap.eventtracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-API-KEY";
    private final EventTrackerSecurityProperties props;

    public ApiKeyAuthFilter(EventTrackerSecurityProperties props) {
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return !(
                "/events".equals(path) ||
                        path.startsWith("/actuator")
        );
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String expected = props.getApiKey();
        String provided = req.getHeader(HEADER);

        if (expected == null || expected.isBlank()) {
            // Fail fast: si no configuraste api key, mejor bloquear TODO en prod
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"error\":\"server misconfigured: api key missing\"}");
            return;
        }

        if (provided == null || provided.isBlank() || !provided.equals(expected)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"error\":\"unauthorized\"}");
            return;
        }

        // ✅ Marcamos el request como "autenticado"
        var auth = new UsernamePasswordAuthenticationToken(
                "tam-api-key",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_EVENT_WRITER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }
}