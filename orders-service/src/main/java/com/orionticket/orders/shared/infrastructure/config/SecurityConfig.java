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
            // CSRF deshabilitado: API stateless, autenticada por JWT, no por sesiÃ³n/cookie
            .csrf(AbstractHttpConfigurer::disable)
            // Sin estado de sesiÃ³n en servidor; el token JWT es el Ãºnico estado
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Healthcheck y mÃ©tricas son pÃºblicos (Docker y load balancer los necesitan)
                .requestMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Todo lo demÃ¡s requiere JWT vÃ¡lido
                .anyRequest().authenticated()
            )
            // Insertar filtro JWT antes del filtro de autenticaciÃ³n estÃ¡ndar de Spring
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

