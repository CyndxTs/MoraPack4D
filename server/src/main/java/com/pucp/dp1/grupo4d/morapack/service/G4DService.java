/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       G4DService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.model.dto.PaisDTO;
import com.pucp.dp1.grupo4d.morapack.security.SesionActual;
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
import com.pucp.dp1.grupo4d.morapack.util.FiltroSolucionCliente;
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
        // Operación: última solución enviada (para que un cliente que recién abre la vista no la vea vacía)
        public SolucionDTO solutionDTO;

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
    // Replanificación automática de la operación (programada por el server; null = ninguna)
    private volatile LocalDateTime replanificacionProgramadaPara = null;
    private final Object lockProgramacion = new Object();
    private final G4DContext operationContext = new G4DContext("OPERATION");
    private final Map<String, G4DContext> simulationContexts = new ConcurrentHashMap<>();
    private final Map<String, G4DContext> importationContexts = new ConcurrentHashMap<>();
    private final Map<String, G4DContext> exportationContexts = new ConcurrentHashMap<>();

    public G4DService(AeropuertoService aeropuertoService, AeropuertoAdapter aeropuertoAdapter, AeropuertoMapper aeropuertoMapper,
                      LoteAdapter loteAdapter, ParametrosService parametrosService, ParametrosMapper parametrosMapper,
                      PedidoService pedidoService, PedidoAdapter pedidoAdapter, PedidoMapper pedidoMapper, PlanService planService, PlanAdapter planAdapter,
                      RegistroAdapter registroAdapter, RutaAdapter rutaAdapter, RutaMapper rutaMapper,
                      SegmentacionAdapter segmentacionAdapter, PaisService paisService, EventoService eventoService, UsuarioAdapter usuarioAdapter,
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

    // Datos maestros que solo el administrador puede modificar
    private static void validarPermisoDeImportacion(String tipo) {
        String t = (tipo != null) ? tipo.toUpperCase() : "";
        boolean soloAdmin = switch (t) {
            case "PARAMETROS", "PAIS", "AEROPUERTO", "AEROPUERTOS" -> true;
            default -> false;
        };
        if (soloAdmin && !SesionActual.esAdministrador()) {
            throw new G4DException("Solo un administrador puede modificar estos datos.");
        }
    }

    public GenericResponse iniciarImportacion(ImportRequest request) throws JsonProcessingException {
        validarPermisoDeImportacion(request.getTipoDto());
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
        try {
            JsonNode dto = request.getDto();
            switch (request.getTipoDto().toUpperCase()) {
                case "AEROPUERTO" -> aeropuertoService.importar(idTransaccion, objectMapper.treeToValue(dto, AeropuertoDTO.class));
                case "PLAN" -> planService.importar(idTransaccion, objectMapper.treeToValue(dto, PlanDTO.class));
                case "PEDIDO" -> {
                    var pedido = pedidoService.importar(idTransaccion, objectMapper.treeToValue(dto, PedidoDTO.class));
                    if (pedido != null && pedido.getTipoEscenario() == TipoEscenario.OPERACION) this.programarReplanificacion(false);
                }
                case "PARAMETROS" -> parametrosService.importar(idTransaccion, objectMapper.treeToValue(dto, ParametrosDTO.class));
                case "PAIS" -> paisService.importar(idTransaccion, objectMapper.treeToValue(dto, PaisDTO.class));
                case "EVENTO" -> {
                    EventoDTO eventoDTO = objectMapper.treeToValue(dto, EventoDTO.class);
                    eventoService.importar(idTransaccion, eventoDTO);
                    if (G4DUtility.Convertor.toAdmissible(eventoDTO.getTipoEscenario(), TipoEscenario.class) == TipoEscenario.OPERACION) this.programarReplanificacion(true);
                }
                default -> throw new G4DException("Tipo de dato inválido!");
            }
        } catch (RuntimeException | JsonProcessingException e) {
            this.reportarErrorDeImportacion(idTransaccion, e);
            throw e;
        }
        return CompletableFuture.completedFuture(null);
    }

    public GenericResponse iniciarImportacion(MultipartFile file, ImportFileRequest request) throws IOException {
        validarPermisoDeImportacion(request.getTipoArchivo());
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
        try {
            // Los usuarios ya no se importan por archivo: se crean uno a uno (registro o administrador)
            boolean esOperacion = G4DUtility.Convertor.toAdmissible(request.getTipoEscenario(), TipoEscenario.class) == TipoEscenario.OPERACION;
            switch (request.getTipoArchivo().toUpperCase()) {
                case "AEROPUERTOS" -> aeropuertoService.importar(idTransaccion, archivo);
                case "PLANES" -> planService.importar(idTransaccion, archivo);
                case "PEDIDOS" -> {
                    int importados = pedidoService.importar(idTransaccion, archivo, request);
                    if (esOperacion && importados > 0) this.programarReplanificacion(false);
                }
                case "EVENTOS" -> {
                    eventoService.importar(idTransaccion, archivo, request);
                    if (esOperacion) this.programarReplanificacion(true);
                }
                default -> throw new G4DException("Tipo de archivo inválido!");
            }
        } catch (RuntimeException e) {
            this.reportarErrorDeImportacion(idTransaccion, e);
            throw e;
        } finally {
            try { Files.deleteIfExists(archivo); } catch (IOException ignored) { }
        }
        return CompletableFuture.completedFuture(null);
    }

    // El error de una importación llega al front con su motivo (línea, país inexistente, formato...)
    private void reportarErrorDeImportacion(String idTransaccion, Exception e) {
        String mensaje = (e instanceof G4DException && e.getMessage() != null) ? e.getMessage() : "El archivo no se pudo importar. Revisa el formato o los datos.";
        communicationService.enviar(String.format("/topic/importation-status-%s", idTransaccion), new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO, mensaje));
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
                if(esPrimeraIteracion) {
                    communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
                    esPrimeraIteracion = false;
                }
                Instant end = Instant.now();
                long milisegundosRealesTranscurridos = Duration.between(start, end).toMillis();
                long segundosSimuladosTranscurridos = (long)(multiplicadorTemporal*milisegundosRealesTranscurridos/500.0); // pongo el doble para aprovechar de manera exacta respecto de la duración de la ultima ejecución en lugar de fijarlo en 30s
                umbralDeReplanificacion = finDePlanificacion.plusSeconds(segundosSimuladosTranscurridos);
                // Colapso: solo cuando un pedido sin atender ya no llega a tiempo. Mientras su plazo siga abierto,
                // se reintenta en el siguiente bloque (un fallo puntual por capacidad no detiene la simulación).
                if(!context.solution.getPedidosNoAtendidos().isEmpty()) {
                    final LocalDateTime umbral = umbralDeReplanificacion;
                    List<Pedido> vencidos = context.solution.getPedidosNoAtendidos().keySet().stream()
                            .filter(p -> !context.problematic.obtenerPlazoMaximo(p).isAfter(umbral)).toList();
                    context.solution.getPedidosNoAtendidos().forEach((p, motivo) -> System.out.printf("[!] SIN ATENDER: '%s' -> '%s' (%d u., generado %s): %s%n",
                            p.getCodigo(), p.getDestino().getCodigo(), p.getCantidadSolicitada(), G4DUtility.Convertor.toDisplayString(p.getFechaHoraGeneracion()), motivo));
                    if(!vencidos.isEmpty()) {
                        System.out.printf("[*] COLAPSO LOGÍSTICO! ('%d' pedidos ya no llegan a tiempo)%n", vencidos.size());
                        huboColapso = true;
                        communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.COLAPSO,
                                String.format("%d pedido(s) ya no pueden entregarse dentro de su plazo.", vencidos.size())));
                        break;
                    }
                    System.out.printf("[~] '%d' pedidos sin atender en este bloque: se reintentan en el siguiente.%n", context.solution.getPedidosNoAtendidos().size());
                }
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

    // Última solución de la operación (null si el server aún no replanificó desde que arrancó)
    public SolutionPayload obtenerOperacionActual() {
        SolucionDTO solucion = operationContext.solutionDTO;
        return (solucion != null) ? new SolutionPayload(solucion) : null;
    }

    // La operación vista por un cliente: solo sus pedidos, vuelos y paquetes
    public SolutionPayload obtenerOperacionDeCliente(String codCliente) {
        SolucionDTO solucion = operationContext.solutionDTO;
        return new SolutionPayload(FiltroSolucionCliente.filtrar(solucion, codCliente));
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
            synchronized (lockProgramacion) { replanificacionProgramadaPara = null; }
            operationContext.task = self.getObject().replanificar(request)
                                                      .whenComplete((r, ex) -> {
                                                          operationContext.running = false;
                                                          operationContext.problematic = null;
                                                          operationContext.task = null;
                                                          this.publicarProgramacion();
                                                      })
                                                      .exceptionally(ex -> {
                                                          asyncExceptionHandler.handleException("Operation", ex);
                                                          return null;
                                                      });
        }
        communicationService.enviar("/topic/operation-status", EstadoEjecucion.INICIADO);
        this.publicarProgramacion();
        return new GenericResponse(true, "Replanificación Iniciada!");
    }

    // Purga periódica de datos de operación que ya no se usan (cada hora por defecto). Se activa/desactiva en PARAMETROS.
    @Scheduled(cron = "${morapack.purga.cron:0 17 * * * *}", zone = "UTC")
    public void purgarOperacionProgramada() {
        try {
            ParametrosEntity parametros = parametrosService.findById(1);
            if (parametros == null || !Boolean.TRUE.equals(parametros.getPurgaAutomatica())) return;
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

    private long segundosDeUmbral() {
        ParametrosEntity parametros = parametrosService.findById(1);
        int segundos = (parametros != null && parametros.getSegundosUmbralReplanificacion() != null) ? parametros.getSegundosUmbralReplanificacion() : 180;
        return Math.max(ParametrosService.MIN_SEGUNDOS_UMBRAL, Math.min(ParametrosService.MAX_SEGUNDOS_UMBRAL, segundos));
    }

    // ---------- Replanificación automática (server) ----------
    // Pedidos de OPERACION nuevos: se programa para dentro de 'minutos_espera_replanificacion' (se respeta la primera programada).
    // Eventos de OPERACION: inmediata si 'replanificar_al_registrar_evento' está activo.
    public void programarReplanificacion(boolean porEvento) {
        ParametrosEntity parametros = parametrosService.findById(1);
        if (parametros == null) return;
        LocalDateTime ahora = LocalDateTime.now();
        synchronized (lockProgramacion) {
            if (porEvento) {
                if (!Boolean.TRUE.equals(parametros.getReplanificarAlRegistrarEvento())) return;
                replanificacionProgramadaPara = ahora;
            } else {
                if (!Boolean.TRUE.equals(parametros.getReplanificacionAutomatica())) return;
                LocalDateTime propuesta = ahora.plusMinutes(Math.max(0, parametros.getMinutosEsperaReplanificacion()));
                if (replanificacionProgramadaPara == null) replanificacionProgramadaPara = propuesta;
            }
        }
        System.out.printf("[*] REPLANIFICACIÓN PROGRAMADA! ('%s')%n", G4DUtility.Convertor.toDisplayString(replanificacionProgramadaPara));
        this.publicarProgramacion();
    }

    public void cancelarProgramacion() {
        synchronized (lockProgramacion) {
            if (replanificacionProgramadaPara == null) return;
            replanificacionProgramadaPara = null;
        }
        this.publicarProgramacion();
    }

    // { programadaPara: "yyyy-MM-dd HH:mm:ss" (UTC) | null }
    public Map<String, Object> obtenerProgramacion() {
        LocalDateTime programada = replanificacionProgramadaPara;
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("programadaPara", (programada != null) ? G4DUtility.Convertor.toSqlString(programada) : null);
        payload.put("enProceso", operationContext.task != null && operationContext.running);
        return payload;
    }

    private void publicarProgramacion() {
        communicationService.enviar("/topic/operation-schedule", this.obtenerProgramacion());
    }

    // Vigilante: lanza la replanificación programada cuando vence. Si hay una en curso, reintenta en el siguiente ciclo.
    @Scheduled(fixedDelayString = "${morapack.replanificacion.vigilancia-ms:5000}", initialDelay = 15000)
    public void vigilarReplanificacion() {
        LocalDateTime programada = replanificacionProgramadaPara;
        if (programada == null || LocalDateTime.now().isBefore(programada)) return;
        if ((operationContext.task != null && operationContext.running) || purgando) return;
        try {
            ReplanificationRequest request = new ReplanificationRequest();
            request.setFechaHoraActual(G4DUtility.Convertor.toSqlString(LocalDateTime.now()));
            request.setAlmacenarParametrizacion(false);
            this.replanificarOperacion(request);
            System.out.println("[>] REPLANIFICACIÓN AUTOMÁTICA LANZADA!");
        } catch (G4DException e) {
            System.out.printf("[*] REPLANIFICACIÓN AUTOMÁTICA POSTERGADA: %s%n", e.getMessage());
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
            if(fechaHoraActual == null) fechaHoraActual = LocalDateTime.now();
            LocalDateTime inicioPlanificacion = fechaHoraActual.minusMinutes(desfaseTemporal);
            long segundosDeUmbral = this.segundosDeUmbral();
            LocalDateTime umbralDeReplanificacion = fechaHoraActual.plusSeconds(segundosDeUmbral); // Predicción por tiempo maximo de replanificacion
            LocalDateTime instanteDeProcesamiento = fechaHoraActual.plusSeconds(segundosDeUmbral / 2); // Predicción por tiempo maximo de carga
            SolucionDTO solucion = planificar(operationContext, parametros, escenario, inicioPlanificacion, fechaHoraActual, umbralDeReplanificacion, instanteDeProcesamiento);
            operationContext.solutionDTO = solucion;
            communicationService.enviar(solutionDestination, new SolutionPayload(solucion));
            // Cada cliente recibe solo su parte de la operación
            for (String codCliente : FiltroSolucionCliente.clientesDe(solucion)) {
                communicationService.enviar(String.format("/topic/cliente/%s/operacion", codCliente), new SolutionPayload(FiltroSolucionCliente.filtrar(solucion, codCliente)));
            }
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
        if(parametros.getCodOrigenes() != null && parametros.getCodOrigenes().isEmpty()) {
            throw new G4DException("No hay sedes registradas: el administrador debe marcar al menos una sede.");
        }
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
