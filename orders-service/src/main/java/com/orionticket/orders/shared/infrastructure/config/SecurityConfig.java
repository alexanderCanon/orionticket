package com.orionticket.orders.shared.infrastructure.config;

import com.orionticket.orders.shared.infrastructure.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF deshabilitado: API stateless, autenticada por JWT, no por sesión/cookie
            .csrf(AbstractHttpConfigurer::disable)
            // Sin estado de sesión en servidor; el token JWT es el único estado
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Healthcheck y métricas son públicos (Docker y load balancer los necesitan)
                .requestMatchers("/actuator/**").permitAll()
                // Todo lo demás requiere JWT válido
                .anyRequest().authenticated()
            )
            // Insertar filtro JWT antes del filtro de autenticación estándar de Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
