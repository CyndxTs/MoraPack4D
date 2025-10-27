package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.db.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Importar a Aeropuertos hacia BD desde archivo
    @PostMapping("/importar")
    public ResponseEntity<String> importarDesdeArchivo(@RequestParam("file") MultipartFile file) {
        try {
            pedidoService.importarDesdeArchivo(file);
            return ResponseEntity.ok("Pedidos importados correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar pedidos: " + e.getMessage());
        }
    }

    @GetMapping
    public List<PedidoEntity> listar() {
        return pedidoService.findAll();
    }
}
