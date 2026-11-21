/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClientePortalController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller;

import com.pucp.dp1.grupo4d.morapack.model.dto.payload.SolutionPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.CancelationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ClientOrderRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.security.SesionActual;
import com.pucp.dp1.grupo4d.morapack.service.ClientePortalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// Espacio del cliente (rol CLIENTE). El código del cliente siempre sale de la sesión, nunca del cuerpo.
@RestController
@RequestMapping("/api/cliente")
public class ClientePortalController {
    private final ClientePortalService clientePortalService;

    public ClientePortalController(ClientePortalService clientePortalService) {
        this.clientePortalService = clientePortalService;
    }

    private String codigoSesion() {
        return SesionActual.requerir().codigo();
    }

    @PostMapping("/aeropuertos")
    public ResponseEntity<ListResponse> aeropuertos() {
        return ResponseEntity.ok(clientePortalService.aeropuertos());
    }

    @PostMapping("/condiciones")
    public ResponseEntity<Map<String, Object>> condiciones() {
        return ResponseEntity.ok(clientePortalService.condiciones());
    }

    @PostMapping("/operacion")
    public ResponseEntity<SolutionPayload> operacion() {
        return ResponseEntity.ok(clientePortalService.operacion(codigoSesion()));
    }

    @PostMapping("/pedidos")
    public ResponseEntity<ListResponse> pedidos(@RequestBody(required = false) ListRequest request) {
        return ResponseEntity.ok(clientePortalService.pedidos(codigoSesion(), request));
    }

    @PostMapping("/pedidos/registrar")
    public ResponseEntity<GenericResponse> registrar(@RequestBody ClientOrderRequest request) {
        return ResponseEntity.ok(clientePortalService.registrar(codigoSesion(), request));
    }

    @PostMapping("/pedidos/cancelar")
    public ResponseEntity<GenericResponse> cancelar(@RequestBody CancelationRequest request) {
        return ResponseEntity.ok(clientePortalService.cancelar(codigoSesion(), request.getCodigo()));
    }
}
