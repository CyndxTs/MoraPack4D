package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.db.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.db.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.service.AeropuertoService;
import com.pucp.dp1.grupo4d.morapack.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Importar a Aeropuertos hacia BD desde archivo
    @PostMapping("/importar")
    public ResponseEntity<String> importarDesdeArchivo(@RequestParam("file") MultipartFile file) {
        try {
            usuarioService.importarDesdeArchivo(file);
            return ResponseEntity.ok("Usuarios importados correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cargar usuarios: " + e.getMessage());
        }
    }

    @GetMapping
    public List<UsuarioEntity> listar() {
        return usuarioService.findAll();
    }
}
