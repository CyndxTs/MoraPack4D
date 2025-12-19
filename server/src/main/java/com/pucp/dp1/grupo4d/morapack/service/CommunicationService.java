/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       CommunicationService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class CommunicationService {
    private static SimpMessagingTemplate template;

    public CommunicationService(SimpMessagingTemplate template) {
        CommunicationService.template = template;
    }

    public void enviar(String destino, Object objeto) {
        template.convertAndSend(destino, objeto);
    }
}
