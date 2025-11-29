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
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ExportationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ReplanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SimulationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.StatusResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoProceso;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class G4DService {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoAdapter pedidoAdapter;

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private AeropuertoAdapter aeropuertoAdapter;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioAdapter usuarioAdapter;

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanAdapter planAdapter;

    @Autowired
    private LoteAdapter loteAdapter;

    @Autowired
    private RutaService rutaService;

    @Autowired
    private RutaAdapter rutaAdapter;

    @Autowired
    private VueloAdapter vueloAdapter;

    @Autowired
    private VueloService vueloService;

    @Autowired
    private RegistroAdapter registroAdapter;

    @Autowired
    private ParametrosMapper parametrosMapper;

    @Autowired
    private PedidoMapper pedidoMapper;

    @Autowired
    private AeropuertoMapper aeropuertoMapper;

    @Autowired
    private VueloMapper vueloMapper;

    @Autowired
    private RutaMapper rutaMapper;

    @Autowired
    private SegmentacionAdapter segmentacionAdapter;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private Future<?> simulationTask = null;
    private Future<?> operationTask = null;
    private Future<?> exportationTask = null;
    private Long segundosEstimadosDeReplanificacion = null;
    private Problematica pSimulacion = null;
    private Problematica pOperacion = null;

    public StatusResponse iniciarSimulacion(SimulationRequest request) throws  Exception {
        if(simulationTask != null) {
            throw new G4DException("Ya hay una simulación en proceso!");
        }
        simulationTask = executorService.submit(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.execute(status -> {
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
                    while(inicioDePlanificacion.isBefore(finDeSimulacion) && !Thread.currentThread().isInterrupted()) {
                        finDePlanificacion = finDePlanificacion.plusMinutes(saltoTemporalEnMinutos);
                        Instant start = Instant.now();
                        SolutionResponse solutionResponse = planificar(escenario, inicioDePlanificacion, finDePlanificacion, umbralDeReplanificacion, umbralDeReplanificacion);
                        if(solutionResponse.getExito()) {
                            messagingTemplate.convertAndSend("/topic/simulator", solutionResponse);
                        } else {
                            messagingTemplate.convertAndSend("/topic/simulator-status", new StatusResponse(true, "COLAPSO!", EstadoProceso.COLAPSADO));
                            System.out.println("[X] COLAPSO LOGÍSTICO!");
                            break;
                        }
                        if(esPrimeraIteracion) {
                            messagingTemplate.convertAndSend("/topic/simulator-status", new StatusResponse(true, "Simulación iniciada!", EstadoProceso.INICIADO));
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
                    if(!Thread.currentThread().isInterrupted()) {
                        messagingTemplate.convertAndSend("/topic/simulator-status", new StatusResponse(true, "Simulación correctamente finalizada!", EstadoProceso.FINALIZADO));
                        System.out.println("[>] SIMULACION CONLCUIDA!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    pSimulacion = null;
                    limpiarPools();
                }
                return null;
            });
        });
        return new StatusResponse(true, "Simulación puesta en marcha!", EstadoProceso.POR_INICIAR);
    }

    public StatusResponse detenerSimulacion() throws Exception {
        if(simulationTask == null) {
            throw new G4DException("No hay ninguna simulación en proceso!");
        }
        simulationTask.cancel(true);
        simulationTask = null;
        pSimulacion = null;
        limpiarPools();
        return new StatusResponse(true, "Simulación detenida!", EstadoProceso.DETENIDO);
    }

    public StatusResponse replanificarOperacion(ReplanificationRequest request) throws Exception{
        if(operationTask != null) {
            throw new G4DException("Ya hay una replanificación en proceso!");
        }
        operationTask = executorService.submit(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.execute(status -> {
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
                    SolutionResponse solutionResponse = planificar(escenario, inicioPlanificacion, fechaHoraActual, umbralDeReplanificacion, instanteDeProcesamiento);
                    if(solutionResponse.getExito()) {
                        messagingTemplate.convertAndSend("/topic/operator", solutionResponse);
                    } else {
                        messagingTemplate.convertAndSend("/topic/operator-status", new StatusResponse(true, "COLAPSO!", EstadoProceso.COLAPSADO));
                        System.out.println("[X] COLAPSO LOGÍSTICO!");
                    }
                    Instant end = Instant.now();
                    long segundosTranscurridos = Duration.between(start, end).toMillis()/1000;
                    this.segundosEstimadosDeReplanificacion = (this.segundosEstimadosDeReplanificacion != null) ? Math.min(this.segundosEstimadosDeReplanificacion, segundosTranscurridos) : segundosTranscurridos;
                    if(!Thread.currentThread().isInterrupted()) {
                        messagingTemplate.convertAndSend("/topic/operator-status", new StatusResponse(true, "Operación correctamente replanificada!", EstadoProceso.FINALIZADO));
                        System.out.println("[>] OPERACION ACTUALIZADA!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    pOperacion = null;
                    limpiarPools();
                }
                return null;
            });
        });
        return new StatusResponse(true, "Replanificación Iniciada!", EstadoProceso.INICIADO);
    }

    public StatusResponse exportarSolucion(ExportationRequest request) throws Exception {
        if(exportationTask != null) {
            throw new G4DException("Ya hay una exportación en proceso!");
        }
        exportationTask = executorService.submit(() -> {
            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            tx.execute(status -> {
                messagingTemplate.convertAndSend("/topic/generator-status", new StatusResponse(true, "Exportación correctamente finalizada!", EstadoProceso.FINALIZADO));
                System.out.println("[>] EXPORTACIÓN CONLCUIDA!");
                return null;
            });
        });
        return new StatusResponse(true, "Exportación iniciada!", EstadoProceso.INICIADO);
    }

    private SolutionResponse planificar(TipoEscenario tipoEscenario, LocalDateTime inicioDePlanificacion, LocalDateTime finDePlanificacion, LocalDateTime umbralDeReplanificacion, LocalDateTime instanteDeProcesamiento) {
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
            return new SolutionResponse(false, "COLAPSO!");
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

    private SolutionResponse devolverSolucion(Solucion solucion) {
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
        return new SolutionResponse(true, "Planificación correctamente concluida!", solucionDTO);
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
