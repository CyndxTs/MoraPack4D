/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SegmentacionController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
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
        ListRequest request = new ListRequest(0, 30);
        ListResponse response = segmentacionService.listar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/listar")
    public ResponseEntity<ListResponse> listar(@RequestBody ListRequest request) {
        ListResponse response = segmentacionService.listar(request);
        return ResponseEntity.ok(response);
    }
}
