package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.db.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.service.AeropuertoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/aeropuertos")
public class AeropuertoController {

    private final AeropuertoService aeropuertoService;

    public AeropuertoController(AeropuertoService aeropuertoService) {
        this.aeropuertoService = aeropuertoService;
    }

    // Importar a Aeropuertos hacia BD desde archivo
    @PostMapping("/importar")
    public ResponseEntity<String> importarDesdeArchivo(@RequestParam("file") MultipartFile file) {
        try {
            aeropuertoService.importarDesdeArchivo(file);
            return ResponseEntity.ok("Aeropuertos importados correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar aeropuertos: " + e.getMessage());
        }
    }

    @GetMapping
    public List<AeropuertoEntity> listar() {
        return aeropuertoService.findAll();
    }
}
