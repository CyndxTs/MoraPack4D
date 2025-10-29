/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VueloController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.VueloService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vuelos")
public class VueloController {
    private final VueloService vueloService;

    public VueloController(VueloService vueloService) {
        this.vueloService = vueloService;
    }

    @GetMapping
    public List<VueloEntity> listar() {
        return vueloService.findAll();
    }
}

