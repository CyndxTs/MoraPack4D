/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       StompAuthInterceptor.java
 [**/

package com.pucp.dp1.grupo4d.morapack.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import java.security.Principal;

/**
 * STOMP: el token de acceso se valida solo en CONNECT (cabecera "Authorization: Bearer ..."). Una vez conectado,
 * la conexión sigue viva aunque el token venza (simulaciones y operación no se cortan). Las suscripciones se
 * autorizan por rol: los tópicos de operación/simulación/importación/exportación son del trabajador; un cliente
 * solo puede escuchar "/topic/cliente/<su código>/...". Una suscripción no permitida se descarta en silencio.
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final SesionService sesionService;

    public StompAuthInterceptor(JwtService jwtService, SesionService sesionService) {
        this.jwtService = jwtService;
        this.sesionService = sesionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) return message;
        StompCommand comando = accessor.getCommand();
        if (StompCommand.CONNECT.equals(comando) || StompCommand.STOMP.equals(comando)) {
            String cabecera = accessor.getFirstNativeHeader("Authorization");
            String token = (cabecera != null && cabecera.startsWith("Bearer ")) ? cabecera.substring(7).trim() : null;
            UsuarioPrincipal principal = (token != null) ? JwtAuthFilter.autenticar(jwtService, sesionService, token, null) : null;
            if (principal == null) throw new MessageDeliveryException("No autenticado");
            accessor.setUser(new UsernamePasswordAuthenticationToken(principal, null, principal.autoridades()));
        } else if (StompCommand.SUBSCRIBE.equals(comando)) {
            UsuarioPrincipal principal = extraer(accessor.getUser());
            if (principal == null || !puedeSuscribirse(principal, accessor.getDestination())) {
                System.out.printf("[*] SUSCRIPCIÓN RECHAZADA: '%s' -> '%s'%n", (principal != null) ? principal.codigo() : "anónimo", accessor.getDestination());
                return null;
            }
        } else if (StompCommand.SEND.equals(comando)) {
            // El front no envía mensajes por STOMP: todo va por la API REST
            return null;
        }
        return message;
    }

    private static UsuarioPrincipal extraer(Principal user) {
        if (user instanceof UsernamePasswordAuthenticationToken auth && auth.getPrincipal() instanceof UsuarioPrincipal p) return p;
        return null;
    }

    static boolean puedeSuscribirse(UsuarioPrincipal principal, String destino) {
        if (destino == null) return false;
        if (destino.startsWith("/topic/cliente/")) {
            return principal.esTrabajador() || destino.startsWith("/topic/cliente/" + principal.codigo() + "/") || destino.equals("/topic/cliente/" + principal.codigo());
        }
        return principal.esTrabajador();
    }
}
