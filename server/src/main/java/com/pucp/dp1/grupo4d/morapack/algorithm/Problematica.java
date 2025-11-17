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
    public List<Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<Plan> planes;
    public List<Cliente> clientes;
    public List<Pedido> pedidos;
    public Set<Vuelo> vuelosEnTransito;
    public Set<Ruta> rutasEnOperacion;

    public Problematica() {
        this.origenes = new ArrayList<>();
        this.destinos = new ArrayList<>();
        this.planes = new ArrayList<>();
        this.clientes = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.vuelosEnTransito = new HashSet<>();
        this.rutasEnOperacion = new HashSet<>();
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
        for(Vuelo vue : this.vuelosEnTransito) problematica.vuelosEnTransito.add(poolVuelos.computeIfAbsent(vue.getCodigo(), codigo -> vue.replicar(poolAeropuertos, poolLotes, poolPlanes)));
        for(Ruta rut : this.rutasEnOperacion) problematica.rutasEnOperacion.add(poolRutas.computeIfAbsent(rut.getCodigo(), codigo -> rut.replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes)));
        return problematica;
    }

    public void reasignar(Problematica problematica) {
        this.clientes = new ArrayList<>(problematica.clientes);
        this.destinos = new ArrayList<>(problematica.destinos);
        this.origenes = new ArrayList<>(problematica.origenes);
        this.planes = new ArrayList<>(problematica.planes);
        this.pedidos = new ArrayList<>(problematica.pedidos);
        this.rutasEnOperacion = new HashSet<>(problematica.rutasEnOperacion);
        this.vuelosEnTransito = new HashSet<>(problematica.vuelosEnTransito);
    }

    public void cargarDatos(AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter,
                            ClienteService clienteService, UsuarioAdapter usuarioAdapter,
                            PlanService planService, PlanAdapter planAdapter,
                            PedidoService pedidoService, PedidoAdapter pedidoAdapter,
                            RutaService rutaService, RutaAdapter rutaAdapter,
                            VueloService vueloService, VueloAdapter vueloAdapter ) {
        G4D.Logger.logln("Cargando datos desde base de datos..");

        List<AeropuertoEntity> aeropuertosEntity = aeropuertoService.findAll();
        aeropuertosEntity.forEach(entity -> {
            Aeropuerto aeropuerto = aeropuertoAdapter.toAlgorithm(entity);
            if (entity.getEsSede()) {
                origenes.add(aeropuerto);
            } else {
                destinos.add(aeropuerto);
            }
        });

        List<ClienteEntity> clientesEntity = clienteService.findByDateTimeRange(FECHA_HORA_INICIO, FECHA_HORA_FIN);
        clientesEntity.forEach(entity -> {
            Cliente cliente = usuarioAdapter.toAlgorithm(entity);
            clientes.add(cliente);
        });

        List<PlanEntity> planesEntity = planService.findAll();
        planesEntity.forEach(entity -> {
            Plan plan = planAdapter.toAlgorithm(entity);
            planes.add(plan);
        });

        G4D.IntegerWrapper cantAtendidos = new G4D.IntegerWrapper();
        List<PedidoEntity> pedidosEntity = pedidoService.findByDateTimeRange(FECHA_HORA_INICIO, FECHA_HORA_FIN);
        pedidosEntity.forEach(entity -> {
            if(entity.getFueAtendido()) cantAtendidos.increment();
            Pedido pedido = pedidoAdapter.toAlgorithm(entity);
            pedidos.add(pedido);
        });

        List<VueloEntity> vuelosEntity = vueloService.findByDateTimeRange(FECHA_HORA_INICIO, FECHA_HORA_FIN);
        vuelosEntity.forEach(entity -> {
            Vuelo vuelo = vueloAdapter.toAlgorithm(entity);
            vuelosEnTransito.add(vuelo);
        });

        List<RutaEntity> rutasEntity = rutaService.findByDateTimeRange(FECHA_HORA_INICIO, FECHA_HORA_FIN);
        rutasEntity.forEach(entity -> {
            Ruta ruta = rutaAdapter.toAlgorithm(entity);
            rutasEnOperacion.add(ruta);
        });

        G4D.Logger.logln("[<] DATOS CARGADOS!");
        G4D.Logger.logf("[:] Aeropuertos: '%d' origenes! & '%d' destinos!%n", origenes.size(), destinos.size());
        G4D.Logger.logf("[:] Clientes: '%d'%n", clientes.size());
        G4D.Logger.logf("[:] Planes: '%d'%n", planes.size());
        G4D.Logger.logf("[:] Pedidos: '%d' por atender! | '%d' ya atendidos!%n", pedidos.size() - cantAtendidos.value, cantAtendidos.value);
        G4D.Logger.logf("[:] Vuelos: '%d'%n", vuelosEnTransito.size());
        G4D.Logger.logf("[:] Rutas: '%d'%n", rutasEnOperacion.size());
    }
}
