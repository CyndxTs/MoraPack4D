package com.pucp.dp1.grupo4d.morapack.algorithm;

import com.pucp.dp1.grupo4d.morapack.adapter.AeropuertoAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.ClienteAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.PedidoAdapter;
import com.pucp.dp1.grupo4d.morapack.adapter.PlanAdapter;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Cliente;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Pedido;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.service.model.AeropuertoService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import com.pucp.dp1.grupo4d.morapack.service.model.PedidoService;
import com.pucp.dp1.grupo4d.morapack.service.model.PlanService;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import java.util.*;

public class Problematica {

    public static Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL = 2;
    public static Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL = 3;
    public static Double MAX_HORAS_RECOJO = 2.0;
    public static Double MIN_HORAS_ESTANCIA = 1.0;
    public static Double MAX_HORAS_ESTANCIA = 12.0;
    private final AeropuertoService aeropuertoService;
    private final ClienteService clienteService;
    private final PlanService planService;
    private final PedidoService pedidoService;
    private final AeropuertoAdapter aeropuertoAdapter;
    private final ClienteAdapter clienteAdapter;
    private final PlanAdapter planAdapter;
    private final PedidoAdapter pedidoAdapter;
    public Map<String, Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<Plan> planes;
    public List<Cliente> clientes;
    public List<Pedido> pedidos;

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
        this.origenes = new HashMap<>();
        origenes.put("SPIM", null);
        origenes.put("EBCI", null);
        origenes.put("UBBB", null);
        this.destinos = new ArrayList<>();
        this.planes = new ArrayList<>();
        this.clientes = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }

    public void cargarDatos() {
        G4D.Logger.logln("Cargando datos desde base de datos..");

        List<AeropuertoEntity> aeropuertosEntities = aeropuertoService.findAll();
        for (AeropuertoEntity entity : aeropuertosEntities) {
            Aeropuerto aeropuerto = aeropuertoAdapter.toAlgorithm(entity);
            if (origenes.containsKey(aeropuerto.getCodigo())) {
                origenes.put(aeropuerto.getCodigo(), aeropuerto);
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
