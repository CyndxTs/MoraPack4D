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
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
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
    private final ParametrosService parametrosService;
    private volatile boolean simulationFlag = false;
    private Future<?> simulationTask = null;
    private Problematica simulationContext = null;
    private Future<?> operationTask = null;
    private Problematica operationContext = null;
    private Future<?> exportationTask = null;
    private Long segundosEstimadosDeReplanificacion = null;



    public G4DService(ClienteService clienteService, PedidoService pedidoService, SegmentacionAdapter segmentacionAdapter, ObjectProvider<G4DService> self,
                      PedidoMapper pedidoMapper, PedidoAdapter pedidoAdapter, AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter,
                      UsuarioAdapter usuarioAdapter, PlanService planService, PlanAdapter planAdapter, LoteAdapter loteAdapter,
                      AeropuertoMapper aeropuertoMapper, RutaMapper rutaMapper, VueloMapper vueloMapper, RegistroAdapter registroAdapter,
                      ParametrosMapper parametrosMapper, RutaService rutaService, VueloService vueloService, RutaAdapter rutaAdapter, VueloAdapter vueloAdapter, ParametrosService parametrosService) {
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
        this.parametrosService = parametrosService;
    }

    public GenericResponse iniciarSimulacion(SimulationRequest request) {
        if(simulationTask != null) {
            throw new G4DException("Ya hay una simulación en proceso!");
        } else simulationFlag = true;
        simulationTask = self.getObject().simular(request).whenComplete((r, ex) -> { simulationFlag = false; simulationTask = null; });
        WebSocketService.enviar("/topic/simulator-status", EstadoEjecucion.POR_INICIAR);
        return new GenericResponse(true, "Simulación en iniciación!");
    }

    @Async
    @Transactional
    public CompletableFuture<Void> simular(SimulationRequest request) {
        try {
            LocalDateTime inicioDeSimulacion = G4DUtility.Convertor.toDateTime(request.getFechaHoraInicio());
            LocalDateTime finDeSimulacion = G4DUtility.Convertor.toAdmissible(request.getFechaHoraFin(), LocalDateTime.MAX);
            ParametrosDTO parametros = request.getParametros();
            parametrosMapper.toAlgorithm(parametros);
            int multiplicadorTemporal = G4DUtility.Convertor.toAdmissible(request.getMultiplicadorTemporal(), 120);
            double saltoDeAlgoritmo = G4DUtility.Convertor.toAdmissible(request.getSaltoDeAlgoritmo(), 2.0);
            long saltoDeConsumo = (long) (multiplicadorTemporal * saltoDeAlgoritmo);
            long limiteDeDesfaseTemporal = 1440L*Math.max(G4DUtility.Convertor.toAdmissible(parametros.getMaxDiasEntregaIntracontinental(), 2), G4DUtility.Convertor.toAdmissible(parametros.getMaxDiasEntregaIntercontinental(), 3));
            LocalDateTime umbralDeReplanificacion = inicioDeSimulacion;
            LocalDateTime inicioDePlanificacion = inicioDeSimulacion;
            LocalDateTime finDePlanificacion = inicioDeSimulacion;
            long minutosPlanificados = 0L;
            boolean esPrimeraIteracion = true;
            while(simulationFlag && !Thread.currentThread().isInterrupted()) {
                finDePlanificacion = (finDePlanificacion.plusMinutes(saltoDeConsumo).isAfter(finDeSimulacion)) ? finDeSimulacion : finDePlanificacion.plusMinutes(saltoDeConsumo);
                Instant start = Instant.now();
                SolucionDTO solucion = planificar(TipoEscenario.SIMULACION, inicioDePlanificacion, finDePlanificacion, umbralDeReplanificacion, umbralDeReplanificacion);
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
                long segundosSimulados = Duration.between(start, end).toMillis()*multiplicadorTemporal/333;
                umbralDeReplanificacion = umbralDeReplanificacion.plusSeconds(segundosSimulados).plusSeconds((long)(60*saltoDeAlgoritmo));
                minutosPlanificados += saltoDeConsumo;
                long desfaseTemporal = Math.min(minutosPlanificados, limiteDeDesfaseTemporal);
                if(inicioDePlanificacion.plusMinutes(minutosPlanificados).isBefore(finDeSimulacion)) {
                    inicioDePlanificacion = inicioDeSimulacion.plusMinutes(minutosPlanificados).minusMinutes(desfaseTemporal);
                } else break;
                try {
                    Thread.sleep((long)(60000L*saltoDeAlgoritmo)); // provisional hasta implementarexceutors periodicos
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if(simulationFlag && !Thread.currentThread().isInterrupted()) {
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
            simulationContext = null;
            limpiarPools();
        }
    }

    public GenericResponse detenerSimulacion() {
        if(simulationTask == null) {
            throw new G4DException("No hay ninguna simulación en proceso!");
        }
        simulationFlag = false;
        simulationTask.cancel(true);
        simulationTask = null;
        simulationContext = null;
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
            boolean almacenarParametrizacion = (request.getAlmacenarParametrizacion() != null) ?  request.getAlmacenarParametrizacion() : false ;
            ParametrosDTO parametros = request.getParametros();
            if(almacenarParametrizacion) {
                ImportRequest<ParametrosDTO> importRequest = new ImportRequest<>();
                importRequest.setDto(parametros);
                parametrosService.importar(importRequest);
            }
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
            operationContext = null;
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
        Problematica.INSTANTE_PROCESAMIENTO = instanteDeProcesamiento;
        Problematica.ESCENARIO = tipoEscenario.toString().toUpperCase();
        Problematica problematica;
        if(esSimulacion) {
            if(simulationContext == null) {
                simulationContext = new Problematica();
                simulationContext.cargarAeropuertos(aeropuertoService, aeropuertoAdapter);
                simulationContext.cargarPlanes(planService, planAdapter);
            }
            simulationContext.cargarClientes(clienteService, usuarioAdapter);
            simulationContext.cargarPedidos(pedidoService, pedidoAdapter);
            simulationContext.cargarRutas(rutaService, rutaAdapter);
            problematica = simulationContext;
        } else {
            operationContext = new Problematica();
            operationContext.cargarAeropuertos(aeropuertoService, aeropuertoAdapter);
            operationContext.cargarPlanes(planService, planAdapter);
            operationContext.cargarClientes(clienteService, usuarioAdapter);
            operationContext.cargarPedidos(pedidoService, pedidoAdapter);
            operationContext.cargarVuelos(vueloService, vueloAdapter);
            operationContext.cargarRutas(rutaService, rutaAdapter);
            problematica = operationContext;
        }
        System.out.printf("[*] SIMULANDO BLOQUE TEMPORAL! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        GVNS gvns = new GVNS();
        gvns.planificar(problematica);
        Solucion solucion = gvns.getSolucion();
        System.out.printf("[*] BLOQUE TEMPORAL PLANIFICADO! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
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
        rutaAdapter.clearPools();
        loteAdapter.clearPools();
        pedidoAdapter.clearPools();
        aeropuertoAdapter.clearPools();
        usuarioAdapter.clearPools();
        planAdapter.clearPools();
        registroAdapter.clearPools();
    }
}
