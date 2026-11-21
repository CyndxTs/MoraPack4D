/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.model.dto.PaisDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.service.model.TrabajadorService;
import com.pucp.dp1.grupo4d.morapack.service.model.PaisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucp.dp1.grupo4d.morapack.adapter.*;
import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Pedido;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.mapper.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ExportationPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.SolutionPayload;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DExceptionHandlerAsync;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
public class G4DService {
    private static class G4DContext {
        public volatile boolean running = true;
        public String id;
        public Future<?> task;
        public Problematica problematic;
        public Solucion solution;
        // Simulación: reporte parcial con los pedidos ya desalojados de la RAM
        public String archivoParcial;
        public int cantidadDesalojada = 0;

        public G4DContext(String id) {
            this.id = id;
        }
    }

    private final AeropuertoService aeropuertoService;
    private final AeropuertoAdapter aeropuertoAdapter;
    private final AeropuertoMapper aeropuertoMapper;
    private final LoteAdapter loteAdapter;
    private final ParametrosService parametrosService;
    private final ParametrosMapper parametrosMapper;
    private final PedidoService pedidoService;
    private final PedidoAdapter pedidoAdapter;
    private final PedidoMapper pedidoMapper;
    private final PlanService planService;
    private final PlanAdapter planAdapter;
    private final RegistroAdapter registroAdapter;
    private final RutaAdapter rutaAdapter;
    private final RutaMapper rutaMapper;
    private final SegmentacionAdapter segmentacionAdapter;
    private final AdministradorService administradorService;
    private final ClienteService clienteService;
    private final TrabajadorService trabajadorService;
    private final PaisService paisService;
    private final EventoService eventoService;
    private final UsuarioAdapter usuarioAdapter;
    private final VueloAdapter vueloAdapter;
    private final VueloMapper vueloMapper;
    private final ObjectProvider<G4DService> self;
    private final CommunicationService communicationService;
    private final ContextService contextService;
    private final ObjectMapper objectMapper;
    private final G4DExceptionHandlerAsync asyncExceptionHandler;
    private final PurgaService purgaService;
    private volatile boolean purgando = false;
    private final G4DContext operationContext = new G4DContext("OPERATION");
    private final Map<String, G4DContext> simulationContexts = new ConcurrentHashMap<>();
    private final Map<String, G4DContext> importationContexts = new ConcurrentHashMap<>();
    private final Map<String, G4DContext> exportationContexts = new ConcurrentHashMap<>();

    public G4DService(AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter, AeropuertoMapper aeropuertoMapper,
                      LoteAdapter loteAdapter, ParametrosService parametrosService, ParametrosMapper parametrosMapper,
                      PedidoService pedidoService, PedidoAdapter pedidoAdapter, PedidoMapper pedidoMapper, PlanService planService, PlanAdapter planAdapter,
                      RegistroAdapter registroAdapter, RutaAdapter rutaAdapter, RutaMapper rutaMapper,
                      SegmentacionAdapter segmentacionAdapter, AdministradorService administradorService, ClienteService clienteService, TrabajadorService trabajadorService, PaisService paisService, EventoService eventoService, UsuarioAdapter usuarioAdapter,
                      VueloAdapter vueloAdapter, VueloMapper vueloMapper, ObjectProvider<G4DService> self,
                      CommunicationService communicationService, ContextService contextService, ObjectMapper objectMapper, G4DExceptionHandlerAsync asyncExceptionHandler, PurgaService purgaService) {
        this.aeropuertoService = aeropuertoService;
        this.aeropuertoAdapter = aeropuertoAdapter;
        this.aeropuertoMapper = aeropuertoMapper;
        this.loteAdapter = loteAdapter;
        this.parametrosService = parametrosService;
        this.parametrosMapper = parametrosMapper;
        this.pedidoService = pedidoService;
        this.pedidoAdapter = pedidoAdapter;
        this.pedidoMapper = pedidoMapper;
        this.planService = planService;
        this.planAdapter = planAdapter;
        this.registroAdapter = registroAdapter;
        this.rutaAdapter = rutaAdapter;
        this.rutaMapper = rutaMapper;
        this.segmentacionAdapter = segmentacionAdapter;
        this.administradorService = administradorService;
        this.clienteService = clienteService;
        this.trabajadorService = trabajadorService;
        this.paisService = paisService;
        this.eventoService = eventoService;
        this.usuarioAdapter = usuarioAdapter;
        this.vueloAdapter = vueloAdapter;
        this.vueloMapper = vueloMapper;
        this.self = self;
        this.communicationService = communicationService;
        this.contextService = contextService;
        this.objectMapper = objectMapper;
        this.asyncExceptionHandler = asyncExceptionHandler;
        this.purgaService = purgaService;
    }

    public GenericResponse iniciarImportacion(ImportRequest request) throws JsonProcessingException {
        GenericResponse response = new GenericResponse(true, "Importación iniciada!");
        String idTransaccion = response.getToken().substring(4);
        G4DContext context = new G4DContext(idTransaccion);
        importationContexts.put(idTransaccion, context);
        context.task = self.getObject().importar(idTransaccion, request)
                                       .whenComplete((r, ex) -> {
                                           context.running = false;
                                           context.task = null;
                                           importationContexts.remove(idTransaccion);
                                       })
                                       .exceptionally(ex -> {
                                           asyncExceptionHandler.handleException("Importation-" + idTransaccion, ex);
                                           return null;
                                       });
        communicationService.enviar(String.format("/topic/importation-status-%s", idTransaccion), new StatusPayload(EstadoEjecucion.POR_INICIAR));
        return response;
    }

    @Async("importationExecutor")
    public CompletableFuture<Void> importar(String idTransaccion, ImportRequest request) throws JsonProcessingException {
        JsonNode dto = request.getDto();
        switch (request.getTipoDto().toUpperCase()) {
            case "AEROPUERTO" -> aeropuertoService.importar(idTransaccion, objectMapper.treeToValue(dto, AeropuertoDTO.class));
            case "PLAN" -> planService.importar(idTransaccion, objectMapper.treeToValue(dto, PlanDTO.class));
            case "PEDIDO" -> pedidoService.importar(idTransaccion, objectMapper.treeToValue(dto, PedidoDTO.class));
            case "PARAMETROS" -> parametrosService.importar(idTransaccion, objectMapper.treeToValue(dto, ParametrosDTO.class));
            case "PAIS" -> paisService.importar(idTransaccion, objectMapper.treeToValue(dto, PaisDTO.class));
            case "EVENTO" -> eventoService.importar(idTransaccion, objectMapper.treeToValue(dto, EventoDTO.class));
            case "USUARIO" -> {
                UsuarioDTO usuarioDTO = objectMapper.treeToValue(dto, UsuarioDTO.class);
                TipoUsuario tipoUsuario = G4DUtility.Convertor.toAdmissible(usuarioDTO.getTipoUsuario(), TipoUsuario.class);
                if (tipoUsuario == null) throw new G4DException(String.format("Tipo de usuario ('%s') inválido!", usuarioDTO.getTipoUsuario()));
                switch (tipoUsuario) {
                    case CLIENTE -> clienteService.importar(idTransaccion, usuarioDTO);
                    case TRABAJADOR -> trabajadorService.importar(idTransaccion, usuarioDTO);
                    case ADMINISTRADOR -> administradorService.importar(idTransaccion, usuarioDTO);
                }
            }
            default -> throw new G4DException("Tipo de dato inválido!");
        }
        return CompletableFuture.completedFuture(null);
    }

    public GenericResponse iniciarImportacion(MultipartFile file, ImportFileRequest request) throws IOException {
        GenericResponse response = new GenericResponse(true, "Importación iniciada!");
        String idTransaccion = response.getToken().substring(4);
        Path archivo = Files.createTempFile("import-" + idTransaccion + "-", "-" + file.getOriginalFilename());
        file.transferTo(archivo.toFile());
        G4DContext context = new G4DContext(idTransaccion);
        importationContexts.put(idTransaccion, context);
        context.task = self.getObject().importar(idTransaccion, archivo, request)
                                       .whenComplete((r, ex) -> {
                                           context.running = false;
                                           context.task = null;
                                           importationContexts.remove(idTransaccion);
                                       })
                                       .exceptionally(ex -> {
                                           asyncExceptionHandler.handleException("Importation-" + idTransaccion, ex);
                                           return null;
                                       });
        communicationService.enviar(String.format("/topic/importation-status-%s", idTransaccion), new StatusPayload(EstadoEjecucion.POR_INICIAR));
        return response;
    }

    @Async("importationExecutor")
    public CompletableFuture<Void> importar(String idTransaccion, Path archivo, ImportFileRequest request) {
        switch (request.getTipoArchivo().toUpperCase()) {
            case "ADMINISTRADORES" -> administradorService.importar(idTransaccion, archivo);
            case "CLIENTES" -> clienteService.importar(idTransaccion, archivo);
            case "TRABAJADORES" -> trabajadorService.importar(idTransaccion, archivo);
            case "AEROPUERTOS" -> aeropuertoService.importar(idTransaccion, archivo);
            case "PLANES" -> planService.importar(idTransaccion, archivo);
            case "PEDIDOS" -> pedidoService.importar(idTransaccion, archivo, request);
            case "EVENTOS" -> eventoService.importar(idTransaccion, archivo, request);
            default -> throw new G4DException("Tipo de archivo inválido!");
        }
        return CompletableFuture.completedFuture(null);
    }

    public GenericResponse iniciarSimulacion(SimulationRequest request) {
        GenericResponse response  = new GenericResponse(true, "Simulacion iniciada!");
        String idTransaccion = response.getToken().substring(4);
        G4DContext context = new G4DContext(idTransaccion);
        simulationContexts.put(idTransaccion, context);
        context.task = self.getObject().simular(idTransaccion, request)
                                       .thenCompose((exito) -> {
                                           if(exito) {
                                               G4DContext contextoDeExportacion = new G4DContext(idTransaccion);
                                               exportationContexts.put(idTransaccion, contextoDeExportacion);
                                               communicationService.enviar(String.format("/topic/exportation-status-%s", idTransaccion), EstadoEjecucion.INICIADO);
                                               return exportar(idTransaccion, new ExportationRequest(idTransaccion, "SIMULACION"))
                                                       .whenComplete((r, ex) -> {
                                                           contextoDeExportacion.running = false;
                                                           contextoDeExportacion.task = null;
                                                           exportationContexts.remove(idTransaccion);
                                                       })
                                                       .exceptionally(ex -> {
                                                           asyncExceptionHandler.handleException("Exportation-" + idTransaccion, ex);
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
        communicationService.enviar(String.format("/topic/simulation-status-%s", idTransaccion), EstadoEjecucion.POR_INICIAR);
        return response;
    }

    @Async("simulationExecutor")
    public CompletableFuture<Boolean> simular(String idTransaccion, SimulationRequest request) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        G4DContext context = simulationContexts.get(idTransaccion);
        String solutionDestination = String.format("/topic/simulation-%s", context.id), statusDestination = String.format("/topic/simulation-status-%s", context.id);
        try {
            System.out.println(">> Simulando..");
            boolean huboColapso = false;
            context.archivoParcial = Paths.get("exports", String.format(".parcial__%s.txt", context.id)).toString();
            Files.deleteIfExists(Paths.get(context.archivoParcial));
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
                System.out.println("[>] SOLUCIÓN ENVIADA!");
                communicationService.enviar(solutionDestination, new SolutionPayload(solucion));
                if(!solucion.getPedidosNoAtendidos().isEmpty()) {
                    // Colapso: la ventana se entrega con sus pedidos no atendidos y la simulación se detiene
                    System.out.printf("[*] COLAPSO LOGÍSTICO! ('%d' pedidos no atendidos)%n", solucion.getPedidosNoAtendidos().size());
                    huboColapso = true;
                    communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.COLAPSO));
                    break;
                }
                if(esPrimeraIteracion) {
                    communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
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
                    // La ventana avanzó: lo que terminó antes de su nuevo inicio se escribe al reporte y sale de la RAM
                    List<Pedido> desalojados = context.problematic.desalojar(inicioDePlanificacion);
                    contextService.exportarPedidosDesalojados(desalojados, context.archivoParcial, context.cantidadDesalojada);
                    context.cantidadDesalojada += desalojados.size();
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
                    communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
                }
                future.complete(true);
            } else {
                System.out.println("[X] SIMULACION DETENIDA FORZOSAMENTE!");
                communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.FORZADO));
                future.complete(false);
            }
        } catch (InterruptedException e) {
            System.out.println("[X] SIMULACION DETENIDA FORZOSAMENTE!");
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.FORZADO));
            future.complete(false);
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.ERRONEO));
            future.completeExceptionally(e);
        } finally {
            limpiarPools(idTransaccion);
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
        communicationService.enviar(String.format("/topic/simulation-status-%s", idTransaccion), new StatusPayload(EstadoEjecucion.POR_DETENER));
        return new GenericResponse(true, "Simulación en detenimiento!");
    }

    public GenericResponse replanificarOperacion(ReplanificationRequest request) {
        synchronized (operationContext) {
            if(operationContext.task != null && operationContext.running) {
                throw new G4DException("Ya hay una replanificación en proceso!");
            }
            if(purgando) {
                throw new G4DException("Hay una purga de datos en proceso, intente en unos segundos.");
            }
            operationContext.running = true;
            operationContext.task = self.getObject().replanificar(request)
                                                      .whenComplete((r, ex) -> {
                                                          operationContext.running = false;
                                                          operationContext.problematic = null;
                                                          operationContext.task = null;
                                                      })
                                                      .exceptionally(ex -> {
                                                          asyncExceptionHandler.handleException("Operation", ex);
                                                          return null;
                                                      });
        }
        communicationService.enviar("/topic/operation-status", EstadoEjecucion.INICIADO);
        return new GenericResponse(true, "Replanificación Iniciada!");
    }

    // Purga periódica de datos de operación que ya no se usan (por defecto cada hora; 'morapack.purga.cron=-' la desactiva)
    @Scheduled(cron = "${morapack.purga.cron:0 17 * * * *}", zone = "UTC")
    public void purgarOperacionProgramada() {
        try {
            this.purgarOperacion();
        } catch (Exception e) {
            System.out.printf("[*] PURGA OMITIDA: %s%n", e.getMessage());
        }
    }

    // Corte: lo que terminó antes de (ahora - plazo máximo de entrega - 1 día) ya no entra en ninguna ventana de operación
    public GenericResponse purgarOperacion() {
        synchronized (operationContext) {
            if(operationContext.task != null) {
                throw new G4DException("Hay una replanificación en proceso; la purga se omitió.");
            }
            purgando = true;
        }
        try {
            ParametrosEntity parametros = parametrosService.findById(1);
            int maxDias = (parametros != null) ? Math.max(parametros.getMaxDiasEntregaIntracontinental(), parametros.getMaxDiasEntregaIntercontinental()) : 3;
            LocalDateTime instanteDeCorte = LocalDateTime.now().minusDays(maxDias + 1L);
            System.out.printf(">> Purgando datos de operación (corte: '%s')..%n", G4DUtility.Convertor.toDisplayString(instanteDeCorte));
            Map<String, Integer> eliminados = purgaService.purgar(instanteDeCorte);
            int total = eliminados.values().stream().mapToInt(Integer::intValue).sum();
            eliminados.forEach((k, v) -> { if (v > 0) System.out.printf("[-] %s: '%d'%n", k, v); });
            System.out.printf("[~] PURGA REALIZADA! ('%d' filas eliminadas)%n", total);
            return new GenericResponse(true, String.format("Purga realizada! ('%d' filas eliminadas)", total));
        } finally {
            purgando = false;
        }
    }

    @Async("operationExecutor")
    public CompletableFuture<Void> replanificar(ReplanificationRequest request) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        String solutionDestination = "/topic/operation", statusDestination = "/topic/operation-status";
        try {
            System.out.println(">> Actualizando operacion..");
            TipoEscenario escenario = TipoEscenario.OPERACION;
            ParametrosDTO parametros = request.getParametros();
            if(parametros == null) {
                // La operación usa los parámetros por defecto guardados por el administrador
                ParametrosEntity parametrosEntity = parametrosService.findById(1);
                if(parametrosEntity == null) throw new G4DException("No hay parámetros registrados para la operación.");
                parametros = parametrosMapper.toDTO(parametrosEntity);
            }
            long desfaseTemporal = 1440L*(Math.max(parametros.getMaxDiasEntregaIntracontinental(), parametros.getMaxDiasEntregaIntercontinental()));
            LocalDateTime fechaHoraActual = G4DUtility.Convertor.toAdmissible(request.getFechaHoraActual(), (LocalDateTime) null);
            LocalDateTime inicioPlanificacion = fechaHoraActual.minusMinutes(desfaseTemporal);
            LocalDateTime umbralDeReplanificacion = fechaHoraActual.plusSeconds(180L); // Predicción por tiempo maximo de replanificacion
            LocalDateTime instanteDeProcesamiento = fechaHoraActual.plusSeconds(90L); // Predicción por tiempo maximo de carga
            SolucionDTO solucion = planificar(operationContext, parametros, escenario, inicioPlanificacion, fechaHoraActual, umbralDeReplanificacion, instanteDeProcesamiento);
            communicationService.enviar(solutionDestination, new SolutionPayload(solucion));
            System.out.println("[>] OPERACION ACTUALIZADA!");
            if(!solucion.getPedidosNoAtendidos().isEmpty()) {
                // En operación no se detiene nada: solo se avisa (el front muestra los pedidos no atendidos)
                System.out.printf("[*] COLAPSO LOGÍSTICO EN OPERACIÓN! ('%d' pedidos no atendidos)%n", solucion.getPedidosNoAtendidos().size());
                communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.COLAPSO));
            } else communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            future.complete(null);
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.ERRONEO));
            future.completeExceptionally(e);
        } finally {
            limpiarPools(operationContext.id);
        }
        return future;
    }

    public GenericResponse iniciarExportacion(ExportationRequest request) {
        GenericResponse response  = new GenericResponse(true, "Exportación iniciada!");
        String idTransaccion = response.getToken().substring(4);
        G4DContext context = new G4DContext(idTransaccion);
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
        communicationService.enviar(String.format("/topic/exportation-status-%s", idTransaccion), EstadoEjecucion.INICIADO);
        return response;
    }

    @Async("exportationExecutor")
    public CompletableFuture<Void> exportar(String idTransaccion, ExportationRequest request) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        G4DContext context = exportationContexts.get(idTransaccion);
        String linkDestination = String.format("/topic/exportation-%s", context.id), statusDestination = String.format("/topic/exportation-status-%s", context.id);
        try {
            System.out.println(">> Exportando solución..");
            String prefijo = G4DUtility.Convertor.toAdmissible(request.getPrefijo());
            String idTransaccionDeContextoDeSolucion = G4DUtility.Convertor.toAdmissible(request.getIdTransaccion(), G4DUtility.Generator.getUniqueString("TOK").substring(4));
            G4DContext contextoDeSolucion = simulationContexts.getOrDefault(idTransaccionDeContextoDeSolucion, operationContext);
            context.solution = contextoDeSolucion.solution;
            String nombreDelArchivo = String.format("%s__%s.txt", prefijo, idTransaccionDeContextoDeSolucion);
            String rutaDeLdirectorio = "exports" + File.separator;
            contextService.exportSolutionAsTxt(context.solution, Paths.get(rutaDeLdirectorio, nombreDelArchivo).toString(), contextoDeSolucion.archivoParcial, contextoDeSolucion.cantidadDesalojada);
            System.out.printf("[+] SOLUCION EXPORTADA! ('%s')%n", nombreDelArchivo);
            communicationService.enviar(linkDestination, new ExportationPayload(nombreDelArchivo, rutaDeLdirectorio));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.EXITOSO));
            future.complete(null);
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO,  EstadoFinalizacion.ERRONEO));
            future.completeExceptionally(e);
        }
        return future;
    }

    private SolucionDTO planificar(G4DContext context, ParametrosDTO parametros, TipoEscenario tipoEscenario, LocalDateTime inicioDePlanificacion, LocalDateTime finDePlanificacion, LocalDateTime umbralDeReplanificacion, LocalDateTime instanteDeProcesamiento) {
        boolean esSimulacion = tipoEscenario.equals(TipoEscenario.SIMULACION);
        context.problematic = contextService.getUpdatedProblematic(context.id, context.problematic, esSimulacion, parametros, inicioDePlanificacion, finDePlanificacion, umbralDeReplanificacion, instanteDeProcesamiento, tipoEscenario);
        if(esSimulacion) {
            System.out.printf("[*] SIMULANDO BLOQUE TEMPORAL! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        } else System.out.printf("[*] OPERANDO BLOQUE TEMPORAL! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        GVNS gvns = new GVNS();
        parametrosMapper.toAlgorithm(gvns, parametros);
        gvns.planificar(context.problematic);
        Solucion solucion = gvns.solucion;
        if(solucion != null) {
            context.solution = solucion;
        } else return null;
        System.out.printf("[*] BLOQUE TEMPORAL PLANIFICADO! ['%s' - '%s']%n", G4DUtility.Convertor.toDisplayString(inicioDePlanificacion), G4DUtility.Convertor.toDisplayString(finDePlanificacion));
        if(!esSimulacion) {
            contextService.importSolution(context.id, solucion, context.problematic);
        }
        return devolverSolucion(solucion, tipoEscenario.toString().toUpperCase());
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
        List<PedidoNoAtendidoDTO> pedidosNoAtendidos = new ArrayList<>();
        solucion.getPedidosNoAtendidos().forEach((p, motivo) -> {
            PedidoNoAtendidoDTO dto = new PedidoNoAtendidoDTO();
            dto.setCodigo(p.getCodigo());
            dto.setCodCliente(p.getCliente() != null ? p.getCliente().getCodigo() : null);
            dto.setCodDestino(p.getDestino().getCodigo());
            dto.setCantidadSolicitada(p.getCantidadSolicitada());
            dto.setFechaHoraGeneracion(G4DUtility.Convertor.toDisplayString(p.getFechaHoraGeneracion()));
            dto.setMotivo(motivo);
            pedidosNoAtendidos.add(dto);
        });
        solucionDTO.setPedidosNoAtendidos(pedidosNoAtendidos);
        System.out.println("[~] SOLUCIÓN ENCAPSULADA!");
        return solucionDTO;
    }

    private void limpiarPools(String idTransaccion) {
        aeropuertoAdapter.clearPools(idTransaccion);
        loteAdapter.clearPools(idTransaccion);
        pedidoAdapter.clearPools(idTransaccion);
        planAdapter.clearPools(idTransaccion);
        registroAdapter.clearPools(idTransaccion);
        rutaAdapter.clearPools(idTransaccion);
        segmentacionAdapter.clearPools(idTransaccion);
        usuarioAdapter.clearPools(idTransaccion);
        vueloAdapter.clearPools(idTransaccion);
    }
}
