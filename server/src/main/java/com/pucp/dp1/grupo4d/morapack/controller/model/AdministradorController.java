/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.AdministradorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {
    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @GetMapping
    public List<AdministradorEntity> listar() {
        return administradorService.findAll();
    }
}
