/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.ParametrosService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/parametros")
public class ParametrosController {
    private final ParametrosService parametrosService;

    public ParametrosController(ParametrosService parametrosService) {
        this.parametrosService = parametrosService;
    }

    @GetMapping
    public List<ParametrosEntity> listar() {
        return parametrosService.findAll();
    }
}
