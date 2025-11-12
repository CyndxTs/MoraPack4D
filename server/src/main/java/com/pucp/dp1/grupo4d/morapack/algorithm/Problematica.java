/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Problematica.java
 [**/

package com.pucp.dp1.grupo4d.morapack.algorithm;

import com.pucp.dp1.grupo4d.morapack.adapter.AeropuertoAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.ClienteAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.PedidoAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.PlanAdapter;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import com.pucp.dp1.grupo4d.morapack.service.model.PlanService;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import java.time.LocalDateTime;
import java.util.*;

public class Problematica {
    public static Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL = 2;
    public static Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL = 3;
    public static Double MAX_HORAS_RECOJO = 2.0;
    public static Double MIN_HORAS_ESTANCIA = 1.0;
    public static Double MAX_HORAS_ESTANCIA = 12.0;
    public static List<String> CODIGOS_DE_ORIGENES = List.of("SPIM", "EBCI", "UBBB");
    public static LocalDateTime FECHA_HORA_INICIO = LocalDateTime.now().withYear(1999);
    public static LocalDateTime FECHA_HORA_FIN = LocalDateTime.now();
    public List<Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<Plan> planes;
    public List<Cliente> clientes;
    public List<Pedido> pedidos;
    public Set<Vuelo> vuelosEnTransito;
    public Set<Ruta> rutasEnOperacion;
    private AeropuertoService aeropuertoService;
    private ClienteService clienteService;
    private PlanService planService;
    private PedidoService pedidoService;
    private AeropuertoAdapter aeropuertoAdapter;
    private ClienteAdapter clienteAdapter;
    private PlanAdapter planAdapter;
    private PedidoAdapter pedidoAdapter;

    public Problematica(AeropuertoService aeropuertoService,
                        ClienteService clienteService,
                        PlanService planService,
                        PedidoService pedidoService,
                        AeropuertoAdapter aeropuertoAdapter,
                        ClienteAdapter clienteAdapter,
                        PlanAdapter planAdapter,
                        PedidoAdapter pedidoAdapter) {
        this.aeropuertoService = aeropuertoService;
        this.clienteService = clienteService;
        this.planService = planService;
        this.pedidoService = pedidoService;
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.clienteAdapter = clienteAdapter;
        this.planAdapter = planAdapter;
        this.pedidoAdapter = pedidoAdapter;
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
        Problematica problematica = new Problematica(this.aeropuertoService,this.clienteService,this.planService,this.pedidoService,this.aeropuertoAdapter,this.clienteAdapter,this.planAdapter,this.pedidoAdapter);
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
        this.aeropuertoService = problematica.aeropuertoService;
        this.clienteService = problematica.clienteService;
        this.planService = problematica.planService;
        this.pedidoService = problematica.pedidoService;
        this.aeropuertoAdapter = problematica.aeropuertoAdapter;
        this.clienteAdapter = problematica.clienteAdapter;
        this.planAdapter = problematica.planAdapter;
        this.pedidoAdapter = problematica.pedidoAdapter;
    }

    public void cargarDatos() {
        G4D.Logger.logln("Cargando datos desde base de datos..");

        List<AeropuertoEntity> aeropuertosEntities = aeropuertoService.findAll();
        for (AeropuertoEntity entity : aeropuertosEntities) {
            Aeropuerto aeropuerto = aeropuertoAdapter.toAlgorithm(entity);
            if (CODIGOS_DE_ORIGENES.contains(aeropuerto.getCodigo())) {
                origenes.add(aeropuerto);
            } else {
                destinos.add(aeropuerto);
            }
        }

        List<ClienteEntity> clienteEntities = clienteService.findAll();
        for (ClienteEntity entity : clienteEntities) {
            clientes.add(clienteAdapter.toAlgorithm(entity));
        }

        List<PlanEntity> planEntities = planService.findAll();
        for (PlanEntity entity : planEntities) {
            planes.add(planAdapter.toAlgorithm(entity));
        }

        List<PedidoEntity> pedidoEntities = pedidoService.findAll();
        for (PedidoEntity entity : pedidoEntities) {
            pedidos.add(pedidoAdapter.toAlgorithm(entity));
        }

        G4D.Logger.logf("[<] DATOS CARGADOS! ('%d' origenes | '%d' destinos | '%d' planes | '%d' clientes | '%d' pedidos)%n",
                origenes.size(), destinos.size(), planes.size(), clientes.size(), pedidos.size());
    }

    public void limpiarPools() {
        aeropuertoAdapter.clearPools();
        clienteAdapter.clearPools();
        planAdapter.clearPools();
        pedidoAdapter.clearPools();
    }
}
