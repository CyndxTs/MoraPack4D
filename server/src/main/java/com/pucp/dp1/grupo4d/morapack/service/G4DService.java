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
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ExportationPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.SolutionPayload;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DExceptionHandlerAsync;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
public class G4DService {
    private final SegmentacionService segmentacionService;
    private final LoteService loteService;
    private final RegistroService registroService;

    private static class G4DContext {
        public volatile boolean running = true;
        public Future<?> task;
        public Problematica problematic;
        public Solucion solution;
    }
    private final G4DExceptionHandlerAsync asyncExceptionHandler;
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
    private final AdministradorService administradorService;
    private final ObjectProvider<G4DService> self;
    private final Map<String, G4DContext> simulationContexts = new ConcurrentHashMap<>();
    private final Map<String, G4DContext> exportationContexts = new ConcurrentHashMap<>();
    private final G4DContext replanificationContext = new G4DContext();
    private final long tiempoMaximoDeReplanificacionEnSegundos = 180L;
    private final long tiempoMaximoDeCargaEnSegundos = 90L;

    public G4DService(ClienteService clienteService, PedidoService pedidoService, SegmentacionAdapter segmentacionAdapter, ObjectProvider<G4DService> self,
                      PedidoMapper pedidoMapper, PedidoAdapter pedidoAdapter, AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter,
                      UsuarioAdapter usuarioAdapter, PlanService planService, PlanAdapter planAdapter, LoteAdapter loteAdapter,
                      AeropuertoMapper aeropuertoMapper, RutaMapper rutaMapper, VueloMapper vueloMapper, RegistroAdapter registroAdapter,
                      ParametrosMapper parametrosMapper, RutaService rutaService, VueloService vueloService, RutaAdapter rutaAdapter, VueloAdapter vueloAdapter, AdministradorService administradorService, G4DExceptionHandlerAsync asyncExceptionHandler, SegmentacionService segmentacionService, LoteService loteService, RegistroService registroService) {
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
        this.administradorService = administradorService;
        this.asyncExceptionHandler = asyncExceptionHandler;
        this.segmentacionService = segmentacionService;
        this.loteService = loteService;
        this.registroService = registroService;
    }

    public GenericResponse iniciarImportacion(MultipartFile file, ImportFileRequest request) throws IOException {
        GenericResponse response = new GenericResponse(true, "Importación iniciada!");
        String idTransaccion = response.getToken();
        Path archivo = Files.createTempFile("import-" + idTransaccion + "-", "-" + file.getOriginalFilename());
        file.transferTo(archivo.toFile());
        self.getObject().importar(idTransaccion.substring(4), archivo, request).exceptionally(ex -> { asyncExceptionHandler.handleException("Importation-" + idTransaccion, ex); return null; });
        return response;
    }

    @Async("importationExecutor")
    @Transactional
    public CompletableFuture<Void> importar(String idTransaccion, Path archivo, ImportFileRequest request) {
        switch (request.getTipoArchivo().toUpperCase()) {
            case "ADMINISTRADORES" -> administradorService.importar(idTransaccion, archivo);
            case "CLIENTES" -> clienteService.importar(idTransaccion, archivo);
            case "AEROPUERTOS" -> aeropuertoService.importar(idTransaccion, archivo);
            case "PLANES" -> planService.importar(idTransaccion, archivo);
            case "PEDIDOS" -> pedidoService.importar(idTransaccion, archivo, request);
            default -> throw new G4DException("Tipo de archivo invalido");
        }
        return CompletableFuture.completedFuture(null);
    }

    public GenericResponse iniciarSimulacion(SimulationRequest request) {
        GenericResponse response  = new GenericResponse(true, "Simulacion iniciada!");
        String idTransaccion = response.getToken().substring(4);
        G4DContext context = new G4DContext();
        simulationContexts.put(idTransaccion, context);
        context.task = self.getObject().simular(idTransaccion, request)
                                       .thenCompose((exito) -> {
                                           if(exito) {
                                               String idTransaccionDeExportacion = new GenericResponse().getToken().substring(4);
                                               G4DContext contextoDeExportacion = new G4DContext();
                                               exportationContexts.put(idTransaccionDeExportacion, contextoDeExportacion);
                                               WebSocketService.enviar(String.format("/topic/exportation-status-%s", idTransaccionDeExportacion), EstadoEjecucion.INICIADO);
                                               return exportar(idTransaccionDeExportacion, new ExportationRequest(idTransaccion, "SIMULACION"))
                                                       .whenComplete((r, ex) -> {
                                                           contextoDeExportacion.running = false;
                                                           contextoDeExportacion.task = null;
                                                           exportationContexts.remove(idTransaccionDeExportacion);
                                                       })
                                                       .exceptionally(ex -> {
                                                           asyncExceptionHandler.handleException("Exportation-" + idTransaccionDeExportacion, ex);
                                                           return null;
                                                       });
                                           }
                                           return CompletableFuture.completedFuture(null);
                                       })
                                       .whenComplete((r, ex) -> {
                                           context.running = false;
                                           context.task = null;
                                           simulationContexts.remove(idTransaccion);
                                       })
                                       .exceptionally(ex -> {
                                           asyncExceptionHandler.handleException("Simulation-" + idTransaccion, ex);
                                           return null;
                                       });
        WebSocketService.enviar(String.format("/topic/simulation-status-%s", idTransaccion), EstadoEjecucion.POR_INICIAR);
        return response;
    }

    @Async("simulationExecutor")
    @Transactional
    public CompletableFuture<Boolean> simular(String idTransaccion, SimulationRequest request) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        String solutionDestination = String.format("/topic/simulation-%s", idTransaccion), statusDestination = String.format("/topic/simulation-status-%s", idTransaccion);
        try {
            System.out.println(">> Simulando..");
            boolean huboColapso = false;
            G4DContext context = simulationContexts.get(idTransaccion);
            LocalDateTime inicioDeSimulacion = G4DUtility.Convertor.toDateTime(request.getFechaHoraInicio());
            LocalDateTime finDeSimulacion = G4DUtility.Convertor.toAdmissible(request.getFechaHoraFin(), LocalDateTime.MAX);
            ParametrosDTO parametros = request.getParametros();
            int multiplicadorTemporal = G4DUtility.Convertor.toAdmissible(request.getMultiplicadorTemporal(), 120);
            double saltoDeAlgoritmoEnMinutos = G4DUtility.Convertor.toAdmissible(request.getSaltoDeAlgoritmo(), 2.0);
            long saltoDeAlgoritmoEnMilisegundos = (long)(60000*saltoDeAlgoritmoEnMinutos);
            long saltoDeConsumoEnMinutos= (long)(multiplicadorTemporal * saltoDeAlgoritmoEnMinutos);
            long limiteDeDesfaseTemporalEnMinutos = 1440L*Math.max(G4DUtility.Convertor.toAdmissible(parametros.getMaxDiasEntregaIntracontinental(), 2), G4DUtility.Convertor.toAdmissible(parametros.getMaxDiasEntregaIntercontinental(), 3));
            LocalDateTime umbralDeReplanificacion = inicioDeSimulacion;
            LocalDateTime inicioDePlanificacion = inicioDeSimulacion;
            LocalDateTime finDePlanificacion = (LocalDateTime) G4DUtility.Calculator.getMin(inicioDeSimulacion.plusMinutes(saltoDeConsumoEnMinutos),finDeSimulacion);
            long minutosPlanificados = 0L;
            boolean esPrimeraIteracion = true;
            while(context.running && !Thread.currentThread().isInterrupted()) {
                finDePlanificacion = (LocalDateTime) G4DUtility.Calculator.getMin(finDePlanificacion.plusMinutes(saltoDeConsumoEnMinutos),finDeSimulacion);
                Instant start = Instant.now();
                SolucionDTO solucion = planificar(context, parametros, TipoEscenario.SIMULACION, inicioDePlanificacion, finDePlanificacion, umbralDeReplanificacion, null);
                if(solucion != null) {
                    WebSocketService.enviar(solutionDestination, new SolutionPayload(solucion));
                } else {
                    System.out.println("[*] COLAPSO LOGÍSTICO!");
                    huboColapso = true;
                    WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.COLAPSO));
                    break;
                }
                if(esPrimeraIteracion) {
                    WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
                    esPrimeraIteracion = false;
                }
                Instant end = Instant.now();
                long milisegundosRealesTranscurridos = Duration.between(start, end).toMillis();
                long segundosSimuladosTranscurridos = (long)(multiplicadorTemporal*milisegundosRealesTranscurridos/500.0); // pongo el doble para aprovechar de manera exacta respecto de la duración de la ultima ejecución en lugar de fijarlo en 30s
                umbralDeReplanificacion = finDePlanificacion.plusSeconds(segundosSimuladosTranscurridos);
                minutosPlanificados += saltoDeConsumoEnMinutos;
                long desfaseTemporal = Math.min(minutosPlanificados, limiteDeDesfaseTemporalEnMinutos);
                if(minutosPlanificados > desfaseTemporal || finDePlanificacion.equals(finDeSimulacion)) {
                    inicioDePlanificacion = inicioDePlanificacion.plusMinutes(saltoDeConsumoEnMinutos);
                }
                if(!inicioDePlanificacion.isBefore(finDeSimulacion)) {
                    break;
                }
                if(G4DUtility.Calculator.isProximatelyFewer(milisegundosRealesTranscurridos, saltoDeAlgoritmoEnMilisegundos, 0.125)) {
                    Thread.sleep(saltoDeAlgoritmoEnMilisegundos - milisegundosRealesTranscurridos);
                }
            }
            if(context.running && !Thread.currentThread().isInterrupted()) {
                System.out.println("[>] SIMULACION CONLCUIDA!");
                if(!finDeSimulacion.equals(LocalDateTime.MAX) && !huboColapso) {
                    WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
                }
                future.complete(true);
            } else {
                System.out.println("[X] SIMULACION DETENIDA FORZOSAMENTE!");
                WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.FORZADO));
                future.complete(false);
            }
        } catch (InterruptedException e) {
            System.out.println("[X] SIMULACION DETENIDA FORZOSAMENTE!");
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.FORZADO));
            future.complete(false);
        } catch (Exception e) {
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.ERRONEO));
            future.completeExceptionally(e);
        } finally {
            limpiarPools();
        }
        return future;
    }

    public GenericResponse detenerSimulacion(TransactionRequest request) {
        String idTransaccion = request.getIdTransaccion();
        G4DContext context = simulationContexts.get(idTransaccion);
        if(context == null || !context.running) {
            throw new G4DException(String.format("No hay ninguna simulación con el id '%s' en proceso!", idTransaccion));
        }
        context.running = false;
        context.task.cancel(true);
        WebSocketService.enviar(String.format("/topic/simulation-status-%s", idTransaccion), new StatusPayload(EstadoEjecucion.POR_DETENER));
        return new GenericResponse(true, "Simulación en detenimiento!");
    }

    public GenericResponse replanificarOperacion(ReplanificationRequest request) {
        if(replanificationContext.task != null && replanificationContext.running) {
            throw new G4DException("Ya hay una replanificación en proceso!");
        }
        replanificationContext.running = true;
        replanificationContext.task = self.getObject().replanificar(request)
                                                      .whenComplete((r, ex) -> {
                                                          replanificationContext.running = false;
                                                          replanificationContext.problematic = null;
                                                          replanificationContext.task = null;
                                                      })
                                                      .exceptionally(ex -> {
                                                          asyncExceptionHandler.handleException("Operation", ex);
                                                          return null;
                                                      });
        WebSocketService.enviar("/topic/operation-status", EstadoEjecucion.INICIADO);
        return new GenericResponse(true, "Replanificación Iniciada!");
    }

    @Async("operationExecutor")
    @Transactional
    public CompletableFuture<Void> replanificar(ReplanificationRequest request) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String solutionDestination = "/topic/operation", statusDestination = "/topic/operation-status";
        try {
            System.out.println(">> Actualizando operacion..");
            TipoEscenario escenario = TipoEscenario.OPERACION;
            ParametrosDTO parametros = request.getParametros();
            long desfaseTemporal = 1440L*(Math.max(parametros.getMaxDiasEntregaIntracontinental(), parametros.getMaxDiasEntregaIntercontinental()));
            LocalDateTime fechaHoraActual = G4DUtility.Convertor.toAdmissible(request.getFechaHoraActual(), (LocalDateTime) null);
            LocalDateTime inicioPlanificacion = fechaHoraActual.minusMinutes(desfaseTemporal);
            LocalDateTime umbralDeReplanificacion = fechaHoraActual.plusSeconds(this.tiempoMaximoDeReplanificacionEnSegundos);
            LocalDateTime instanteDeProcesamiento = fechaHoraActual.plusSeconds(this.tiempoMaximoDeCargaEnSegundos);
            SolucionDTO solucion = planificar(replanificationContext, parametros, escenario, inicioPlanificacion, fechaHoraActual, umbralDeReplanificacion, instanteDeProcesamiento);
            if(solucion != null) {
                WebSocketService.enviar(solutionDestination, new SolutionPayload(solucion));
                System.out.println("[>] OPERACION ACTUALIZADA!");
                WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
                future.complete(null);
            } else {
                System.out.println("[*] COLAPSO LOGÍSTICO!");
                throw new G4DException("OPERACIÓN COLAPSADA!");
            }
        } catch (Exception e) {
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.ERRONEO));
            future.completeExceptionally(e);
        } finally {
            limpiarPools();
        }
        return future;
    }

    private SolucionDTO planificar(G4DContext context, ParametrosDTO parametros, TipoEscenario tipoEscenario, LocalDateTime inicioDePlanificacion, LocalDateTime finDePlanificacion, LocalDateTime umbralDeReplanificacion, LocalDateTime instanteDeProcesamiento) {
        boolean esSimulacion = tipoEscenario.equals(TipoEscenario.SIMULACION);
        Problematica problematica;
        if(esSimulacion) {
            if(context.problematic == null) {
                context.problematic = new Problematica();
                parametrosMapper.toAlgorithm(context.problematic, parametros);
                context.problematic.cargarAeropuertos(aeropuertoService, aeropuertoAdapter);
                context.problematic.cargarPlanes(planService, planAdapter);
            }
            context.problematic.inicioDePlanificacion = inicioDePlanificacion;
            context.problematic.finDePlanificacion = finDePlanificacion;
            context.problematic.umbralDeReplanificacion = umbralDeReplanificacion;
            context.problematic.instanteDeProcesamiento = instanteDeProcesamiento;
            context.problematic.tipoEscenario = tipoEscenario.toString().toUpperCase();
            context.problematic.cargarClientes(clienteService, usuarioAdapter);
            context.problematic.cargarPedidos(pedidoService, pedidoAdapter);
            context.problematic.cargarRutas(rutaService, rutaAdapter);
            problematica = context.problematic;
            System.out.printf("[*] SIMULANDO BLOQUE TEMPORAL! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        } else {
            context.problematic = new Problematica();
            parametrosMapper.toAlgorithm(context.problematic, parametros);
            context.problematic.inicioDePlanificacion = inicioDePlanificacion;
            context.problematic.finDePlanificacion = finDePlanificacion;
            context.problematic.umbralDeReplanificacion = umbralDeReplanificacion;
            context.problematic.instanteDeProcesamiento = instanteDeProcesamiento;
            context.problematic.tipoEscenario = tipoEscenario.toString().toUpperCase();
            context.problematic.cargarAeropuertos(aeropuertoService, aeropuertoAdapter);
            context.problematic.cargarPlanes(planService, planAdapter);
            context.problematic.cargarClientes(clienteService, usuarioAdapter);
            context.problematic.cargarPedidos(pedidoService, pedidoAdapter);
            context.problematic.cargarVuelos(vueloService, vueloAdapter);
            context.problematic.cargarRutas(rutaService, rutaAdapter);
            problematica = context.problematic;
            System.out.printf("[*] OPERANDO BLOQUE TEMPORAL! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        }
        GVNS gvns = new GVNS();
        parametrosMapper.toAlgorithm(gvns, parametros);
        gvns.planificar(problematica);
        Solucion solucion = gvns.solucion;
        if(solucion != null) {
            context.solution = solucion;
        } else return null;
        System.out.printf("[*] BLOQUE TEMPORAL PLANIFICADO! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        if(!esSimulacion) {
            almacenarSolucion(solucion, tipoEscenario.toString().toUpperCase());
            limpiarPools();
        }
        return devolverSolucion(solucion, tipoEscenario.toString().toUpperCase());
    }

    private void almacenarSolucion(Solucion solucion, String tipoEscenario) {
        if (solucion == null || solucion.getPedidosAtendidos() == null) {
            return;
        }
        System.out.println(">> Guardando solución en bd..");
        // Vuelos
        for (Vuelo vuelo : solucion.getVuelosEnTransito()) {
            VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
            if (vueloEntity != null) {
                vueloService.save(vueloEntity);
                System.out.println("[*] VUELO: " + vueloEntity.getCodigo());
            }
        }
        // Rutas & RutasPorVuelos
        for (Ruta ruta : solucion.getRutasEnOperacion()) {
            RutaEntity rutaEntity = rutaAdapter.toEntity(ruta);
            if (rutaEntity != null) {
                for (Vuelo vuelo : ruta.getVuelos()) {
                    VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
                    if (vueloEntity != null) {
                        rutaEntity.getVuelos().remove(vueloEntity);
                        rutaEntity.getVuelos().add(vueloEntity);
                    }
                }
                rutaEntity.getVuelos().sort(Comparator.comparing(VueloEntity::getFechaHoraSalidaUTC));
                rutaService.save(rutaEntity);
                System.out.println("[*] RUTA: " + rutaEntity.getCodigo() + " {'" + rutaEntity.getVuelos().size() + "' vuelos!}");
            }
        }
        // Pedidos & Segmentaciones & Lotes
        for (Pedido pedido : solucion.getPedidosAtendidos()) {
            PedidoEntity pedidoEntity = pedidoAdapter.toEntity(pedido, tipoEscenario);
            if(pedidoEntity != null) {
                pedidoService.save(pedidoEntity);
                System.out.println("[*] PEDIDO: " + pedidoEntity.getCodigo());
                for(Segmentacion segmentacion : pedido.getSegmentaciones()) {
                    SegmentacionEntity segmentacionEntity = segmentacionAdapter.toEntity(segmentacion);
                    if(segmentacionEntity != null) {
                        segmentacionEntity.setFechaHoraAplicacionLocal(G4DUtility.Convertor.toLocal(segmentacionEntity.getFechaHoraAplicacionUTC(), pedidoEntity.getDestino().getHusoHorario()));
                        segmentacionEntity.setFechaHoraSustitucionLocal((segmentacionEntity.getFechaHoraSustitucionUTC() != null)? (G4DUtility.Convertor.toLocal(segmentacionEntity.getFechaHoraSustitucionUTC(), pedidoEntity.getDestino().getHusoHorario())) : null);
                        segmentacionEntity.setPedido(pedidoEntity);
                        segmentacionService.save(segmentacionEntity);
                        System.out.println("[*] SEGMENTACION: " + segmentacion.getCodigo());
                        for(Map.Entry<Ruta, Lote> entry : segmentacion.getLotesPorRuta().entrySet()) {
                            RutaEntity rutaEntity = rutaAdapter.toEntity(entry.getKey());
                            if(rutaEntity != null) {
                                LoteEntity loteEntity = loteAdapter.toEntity(entry.getValue());
                                loteEntity.setRuta(rutaEntity);
                                loteEntity.setSegmentacion(segmentacionEntity);
                                loteService.save(loteEntity);
                                System.out.println("[*] LOTE: " + loteEntity.getCodigo());
                            }
                        }
                    }
                }
            }
        }
        // Aeropuertos && Registros && Lotes
        for(Aeropuerto aeropuerto : solucion.getAeropuertosTransitados()) {
            AeropuertoEntity aeropuertoEntity = aeropuertoAdapter.toEntity(aeropuerto);
            if(aeropuertoEntity != null) {
                aeropuertoService.save(aeropuertoEntity);
                System.out.println("[*] AEROPUERTO: " + aeropuertoEntity.getCodigo());
                for(Registro registro : aeropuerto.getRegistros()) {
                    RegistroEntity registroEntity = registroAdapter.toEntity(registro);
                    if(registroEntity.getLote().getId() != null) {
                        registroEntity.setFechaHoraIngresoLocal(G4DUtility.Convertor.toLocal(registroEntity.getFechaHoraIngresoUTC(), aeropuertoEntity.getHusoHorario()));
                        registroEntity.setFechaHoraEgresoLocal((registroEntity.getFechaHoraEgresoUTC() != null)? G4DUtility.Convertor.toLocal(registroEntity.getFechaHoraEgresoUTC(), aeropuertoEntity.getHusoHorario()): null);
                        registroEntity.setAeropuerto(aeropuertoEntity);
                        registroService.save(registroEntity);
                        System.out.println("[*] REGISTRO: " + registroEntity.getCodigo());
                    }
                }
            }
        }
        System.out.println("[~] SOLUCIÓN ALMACENADA!");
    }

    private SolucionDTO devolverSolucion(Solucion solucion, String tipoEscenario) {
        System.out.println(">> Encapsulando solución..");
        SolucionDTO solucionDTO = new SolucionDTO();
        solucionDTO.setRatioPromedioDeUtilizacionTemporal(solucion.getRatioPromedioDeUtilizacionTemporal());
        solucionDTO.setRatioPromedioDeDesviacionEspacial(solucion.getRatioPromedioDeDesviacionEspacial());
        solucionDTO.setRatioPromedioDeDisposicionOperacional(solucion.getRatioPromedioDeDisposicionOperacional());
        List<PedidoDTO> pedidosAtendidos = new ArrayList<>();
        solucion.getPedidosAtendidos().forEach(p -> pedidosAtendidos.add(pedidoMapper.toDTO(p, tipoEscenario)));
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
        System.out.println("[~] SOLUCIÓN ENCAPSULADA!");
        return solucionDTO;
    }

    public GenericResponse iniciarExportacion(ExportationRequest request) {
        GenericResponse response  = new GenericResponse(true, "Exportación iniciada!");
        String idTransaccion = response.getToken().substring(4);
        G4DContext context = new G4DContext();
        exportationContexts.put(idTransaccion, context);
        context.task = self.getObject().exportar(idTransaccion, request)
                                       .whenComplete((r, ex) -> {
                                           context.running = false;
                                           context.task = null;
                                           exportationContexts.remove(idTransaccion);
                                       })
                                       .exceptionally(ex -> {
                                           asyncExceptionHandler.handleException("Exportation-" + idTransaccion, ex);
                                           return null;
                                       });
        WebSocketService.enviar(String.format("/topic/exportation-status-%s", idTransaccion), EstadoEjecucion.INICIADO);
        return response;
    }

    @Async("exportationExecutor")
    public CompletableFuture<Void> exportar(String idTransaccion, ExportationRequest request) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String linkDestination = String.format("/topic/exportation-%s", idTransaccion), statusDestination = String.format("/topic/exportation-status-%s", idTransaccion);
        try {
            System.out.println(">> Exportando solución..");
            G4DContext contexto = exportationContexts.get(idTransaccion);
            String prefijo = G4DUtility.Convertor.toAdmissible(request.getPrefijo());
            String idTransaccionDeContextoDeSolucion = G4DUtility.Convertor.toAdmissible(request.getIdTransaccion(), G4DUtility.Generator.getUniqueString("TOK").substring(4));
            contexto.solution = simulationContexts.getOrDefault(idTransaccionDeContextoDeSolucion, replanificationContext).solution;
            String nombreDelArchivo = String.format("%s__%s.txt", prefijo, idTransaccionDeContextoDeSolucion);
            String rutaDeLdirectorio = "exports" + File.separator;
            GVNS.imprimirSolucion(contexto.solution, Paths.get(rutaDeLdirectorio, nombreDelArchivo).toString());
            System.out.printf("[+] SOLUCION EXPORTADA! ('%s')%n", nombreDelArchivo);
            WebSocketService.enviar(linkDestination, new ExportationPayload(nombreDelArchivo, rutaDeLdirectorio));
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.EXITOSO));
            future.complete(null);
        } catch (Exception e) {
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.ERRONEO));
            future.completeExceptionally(e);
        }
        return future;
    }

    private void limpiarPools() {
        aeropuertoAdapter.clearPools();
        loteAdapter.clearPools();
        pedidoAdapter.clearPools();
        planAdapter.clearPools();
        registroAdapter.clearPools();
        rutaAdapter.clearPools();
        segmentacionAdapter.clearPools();
        usuarioAdapter.clearPools();
        vueloAdapter.clearPools();
    }
}
