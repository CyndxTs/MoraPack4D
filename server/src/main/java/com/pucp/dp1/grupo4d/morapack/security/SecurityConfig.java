/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SecurityConfig.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ErrorResponse;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DAuthException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * API sin estado (JWT). Reglas:
 *  - Públicas: /api/auth/** (salvo /api/auth/mi-cuenta/**), listado de países (registro), /ws (STOMP valida en CONNECT).
 *  - /api/usuarios/**   : ADMINISTRADOR.
 *  - /api/cliente/**    : CLIENTE.
 *  - Resto de /api/**   : TRABAJADOR (el administrador también lo es).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // CORS_ORIGINS: "http://localhost:5173" en desarrollo; la URL del front en Render en producción (separadas por comas)
    @Value("${morapack.cors.origenes:http://localhost:5173}")
    private String origenes;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwtService, SesionService sesionService, ObjectMapper objectMapper) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, ex) -> {
                    Object motivo = request.getAttribute(JwtAuthFilter.ATRIBUTO_ERROR);
                    String codigo = (motivo != null) ? motivo.toString() : G4DAuthException.NO_AUTENTICADO;
                    String mensaje = (motivo != null) ? "Tu acceso venció, renovándolo.." : "Inicia sesión para continuar.";
                    escribir(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED, new ErrorResponse(mensaje, codigo));
                })
                .accessDeniedHandler((request, response, ex) ->
                    escribir(response, objectMapper, HttpServletResponse.SC_FORBIDDEN, new ErrorResponse("No tienes permisos para esta acción.", "SIN_PERMISO"))))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/error", "/ws", "/ws/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api").permitAll()
                .requestMatchers("/api/auth/mi-cuenta", "/api/auth/mi-cuenta/**").authenticated()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/paises").permitAll()
                .requestMatchers("/api/paises/listar").permitAll()
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                .requestMatchers("/api/**").hasRole("TRABAJADOR")
                .anyRequest().permitAll())
            .addFilterBefore(new JwtAuthFilter(jwtService, sesionService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> permitidos = Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(permitidos);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void escribir(HttpServletResponse response, ObjectMapper objectMapper, int status, Object cuerpo) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), cuerpo);
    }
}
