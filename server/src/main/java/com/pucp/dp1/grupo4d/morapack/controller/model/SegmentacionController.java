/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.model.SegmentacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/segmentaciones")
public class SegmentacionController {

    private final SegmentacionService segmentacionService;

    public SegmentacionController(SegmentacionService segmentacionService) {
        this.segmentacionService = segmentacionService;
    }

    @GetMapping
    public ResponseEntity<ListResponse> listar() {
        try {
            ListRequest request = new ListRequest(1, 30);
            ListResponse response = segmentacionService.listar(request);
            return ResponseEntity.ok(response);
        } catch (G4DException e) {
            return ResponseEntity.badRequest().body(new ListResponse(false, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ListResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        try {
            ListResponse response = segmentacionService.listar(request);
            return ResponseEntity.ok(response);
        } catch (G4DException e) {
            return ResponseEntity.badRequest().body(new ListResponse(false, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new ListResponse(false, "ERROR INTERNO: " + e.getMessage()));
        }
    }
}
