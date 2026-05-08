package com.orionticket.events.infrastructure.adapters.in.rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro HTTP que propaga el Correlation ID a través del MDC de SLF4J.
 * <p>
 * Si la petición entrante incluye la cabecera {@code X-Correlation-Id},
 * se usa ese valor. En caso contrario, se genera uno nuevo.
 * El valor se incluye en la respuesta para facilitar la trazabilidad
 * distribuida entre servicios.
 * </p>
 * Cumple con el requerimiento de la DoD §2:
 * "Structured JSON log emitted on entry and exit (correlation ID propagated
 * via X-Correlation-Id header)."
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
