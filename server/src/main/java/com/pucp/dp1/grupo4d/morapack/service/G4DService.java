/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.adapter.*;
import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.mapper.*;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ExportationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ReplanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.SolutionPayload;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class G4DService {
    private final PedidoService pedidoService;
    private final PedidoAdapter pedidoAdapter;
    private final AeropuertoService aeropuertoService;
    private final AeropuertoAdapter aeropuertoAdapter;
    private final ClienteService clienteService;
    private final UsuarioAdapter usuarioAdapter;
    private final PlanService planService;
    private final PlanAdapter planAdapter;
    private final LoteAdapter loteAdapter;
    private final RutaService rutaService;
    private final RutaAdapter rutaAdapter;
    private final VueloAdapter vueloAdapter;
    private final VueloService vueloService;
    private final RegistroAdapter registroAdapter;
    private final ParametrosMapper parametrosMapper;
    private final PedidoMapper pedidoMapper;
    private final AeropuertoMapper aeropuertoMapper;
    private final VueloMapper vueloMapper;
    private final RutaMapper rutaMapper;
    private final SegmentacionAdapter segmentacionAdapter;
    private final ObjectProvider<G4DService> self;
    private Future<?> simulationTask = null;
    private Future<?> operationTask = null;
    private Future<?> exportationTask = null;
    private Long segundosEstimadosDeReplanificacion = null;
    private Problematica pSimulacion = null;
    private Problematica pOperacion = null;

    public G4DService(ClienteService clienteService, PedidoService pedidoService, SegmentacionAdapter segmentacionAdapter, ObjectProvider<G4DService> self,
                      PedidoMapper pedidoMapper, PedidoAdapter pedidoAdapter, AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter,
                      UsuarioAdapter usuarioAdapter, PlanService planService, PlanAdapter planAdapter, LoteAdapter loteAdapter,
                      AeropuertoMapper aeropuertoMapper, RutaMapper rutaMapper, VueloMapper vueloMapper, RegistroAdapter registroAdapter,
                      ParametrosMapper parametrosMapper, RutaService rutaService, VueloService vueloService, RutaAdapter rutaAdapter, VueloAdapter vueloAdapter) {
        this.clienteService = clienteService;
        this.pedidoService = pedidoService;
        this.segmentacionAdapter = segmentacionAdapter;
        this.self = self;
        this.pedidoMapper = pedidoMapper;
        this.pedidoAdapter = pedidoAdapter;
        this.aeropuertoService = aeropuertoService;
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.usuarioAdapter = usuarioAdapter;
        this.planService = planService;
        this.planAdapter = planAdapter;
        this.loteAdapter = loteAdapter;
        this.aeropuertoMapper = aeropuertoMapper;
        this.rutaMapper = rutaMapper;
        this.vueloMapper = vueloMapper;
        this.registroAdapter = registroAdapter;
        this.parametrosMapper = parametrosMapper;
        this.rutaService = rutaService;
        this.vueloService = vueloService;
        this.rutaAdapter = rutaAdapter;
        this.vueloAdapter = vueloAdapter;
    }

    public GenericResponse iniciarSimulacion(SimulationRequest request) {
        if(simulationTask != null) {
            throw new G4DException("Ya hay una simulación en proceso!");
        }
        simulationTask = self.getObject().simular(request).whenComplete((r, ex) -> simulationTask = null);
        WebSocketService.enviar("/topic/simulator-status", EstadoEjecucion.POR_INICIAR);
        return new GenericResponse(true, "Simulación en iniciación!");
    }

    @Async
    @Transactional
    public CompletableFuture<Void> simular(SimulationRequest request) {
        try {
            TipoEscenario escenario = TipoEscenario.SIMULACION;
            LocalDateTime inicioDeSimulacion = G4DUtility.Convertor.toDateTime(request.getFechaHoraInicio());
            LocalDateTime inicioDePlanificacion = inicioDeSimulacion;
            LocalDateTime finDePlanificacion = inicioDeSimulacion;
            LocalDateTime umbralDeReplanificacion = inicioDeSimulacion;
            LocalDateTime finDeSimulacion = G4DUtility.Convertor.toAdmissible(request.getFechaHoraFin(), LocalDateTime.MAX);
            ParametrosDTO parametros = request.getParametros();
            parametrosMapper.toAlgorithm(parametros);
            int maxDesfaseTemporalEnDias = Math.max(parametros.getMaxDiasEntregaIntracontinental(), parametros.getMaxDiasEntregaIntercontinental());
            Double multiplicadorTemporal = G4DUtility.Convertor.toAdmissible(request.getMultiplicadorTemporal(), 600.0);
            Double saltoTemporalEnHoras = G4DUtility.Convertor.toAdmissible(request.getTamanioDeSaltoTemporal(), 2.0);
            long saltoTemporalEnMinutos = (long) (60*saltoTemporalEnHoras);
            long minutosPlanificados = 0L;
            double horasPlanificadas = 0.0;
            boolean esPrimeraIteracion = true;
            while(inicioDePlanificacion.isBefore(finDeSimulacion) && simulationTask != null) {
                finDePlanificacion = finDePlanificacion.plusMinutes(saltoTemporalEnMinutos);
                Instant start = Instant.now();
                SolucionDTO solucion = planificar(escenario, inicioDePlanificacion, finDePlanificacion, umbralDeReplanificacion, umbralDeReplanificacion);
                if(solucion != null) {
                    WebSocketService.enviar("/topic/simulator", new SolutionPayload(solucion));
                } else {
                    WebSocketService.enviar("/topic/simulator-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.COLAPSO));
                    System.out.println("[*] COLAPSO LOGÍSTICO!");
                    return CompletableFuture.completedFuture(null);
                }
                if(esPrimeraIteracion) {
                    WebSocketService.enviar("/topic/simulator-status", new StatusPayload(EstadoEjecucion.INICIADO));
                    esPrimeraIteracion = false;
                }
                Instant end = Instant.now();
                long segundosSimulados = (long) (Duration.between(start, end).toMillis()*multiplicadorTemporal/500);
                umbralDeReplanificacion = umbralDeReplanificacion.plusSeconds(segundosSimulados);
                horasPlanificadas += saltoTemporalEnHoras;
                minutosPlanificados += saltoTemporalEnMinutos;
                long desfaseTemporal = (long) (60*(Math.min(horasPlanificadas, 24.0*maxDesfaseTemporalEnDias)));
                inicioDePlanificacion = inicioDePlanificacion.plusMinutes(minutosPlanificados).minusMinutes(desfaseTemporal);
            }
            if(simulationTask != null) {
                WebSocketService.enviar("/topic/simulator-status", new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.EXITOSO));
                System.out.println("[>] SIMULACION CONLCUIDA!");
            } else {
                WebSocketService.enviar("/topic/simulator-status", new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.FORZADO));
                System.out.println("[X] SIMULACION DETENIDA FORZOSAMENTE!");
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            WebSocketService.enviar("/topic/simulator-status", new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.ERRONEO));
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            pSimulacion = null;
            limpiarPools();
        }
    }

    public GenericResponse detenerSimulacion() {
        if(simulationTask == null) {
            throw new G4DException("No hay ninguna simulación en proceso!");
        }
        simulationTask.cancel(true);
        simulationTask = null;
        pSimulacion = null;
        limpiarPools();
        WebSocketService.enviar("/topic/simulator-status", new StatusPayload(EstadoEjecucion.POR_DETENER));
        return new GenericResponse(true, "Simulación en detenimiento!");
    }

    public GenericResponse replanificarOperacion(ReplanificationRequest request) {
        if(operationTask != null) {
            throw new G4DException("Ya hay una replanificación en proceso!");
        }
        operationTask = self.getObject().replanificar(request).whenComplete((r, ex) -> operationTask = null);
        WebSocketService.enviar("/topic/operator-status", EstadoEjecucion.INICIADO);
        return new GenericResponse(true, "Replanificación Iniciada!");
    }

    @Async
    @Transactional
    public CompletableFuture<Void> replanificar(ReplanificationRequest request) {
        try {
            TipoEscenario escenario = TipoEscenario.OPERACION;
            ParametrosDTO parametros = request.getParametros();
            parametrosMapper.toAlgorithm(parametros);
            long desfaseTemporal = 1440L*(Math.max(parametros.getMaxDiasEntregaIntracontinental(), parametros.getMaxDiasEntregaIntercontinental()));
            LocalDateTime fechaHoraActual = G4DUtility.Convertor.toAdmissible(request.getFechaHoraActual(), (LocalDateTime) null);
            LocalDateTime inicioPlanificacion = fechaHoraActual.minusMinutes(desfaseTemporal);
            LocalDateTime umbralDeReplanificacion = (this.segundosEstimadosDeReplanificacion != null) ? fechaHoraActual.plusSeconds(this.segundosEstimadosDeReplanificacion) : fechaHoraActual.plusMinutes(30L);
            LocalDateTime instanteDeProcesamiento = (this.segundosEstimadosDeReplanificacion != null) ? fechaHoraActual.plusSeconds(this.segundosEstimadosDeReplanificacion): fechaHoraActual.plusMinutes(5L);
            Instant start = Instant.now();
            SolucionDTO solucion = planificar(escenario, inicioPlanificacion, fechaHoraActual, umbralDeReplanificacion, instanteDeProcesamiento);
            if(solucion != null) {
                WebSocketService.enviar("/topic/operator", new SolutionPayload(solucion));
            } else {
                WebSocketService.enviar("/topic/operator-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.COLAPSO));
                System.out.println("[*] COLAPSO LOGÍSTICO!");
                return CompletableFuture.completedFuture(null);
            }
            Instant end = Instant.now();
            long segundosTranscurridos = Duration.between(start, end).toMillis()/1000;
            this.segundosEstimadosDeReplanificacion = (this.segundosEstimadosDeReplanificacion != null) ? Math.min(this.segundosEstimadosDeReplanificacion, segundosTranscurridos) : segundosTranscurridos;
            if(operationTask != null) {
                WebSocketService.enviar("/topic/operator-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
                System.out.println("[>] OPERACION ACTUALIZADA!");
            } else {
                WebSocketService.enviar("/topic/operator-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.FORZADO));
                System.out.println("[X] REPLANIFICACIÓN DETENIDA FORZOSAMENTE!");
            }
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            WebSocketService.enviar("/topic/operator-status", new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.ERRONEO));
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            pOperacion = null;
            limpiarPools();
        }
    }

    public GenericResponse exportarSolucion(ExportationRequest request) {
        if(exportationTask != null) {
            throw new G4DException("Ya hay una exportación en proceso!");
        }
        exportationTask = self.getObject().exportar(request).whenComplete((r, ex) -> exportationTask = null);
        WebSocketService.enviar("/topic/generator-status", new StatusPayload(EstadoEjecucion.INICIADO));
        return new GenericResponse(true, "Exportación iniciada!");
    }

    @Async
    @Transactional
    public CompletableFuture<Void> exportar(ExportationRequest request) {
        try {
            WebSocketService.enviar("/topic/generator-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            WebSocketService.enviar("/topic/generator-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {

        }
        return CompletableFuture.completedFuture(null);
    }

    private SolucionDTO planificar(TipoEscenario tipoEscenario, LocalDateTime inicioDePlanificacion, LocalDateTime finDePlanificacion, LocalDateTime umbralDeReplanificacion, LocalDateTime instanteDeProcesamiento) {
        boolean esSimulacion = tipoEscenario.equals(TipoEscenario.SIMULACION);
        Problematica.INICIO_PLANIFICACION = inicioDePlanificacion;
        Problematica.FIN_PLANIFICACION = finDePlanificacion;
        Problematica.UMBRAL_REPLANIFICACION = umbralDeReplanificacion;
        Problematica.INSTANTE_DE_PROCESAMIENTO = instanteDeProcesamiento;
        Problematica.ESCENARIO = tipoEscenario.toString().toUpperCase();
        Problematica problematica;
        if(esSimulacion) {
            if(pSimulacion == null) {
                pSimulacion = new Problematica();
                pSimulacion.cargarAeropuertos(aeropuertoService, aeropuertoAdapter);
                pSimulacion.cargarPlanes(planService, planAdapter);
            }
            pSimulacion.cargarClientes(clienteService, usuarioAdapter);
            pSimulacion.cargarPedidos(pedidoService, pedidoAdapter);
            problematica = pSimulacion;
            System.out.printf("[*] SIMULANDO BLOQUE TEMPORAL! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        } else {
            pOperacion = new Problematica();
            pOperacion.cargarAeropuertos(aeropuertoService, aeropuertoAdapter);
            pOperacion.cargarPlanes(planService, planAdapter);
            pOperacion.cargarClientes(clienteService, usuarioAdapter);
            pOperacion.cargarPedidos(pedidoService, pedidoAdapter);
            pOperacion.cargarVuelos(vueloService, vueloAdapter);
            pOperacion.cargarRutas(rutaService, rutaAdapter);
            problematica = pOperacion;
            System.out.printf("[*] REPLANIFICACION BLOQUE TEMPORAL DE OPERACION! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        }
        GVNS gvns = new GVNS();
        gvns.planificar(problematica);
        Solucion solucion = gvns.getSolucion();
        if(solucion == null) {
            return null;
        }
        if(!esSimulacion) {
            almacenarSolucion(solucion);
            limpiarPools();
        }
        return devolverSolucion(solucion);
    }

    private void almacenarSolucion(Solucion solucion) {
        if (solucion == null || solucion.getPedidosAtendidos() == null) {
            return;
        }
        System.out.println("\nGuardando solución en bd..\n");
        // Vuelos (ADD)
        for (Vuelo vuelo : solucion.getVuelosEnTransito()) {
            VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
            if (vueloEntity != null && vueloEntity.getId() == null) {
                vueloService.save(vueloEntity);
                System.out.println("[*] VUELO: " + vueloEntity.getCodigo());
            }
        }
        // Rutas (ADD/UP) & Vuelos (UP)
        for (Ruta ruta : solucion.getRutasEnOperacion()) {
            RutaEntity rutaEntity = rutaAdapter.toEntity(ruta);
            if (rutaEntity != null) {
                for (Vuelo vuelo : ruta.getVuelos()) {
                    VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
                    if (vueloEntity != null) {
                        if(rutaEntity.getVuelos().contains(vueloEntity)) {
                            rutaEntity.getVuelos().remove(vueloEntity);
                        }
                        rutaEntity.getVuelos().add(vueloEntity);
                    }
                }
                rutaService.save(rutaEntity);
                System.out.println("[*] RUTA: " + rutaEntity.getCodigo() + " {'" + rutaEntity.getVuelos().size() + "' vuelos!}");
            }
        }
        // Pedidos (UP) & Segmentaciones (ADD/UP)
        for (Pedido pedido : solucion.getPedidosAtendidos()) {
            PedidoEntity pedidoEntity = pedidoAdapter.toEntity(pedido);
            if(pedidoEntity != null) {
                for(Segmentacion segmentacion : pedido.getSegmentaciones()) {
                    SegmentacionEntity segmentacionEntity = segmentacionAdapter.toEntity(segmentacion);
                    if(segmentacionEntity != null) {
                        if(pedidoEntity.getSegmentaciones().contains(segmentacionEntity)) {
                            pedidoEntity.getSegmentaciones().remove(segmentacionEntity);
                        }
                        pedidoEntity.getSegmentaciones().add(segmentacionEntity);
                    }
                }
            }
            pedidoService.save(pedidoEntity);
            System.out.println("[*] PEDIDO: " + pedidoEntity.getCodigo() + " ('" + pedidoEntity.getSegmentaciones().getLast().getLotes().size() + "' lotes!)");
        }
        // Aeropuertos (UP) && Registros (ADD/UP)
        for(Aeropuerto aeropuerto : solucion.getAeropuertosTransitados()) {
            AeropuertoEntity aeropuertoEntity = aeropuertoAdapter.toEntity(aeropuerto);
            if(aeropuertoEntity != null) {
                for(Registro registro : aeropuerto.getRegistros()) {
                    RegistroEntity registroEntity = registroAdapter.toEntity(registro);
                    if(registroEntity != null) {
                        if(aeropuertoEntity.getRegistros().contains(registroEntity)) {
                            aeropuertoEntity.getRegistros().remove(registroEntity);
                        }
                        aeropuertoEntity.getRegistros().add(registroEntity);
                    }
                }
                aeropuertoService.save(aeropuertoEntity);
                System.out.println("[*] AEROPUERTO: " + aeropuertoEntity.getCodigo());
            }
        }
        System.out.println("\nSOLUCIÓN ALMACENADA!");
    }

    private SolucionDTO devolverSolucion(Solucion solucion) {
        SolucionDTO solucionDTO = new SolucionDTO();
        solucionDTO.setRatioPromedioDeUtilizacionTemporal(solucion.getRatioPromedioDeUtilizacionTemporal());
        solucionDTO.setRatioPromedioDeDesviacionEspacial(solucion.getRatioPromedioDeDesviacionEspacial());
        solucionDTO.setRatioPromedioDeDisposicionOperacional(solucion.getRatioPromedioDeDisposicionOperacional());
        List<PedidoDTO> pedidosAtendidos = new ArrayList<>();
        solucion.getPedidosAtendidos().forEach(p -> pedidosAtendidos.add(pedidoMapper.toDTO(p)));
        solucionDTO.setPedidosAtendidos(pedidosAtendidos);
        List<AeropuertoDTO> aeropuertosTransitados = new ArrayList<>();
        solucion.getAeropuertosTransitados().forEach(a ->  aeropuertosTransitados.add(aeropuertoMapper.toDTO(a)));
        solucionDTO.setAeropuertosTransitados(aeropuertosTransitados);
        List<VueloDTO> vuelosEnTransito = new ArrayList<>();
        solucion.getVuelosEnTransito().forEach(v -> vuelosEnTransito.add(vueloMapper.toDTO(v)));
        solucionDTO.setVuelosEnTransito(vuelosEnTransito);
        List<RutaDTO> rutasEnOperacion = new ArrayList<>();
        solucion.getRutasEnOperacion().forEach(r -> rutasEnOperacion.add(rutaMapper.toDTO(r)));
        solucionDTO.setRutasEnOperacion(rutasEnOperacion);
        return solucionDTO;
    }

    private void limpiarPools() {
        vueloAdapter.clearPools();
        vueloMapper.clearPools();
        rutaAdapter.clearPools();
        rutaMapper.clearPools();
        loteAdapter.clearPools();
        pedidoAdapter.clearPools();
        pedidoMapper.clearPools();
        aeropuertoAdapter.clearPools();
        aeropuertoMapper.clearPools();
        usuarioAdapter.clearPools();
        planAdapter.clearPools();
        registroAdapter.clearPools();
    }
}
