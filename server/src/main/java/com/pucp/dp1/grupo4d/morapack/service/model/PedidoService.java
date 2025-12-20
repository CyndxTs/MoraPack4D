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

    public Optional<PedidoEntity> findById(Integer id) {
        return pedidoRepository.findById(id);
    }

    public boolean existsById(Integer id) {
        return pedidoRepository.existsById(id);
    }

    public void deleteById(Integer id) {
        pedidoRepository.deleteById(id);
    }

    public Optional<PedidoEntity> findByUniqueAttributes(String codigo, String tipoEscenario) {
        return pedidoRepository.findByUniqueAttributes(codigo, tipoEscenario);
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

    @Transactional
    public void importar(String idTransaccion, PedidoDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando pedido..");
            PedidoEntity pedido = new PedidoEntity();
            String codDestino = dto.getCodDestino();
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando pedido", 0, 1));
            AeropuertoEntity destino = aeropuertoService.findByCodigo(codDestino).orElse(null);
            if(destino != null) {
                String codCliente = dto.getCodCliente();
                ClienteEntity cliente = clienteService.findByCodigo(codCliente).orElse(null);
                if(cliente != null) {
                    pedido.setCliente(cliente);
                    pedido.setDestino(destino);
                    pedido.setTipoEscenario(TipoEscenario.valueOf(dto.getTipoEscenario()));
                    pedido.setCantidadSolicitada(dto.getCantidadSolicitada());
                    pedido.setFechaHoraGeneracionUTC(G4DUtility.Convertor.toDateTime(dto.getFechaHoraGeneracion()));
                    pedido.setFechaHoraGeneracionLocal(G4DUtility.Convertor.toLocal(pedido.getFechaHoraGeneracionUTC(), destino.getHusoHorario()));
                    pedido.setFechaHoraProcesamientoUTC(G4DUtility.Convertor.toAdmissible(dto.getFechaHoraProcesamiento(), (LocalDateTime) null));
                    pedido.setFechaHoraProcesamientoLocal(pedido.getFechaHoraProcesamientoUTC() != null ? G4DUtility.Convertor.toLocal(pedido.getFechaHoraProcesamientoUTC(), destino.getHusoHorario()) : null);
                    pedido.setFechaHoraExpiracionUTC(G4DUtility.Convertor.toAdmissible(dto.getFechaHoraExpiracion(), (LocalDateTime) null));
                    pedido.setFechaHoraExpiracionLocal(pedido.getFechaHoraExpiracionUTC() != null ? G4DUtility.Convertor.toLocal(pedido.getFechaHoraExpiracionUTC(), destino.getHusoHorario()) : null);
                    pedido.setFueAtendido(dto.getFueAtendido() != null ? dto.getFueAtendido() : false);
                    pedido.setCodigo(String.format("%s%09d", destino.getCodigo(), 1 + this.findMaxCodeOfDestinationByScenario(codDestino, dto.getTipoEscenario())));
                    this.save(pedido);
                } else throw new G4DException(String.format("El cliente ('%s') del pedido es inválido.", codCliente));
            } else throw new G4DException(String.format("El destino ('%s') del pedido es inválido.", codDestino));
            System.out.println("[<] PEDIDO IMPORTADO!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando pedido", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new RuntimeException(e);
        }
    }

    public void importar(String idTransaccion, Path archivo, ImportFileRequest request) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.printf(">> Importando pedidos desde '%s'.. (batch 500)%n", archivo.getFileName());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 0, 5));
            BufferedReader br = Files.newBufferedReader(archivo, G4DUtility.Reader.getFileCharset(archivo));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 1, 5));
            TipoEscenario tipoEscenario = G4DUtility.Convertor.toAdmissible(request.getTipoEscenario(), TipoEscenario.class);
            Map<String, ClienteEntity> poolClientes = importationService.getNewLimitedPool(500);
            clienteService.findAll(PageRequest.of(0, 250)).forEach(c -> poolClientes.put(c.getCodigo(), c));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 2, 5));
            Map<String, AeropuertoEntity> poolAeropuertos = aeropuertoService.findAll().stream().collect(Collectors.toMap(AeropuertoEntity::getCodigo, a -> a));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 3, 5));
            Map<String, Integer> poolMaxCodigos = this.findAllMaxCodesByScenario(tipoEscenario.toString().toUpperCase());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 4, 5));
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0;
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 5, 5));
            List<PedidoEntity> pedidos = new ArrayList<>();
            List<ClienteEntity> clientes = new ArrayList<>();
            LocalDateTime fechaHoraInicio = G4DUtility.Convertor.toAdmissible(request.getFechaHoraInicio(), LocalDateTime.MIN);
            LocalDateTime fechaHoraFin = G4DUtility.Convertor.toAdmissible(request.getFechaHoraFin(), LocalDateTime.MAX);
            boolean esSimulacion = tipoEscenario.equals(TipoEscenario.SIMULACION);
            String linea;
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
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
                    if (destino != null) {
                        LocalDateTime fechaHoraGeneracionUTC = G4DUtility.Convertor.toUTC(fechaHoraGeneracionLocal, destino.getHusoHorario());
                        if (!fechaHoraGeneracionUTC.isBefore(fechaHoraInicio) && !fechaHoraGeneracionUTC.isAfter(fechaHoraFin)) {
                            ClienteEntity cliente = poolClientes.getOrDefault(codCliente, null);
                            if (cliente == null) {
                                cliente = clienteService.findByCodigo(codCliente).orElse(null);
                                if(cliente == null) {
                                    cliente = new ClienteEntity();
                                    cliente.setCodigo(codCliente);
                                    cliente.setNombre(G4DUtility.Generator.getUniqueName());
                                    String correo = G4DUtility.Generator.getUniqueEmail();
                                    boolean existeCorreo = clienteService.existsByCorreo(correo);
                                    if(existeCorreo) {
                                        String newCorreo = "";
                                        while (existeCorreo) {
                                            newCorreo = G4DUtility.Generator.addRandomInteger(correo, correo.indexOf('@'));
                                            existeCorreo = clienteService.existsByCorreo(newCorreo);
                                        }
                                        cliente.setCorreo(newCorreo);
                                    } else cliente.setCorreo(correo);
                                    cliente.setContrasenia("12345678");
                                    clientes.add(cliente);
                                }
                                poolClientes.put(codCliente, cliente);
                            }
                            PedidoEntity pedido = new PedidoEntity();
                            poolMaxCodigos.merge(codDestino, 1, Integer::sum);
                            pedido.setDestino(destino);
                            pedido.setCliente(cliente);
                            pedido.setCantidadSolicitada(cantidad);
                            pedido.setFechaHoraGeneracionLocal(fechaHoraGeneracionLocal);
                            pedido.setFechaHoraGeneracionUTC(fechaHoraGeneracionUTC);
                            if(esSimulacion) {
                                pedido.setCodigo(destino.getCodigo() + numPed);
                                pedido.setFechaHoraProcesamientoLocal(fechaHoraGeneracionLocal);
                                pedido.setFechaHoraProcesamientoUTC(fechaHoraGeneracionUTC);
                            } else pedido.setCodigo(String.format("%s%09d", destino.getCodigo(), poolMaxCodigos.get(codDestino)));
                            pedido.setTipoEscenario(tipoEscenario);
                            pedidos.add(pedido);
                        }
                    } else throw new G4DException(String.format("Destino '%s' inválido en línea #%d", codDestino, lProcesadas + 1));
                }
                lProcesadas++;
                communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (pedidos.size() % 500 == 0 || lProcesadas == lTotales) {
                    if(!clientes.isEmpty()) {
                        importationService.batchSave(clientes, progressDestination, "clientes");
                        System.out.printf("[<] CLIENTES IMPORTADOS! ('%d')%n", clientes.size());
                        clientes.clear();
                    }
                    importationService.batchSave(pedidos, progressDestination, "pedidos");
                    System.out.printf("[<] PEDIDOS IMPORTADOS! ('%d')%n", pedidos.size());
                    pedidos.clear();
                }
            }
            poolMaxCodigos.clear();
            poolAeropuertos.clear();
            poolClientes.clear();
            br.close();
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (ArrayIndexOutOfBoundsException | NoSuchElementException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getFileName()));
        } catch (IOException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getFileName()));
        }
    }
}
