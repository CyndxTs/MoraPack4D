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
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.PedidoRepository;
import com.pucp.dp1.grupo4d.morapack.service.ImportService;
import com.pucp.dp1.grupo4d.morapack.service.WebSocketService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final AeropuertoService aeropuertoService;
    private final ClienteService clienteService;
    private final PedidoMapper pedidoMapper;
    private final ImportService importService;

    public PedidoService(PedidoRepository pedidoRepository, AeropuertoService aeropuertoService, ClienteService clienteService, PedidoMapper pedidoMapper, ImportService importService) {
        this.pedidoRepository = pedidoRepository;
        this.aeropuertoService = aeropuertoService;
        this.clienteService = clienteService;
        this.pedidoMapper = pedidoMapper;
        this.importService = importService;
    }

    public List<PedidoEntity> findAll() {
        return pedidoRepository.findAll();
    }

    public List<PedidoEntity> findAll(Pageable pageable) {
        return pedidoRepository.findAll(pageable).getContent();
    }

    public Optional<PedidoEntity> findById(Integer id) {
        return pedidoRepository.findById(id);
    }

    public PedidoEntity save(PedidoEntity pedido) {
        return pedidoRepository.save(pedido);
    }

    public void deleteById(Integer id) {
        pedidoRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return pedidoRepository.existsById(id);
    }

    public Map<String, Integer> findAllMaxCodigoByEscenario(String tipoEscenario) {
        return pedidoRepository.findAllMaxCodigoByTipoEscenario(tipoEscenario).stream().collect(Collectors.toMap(row -> (String) row[0], row -> row[1] != null ? ((Number) row[1]).intValue() : 0));
    }

    public Integer findMaxCodigoByDestinoEscenario(String codDestino, String tipoEscenario) {
        return G4DUtility.Convertor.toAdmissible(pedidoRepository.findMaxCodigoByDestinoEscenario(codDestino, tipoEscenario), 0);
    }

    public Optional<PedidoEntity> findByCodigoEscenario(String codigo, String tipoEscenario) {
        return pedidoRepository.findByCodigoEscenario(codigo, tipoEscenario);
    }

    public boolean existsByCodigoEscenario(String codigo, String tipoEscenario) {
        return pedidoRepository.findByCodigoEscenario(codigo, tipoEscenario).isPresent();
    }

    public List<PedidoEntity> findAllByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario) {
        return pedidoRepository.findAllByDateTimeRange(fechaHoraInicio, fechaHoraFin, tipoEscenario);
    }

    public List<PedidoEntity> findAllByDestino(AeropuertoEntity destino) {
        return pedidoRepository.findAllByDestino(destino);
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
        String codigoPedido = G4DUtility.Convertor.toAdmissible(modelo.getCodigo());
        Boolean fueAtendido = modelo.getFueAtendido();
        String fechaHoraGeneracion = G4DUtility.Convertor.toAdmissibleDateTimeString(modelo.getFechaHoraGeneracion());
        String fechaHoraExpiracion = G4DUtility.Convertor.toAdmissibleDateTimeString(modelo.getFechaHoraExpiracion());
        String codDestino = G4DUtility.Convertor.toAdmissible(modelo.getCodDestino());
        List<DTO> dtos = new ArrayList<>();
        List<PedidoEntity> entities = pedidoRepository.filterBy(tipoEscenario, codCliente, codigoPedido, fueAtendido, fechaHoraGeneracion, fechaHoraExpiracion, codDestino, pageable).getContent();
        entities.forEach(entity -> dtos.add(pedidoMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Pedidos filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    public GenericResponse importar(ImportRequest<PedidoDTO> request) {
        System.out.println("Importando pedido..");
        PedidoDTO dto = request.getDto();
        PedidoEntity pedido = new PedidoEntity();
        String codDestino = dto.getCodDestino();
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
                pedido.setCodigo(String.format("%s%09d", destino.getCodigo(), 1 + this.findMaxCodigoByDestinoEscenario(codDestino, dto.getTipoEscenario())));
                this.save(pedido);
            } else throw new G4DException(String.format("El cliente ('%s') del pedido es inválido.", codCliente));
        } else throw new G4DException(String.format("El destino ('%s') del pedido es inválido.", codDestino));
        System.out.println("[<] PEDIDO IMPORTADO!");
        return new GenericResponse(true, "Pedido importado correctamente!");
    }

    public GenericResponse importar(MultipartFile archivo, ImportFileRequest request) {
        try {
            System.out.printf("Importando pedidos desde '%s'.. (batch 500)%n", archivo.getName());
            LocalDateTime fechaHoraInicio = G4DUtility.Convertor.toAdmissible(request.getFechaHoraInicio(), LocalDateTime.MIN);
            LocalDateTime fechaHoraFin = G4DUtility.Convertor.toAdmissible(request.getFechaHoraFin(), LocalDateTime.MAX);
            if (fechaHoraFin.isBefore(fechaHoraInicio)) throw new G4DException("Rango de tiempo inválido.");
            TipoEscenario tipoEscenario = G4DUtility.Convertor.toAdmissible(request.getTipoEscenario(), TipoEscenario.class);
            boolean tieneNumeroDePedido = tipoEscenario.equals(TipoEscenario.SIMULACION);
            BufferedReader br = new BufferedReader(new InputStreamReader(archivo.getInputStream(), G4DUtility.Reader.getFileCharset(archivo)));
            List<PedidoEntity> pedidos = new ArrayList<>();
            List<ClienteEntity> clientes = new ArrayList<>();
            Map<String, ClienteEntity> poolClientes = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ClienteEntity> eldest) {
                    return size() > 500;
                }
            };
            clienteService.findAll(PageRequest.of(0, 250)).forEach(c -> poolClientes.put(c.getCodigo(), c));
            Map<String, AeropuertoEntity> poolAeropuertos = aeropuertoService.findAll().stream().collect(Collectors.toMap(AeropuertoEntity::getCodigo, a -> a));
            Map<String, Integer> poolMaxCodigos = this.findAllMaxCodigoByEscenario(tipoEscenario.toString().toUpperCase());
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0;
            String linea;
            WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.INICIADO));
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isBlank()) {
                    String[] partes = linea.split("-");
                    int idx = 0;
                    String numPed = tieneNumeroDePedido ? partes[idx++] : null;
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
                                }
                                clientes.add(cliente);
                                poolClientes.put(codCliente, cliente);
                            }
                            PedidoEntity pedido = new PedidoEntity();
                            poolMaxCodigos.merge(codDestino, 1, Integer::sum);
                            pedido.setCodigo(numPed != null ? destino.getCodigo() + numPed : String.format("%s%09d", destino.getCodigo(), poolMaxCodigos.get(codDestino)));
                            pedido.setDestino(destino);
                            pedido.setCliente(cliente);
                            pedido.setCantidadSolicitada(cantidad);
                            pedido.setFechaHoraGeneracionLocal(fechaHoraGeneracionLocal);
                            pedido.setFechaHoraGeneracionUTC(fechaHoraGeneracionUTC);
                            pedido.setTipoEscenario(tipoEscenario);
                            pedidos.add(pedido);
                        }
                    } else throw new G4DException(String.format("Destino '%s' inválido en línea #%d", codDestino, lProcesadas + 1));
                }
                lProcesadas++;
                WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (pedidos.size() % 500 == 0 || lProcesadas == lTotales) {
                    importService.batchSave(clientes, "clientes");
                    System.out.printf("[<] CLIENTES IMPORTADOS! ('%d')%n", clientes.size());
                    clientes.clear();
                    importService.batchSave(pedidos, "pedidos");
                    System.out.printf("[<] PEDIDOS IMPORTADOS! ('%d')%n", pedidos.size());
                    pedidos.clear();
                }
            }
            poolMaxCodigos.clear();
            poolAeropuertos.clear();
            poolClientes.clear();
            br.close();
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            return new GenericResponse(true, "Pedidos importados correctamente!");
        } catch (ArrayIndexOutOfBoundsException | NoSuchElementException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getName()));
        } catch (IOException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getName()));
        }
    }
}
