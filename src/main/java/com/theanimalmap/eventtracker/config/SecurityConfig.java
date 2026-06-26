package com.theanimalmap.eventtracker.config;

import com.theanimalmap.eventtracker.filter.ApiKeyAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;


@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendError(401))
                        .accessDeniedHandler((req, res, e) -> res.sendError(403))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/events").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(apiKeyAuthFilter, org.springframework.security.web.access.intercept.AuthorizationFilter.class)
                .build();
    }
}