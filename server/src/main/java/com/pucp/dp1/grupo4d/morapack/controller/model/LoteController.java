/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       LoteController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.LoteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {
    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @GetMapping
    public List<LoteEntity> listar() {
        return loteService.findAll();
    }
}
