/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.RegistroService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/registros")
public class RegistroController {
    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    @GetMapping
    public List<RegistroEntity> listar() {
        return registroService.findAll();
    }
}

