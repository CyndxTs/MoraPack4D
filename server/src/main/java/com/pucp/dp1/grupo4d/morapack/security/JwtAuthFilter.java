/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       JwtAuthFilter.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Lee "Authorization: Bearer <token de acceso>". Si el token no sirve no corta la petición: la deja sin usuario
 * y anota el motivo; si la ruta lo exige, el entry point responde 401 con ese código (el front renueva el token).
 * No es un @Component a propósito: así no se registra dos veces como filtro del servlet.
 */
public class JwtAuthFilter extends OncePerRequestFilter {
    public static final String ATRIBUTO_ERROR = "morapack.auth.error";

    private final JwtService jwtService;
    private final SesionService sesionService;

    public JwtAuthFilter(JwtService jwtService, SesionService sesionService) {
        this.jwtService = jwtService;
        this.sesionService = sesionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            UsuarioPrincipal principal = autenticar(jwtService, sesionService, cabecera.substring(7).trim(), request);
            if (principal != null) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, principal.autoridades());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }

    // Compartido con STOMP (request puede ser null)
    static UsuarioPrincipal autenticar(JwtService jwtService, SesionService sesionService, String token, HttpServletRequest request) {
        try {
            JwtService.TokenAcceso t = jwtService.validar(token);
            if (sesionService.tokenInvalidado(t.idSesion(), t.emitidoEnSeg())) throw new JwtService.TokenInvalidoException(true);
            return new UsuarioPrincipal(t.idUsuario(), t.codigo(), t.rol(), t.idSesion());
        } catch (JwtService.TokenInvalidoException e) {
            if (request != null) request.setAttribute(ATRIBUTO_ERROR, e.isExpirado() ? "TOKEN_EXPIRADO" : "TOKEN_INVALIDO");
            return null;
        }
    }
}
