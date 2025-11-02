/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteEntity> listar() {
        return clienteService.findAll();
    }

    //Filtrado
    @GetMapping("/filtrar")
    public List<ClienteEntity> filtrarClientes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) String estado
    ) {
        return clienteService.filtrarClientes(nombre, correo, estado);
    }
}
