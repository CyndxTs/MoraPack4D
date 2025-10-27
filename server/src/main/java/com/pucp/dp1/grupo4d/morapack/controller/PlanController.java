package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.db.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.service.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/planes")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    // Importar a Aeropuertos hacia BD desde archivo
    @PostMapping("/importar")
    public ResponseEntity<String> importarDesdeArchivo(@RequestParam("file") MultipartFile file) {
        try {
            planService.importarDesdeArchivo(file);
            return ResponseEntity.ok("Planes importados correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar planes: " + e.getMessage());
        }
    }

    @GetMapping
    public List<PlanEntity> listar() {
        return planService.findAll();
    }
}
