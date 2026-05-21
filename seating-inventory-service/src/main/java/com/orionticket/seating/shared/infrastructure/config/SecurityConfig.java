package com.orionticket.seating.shared.infrastructure.config;

import com.orionticket.seating.shared.infrastructure.filter.CorrelationIdFilter;
import com.orionticket.seating.shared.infrastructure.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorrelationIdFilter correlationIdFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF deshabilitado: el servicio es stateless y usa JWT en el header Authorization,
            // no cookies de sesión. El ataque CSRF explota que los navegadores envían cookies
            // automáticamente; con JWT en Authorization eso no aplica.
            .csrf(AbstractHttpConfigurer::disable)

            // STATELESS: Spring Security NO crea HttpSession. Cada request porta su propio JWT.
            // POR QUÉ: en microservicios el servidor no guarda estado de sesión.
            // Escala horizontalmente sin necesidad de sesión compartida (Redis, etc.).
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // GET /v1/events/**/seats es público: un comprador anónimo puede ver
                // el mapa de disponibilidad antes de crear una cuenta o loguearse.
                .requestMatchers("/v1/events/*/dates/*/seats").permitAll()
                // Batches y seating-map también son públicos temporalmente para MVP:
                // el API Gateway es el punto de autorización real en producción.
                .requestMatchers("/v1/events/*/dates/*/batches", "/v1/events/*/dates/*/batches/**").permitAll()
                .requestMatchers("/v1/events/*/dates/*/seating-map").permitAll()
                .requestMatchers("/v1/reservations", "/v1/reservations/**").permitAll()

                // /actuator/health: Docker y Kubernetes hacen healthchecks sin token.
                // /actuator/info: monitoreo básico. Sin este permiso el contenedor
                // siempre aparece como "unhealthy" aunque esté funcionando.
                .requestMatchers("/actuator/**").permitAll()

                // Swagger UI y OpenAPI spec: herramienta de desarrollo y documentación.
                // Debe ser accesible sin token para que el equipo pueda explorar la API.
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Endpoints internos de seed: solo para poblar datos en entornos locales.
                // En producción, este path debería estar bloqueado por el API Gateway.
                .requestMatchers("/v1/internal/**").permitAll()

                // Cualquier otro endpoint requiere JWT válido:
                // POST /v1/reservations, POST /batches, DELETE /reservations/{id}, etc.
                .anyRequest().authenticated()
            )

            // CorrelationIdFilter va PRIMERO: extrae o genera el X-Correlation-ID
            // antes de que cualquier otro filtro ejecute lógica de negocio.
            // Así todos los logs del request comparten el mismo ID desde el inicio.
            .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)

            // JwtAuthenticationFilter: valida la firma del token y carga el userId
            // en el SecurityContext. Debe ir antes del filtro estándar de Spring
            // para que la autenticación esté lista cuando se evalúen las reglas de autorización.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
