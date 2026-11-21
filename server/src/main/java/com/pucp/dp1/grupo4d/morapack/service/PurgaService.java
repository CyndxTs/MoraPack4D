/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PurgaService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Borra físicamente de la BD lo que la OPERACIÓN ya no usa (lo que antes se marcó con soft delete).
 * Solo toca datos de operación: RUTA, VUELO, SEGMENTACION, LOTE y REGISTRO solo se guardan en operación
 * (la simulación vive en RAM), y EVENTO/PEDIDO se filtran por tipo_escenario. Nunca borra PEDIDOS
 * (quedan como historial del cliente, incluidos los cancelados) ni planes, aeropuertos o usuarios.
 *
 * Se hace en Java y no con procedimientos/eventos SQL porque TiDB Cloud no los soporta.
 * El llamador (G4DService) garantiza que no corra a la vez que una replanificación.
 */
@Service
public class PurgaService {
    private final JdbcTemplate jdbc;

    public PurgaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 'instanteDeCorte': todo lo que terminó antes de este instante ya no entra en ninguna ventana de replanificación
    @Transactional
    public Map<String, Integer> purgar(LocalDateTime instanteDeCorte) {
        Timestamp corte = Timestamp.valueOf(instanteDeCorte);
        Map<String, Integer> eliminados = new LinkedHashMap<>();
        // 1. Registros dados de baja (soft delete)
        eliminados.put("registros no vigentes", jdbc.update("DELETE FROM `REGISTRO` WHERE `sigue_vigente` = 0"));
        // 2. Pedidos cancelados (por el cliente o por error): se conserva el pedido, no su planificación
        eliminados.put("registros de pedidos cancelados", jdbc.update("""
            DELETE r FROM `REGISTRO` r
            JOIN `LOTE` l ON l.`id` = r.`id_lote`
            JOIN `SEGMENTACION` s ON s.`id` = l.`id_segmentacion`
            JOIN `PEDIDO` p ON p.`id` = s.`id_pedido`
            WHERE p.`tipo_escenario` = 'OPERACION' AND p.`estado` <> 'ACTIVO'
            """));
        eliminados.put("lotes de pedidos cancelados", jdbc.update("""
            DELETE l FROM `LOTE` l
            JOIN `SEGMENTACION` s ON s.`id` = l.`id_segmentacion`
            JOIN `PEDIDO` p ON p.`id` = s.`id_pedido`
            WHERE p.`tipo_escenario` = 'OPERACION' AND p.`estado` <> 'ACTIVO'
            """));
        eliminados.put("segmentaciones de pedidos cancelados", jdbc.update("""
            DELETE s FROM `SEGMENTACION` s
            JOIN `PEDIDO` p ON p.`id` = s.`id_pedido`
            WHERE p.`tipo_escenario` = 'OPERACION' AND p.`estado` <> 'ACTIVO'
            """));
        // 3. Lotes reemplazados por una replanificación que ya no tienen ningún registro
        eliminados.put("lotes replanificados", jdbc.update("""
            DELETE l FROM `LOTE` l
            LEFT JOIN `REGISTRO` r ON r.`id_lote` = l.`id`
            WHERE l.`estado` = 'REPLANIFICADO' AND r.`id` IS NULL
            """));
        // 4. Segmentaciones sustituidas que quedaron sin lotes
        eliminados.put("segmentaciones sustituidas", jdbc.update("""
            DELETE s FROM `SEGMENTACION` s
            LEFT JOIN `LOTE` l ON l.`id_segmentacion` = s.`id`
            WHERE s.`fh_sustitucion` IS NOT NULL AND l.`id` IS NULL
            """));
        // 5. Rutas que ningún lote usa (primero su relación con los vuelos)
        jdbc.update("""
            DELETE rv FROM `RUTA_POR_VUELO` rv
            JOIN `RUTA` ru ON ru.`id` = rv.`id_ruta`
            LEFT JOIN `LOTE` l ON l.`id_ruta` = ru.`id`
            WHERE l.`id` IS NULL
            """);
        eliminados.put("rutas sin uso", jdbc.update("""
            DELETE ru FROM `RUTA` ru
            LEFT JOIN `LOTE` l ON l.`id_ruta` = ru.`id`
            WHERE l.`id` IS NULL
            """));
        // 6. Vuelos que ninguna ruta usa (sin carga, o ya terminados)
        eliminados.put("vuelos sin uso", jdbc.update("""
            DELETE v FROM `VUELO` v
            LEFT JOIN `RUTA_POR_VUELO` rv ON rv.`id_vuelo` = v.`id`
            WHERE rv.`id_ruta` IS NULL AND (v.`capacidad_disponible` = v.`capacidad` OR v.`fh_llegada` < ?)
            """, corte));
        // 7. Eventos de operación cuyo vuelo afectado ya terminó antes del corte
        eliminados.put("eventos vencidos", jdbc.update("""
            DELETE FROM `EVENTO`
            WHERE `tipo_escenario` = 'OPERACION' AND GREATEST(`fh_fin`, COALESCE(`fh_nueva_llegada`, `fh_fin`)) < ?
            """, corte));
        return eliminados;
    }
}
