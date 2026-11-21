/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.PedidoMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.SegmentacionEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.LoteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.RegistroEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.VueloEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoLote;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoPedido;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.PedidoRepository;
import com.pucp.dp1.grupo4d.morapack.service.CommunicationService;
import com.pucp.dp1.grupo4d.morapack.service.ImportationService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final AeropuertoService aeropuertoService;
    private final UsuarioService usuarioService;
    private final CommunicationService communicationService;
    private final ImportationService importationService;
    private final SegmentacionService segmentacionService;
    private final LoteService loteService;
    private final RegistroService registroService;
    private final VueloService vueloService;

    public PedidoService(PedidoRepository pedidoRepository, PedidoMapper pedidoMapper, AeropuertoService aeropuertoService, UsuarioService usuarioService, CommunicationService communicationService, ImportationService importationService,
                         SegmentacionService segmentacionService, LoteService loteService, RegistroService registroService, VueloService vueloService) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.aeropuertoService = aeropuertoService;
        this.usuarioService = usuarioService;
        this.communicationService = communicationService;
        this.importationService = importationService;
        this.segmentacionService = segmentacionService;
        this.loteService = loteService;
        this.registroService = registroService;
        this.vueloService = vueloService;
    }

    public PedidoEntity save(PedidoEntity pedido) {
        return pedidoRepository.save(pedido);
    }

    public List<PedidoEntity> findAll() {
        return pedidoRepository.findAll();
    }

    public List<PedidoEntity> findAll(Pageable pageable) {
        return pedidoRepository.findAll(pageable).getContent();
    }

    public List<PedidoEntity> findAllByAttributes(String tipoEscenario, String codCliente, String codigo, Boolean fueAtendido, String fechaHoraGeneracion, String fechaHoraExpiracion, String codDestino, Pageable pageable) {
        return pedidoRepository.findAllByAttributes(tipoEscenario, codCliente, codigo, fueAtendido, fechaHoraGeneracion, fechaHoraExpiracion, codDestino, pageable).getContent();
    }

    public List<PedidoEntity> findAllInRangeByScenario(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario, List<String> codOrigenes) {
        return pedidoRepository.findAllInRangeByScenario(fechaHoraInicio, fechaHoraFin, tipoEscenario, codOrigenes);
    }

    public Map<String, Integer> findAllMaxCodesByScenario(String tipoEscenario) {
        return pedidoRepository.findAllMaxCodesByScenario(tipoEscenario).stream().collect(Collectors.toMap(row -> (String) row[0], row -> row[1] != null ? ((Number) row[1]).intValue() : 0));
    }

    public Optional<PedidoEntity> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    public boolean existsById(Long id) {
        return pedidoRepository.existsById(id);
    }

    public void deleteById(Long id) {
        pedidoRepository.deleteById(id);
    }

    public Optional<PedidoEntity> findByUniqueAttributes(String codigo, String tipoEscenario) {
        return pedidoRepository.findByCodigoAndTipoEscenario(codigo, TipoEscenario.valueOf(tipoEscenario.toUpperCase()));
    }

    public Integer findMaxCodeOfDestinationByScenario(String codDestino, String tipoEscenario) {
        return G4DUtility.Convertor.toAdmissible(pedidoRepository.findMaxCodeOfDestinationByScenario(codDestino, tipoEscenario), 0);
    }

    public ListResponse listar(ListRequest request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        List<DTO> dtos = new ArrayList<>();
        List<PedidoEntity> entities = this.findAll(pageable);
        entities.forEach(entity -> dtos.add(pedidoMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Pedidos listados correctamente! ('%d')", dtos.size()), dtos);
    }

    public ListResponse filtrar(FilterRequest<PedidoDTO> request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        PedidoDTO modelo = request.getModelo();
        String tipoEscenario = G4DUtility.Convertor.toAdmissibleEnumString(modelo.getTipoEscenario(),  TipoEscenario.class);
        String codCliente = G4DUtility.Convertor.toAdmissible(modelo.getCodCliente());
        String codigo = G4DUtility.Convertor.toAdmissible(modelo.getCodigo());
        Boolean fueAtendido = modelo.getFueAtendido();
        String fechaHoraGeneracion = G4DUtility.Convertor.toAdmissibleDateTimeString(modelo.getFechaHoraGeneracion());
        String fechaHoraExpiracion = G4DUtility.Convertor.toAdmissibleDateTimeString(modelo.getFechaHoraExpiracion());
        String codDestino = G4DUtility.Convertor.toAdmissible(modelo.getCodDestino());
        List<DTO> dtos = new ArrayList<>();
        Page<PedidoEntity> pagina = pedidoRepository.findAllByAttributes(tipoEscenario, codCliente, codigo, fueAtendido, fechaHoraGeneracion, fechaHoraExpiracion, codDestino, pageable);
        pagina.getContent().forEach(entity -> dtos.add(pedidoMapper.toDTO(entity)));
        ListResponse response = new ListResponse(true, String.format("Pedidos filtrados correctamente! ('%d')", dtos.size()), dtos);
        response.setTotal(pagina.getTotalElements());
        return response;
    }

    // Un pedido de OPERACION cuyo destino es una sede se atiende al instante: no genera vuelos ni rutas.
    // Devuelve true si el pedido quedó atendido de esta forma (luego hay que crear su entrega directa).
    private boolean atenderInstantaneamenteSiEsSede(PedidoEntity pedido) {
        if (pedido.getTipoEscenario() == TipoEscenario.OPERACION && Boolean.TRUE.equals(pedido.getDestino().getEsSede())) {
            pedido.setFueAtendido(true);
            pedido.setFechaHoraProcesamiento(pedido.getFechaHoraGeneracion());
            pedido.setFechaHoraExpiracion(pedido.getFechaHoraGeneracion());
            return true;
        }
        return false;
    }

    // Entrega directa en sede: segmentación + lote (sin ruta, ENTREGADO) + registro con ingreso = egreso = generación.
    // El pedido ya debe estar persistido (tener id). Devuelve [segmentacion, lote, registro].
    private Object[] crearEntregaDirecta(PedidoEntity pedido) {
        LocalDateTime instante = pedido.getFechaHoraGeneracion();
        SegmentacionEntity segmentacion = new SegmentacionEntity();
        segmentacion.setCodigo(G4DUtility.Generator.getUniqueString("SEG"));
        segmentacion.setFechaHoraAplicacion(instante);
        segmentacion.setPedido(pedido);
        LoteEntity lote = new LoteEntity();
        lote.setCodigo(G4DUtility.Generator.getUniqueString("LOT"));
        lote.setTamanio(pedido.getCantidadSolicitada());
        lote.setEstado(EstadoLote.ENTREGADO);
        lote.setSegmentacion(segmentacion);
        lote.setRuta(null);
        RegistroEntity registro = new RegistroEntity();
        registro.setCodigo(G4DUtility.Generator.getUniqueString("REG"));
        registro.setFechaHoraIngreso(instante);
        registro.setFechaHoraEgreso(instante);
        registro.setSigueVigente(true);
        registro.setAeropuerto(pedido.getDestino());
        registro.setLote(lote);
        return new Object[] { segmentacion, lote, registro };
    }

    // Lotes del plan vigente de un pedido (vacío si aún no se planificó)
    private List<LoteEntity> lotesVigentes(PedidoEntity pedido) {
        SegmentacionEntity sVigente = pedido.getSegmentaciones().stream().filter(sg -> sg.getFechaHoraSustitucion() == null).findFirst().orElse(null);
        return (sVigente != null) ? sVigente.getLotes() : new ArrayList<>();
    }

    // Se puede cancelar si está activo, no es una entrega en sede y ningún vuelo de su plan despegó
    public boolean esCancelable(PedidoEntity pedido) {
        if (pedido.getTipoEscenario() != TipoEscenario.OPERACION || pedido.getEstado() != EstadoPedido.ACTIVO) return false;
        if (Boolean.TRUE.equals(pedido.getDestino().getEsSede())) return false;
        LocalDateTime ahora = LocalDateTime.now();
        return this.lotesVigentes(pedido).stream().noneMatch(l -> l.getRuta() != null && !l.getRuta().getFechaHoraSalida().isAfter(ahora));
    }

    // Pedidos de operación de un cliente (más recientes primero), con salida/llegada del plan vigente
    @Transactional(readOnly = true)
    public ListResponse listarDeCliente(String codCliente, ListRequest request) {
        int pagina = (request != null && request.getPagina() != null && request.getPagina() >= 0) ? request.getPagina() : 0;
        int tamanio = (request != null && request.getTamanio() != null && request.getTamanio() > 0) ? Math.min(request.getTamanio(), 200) : 50;
        List<PedidoEntity> todos = new ArrayList<>(this.findAllByAttributes(TipoEscenario.OPERACION.toString(), codCliente, null, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 2000)));
        todos.sort(Comparator.comparing(PedidoEntity::getFechaHoraGeneracion).reversed());
        List<DTO> dtos = new ArrayList<>();
        todos.stream().skip((long) pagina * tamanio).limit(tamanio).forEach(entity -> {
            PedidoDTO dto = pedidoMapper.toDTO(entity);
            LocalDateTime salida = null, llegada = null;
            for (LoteEntity lote : this.lotesVigentes(entity)) {
                if (lote.getRuta() == null) continue;
                if (salida == null || lote.getRuta().getFechaHoraSalida().isBefore(salida)) salida = lote.getRuta().getFechaHoraSalida();
                if (llegada == null || lote.getRuta().getFechaHoraLlegada().isAfter(llegada)) llegada = lote.getRuta().getFechaHoraLlegada();
            }
            if (salida == null && Boolean.TRUE.equals(entity.getFueAtendido())) { salida = entity.getFechaHoraGeneracion(); llegada = entity.getFechaHoraGeneracion(); }
            dto.setFechaHoraPrimeraSalida(salida != null ? G4DUtility.Convertor.toDisplayString(salida) : null);
            dto.setFechaHoraUltimaLlegada(llegada != null ? G4DUtility.Convertor.toDisplayString(llegada) : null);
            dto.setCancelable(this.esCancelable(entity));
            dto.setCiudadDestino(entity.getDestino().getCiudad());
            dtos.add(dto);
        });
        return new ListResponse(true, String.format("%d", todos.size()), dtos);
    }

    // Cancelación pedida por el propio cliente: solo sus pedidos
    @Transactional
    public GenericResponse cancelarDeCliente(String codigo, String codCliente) {
        PedidoEntity pedido = this.findByUniqueAttributes(codigo, TipoEscenario.OPERACION.toString()).orElseThrow(() -> new G4DException(String.format("El pedido ('%s') no existe.", codigo)));
        if (pedido.getCliente() == null || !pedido.getCliente().getCodigo().equals(codCliente)) {
            throw new G4DException(String.format("El pedido ('%s') no existe.", codigo));
        }
        return this.cancelar(codigo);
    }

    // Cancelación: solo pedidos de OPERACION activos cuyo plan aún no inició (ningún vuelo ha salido).
    // Libera la capacidad de los vuelos y deja los registros como no vigentes (soft delete).
    @Transactional
    public GenericResponse cancelar(String codigo) {
        PedidoEntity pedido = this.findByUniqueAttributes(codigo, TipoEscenario.OPERACION.toString()).orElseThrow(() -> new G4DException(String.format("El pedido ('%s') no existe.", codigo)));
        if (pedido.getEstado() != EstadoPedido.ACTIVO) {
            throw new G4DException(String.format("El pedido ('%s') ya fue cancelado.", codigo));
        }
        if (Boolean.TRUE.equals(pedido.getDestino().getEsSede())) {
            throw new G4DException(String.format("El pedido ('%s') ya fue entregado en sede.", codigo));
        }
        LocalDateTime ahora = LocalDateTime.now();
        SegmentacionEntity sVigente = pedido.getSegmentaciones().stream().filter(sg -> sg.getFechaHoraSustitucion() == null).findFirst().orElse(null);
        List<LoteEntity> lotes = (sVigente != null) ? sVigente.getLotes() : new ArrayList<>();
        boolean yaInicio = lotes.stream().anyMatch(l -> l.getRuta() != null && !l.getRuta().getFechaHoraSalida().isAfter(ahora));
        if (yaInicio) {
            throw new G4DException(String.format("El pedido ('%s') ya inició su traslado y no se puede cancelar.", codigo));
        }
        for (LoteEntity lote : lotes) {
            if (lote.getRuta() != null) {
                for (VueloEntity vuelo : lote.getRuta().getVuelos()) {
                    vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + lote.getTamanio());
                    vueloService.save(vuelo);
                }
            }
            for (RegistroEntity registro : lote.getRegistros()) {
                registro.setSigueVigente(false);
                registroService.save(registro);
            }
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        this.save(pedido);
        return new GenericResponse(true, String.format("Pedido '%s' cancelado correctamente!", codigo));
    }

    // Carga unitaria (fecha de generación en UTC)
    @Transactional
    public PedidoEntity importar(String idTransaccion, PedidoDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando pedido..");
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando pedido", 0, 1));
            TipoEscenario tipoEscenario = G4DUtility.Convertor.toAdmissible(dto.getTipoEscenario(), TipoEscenario.class);
            if (tipoEscenario == null) {
                throw new G4DException(String.format("El tipo de escenario ('%s') del pedido es inválido.", dto.getTipoEscenario()));
            }
            String codDestino = dto.getCodDestino();
            AeropuertoEntity destino = aeropuertoService.findByCodigo(codDestino).orElseThrow(() -> new G4DException(String.format("El destino ('%s') del pedido es inválido.", codDestino)));
            String codCliente = dto.getCodCliente();
            PedidoEntity pedido = new PedidoEntity();
            if (tipoEscenario == TipoEscenario.OPERACION) {
                UsuarioEntity cliente = usuarioService.findClienteByCodigo(codCliente).orElseThrow(() -> new G4DException(String.format("El cliente ('%s') del pedido no existe.", codCliente)));
                pedido.setCliente(cliente);
            } else {
                if (G4DUtility.Convertor.toAdmissible(codCliente) == null) {
                    throw new G4DException("El código de cliente del pedido simulado es obligatorio.");
                }
                pedido.setCodClienteSimulado(codCliente);
            }
            pedido.setDestino(destino);
            pedido.setTipoEscenario(tipoEscenario);
            pedido.setCantidadSolicitada(dto.getCantidadSolicitada());
            pedido.setFechaHoraGeneracion(G4DUtility.Convertor.toDateTime(dto.getFechaHoraGeneracion()));
            pedido.setFechaHoraProcesamiento(G4DUtility.Convertor.toAdmissible(dto.getFechaHoraProcesamiento(), (LocalDateTime) null));
            if (tipoEscenario == TipoEscenario.SIMULACION && pedido.getFechaHoraProcesamiento() == null) {
                pedido.setFechaHoraProcesamiento(pedido.getFechaHoraGeneracion()); // igual que en la carga por archivo
            }
            pedido.setFechaHoraExpiracion(G4DUtility.Convertor.toAdmissible(dto.getFechaHoraExpiracion(), (LocalDateTime) null));
            pedido.setFueAtendido(dto.getFueAtendido() != null ? dto.getFueAtendido() : false);
            pedido.setCodigo(String.format("%s%09d", destino.getCodigo(), 1 + this.findMaxCodeOfDestinationByScenario(codDestino, tipoEscenario.toString())));
            boolean esEntregaDirecta = this.atenderInstantaneamenteSiEsSede(pedido);
            this.save(pedido);
            if (esEntregaDirecta) {
                Object[] entrega = this.crearEntregaDirecta(pedido);
                segmentacionService.save((SegmentacionEntity) entrega[0]);
                loteService.save((LoteEntity) entrega[1]);
                registroService.save((RegistroEntity) entrega[2]);
            }
            System.out.println("[<] PEDIDO IMPORTADO!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando pedido", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            return pedido;
        } catch (Exception e) {
            throw (e instanceof G4DException g4dEx) ? g4dEx : new RuntimeException(e);
        }
    }

    // Guarda un bloque de pedidos y, después (ya con id), las entregas directas en sede de ese bloque
    private void guardarLote(List<PedidoEntity> pedidos, List<PedidoEntity> entregasDirectas, String progressDestination) {
        importationService.batchSave(pedidos, progressDestination, "pedidos");
        System.out.printf("[<] PEDIDOS IMPORTADOS! ('%d')%n", pedidos.size());
        if (!entregasDirectas.isEmpty()) {
            List<SegmentacionEntity> segmentaciones = new ArrayList<>();
            List<LoteEntity> lotes = new ArrayList<>();
            List<RegistroEntity> registros = new ArrayList<>();
            for (PedidoEntity pedido : entregasDirectas) {
                Object[] entrega = this.crearEntregaDirecta(pedido);
                segmentaciones.add((SegmentacionEntity) entrega[0]);
                lotes.add((LoteEntity) entrega[1]);
                registros.add((RegistroEntity) entrega[2]);
            }
            importationService.batchSave(segmentaciones, progressDestination, "segmentaciones");
            importationService.batchSave(lotes, progressDestination, "lotes");
            importationService.batchSave(registros, progressDestination, "registros");
            entregasDirectas.clear();
        }
        pedidos.clear();
    }

    // Carga por archivo (fecha/hora local del destino -> UTC)
    //  - SIMULACION: NNNNNNNNN-AAAAMMDD-HH-MM-DEST-CANT-CLIENTE  (el cliente no se busca en BD; se genera en RAM al simular)
    //  - OPERACION :           AAAAMMDD-HH-MM-DEST-CANT-CLIENTE  (si el cliente no existe en BD, el pedido se omite)
    // Devuelve la cantidad de pedidos guardados
    public int importar(String idTransaccion, Path archivo, ImportFileRequest request) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.printf(">> Importando pedidos desde '%s'.. (batch 500)%n", archivo.getFileName());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 0, 4));
            TipoEscenario tipoEscenario = G4DUtility.Convertor.toAdmissible(request.getTipoEscenario(), TipoEscenario.class);
            if (tipoEscenario == null) {
                throw new G4DException(String.format("El tipo de escenario ('%s') es inválido.", request.getTipoEscenario()));
            }
            boolean esSimulacion = tipoEscenario.equals(TipoEscenario.SIMULACION);
            BufferedReader br = Files.newBufferedReader(archivo, G4DUtility.Reader.getFileCharset(archivo));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 1, 4));
            Map<String, UsuarioEntity> poolClientes = importationService.getNewLimitedPool(500);
            Set<String> clientesInexistentes = new HashSet<>();
            Map<String, AeropuertoEntity> poolAeropuertos = aeropuertoService.findAll().stream().collect(Collectors.toMap(AeropuertoEntity::getCodigo, a -> a));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 2, 4));
            Map<String, Integer> poolMaxCodigos = this.findAllMaxCodesByScenario(tipoEscenario.toString());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 3, 4));
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0, omitidos = 0, atendidosEnSede = 0, importados = 0;
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 4, 4));
            List<PedidoEntity> pedidos = new ArrayList<>();
            List<PedidoEntity> entregasDirectas = new ArrayList<>();
            LocalDateTime fechaHoraInicio = G4DUtility.Convertor.toAdmissible(request.getFechaHoraInicio(), LocalDateTime.MIN);
            LocalDateTime fechaHoraFin = G4DUtility.Convertor.toAdmissible(request.getFechaHoraFin(), LocalDateTime.MAX);
            String linea;
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            if (lProcesadas % 20 == 0 || lProcesadas >= lTotales) communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            while ((linea = br.readLine()) != null) {
                linea = linea.replace("\uFEFF", "").trim();
                if (!linea.isBlank()) {
                    String[] partes = linea.split("-");
                    int idx = 0;
                    String numPed = esSimulacion ? partes[idx++] : null;
                    int fechaStr = Integer.parseInt(partes[idx++]);
                    int hora = Integer.parseInt(partes[idx++]);
                    int minuto = Integer.parseInt(partes[idx++]);
                    String codDestino = partes[idx++];
                    int cantidad = Integer.parseInt(partes[idx++]);
                    String codCliente = partes[idx];
                    LocalDateTime fechaHoraGeneracionLocal = LocalDateTime.of(G4DUtility.Convertor.toDate(fechaStr), LocalTime.of(hora, minuto, 0));
                    AeropuertoEntity destino = poolAeropuertos.get(codDestino);
                    if (destino == null) {
                        throw new G4DException(String.format("Destino '%s' inválido en línea #%d", codDestino, lProcesadas + 1));
                    }
                    LocalDateTime fechaHoraGeneracion = G4DUtility.Convertor.toUTC(fechaHoraGeneracionLocal, destino.getHusoHorario());
                    if (!fechaHoraGeneracion.isBefore(fechaHoraInicio) && !fechaHoraGeneracion.isAfter(fechaHoraFin)) {
                        PedidoEntity pedido = new PedidoEntity();
                        boolean esValido = true;
                        if (esSimulacion) {
                            pedido.setCodClienteSimulado(codCliente);
                        } else {
                            UsuarioEntity cliente = poolClientes.get(codCliente);
                            if (cliente == null && !clientesInexistentes.contains(codCliente)) {
                                cliente = usuarioService.findClienteByCodigo(codCliente).orElse(null);
                                if (cliente != null) poolClientes.put(codCliente, cliente);
                                else clientesInexistentes.add(codCliente);
                            }
                            if (cliente != null) pedido.setCliente(cliente);
                            else esValido = false;
                        }
                        if (esValido) {
                            pedido.setDestino(destino);
                            pedido.setCantidadSolicitada(cantidad);
                            pedido.setFechaHoraGeneracion(fechaHoraGeneracion);
                            pedido.setTipoEscenario(tipoEscenario);
                            if (esSimulacion) {
                                pedido.setCodigo(destino.getCodigo() + numPed);
                                pedido.setFechaHoraProcesamiento(fechaHoraGeneracion);
                            } else {
                                poolMaxCodigos.merge(codDestino, 1, Integer::sum);
                                pedido.setCodigo(String.format("%s%09d", destino.getCodigo(), poolMaxCodigos.get(codDestino)));
                                if (this.atenderInstantaneamenteSiEsSede(pedido)) {
                                    entregasDirectas.add(pedido);
                                    atendidosEnSede++;
                                }
                            }
                            pedidos.add(pedido);
                            importados++;
                        } else omitidos++;
                    }
                }
                lProcesadas++;
                if (lProcesadas % 20 == 0 || lProcesadas >= lTotales) communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (pedidos.size() >= 500 || (lProcesadas == lTotales && !pedidos.isEmpty())) {
                    this.guardarLote(pedidos, entregasDirectas, progressDestination);
                }
            }
            if (!pedidos.isEmpty()) {
                this.guardarLote(pedidos, entregasDirectas, progressDestination);
            }
            if (atendidosEnSede > 0) System.out.printf("[*] PEDIDOS ATENDIDOS AL INSTANTE (DESTINO SEDE): '%d'%n", atendidosEnSede);
            if (omitidos > 0) System.out.printf("[*] PEDIDOS OMITIDOS POR CLIENTE INEXISTENTE: '%d' (clientes: %s)%n", omitidos, clientesInexistentes);
            poolMaxCodigos.clear();
            poolAeropuertos.clear();
            poolClientes.clear();
            br.close();
            String detalle = (omitidos > 0) ? String.format("Se importaron %d pedidos. Se omitieron %d porque su cliente no existe.", importados, omitidos) : String.format("Se importaron %d pedidos.", importados);
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO, detalle));
            return importados;
        } catch (ArrayIndexOutOfBoundsException | NoSuchElementException | NumberFormatException e) {
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getFileName()));
        } catch (IOException e) {
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getFileName()));
        } catch (G4DException e) {
            throw e;
        }
    }
}
