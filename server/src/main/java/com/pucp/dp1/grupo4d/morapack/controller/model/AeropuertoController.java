/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/aeropuertos")
public class AeropuertoController {
    private final AeropuertoService aeropuertoService;

    public AeropuertoController(AeropuertoService aeropuertoService) {
        this.aeropuertoService = aeropuertoService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = aeropuertoService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = aeropuertoService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest<AeropuertoDTO> request) {
        ListResponse response = aeropuertoService.filtrar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/importar")
    public ResponseEntity<GenericResponse> importar(@RequestBody ImportRequest<AeropuertoDTO> request) {
        GenericResponse response = aeropuertoService.importar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/importar-archivo")
    public ResponseEntity<GenericResponse> importar(@RequestParam("file") MultipartFile file) {
        GenericResponse response = aeropuertoService.importar(file);
        return ResponseEntity.ok(response);
    }
}
