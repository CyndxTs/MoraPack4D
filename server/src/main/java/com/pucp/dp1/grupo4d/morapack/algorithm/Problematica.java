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
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import java.time.LocalDateTime;
import java.util.*;

public class Problematica {
    public String idTransaccion;
    public Integer maxDiasDeEntregaIntracontinental;
    public Integer maxDiasDeEntregaIntercontinental;
    public Double maxHorasDeRecojo;
    public Double minHorasDeEstancia;
    public Double maxHorasDeEstancia;
    public Double probabilidadDeReplanificacion;
    public Double factorDeLejania;
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
        return this.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes, new HashMap<>());
    }

    public Problematica replicar(Map<String, Cliente> poolClientes, Map<String, Aeropuerto> poolAeropuertos, Map<String, Ruta> poolRutas, Map<String, Lote> poolLotes, Map<String, Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        return this.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes, new HashMap<>());
    }

    // 'poolPedidos' compartido con Solucion.replicar: así la problemática y la solución replicadas apuntan a los mismos pedidos
    public Problematica replicar(Map<String, Cliente> poolClientes, Map<String, Aeropuerto> poolAeropuertos, Map<String, Ruta> poolRutas, Map<String, Lote> poolLotes, Map<String, Vuelo> poolVuelos, Map<String, Plan> poolPlanes, Map<String, Pedido> poolPedidos){
        Problematica problematica = new Problematica();
        problematica.idTransaccion = this.idTransaccion;
        problematica.maxDiasDeEntregaIntracontinental = this.maxDiasDeEntregaIntracontinental;
        problematica.maxDiasDeEntregaIntercontinental = this.maxDiasDeEntregaIntercontinental;
        problematica.maxHorasDeRecojo = this.maxHorasDeRecojo;
        problematica.minHorasDeEstancia = this.minHorasDeEstancia;
        problematica.maxHorasDeEstancia = this.maxHorasDeEstancia;
        problematica.probabilidadDeReplanificacion = this.probabilidadDeReplanificacion;
        problematica.factorDeLejania = this.factorDeLejania;
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
        this.pedidos.forEach(p -> problematica.pedidos.add(poolPedidos.computeIfAbsent(p.getCodigo(), codigo -> p.replicar(poolClientes, poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes))));
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
        this.factorDeLejania = problematica.factorDeLejania;
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
        G4DUtility.Logger.logln(">> Cargando aeropuertos..");
        List<AeropuertoEntity> aeropuertosEntity = aeropuertoService.findAll();
        aeropuertosEntity.forEach(entity -> {
            // La simulación arranca con los almacenes vacíos: los registros guardados en BD son de la operación
            Aeropuerto aeropuerto = aeropuertoAdapter.toAlgorithm(idTransaccion, entity, !"SIMULACION".equals(this.tipoEscenario));
            if (this.origenes.containsKey(aeropuerto.getCodigo())) {
                aeropuerto.setEsSede(true);
                origenes.put(aeropuerto.getCodigo(), aeropuerto);
            } else {
                aeropuerto.setEsSede(false);
                destinos.add(aeropuerto);
            }
        });
        G4DUtility.Logger.logf("[:] AEROPUERTOS CARGADOS! | '%d' origenes! & '%d' destinos!%n", origenes.size(), destinos.size());
    }

    public void cargarPlanes(PlanService planService, PlanAdapter planAdapter) {
        G4DUtility.Logger.logln(">> Cargando planes..");
        List<PlanEntity> planesEntity = planService.findAllConEventos(); // una sola consulta (sin N+1 de eventos)
        int cantEventos = 0;
        for (PlanEntity entity : planesEntity) {
            Plan plan = planAdapter.toAlgorithm(idTransaccion, entity);
            plan.getEventos().removeIf(e -> !Objects.equals(e.getTipoEscenario(), this.tipoEscenario));
            cantEventos += plan.getEventos().size();
            planes.add(plan);
        }
        G4DUtility.Logger.logf("[:] PLANES DE VUELO CARGADOS! | '%d' planes! & '%d' eventos!%n", planes.size(), cantEventos);
    }

    public void cargarClientes(UsuarioService usuarioService, UsuarioAdapter usuarioAdapter) {
        G4DUtility.Logger.logln(">> Cargando clientes..");
        List<UsuarioEntity> clientesEntity = usuarioService.findAllClientesInRangeByScenario(this.inicioDePlanificacion, this.finDePlanificacion, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        clientesEntity.forEach(entity -> {
            if(clientes.stream().noneMatch(c -> c.getCodigo().equals(entity.getCodigo()))) {
                Cliente cliente = usuarioAdapter.toAlgorithm(idTransaccion, entity);
                clientes.add(cliente);
            }
        });
        G4DUtility.Logger.logf("[:] CLIENTES CARGADOS! | '%d' clientes!%n", clientes.size());
    }

    public void cargarPedidos(PedidoService pedidoService, PedidoAdapter pedidoAdapter) {
        G4DUtility.Logger.logln(">> Cargando pedidos..");
        List<PedidoEntity> pedidosEntity = pedidoService.findAllInRangeByScenario(this.inicioDePlanificacion, this.finDePlanificacion, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        Set<String> codigosCargados = new HashSet<>();
        pedidos.forEach(p -> codigosCargados.add(p.getCodigo()));
        this.reintentarNoAtendidos();
        pedidosEntity.forEach(entity -> {
            if(codigosCargados.add(entity.getCodigo())) {
                Pedido pedido = pedidoAdapter.toAlgorithm(idTransaccion, entity);
                if (pedido.getFechaHoraProcesamiento() == null) {
                    pedido.setFechaHoraProcesamiento(this.instanteDeProcesamiento);
                }
                pedidos.add(pedido);
            }
        });
        int cantAtendidos = pedidos.stream().filter(Pedido::getFueAtendido).toList().size();
        G4DUtility.Logger.logf("[:] PEDIDOS CARGADOS! | '%d' por atender! & '%d' ya atendidos!%n", pedidos.size() - cantAtendidos, cantAtendidos);
    }

    // Pedidos que quedaron sin atender en un bloque anterior: se reintentan desde el umbral actual
    // (los vuelos anteriores al umbral ya despegaron en el tiempo simulado)
    private void reintentarNoAtendidos() {
        if (this.umbralDeReplanificacion == null) return;
        pedidos.stream().filter(p -> !p.getFueAtendido())
                        .filter(p -> p.getFechaHoraProcesamiento() == null || p.getFechaHoraProcesamiento().isBefore(this.umbralDeReplanificacion))
                        .forEach(p -> p.setFechaHoraProcesamiento(this.umbralDeReplanificacion));
    }

    // ---------- Simulación: foto fija de los datos al iniciar ----------
    // Una simulación trabaja con los pedidos que existían al iniciarla (los que se registren después no entran).
    // Se cargan una sola vez para todo su rango y en cada bloque se liberan los que ya se generaron.
    public final List<Pedido> pedidosPorLiberar = new ArrayList<>();

    public void cargarFotoDePedidos(PedidoService pedidoService, PedidoAdapter pedidoAdapter, LocalDateTime inicio, LocalDateTime fin) {
        G4DUtility.Logger.logln(">> Cargando pedidos de la simulación (foto fija)..");
        List<PedidoEntity> pedidosEntity = pedidoService.findAllInRangeByScenario(inicio, fin, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        pedidosEntity.forEach(entity -> pedidosPorLiberar.add(pedidoAdapter.toAlgorithm(idTransaccion, entity)));
        pedidosPorLiberar.sort(Comparator.comparing(Pedido::getFechaHoraGeneracion));
        G4DUtility.Logger.logf("[:] FOTO DE PEDIDOS CARGADA! | '%d' pedidos en el rango de la simulación%n", pedidosPorLiberar.size());
    }

    // Pasa a la problemática los pedidos de la foto generados hasta el fin del bloque actual
    public void liberarPedidosDeLaFoto() {
        this.reintentarNoAtendidos();
        int liberados = 0;
        while (!pedidosPorLiberar.isEmpty() && !pedidosPorLiberar.getFirst().getFechaHoraGeneracion().isAfter(this.finDePlanificacion)) {
            Pedido pedido = pedidosPorLiberar.removeFirst();
            if (pedido.getFechaHoraProcesamiento() == null) pedido.setFechaHoraProcesamiento(pedido.getFechaHoraGeneracion());
            pedidos.add(pedido);
            liberados++;
        }
        G4DUtility.Logger.logf("[:] PEDIDOS DEL BLOQUE! | '%d' nuevos, '%d' en RAM, '%d' por llegar%n", liberados, pedidos.size(), pedidosPorLiberar.size());
    }

    public void cargarVuelos(VueloService vueloService, VueloAdapter vueloAdapter) {
        G4DUtility.Logger.logln(">> Cargando vuelos..");
        List<VueloEntity> vuelosEntity = vueloService.findAllInRangeByScenario(this.inicioDePlanificacion, this.finDePlanificacion, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        vuelosEntity.forEach(entity -> {
            Vuelo vuelo = vueloAdapter.toAlgorithm(idTransaccion, entity);
            vuelos.add(vuelo);
        });
        G4DUtility.Logger.logf("[:] VUELOS CARGADOS! | '%d' vuelos!%n", vuelos.size());
    }

    public void cargarRutas(RutaService rutaService, RutaAdapter rutaAdapter) {
        G4DUtility.Logger.logln(">> Cargando rutas..");
        List<RutaEntity> rutasEntity = rutaService.findAllInRangeByScenario(this.inicioDePlanificacion, this.finDePlanificacion, this.tipoEscenario, new ArrayList<>(this.origenes.keySet()));
        rutasEntity.forEach(entity -> {
            Ruta ruta = rutaAdapter.toAlgorithm(idTransaccion, entity);
            rutas.add(ruta);
        });
        rutas.stream().filter(r -> r.getEstado().equals(EstadoRuta.OPERATIVA)).forEach(r -> r.setEstado(EstadoRuta.REVISION_PENDIENTE));
        G4DUtility.Logger.logf("[:] RUTAS CARGADAS! | '%d' rutas!%n", rutas.size());
    }

    // Último instante en que un pedido aún podría entregarse a tiempo (el mayor plazo entre sus posibles orígenes)
    public LocalDateTime obtenerPlazoMaximo(Pedido pedido) {
        int dias = 0;
        for (Aeropuerto origen : this.origenes.values()) {
            if (origen == null || pedido.getDestino() == null) continue;
            boolean mismoContinente = java.util.Objects.equals(origen.getContinente(), pedido.getDestino().getContinente());
            dias = Math.max(dias, mismoContinente ? this.maxDiasDeEntregaIntracontinental : this.maxDiasDeEntregaIntercontinental);
        }
        if (dias == 0) dias = Math.max(this.maxDiasDeEntregaIntracontinental, this.maxDiasDeEntregaIntercontinental);
        return pedido.getFechaHoraGeneracion().plusDays(dias);
    }

    // Libera de la RAM lo que ya terminó antes de 'instanteDeCorte' (inicio de la siguiente ventana):
    // pedidos entregados (llegada + recojo <= corte), sus lotes y registros, rutas y vuelos que ya no usa nadie.
    // Devuelve los pedidos desalojados (para escribirlos en el reporte antes de perderlos).
    public List<Pedido> desalojar(LocalDateTime instanteDeCorte) {
        List<Pedido> desalojados = new ArrayList<>();
        long minutosDeRecojo = (long)(60*this.maxHorasDeRecojo);
        for (Pedido pedido : this.pedidos) {
            if (!pedido.getFueAtendido()) continue;
            Segmentacion sVigente = pedido.obtenerSegementacionVigente();
            if (sVigente == null || sVigente.getLotesPorRuta().isEmpty()) continue;
            boolean terminado = sVigente.getLotesPorRuta().keySet().stream().allMatch(r -> !r.getFechaHoraLlegada().plusMinutes(minutosDeRecojo).isAfter(instanteDeCorte));
            if (terminado) desalojados.add(pedido);
        }
        if (desalojados.isEmpty()) return desalojados;
        Set<Pedido> setDesalojados = new HashSet<>(desalojados);
        Set<Lote> lotesDesalojados = new HashSet<>();
        desalojados.forEach(p -> p.getSegmentaciones().forEach(sg -> lotesDesalojados.addAll(sg.getLotesPorRuta().values())));
        this.pedidos.removeIf(setDesalojados::contains);
        Set<Ruta> rutasEnUso = new HashSet<>();
        this.pedidos.forEach(p -> p.getSegmentaciones().forEach(sg -> rutasEnUso.addAll(sg.getLotesPorRuta().keySet())));
        int rutasAntes = this.rutas.size(), vuelosAntes = this.vuelos.size(), registros = 0;
        this.rutas.removeIf(r -> !rutasEnUso.contains(r));
        Set<Vuelo> vuelosEnUso = new HashSet<>();
        this.rutas.forEach(r -> vuelosEnUso.addAll(r.getVuelos()));
        this.vuelos.removeIf(v -> !vuelosEnUso.contains(v) && !v.getFechaHoraLlegada().isAfter(instanteDeCorte));
        for (Aeropuerto aeropuerto : this.origenes.values()) registros += aeropuerto.desalojarRegistros(lotesDesalojados, instanteDeCorte);
        for (Aeropuerto aeropuerto : this.destinos) registros += aeropuerto.desalojarRegistros(lotesDesalojados, instanteDeCorte);
        // Los clientes NO se desalojan: en simulación viven en RAM toda la simulación (un pedido posterior puede ser suyo)
        G4DUtility.Logger.logf("[~] RAM LIBERADA! | '%d' pedidos, '%d' rutas, '%d' vuelos y '%d' registros desalojados%n", desalojados.size(), rutasAntes - this.rutas.size(), vuelosAntes - this.vuelos.size(), registros);
        return desalojados;
    }
}
