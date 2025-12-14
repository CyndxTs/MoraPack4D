/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ImportService {

    @PersistenceContext
    private EntityManager em;

    public <T> void batchSave(List<T> entities, String type) {
        for (int i = 0; i < entities.size(); i++) {
            WebSocketService.enviar("/topic/loader", new ProgressPayload(String.format("Guardando %s", type), i + 1, entities.size()));
            em.persist(entities.get(i));
        }
        em.flush();
    }
}
