/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       WebSocketService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketService {
    private static SimpMessagingTemplate template;

    public WebSocketService(SimpMessagingTemplate template) {
        WebSocketService.template = template;
    }

    public static void enviar(String destino, Object objeto) {
        template.convertAndSend(destino, objeto);
    }
}
