/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportFileRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = pedidoService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = pedidoService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/filtrar")
    public ResponseEntity<ListResponse> filtrar(@RequestBody FilterRequest<PedidoDTO> request) {
        ListResponse response = pedidoService.filtrar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/importar")
    public ResponseEntity<GenericResponse> importar(@RequestBody ImportRequest<PedidoDTO> request) {
        GenericResponse response = pedidoService.importar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/importar-archivo")
    public ResponseEntity<GenericResponse> importar(@RequestPart("file") MultipartFile file, @RequestPart("request") ImportFileRequest request) {
        GenericResponse response = pedidoService.importar(file, request);
        return ResponseEntity.ok(response);
    }
}
