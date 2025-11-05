package com.pucp.dp1.grupo4d.morapack.controller;


import com.pucp.dp1.grupo4d.morapack.model.dto.response.VuelosResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.VueloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VuelosController {

    @Autowired
    private VueloService vueloService;

    // Solo es para ver que los vuelos esten obteniendose correctamente (borrar luego)
    @GetMapping("/vuelosPlanificacion")
    public List<VuelosResponse> listarVuelosPlanificacion() {
        System.out.println(" GET /api/vuelosPlanificacion solicitado");
        var vuelos = vueloService.listarVuelosSimulacion();
        System.out.println("✅ Retornando " + vuelos.size() + " vuelos de planificación");
        return vuelos;
    }
}
