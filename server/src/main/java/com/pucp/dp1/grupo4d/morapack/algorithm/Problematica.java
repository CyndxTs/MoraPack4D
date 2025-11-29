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
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import java.time.LocalDateTime;
import java.util.*;

public class Problematica {
    public static Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL;
    public static Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL;
    public static Double MAX_HORAS_RECOJO;
    public static Double MIN_HORAS_ESTANCIA;
    public static Double MAX_HORAS_ESTANCIA;
    public static LocalDateTime INICIO_PLANIFICACION;
    public static LocalDateTime FIN_PLANIFICACION;
    public static LocalDateTime UMBRAL_REPLANIFICACION;
    public static LocalDateTime INSTANTE_DE_PROCESAMIENTO;
    public static String ESCENARIO;
    public static List<String> CODIGOS_DE_ORIGENES;
    public static List<PuntoDeReplanificacion> PUNTOS_REPLANIFICACION;
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
        this.destinos = new ArrayList<>(problematica.destinos);
        this.origenes = new ArrayList<>(problematica.origenes);
        this.planes = new ArrayList<>(problematica.planes);
        this.clientes = new ArrayList<>(problematica.clientes);
        this.pedidos = new ArrayList<>(problematica.pedidos);
        this.rutas = new HashSet<>(problematica.rutas);
        this.vuelos = new HashSet<>(problematica.vuelos);
    }

    public void cargarAeropuertos(AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter) {
        System.out.println(">> Cargando aeropuertos desde la base de datos..");
        List<AeropuertoEntity> aeropuertosEntity = aeropuertoService.findAll();
        aeropuertosEntity.forEach(entity -> {
            Aeropuerto aeropuerto = aeropuertoAdapter.toAlgorithm(entity);
            if (CODIGOS_DE_ORIGENES.contains(aeropuerto.getCodigo())) {
                origenes.add(aeropuerto);
            } else {
                destinos.add(aeropuerto);
            }
        });
        System.out.printf("[:] AEROPUERTOS CARGADOS! | '%d' origenes! & '%d' destinos!%n", origenes.size(), destinos.size());
    }

    public void cargarPlanes(PlanService planService, PlanAdapter planAdapter) {
        System.out.println(">> Cargando planes desde la base de datos..");
        List<PlanEntity> planesEntity = planService.findAll();
        planesEntity.forEach(entity -> {
            Plan plan = planAdapter.toAlgorithm(entity);
            planes.add(plan);
        });
        System.out.printf("[:] PLANES DE VUELO CARGADOS! | '%d' planes!%n", planes.size());
    }

    public void cargarClientes(ClienteService clienteService, UsuarioAdapter usuarioAdapter) {
        System.out.println(">> Cargando clientes desde la base de datos..");
        List<ClienteEntity> clientesEntity = clienteService.findAllByDateTimeRange(INICIO_PLANIFICACION, FIN_PLANIFICACION, ESCENARIO);
        clientesEntity.forEach(entity -> {
            Cliente cliente = usuarioAdapter.toAlgorithm(entity);
            int posCli = clientes.indexOf(cliente);
            if(posCli == -1) {
                clientes.add(cliente);
            }
        });
        System.out.printf("[:] CLIENTES CARGADOS! | '%d' clientes!%n", clientes.size());
    }

    public void cargarPedidos(PedidoService pedidoService, PedidoAdapter pedidoAdapter) {
        System.out.println(">> Cargando pedidos desde la base de datos..");
        G4DUtility.IntegerWrapper cantAtendidos = new G4DUtility.IntegerWrapper();
        List<PedidoEntity> pedidosEntity = pedidoService.findAllByDateTimeRange(INICIO_PLANIFICACION, FIN_PLANIFICACION, ESCENARIO);
        pedidosEntity.forEach(entity -> {
            if(entity.getFueAtendido()) {
                cantAtendidos.increment();
            }
            if(entity.getFechaHoraProcesamientoUTC() == null) {
                entity.setFechaHoraProcesamientoUTC(INSTANTE_DE_PROCESAMIENTO);
            }
            Pedido pedido = pedidoAdapter.toAlgorithm(entity);
            int posPed = pedidos.indexOf(pedido);
            if(posPed == -1) {
                pedidos.add(pedido);
            }
        });
        System.out.printf("[:] PEDIDOS CARGADOS! | '%d' por atender! & '%d' ya atendidos!%n", pedidos.size() - cantAtendidos.value, cantAtendidos.value);
    }

    public void cargarVuelos(VueloService vueloService, VueloAdapter vueloAdapter) {
        System.out.println(">> Cargando vuelos desde la base de datos..");
        List<VueloEntity> vuelosEntity = vueloService.findAllByDateTimeRange(INICIO_PLANIFICACION, FIN_PLANIFICACION, ESCENARIO);
        vuelosEntity.forEach(entity -> {
            Vuelo vuelo = vueloAdapter.toAlgorithm(entity);
            vuelos.add(vuelo);
        });
        System.out.printf("[:] VUELOS CARGADOS! | '%d' vuelos!%n", vuelos.size());

    }

    public void cargarRutas(RutaService rutaService, RutaAdapter rutaAdapter) {
        System.out.println(">> Cargando rutas desde la base de datos..");
        List<RutaEntity> rutasEntity = rutaService.findAllByDateTimeRange(INICIO_PLANIFICACION, FIN_PLANIFICACION, ESCENARIO);
        rutasEntity.forEach(entity -> {
            Ruta ruta = rutaAdapter.toAlgorithm(entity);
            rutas.add(ruta);
        });
        System.out.printf("[:] RUTAS CARGADAS! | '%d' rutas!%n", rutas.size());
    }
}
