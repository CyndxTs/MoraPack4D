/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RutaController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.RutaEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.RutaService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {
    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    @GetMapping
    public List<RutaEntity> listar() {
        return rutaService.findAll();
    }
}

