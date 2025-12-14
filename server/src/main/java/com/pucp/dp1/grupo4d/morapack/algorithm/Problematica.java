/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Problematica.java
 [**/

package com.pucp.dp1.grupo4d.morapack.algorithm;

import com.pucp.dp1.grupo4d.morapack.adapter.*;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoRuta;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import java.time.LocalDateTime;
import java.util.*;

public class Problematica {
    public Integer maxDiasDeEntregaIntracontinental;
    public Integer maxDiasDeEntregaIntercontinental;
    public Double maxHorasDeRecojo;
    public Double minHorasDeEstancia;
    public Double maxHorasDeEstancia;
    public Double probabilidadDeReplanificacion;
    public String tipoEscenario;
    public LocalDateTime inicioDePlanificacion;
    public LocalDateTime finDePlanificacion;
    public LocalDateTime umbralDeReplanificacion;
    public LocalDateTime instanteDeProcesamiento;
    public List<PuntoDeReplanificacion> puntosDeReplanificacion;
    public Map<String, Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<Plan> planes;
    public List<Cliente> clientes;
    public List<Pedido> pedidos;
    public Set<Vuelo> vuelos;
    public Set<Ruta> rutas;

    public Problematica() {
        this.puntosDeReplanificacion = new ArrayList<>();
        this.origenes = new HashMap<>();
        this.destinos = new ArrayList<>();
        this.planes = new ArrayList<>();
        this.clientes = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.vuelos = new HashSet<>();
        this.rutas = new HashSet<>();
    }

    public Problematica(Problematica problematica) {
        this.reasignar(problematica);
    }

    public Problematica replicar() {
        Map<String, Cliente> poolClientes = new HashMap<>();
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Map<String, Lote> poolLotes = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        Map<String, Plan> poolPlanes = new HashMap<>();
        return this.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes);
    }

    public Problematica replicar(Map<String, Cliente> poolClientes, Map<String, Aeropuerto> poolAeropuertos, Map<String, Ruta> poolRutas, Map<String, Lote> poolLotes, Map<String, Vuelo> poolVuelos, Map<String, Plan> poolPlanes){
        Problematica problematica = new Problematica();
        problematica.maxDiasDeEntregaIntracontinental = this.maxDiasDeEntregaIntracontinental;
        problematica.maxDiasDeEntregaIntercontinental = this.maxDiasDeEntregaIntercontinental;
        problematica.maxHorasDeRecojo = this.maxHorasDeRecojo;
        problematica.minHorasDeEstancia = this.minHorasDeEstancia;
        problematica.maxHorasDeEstancia = this.maxHorasDeEstancia;
        problematica.probabilidadDeReplanificacion = this.probabilidadDeReplanificacion;
        problematica.tipoEscenario = this.tipoEscenario;
        problematica.inicioDePlanificacion = this.inicioDePlanificacion;
        problematica.finDePlanificacion = this.finDePlanificacion;
        problematica.umbralDeReplanificacion = this.umbralDeReplanificacion;
        problematica.instanteDeProcesamiento = this.instanteDeProcesamiento;
        this.puntosDeReplanificacion.forEach(pdr -> problematica.puntosDeReplanificacion.add(pdr.replicar(poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes)));
        this.origenes.forEach((cod, a) ->  problematica.origenes.put(cod, poolAeropuertos.computeIfAbsent(cod, codigo -> a.replicar(poolLotes))));
        this.destinos.forEach(a -> problematica.destinos.add(poolAeropuertos.computeIfAbsent(a.getCodigo(), codigo -> a.replicar(poolLotes))));
        this.planes.forEach(p -> problematica.planes.add(poolPlanes.computeIfAbsent(p.getCodigo(), codigo -> p.replicar(poolAeropuertos, poolLotes))));
        this.clientes.forEach(c -> problematica.clientes.add(poolClientes.computeIfAbsent(c.getCodigo(), codigo -> c.replicar())));
        this.pedidos.forEach(p -> problematica.pedidos.add(p.replicar(poolClientes, poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes)));
        this.vuelos.forEach(v -> problematica.vuelos.add(poolVuelos.computeIfAbsent(v.getCodigo(), codigo -> v.replicar(poolAeropuertos, poolLotes, poolPlanes))));
        this.rutas.forEach(r -> problematica.rutas.add(poolRutas.computeIfAbsent(r.getCodigo(), codigo -> r.replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes))));
        return problematica;
    }

    public void reasignar(Problematica problematica) {
        this.maxDiasDeEntregaIntracontinental = problematica.maxDiasDeEntregaIntracontinental;
        this.maxDiasDeEntregaIntercontinental = problematica.maxDiasDeEntregaIntercontinental;
        this.maxHorasDeRecojo = problematica.maxHorasDeRecojo;
        this.minHorasDeEstancia = problematica.minHorasDeEstancia;
        this.maxHorasDeEstancia = problematica.maxHorasDeEstancia;
        this.probabilidadDeReplanificacion = problematica.probabilidadDeReplanificacion;
        this.tipoEscenario = problematica.tipoEscenario;
        this.inicioDePlanificacion = problematica.inicioDePlanificacion;
        this.finDePlanificacion = problematica.finDePlanificacion;
        this.umbralDeReplanificacion = problematica.umbralDeReplanificacion;
        this.instanteDeProcesamiento = problematica.instanteDeProcesamiento;
        this.puntosDeReplanificacion = new ArrayList<>(problematica.puntosDeReplanificacion);
        this.origenes = new HashMap<>(problematica.origenes);
        this.destinos = new ArrayList<>(problematica.destinos);
        this.planes = new ArrayList<>(problematica.planes);
        this.clientes = new ArrayList<>(problematica.clientes);
        this.pedidos = new ArrayList<>(problematica.pedidos);
        this.rutas = new HashSet<>(problematica.rutas);
        this.vuelos = new HashSet<>(problematica.vuelos);
    }

    public void cargarAeropuertos(AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter) {
        System.out.println(">> Cargando aeropuertos..");
        List<AeropuertoEntity> aeropuertosEntity = aeropuertoService.findAll();
        aeropuertosEntity.forEach(entity -> {
            Aeropuerto aeropuerto = aeropuertoAdapter.toAlgorithm(entity);
            if (this.origenes.containsKey(aeropuerto.getCodigo())) {
                aeropuerto.setEsSede(true);
                origenes.put(aeropuerto.getCodigo(), aeropuerto);
            } else {
                aeropuerto.setEsSede(false);
                destinos.add(aeropuerto);
            }
        });
        System.out.printf("[:] AEROPUERTOS CARGADOS! | '%d' origenes! & '%d' destinos!%n", origenes.size(), destinos.size());
    }

    public void cargarPlanes(PlanService planService, PlanAdapter planAdapter) {
        System.out.println(">> Cargando planes..");
        List<PlanEntity> planesEntity = planService.findAll();
        planesEntity.forEach(entity -> {
            Plan plan = planAdapter.toAlgorithm(entity);
            planes.add(plan);
        });
        System.out.printf("[:] PLANES DE VUELO CARGADOS! | '%d' planes!%n", planes.size());
    }

    public void cargarClientes(ClienteService clienteService, UsuarioAdapter usuarioAdapter) {
        System.out.println(">> Cargando clientes..");
        List<ClienteEntity> clientesEntity = clienteService.findAllInRangeByScenario(this.inicioDePlanificacion, this.finDePlanificacion, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        clientesEntity.forEach(entity -> {
            if(clientes.stream().noneMatch(c -> c.getCodigo().equals(entity.getCodigo()))) {
                Cliente cliente = usuarioAdapter.toAlgorithm(entity);
                clientes.add(cliente);
            }
        });
        System.out.printf("[:] CLIENTES CARGADOS! | '%d' clientes!%n", clientes.size());
    }

    public void cargarPedidos(PedidoService pedidoService, PedidoAdapter pedidoAdapter) {
        System.out.println(">> Cargando pedidos..");
        List<PedidoEntity> pedidosEntity = pedidoService.findAllInRangeByScenario(this.inicioDePlanificacion, this.finDePlanificacion, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        pedidosEntity.forEach(entity -> {
            if(pedidos.stream().noneMatch(p -> p.getCodigo().equals(entity.getCodigo()))) {
                Pedido pedido = pedidoAdapter.toAlgorithm(entity);
                if (pedido.getFechaHoraProcesamiento() == null) {
                    pedido.setFechaHoraProcesamiento(this.instanteDeProcesamiento);
                }
                pedidos.add(pedido);
            }
        });
        int cantAtendidos = pedidos.stream().filter(Pedido::getFueAtendido).toList().size();
        System.out.printf("[:] PEDIDOS CARGADOS! | '%d' por atender! & '%d' ya atendidos!%n", pedidos.size() - cantAtendidos, cantAtendidos);
    }

    public void cargarVuelos(VueloService vueloService, VueloAdapter vueloAdapter) {
        System.out.println(">> Cargando vuelos..");
        List<VueloEntity> vuelosEntity = vueloService.findAllInRangeByScenario(this.inicioDePlanificacion, this.finDePlanificacion, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        vuelosEntity.forEach(entity -> {
            Vuelo vuelo = vueloAdapter.toAlgorithm(entity);
            vuelos.add(vuelo);
        });
        System.out.printf("[:] VUELOS CARGADOS! | '%d' vuelos!%n", vuelos.size());
    }

    public void cargarRutas(RutaService rutaService, RutaAdapter rutaAdapter) {
        System.out.println(">> Cargando rutas..");
        List<RutaEntity> rutasEntity = rutaService.findAllInRangeByScenario(this.inicioDePlanificacion, this.finDePlanificacion, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        rutasEntity.forEach(entity -> {
            Ruta ruta = rutaAdapter.toAlgorithm(entity);
            rutas.add(ruta);
        });
        rutas.stream().filter(r -> r.getEstado().equals(EstadoRuta.OPERATIVA)).forEach(r -> r.setEstado(EstadoRuta.REVISION_PENDIENTE));
        System.out.printf("[:] RUTAS CARGADAS! | '%d' rutas!%n", rutas.size());
    }
}
