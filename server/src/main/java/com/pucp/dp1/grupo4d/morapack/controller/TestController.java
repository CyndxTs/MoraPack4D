/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       TestController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.model.VueloResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.VueloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private VueloService vueloService;

    @GetMapping("/vuelosPlanificacion")
    public List<VueloResponse> listarVuelosPlanificacion() {
        System.out.println(" GET /api/vuelosPlanificacion solicitado");
        var vuelos = vueloService.listarVuelosSimulacion();
        System.out.println("✅ Retornando " + vuelos.size() + " vuelos de planificación");
        return vuelos;
    }
}
