package com.orionticket.events.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orionticket.events.infrastructure.adapters.in.rest.EventCatalogController;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Configuration
public class PublicCatalogBypassFilterConfig {

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> publicCatalogBypassFilter(
            EventCatalogController eventCatalogController,
            ObjectMapper objectMapper) {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                if (!isCatalogEventsRequest(request)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                try {
                    var result = eventCatalogController.getCatalog(
                            request.getParameter("category"),
                            request.getParameter("city"),
                            parseDate(request.getParameter("date")),
                            parseUuid(request.getParameter("organizerId")),
                            pageable(request)
                    );

                    response.setStatus(result.getStatusCode().value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getOutputStream(), result.getBody());
                } catch (IllegalArgumentException ex) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    objectMapper.writeValue(response.getOutputStream(), java.util.Map.of(
                            "status", HttpServletResponse.SC_BAD_REQUEST,
                            "error", "Bad Request",
                            "message", ex.getMessage()
                    ));
                }
            }
        });
        registration.addUrlPatterns("/v1/catalog/events");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    private static boolean isCatalogEventsRequest(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod())
                && "/v1/catalog/events".equals(request.getRequestURI());
    }

    private static LocalDate parseDate(String value) {
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private static UUID parseUuid(String value) {
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private static Pageable pageable(HttpServletRequest request) {
        int page = parseInt(request.getParameter("page"), 0);
        int size = parseInt(request.getParameter("size"), 20);
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    }

    private static int parseInt(String value, int fallback) {
        return value == null || value.isBlank() ? fallback : Integer.parseInt(value);
    }
}
