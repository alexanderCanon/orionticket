package com.orionticket.seating.shared.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// Trazabilidad cross-service: cada request que entra tiene (o recibe) un X-Correlation-ID único.
// Si el API Gateway ya lo envía (flujo normal en producción), lo reutilizamos para que todos los
// servicios involucrados en esa operación compartan el mismo ID en sus logs.
// Si no viene (llamada directa en desarrollo), generamos uno nuevo aquí.
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_KEY               = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // MDC (Mapped Diagnostic Context) de SLF4J: un mapa por-thread que los appenders
        // de logging (Logback, Log4j) pueden inyectar en cada línea de log automáticamente.
        // Con el pattern "%X{correlationId}" en logback.xml, no es necesario pasar el ID
        // manualmente a cada logger; aparece solo en todas las líneas de este request.
        MDC.put(MDC_KEY, correlationId);

        // Incluimos el ID en la respuesta HTTP para que el cliente (frontend, Postman, otro
        // servicio) pueda correlacionar su request con los logs del servidor.
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // finally garantiza limpieza aunque el request falle con excepción.
            // POR QUÉ es crítico: los servidores de aplicación reutilizan threads (thread pool).
            // Sin limpiar el MDC, el thread siguiente heredaría el correlationId del request anterior,
            // mezclando logs de diferentes requests bajo el mismo ID.
            MDC.remove(MDC_KEY);
        }
    }
}
