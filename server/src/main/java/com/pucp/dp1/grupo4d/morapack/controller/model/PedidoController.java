/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoEntity> listar() {
        return pedidoService.findAll();
    }
}
