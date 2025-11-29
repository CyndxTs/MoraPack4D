/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.AdministradorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {
    private final AdministradorService administradorService;

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = administradorService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = administradorService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest<UsuarioDTO> request) {
        ListResponse response = administradorService.filtrar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/importar-archivo")
    public ResponseEntity<GenericResponse> importar(@RequestParam("file") MultipartFile file) {
        GenericResponse response = administradorService.importar(file);
        return ResponseEntity.ok(response);
    }
}
