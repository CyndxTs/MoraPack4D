/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.response.AeropuertoResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aeropuertos")
public class AeropuertoController {

    private final AeropuertoService aeropuertoService;

    public AeropuertoController(AeropuertoService aeropuertoService) {
        this.aeropuertoService = aeropuertoService;
    }

    @GetMapping
    public List<AeropuertoEntity> listar() {
        return aeropuertoService.findAll();
    }

    //LISTAR BASICO
    @GetMapping("/simple")
    public ResponseEntity<List<AeropuertoResponse>> listarBasico() {
        List<AeropuertoResponse> lista = aeropuertoService.listarBasico();
        return ResponseEntity.ok(lista);
    }

    //FILTRADO
    @GetMapping("/filtrar")
    public List<AeropuertoResponse> filtrarAeropuertos(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String continente,
            @RequestParam(required = false) String ordenCapacidad
    ) {
        return aeropuertoService.filtrarAeropuertos(codigo, ciudad, continente, ordenCapacidad);
    }


}
