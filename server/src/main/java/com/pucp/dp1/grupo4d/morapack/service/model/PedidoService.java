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
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.PedidoRepository;
import com.pucp.dp1.grupo4d.morapack.service.CommunicationService;
import com.pucp.dp1.grupo4d.morapack.service.ImportationService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
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
    private final ClienteService clienteService;
    private final CommunicationService communicationService;
    private final ImportationService importationService;

    public PedidoService(PedidoRepository pedidoRepository, PedidoMapper pedidoMapper, AeropuertoService aeropuertoService, ClienteService clienteService, CommunicationService communicationService, ImportationService importationService) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoMapper = pedidoMapper;
        this.aeropuertoService = aeropuertoService;
        this.clienteService = clienteService;
        this.communicationService = communicationService;
        this.importationService = importationService;
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
        List<PedidoEntity> entities = this.findAllByAttributes(tipoEscenario, codCliente, codigo, fueAtendido, fechaHoraGeneracion, fechaHoraExpiracion, codDestino, pageable);
        entities.forEach(entity -> dtos.add(pedidoMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Pedidos filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    // Un pedido de OPERACION cuyo destino es una sede se atiende al instante (sin vuelos, rutas ni registros)
    private void atenderInstantaneamenteSiEsSede(PedidoEntity pedido) {
        if (pedido.getTipoEscenario() == TipoEscenario.OPERACION && Boolean.TRUE.equals(pedido.getDestino().getEsSede())) {
            pedido.setFueAtendido(true);
            pedido.setFechaHoraProcesamiento(pedido.getFechaHoraGeneracion());
            pedido.setFechaHoraExpiracion(pedido.getFechaHoraGeneracion());
        }
    }

    // Carga unitaria (fecha de generación en UTC)
    @Transactional
    public void importar(String idTransaccion, PedidoDTO dto) {
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
                ClienteEntity cliente = clienteService.findByCodigo(codCliente).orElseThrow(() -> new G4DException(String.format("El cliente ('%s') del pedido no existe.", codCliente)));
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
            pedido.setFechaHoraExpiracion(G4DUtility.Convertor.toAdmissible(dto.getFechaHoraExpiracion(), (LocalDateTime) null));
            pedido.setFueAtendido(dto.getFueAtendido() != null ? dto.getFueAtendido() : false);
            pedido.setCodigo(String.format("%s%09d", destino.getCodigo(), 1 + this.findMaxCodeOfDestinationByScenario(codDestino, tipoEscenario.toString())));
            this.atenderInstantaneamenteSiEsSede(pedido);
            this.save(pedido);
            System.out.println("[<] PEDIDO IMPORTADO!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando pedido", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw (e instanceof G4DException g4dEx) ? g4dEx : new RuntimeException(e);
        }
    }

    // Carga por archivo (fecha/hora local del destino -> UTC)
    //  - SIMULACION: NNNNNNNNN-AAAAMMDD-HH-MM-DEST-CANT-CLIENTE  (el cliente no se busca en BD; se genera en RAM al simular)
    //  - OPERACION :           AAAAMMDD-HH-MM-DEST-CANT-CLIENTE  (si el cliente no existe en BD, el pedido se omite)
    public void importar(String idTransaccion, Path archivo, ImportFileRequest request) {
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
            Map<String, ClienteEntity> poolClientes = importationService.getNewLimitedPool(500);
            Set<String> clientesInexistentes = new HashSet<>();
            Map<String, AeropuertoEntity> poolAeropuertos = aeropuertoService.findAll().stream().collect(Collectors.toMap(AeropuertoEntity::getCodigo, a -> a));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 2, 4));
            Map<String, Integer> poolMaxCodigos = this.findAllMaxCodesByScenario(tipoEscenario.toString());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 3, 4));
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0, omitidos = 0, atendidosEnSede = 0;
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 4, 4));
            List<PedidoEntity> pedidos = new ArrayList<>();
            LocalDateTime fechaHoraInicio = G4DUtility.Convertor.toAdmissible(request.getFechaHoraInicio(), LocalDateTime.MIN);
            LocalDateTime fechaHoraFin = G4DUtility.Convertor.toAdmissible(request.getFechaHoraFin(), LocalDateTime.MAX);
            String linea;
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
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
                            ClienteEntity cliente = poolClientes.get(codCliente);
                            if (cliente == null && !clientesInexistentes.contains(codCliente)) {
                                cliente = clienteService.findByCodigo(codCliente).orElse(null);
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
                                this.atenderInstantaneamenteSiEsSede(pedido);
                                if (pedido.getFueAtendido()) atendidosEnSede++;
                            }
                            pedidos.add(pedido);
                        } else omitidos++;
                    }
                }
                lProcesadas++;
                communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (pedidos.size() >= 500 || (lProcesadas == lTotales && !pedidos.isEmpty())) {
                    importationService.batchSave(pedidos, progressDestination, "pedidos");
                    System.out.printf("[<] PEDIDOS IMPORTADOS! ('%d')%n", pedidos.size());
                    pedidos.clear();
                }
            }
            if (!pedidos.isEmpty()) {
                importationService.batchSave(pedidos, progressDestination, "pedidos");
                System.out.printf("[<] PEDIDOS IMPORTADOS! ('%d')%n", pedidos.size());
            }
            if (atendidosEnSede > 0) System.out.printf("[*] PEDIDOS ATENDIDOS AL INSTANTE (DESTINO SEDE): '%d'%n", atendidosEnSede);
            if (omitidos > 0) System.out.printf("[*] PEDIDOS OMITIDOS POR CLIENTE INEXISTENTE: '%d' (clientes: %s)%n", omitidos, clientesInexistentes);
            poolMaxCodigos.clear();
            poolAeropuertos.clear();
            poolClientes.clear();
            br.close();
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (ArrayIndexOutOfBoundsException | NoSuchElementException | NumberFormatException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getFileName()));
        } catch (IOException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getFileName()));
        } catch (G4DException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw e;
        }
    }
}
