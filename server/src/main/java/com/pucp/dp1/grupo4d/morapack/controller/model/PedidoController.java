/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoController.java
 [**/

package com.pucp.dp1.grupo4d.morapack.controller.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoEntity> listar() {
        return pedidoService.findAll();
    }

    //LISTAR SIMPLE
    @GetMapping("/listar")
    public List<PedidoSimplificado> listarSimplificado() {
        return pedidoService.findAll().stream()
                .map(p -> new PedidoSimplificado(
                        p.getId(),
                        p.getCodigo(),
                        p.getCantidadSolicitada(),
                        p.getFechaHoraGeneracionLocal(),
                        p.getFechaHoraGeneracionUTC(),
                        p.getFechaHoraExpiracionLocal(),
                        p.getFechaHoraExpiracionUTC(),
                        (p.getCliente() != null ? p.getCliente().getId() : null),
                        (p.getCliente() != null ? p.getCliente().getNombre() : null),
                        (p.getDestino() != null ? p.getDestino().getId() : null),
                        (p.getDestino() != null ? p.getDestino().getCodigo() : null)
                ))
                .toList();
    }

    /*DTO local para listar pedidos simplificados*/
    static class PedidoSimplificado {
        private Integer id;
        private String codigo;
        private Integer cantidadSolicitada;
        private LocalDateTime fechaHoraGeneracionLocal;
        private LocalDateTime fechaHoraGeneracionUTC;
        private LocalDateTime fechaHoraExpiracionLocal;
        private LocalDateTime fechaHoraExpiracionUTC;
        private Integer idCliente;
        private String nombreCliente;
        private Integer idAeropuertoDestino;
        private String codigoAeropuertoDestino;

        public PedidoSimplificado(Integer id, String codigo, Integer cantidadSolicitada,
                                LocalDateTime fechaHoraGeneracionLocal, LocalDateTime fechaHoraGeneracionUTC,
                                LocalDateTime fechaHoraExpiracionLocal, LocalDateTime fechaHoraExpiracionUTC,
                                Integer idCliente, String nombreCliente,
                                Integer idAeropuertoDestino, String codigoAeropuertoDestino) {
            this.id = id;
            this.codigo = codigo;
            this.cantidadSolicitada = cantidadSolicitada;
            this.fechaHoraGeneracionLocal = fechaHoraGeneracionLocal;
            this.fechaHoraGeneracionUTC = fechaHoraGeneracionUTC;
            this.fechaHoraExpiracionLocal = fechaHoraExpiracionLocal;
            this.fechaHoraExpiracionUTC = fechaHoraExpiracionUTC;
            this.idCliente = idCliente;
            this.nombreCliente = nombreCliente;
            this.idAeropuertoDestino = idAeropuertoDestino;
            this.codigoAeropuertoDestino = codigoAeropuertoDestino;
        }

        public Integer getId() { return id; }
        public String getCodigo() { return codigo; }
        public Integer getCantidadSolicitada() { return cantidadSolicitada; }
        public LocalDateTime getFechaHoraGeneracionLocal() { return fechaHoraGeneracionLocal; }
        public LocalDateTime getFechaHoraGeneracionUTC() { return fechaHoraGeneracionUTC; }
        public LocalDateTime getFechaHoraExpiracionLocal() { return fechaHoraExpiracionLocal; }
        public LocalDateTime getFechaHoraExpiracionUTC() { return fechaHoraExpiracionUTC; }
        public Integer getIdCliente() { return idCliente; }
        public String getNombreCliente() { return nombreCliente; }
        public Integer getIdAeropuertoDestino() { return idAeropuertoDestino; }
        public String getCodigoAeropuertoDestino() { return codigoAeropuertoDestino; }
    }

    //FILTRADO
    @GetMapping("/filtrar")
    public List<PedidoSimplificado> listarOrdenado(
            @RequestParam(required = false, defaultValue = "codigo") List<String> campos,
            @RequestParam(required = false, defaultValue = "asc") List<String> orden
    ) {
        // Validación básica: mismo tamaño o usar asc por defecto
        if (orden.size() < campos.size()) {
            while (orden.size() < campos.size()) {
                orden.add("asc");
            }
        }

        List<PedidoEntity> pedidos = pedidoService.findAllOrdenado(campos, orden);

        return pedidos.stream()
                .map(p -> new PedidoSimplificado(
                        p.getId(),
                        p.getCodigo(),
                        p.getCantidadSolicitada(),
                        p.getFechaHoraGeneracionLocal(),
                        p.getFechaHoraGeneracionUTC(),
                        p.getFechaHoraExpiracionLocal(),
                        p.getFechaHoraExpiracionUTC(),
                        (p.getCliente() != null ? p.getCliente().getId() : null),
                        (p.getCliente() != null ? p.getCliente().getNombre() : null),
                        (p.getDestino() != null ? p.getDestino().getId() : null),
                        (p.getDestino() != null ? p.getDestino().getCodigo() : null)
                ))
                .toList();
    }

}
