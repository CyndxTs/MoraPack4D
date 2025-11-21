/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        try {
            ListRequest request = new ListRequest();
            request.setPage(0);
            request.setSize(30);
            ListResponse response = clienteService.listar(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ListResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<ListResponse> listar(ListRequest request) {
        try {
            ListResponse response = clienteService.listar(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ListResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @GetMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest<UsuarioDTO> request) {
        try {
            ListResponse response = clienteService.filtrar(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ListResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/importar-archivo")
    public ResponseEntity<GenericResponse> importar(@RequestParam("file") MultipartFile file) {
        try {
            GenericResponse response = clienteService.importar(file);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new GenericResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }
}
