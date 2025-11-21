/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PlanDTO;
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
        try {
            ListRequest request = new ListRequest();
            request.setPage(0);
            request.setSize(30);
            ListResponse response = pedidoService.listar(request);
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
            ListResponse response = pedidoService.listar(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ListResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/importar")
    public ResponseEntity<GenericResponse> importar(@RequestBody ImportRequest<PedidoDTO> request) {
        try {
            GenericResponse response = pedidoService.importar(request);
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new GenericResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/importar-archivo")
    public ResponseEntity<GenericResponse> importar(@RequestPart("file") MultipartFile file, @RequestPart("request")ImportFileRequest request) {
        try {
            GenericResponse response = pedidoService.importar(file, request);
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
