/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Problematica.java
 [**/

package com.pucp.dp1.grupo4d.morapack.algorithm;

import com.pucp.dp1.grupo4d.morapack.adapter.*;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import java.time.LocalDateTime;
import java.util.*;

public class Problematica {
    public static Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL;
    public static Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL;
    public static Double MAX_HORAS_RECOJO;
    public static Double MIN_HORAS_ESTANCIA;
    public static Double MAX_HORAS_ESTANCIA;
    public static LocalDateTime FECHA_HORA_INICIO;
    public static LocalDateTime FECHA_HORA_FIN;
    public static List<String> CODIGOS_DE_ORIGENES;
    public List<Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<Plan> planes;
    public List<Cliente> clientes;
    public List<Pedido> pedidos;
    public Set<Vuelo> vuelos;
    public Set<Ruta> rutas;

    public Problematica() {
        this.origenes = new ArrayList<>();
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
        Problematica problematica = new Problematica();
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Lote> poolLotes = new HashMap<>();
        Map<String, Cliente> poolClientes = new HashMap<>();
        Map<String, Plan> poolPlanes = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        for(Aeropuerto orig : this.origenes) problematica.origenes.add(poolAeropuertos.computeIfAbsent(orig.getCodigo(), codigo -> orig.replicar(poolLotes)));
        for(Aeropuerto dest : this.destinos) problematica.destinos.add(poolAeropuertos.computeIfAbsent(dest.getCodigo(), codigo -> dest.replicar(poolLotes)));
        for(Plan plan : this.planes) problematica.planes.add(poolPlanes.computeIfAbsent(plan.getCodigo(), codigo -> plan.replicar(poolAeropuertos, poolLotes)));
        for(Cliente cli : this.clientes) problematica.clientes.add(poolClientes.computeIfAbsent(cli.getCodigo(), codigo -> cli.replicar()));
        for(Pedido ped : this.pedidos) problematica.pedidos.add(ped.replicar(poolClientes, poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes));
        for(Vuelo vue : this.vuelos) problematica.vuelos.add(poolVuelos.computeIfAbsent(vue.getCodigo(), codigo -> vue.replicar(poolAeropuertos, poolLotes, poolPlanes)));
        for(Ruta rut : this.rutas) problematica.rutas.add(poolRutas.computeIfAbsent(rut.getCodigo(), codigo -> rut.replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes)));
        return problematica;
    }

    public void reasignar(Problematica problematica) {
        this.clientes = new ArrayList<>(problematica.clientes);
        this.destinos = new ArrayList<>(problematica.destinos);
        this.origenes = new ArrayList<>(problematica.origenes);
        this.planes = new ArrayList<>(problematica.planes);
        this.pedidos = new ArrayList<>(problematica.pedidos);
        this.rutas = new HashSet<>(problematica.rutas);
        this.vuelos = new HashSet<>(problematica.vuelos);
    }

    public void cargarDatos(AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter,
                            ClienteService clienteService, UsuarioAdapter usuarioAdapter,
                            PlanService planService, PlanAdapter planAdapter,
                            PedidoService pedidoService, PedidoAdapter pedidoAdapter,
                            RutaService rutaService, RutaAdapter rutaAdapter,
                            VueloService vueloService, VueloAdapter vueloAdapter ) {
        G4D.Logger.logln(">> Cargando desde base de datos..");
        // Aeropuertos
        List<AeropuertoEntity> aeropuertosEntity = aeropuertoService.findAll();
        aeropuertosEntity.forEach(entity -> {
            Aeropuerto aeropuerto = aeropuertoAdapter.toAlgorithm(entity);
            if (CODIGOS_DE_ORIGENES.contains(aeropuerto.getCodigo())) {
                origenes.add(aeropuerto);
            } else {
                destinos.add(aeropuerto);
            }
        });
        G4D.Logger.logf("[:] AEROPUERTOS CARGADOS! | '%d' origenes! & '%d' destinos!%n", origenes.size(), destinos.size());
        // Planes
        List<PlanEntity> planesEntity = planService.findAll();
        planesEntity.forEach(entity -> {
            Plan plan = planAdapter.toAlgorithm(entity);
            planes.add(plan);
        });
        G4D.Logger.logf("[:] PLANES DE VUELO CARGADOS! | '%d' planes!%n", planes.size());
        // Clientes
        List<ClienteEntity> clientesEntity = clienteService.findByDateTimeRange(FECHA_HORA_INICIO, FECHA_HORA_FIN);
        clientesEntity.forEach(entity -> {
            Cliente cliente = usuarioAdapter.toAlgorithm(entity);
            clientes.add(cliente);
        });
        G4D.Logger.logf("[:] CLIENTES CARGADOS! | '%d' clientes!%n", clientes.size());
        // Pedidos
        G4D.IntegerWrapper cantAtendidos = new G4D.IntegerWrapper();
        List<PedidoEntity> pedidosEntity = pedidoService.findByDateTimeRange(FECHA_HORA_INICIO, FECHA_HORA_FIN);
        pedidosEntity.forEach(entity -> {
            if(entity.getFueAtendido()) cantAtendidos.increment();
            Pedido pedido = pedidoAdapter.toAlgorithm(entity);
            pedidos.add(pedido);
        });
        G4D.Logger.logf("[:] PEDIDOS CARGADOS! | '%d' por atender! & '%d' ya atendidos!%n", pedidos.size() - cantAtendidos.value, cantAtendidos.value);
        // Vuelos
        List<VueloEntity> vuelosEntity = vueloService.findByDateTimeRange(FECHA_HORA_INICIO, FECHA_HORA_FIN);
        vuelosEntity.forEach(entity -> {
            Vuelo vuelo = vueloAdapter.toAlgorithm(entity);
            vuelos.add(vuelo);
        });
        G4D.Logger.logf("[:] VUELOS CARGADOS! | '%d' vuelos!%n", vuelos.size());
        // Rutas
        List<RutaEntity> rutasEntity = rutaService.findByDateTimeRange(FECHA_HORA_INICIO, FECHA_HORA_FIN);
        rutasEntity.forEach(entity -> {
            Ruta ruta = rutaAdapter.toAlgorithm(entity);
            rutas.add(ruta);
        });
        G4D.Logger.logf("[:] RUTAS CARGADAS! | '%d' rutas!%n", rutas.size());
        G4D.Logger.logln("[<] DATOS CARGADOS!");
    }
}
