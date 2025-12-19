/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ImportationService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportationService {

    @PersistenceContext
    private EntityManager em;

    private final CommunicationService communicationService;

    public ImportationService(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    public <K, V> Map<K, V> getNewLimitedPool(int maxSize) {
        return new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }

    public <T> void batchSave(List<T> entities, String type) {
        for (int i = 0; i < entities.size(); i++) {
            communicationService.enviar("/topic/loader", new ProgressPayload(String.format("Guardando %s", type), i + 1, entities.size()));
            em.persist(entities.get(i));
        }
        em.flush();
    }
}
