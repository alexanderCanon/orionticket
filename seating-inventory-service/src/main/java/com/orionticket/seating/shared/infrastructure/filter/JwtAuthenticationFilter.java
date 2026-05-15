package com.orionticket.seating.shared.infrastructure.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

// OncePerRequestFilter: garantiza que este filtro se ejecute exactamente UNA VEZ
// por request, incluso si Spring hace forwards o includes internos.
// Es la superclase estándar para filtros de seguridad en Spring.
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey key;

    // El mismo secret que Identity Service usa para firmar los tokens.
    // En producción viene de una variable de entorno (JWT_SECRET).
    // Para dev local, el valor por defecto en application.yml es suficiente
    // siempre que TODOS los servicios usen el mismo default.
    public JwtAuthenticationFilter(
            @Value("${jwt.secret:defaultSecretKeyThatMustBeVeryLongAndSecureForHS256AlgorithmXYZ}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // Sin header o sin "Bearer ": dejamos pasar el request sin autenticar.
        // Spring Security rechazará el acceso más adelante si el endpoint lo requiere.
        // Esto permite que endpoints públicos (GET /seats, /actuator) funcionen sin token.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Jwts.parser().verifyWith(key): verifica la firma HMAC-SHA256.
            // Si el token fue alterado, expiró, o el secret no coincide → excepción.
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authHeader.substring(7)) // quita "Bearer "
                    .getPayload();

            String userId = claims.getSubject(); // "sub" del JWT = userId del comprador

            // Cargamos la autenticación en el SecurityContext del thread actual.
            // A partir de aquí, los controllers pueden leer el userId con:
            // SecurityContextHolder.getContext().getAuthentication().getPrincipal()
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId, null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // Token inválido o expirado: limpiamos el contexto para que no haya
            // autenticación residual. Spring Security devolverá 401 si el endpoint lo requiere.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
