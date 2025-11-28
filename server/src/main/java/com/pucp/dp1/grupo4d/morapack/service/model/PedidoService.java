/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.PedidoMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoLote;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.repository.PedidoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final AeropuertoService aeropuertoService;
    private final ClienteService clienteService;
    private final PedidoMapper pedidoMapper;
    private final HashMap<String, PedidoEntity> pedidos =  new HashMap<>();

    public PedidoService(PedidoRepository pedidoRepository, AeropuertoService aeropuertoService, ClienteService clienteService, PedidoMapper pedidoMapper) {
        this.pedidoRepository = pedidoRepository;
        this.aeropuertoService = aeropuertoService;
        this.clienteService = clienteService;
        this.pedidoMapper = pedidoMapper;
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

    public Optional<PedidoEntity> findByCodigo(String codigo) {
        return pedidoRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return pedidoRepository.findByCodigo(codigo).isPresent();
    }

    public List<PedidoEntity> findAllByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario) {
        return  pedidoRepository.findAllByDateTimeRange(fechaHoraInicio, fechaHoraFin, tipoEscenario);
    }

    public List<PedidoEntity> findAllSinceDateTime(LocalDateTime fechaHoraInicio, String tipoEscenario) {
        return  pedidoRepository.findAllSinceDateTime(fechaHoraInicio, tipoEscenario);
    }

    public List<PedidoEntity> findAllByDestino(AeropuertoEntity destino) {
        return pedidoRepository.findAllByDestino(destino);
    }

    private String obtenerNuevoCodigo(AeropuertoEntity destino) {
        OptionalInt maxCodigo = Stream.concat(pedidos.keySet().stream(), findAllByDestino(destino).stream().map(p -> p.getCodigo()))
                                      .filter(c -> c.startsWith(destino.getCodigo()))
                                      .mapToInt(c -> Integer.parseInt(c.substring(destino.getCodigo().length()))).max();
        return String.format("%s%09d", destino.getCodigo(), maxCodigo.orElse(0) + 1);
    }

    public ListResponse listar(ListRequest request) {
        try {
            int page = G4D.toAdmissibleValue(request.getPage(), 0);
            int size = G4D.toAdmissibleValue(request.getSize(), 10);
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            List<DTO> dtos = new ArrayList<>();
            List<PedidoEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(pedidoMapper.toDTO(entity)));
            return new ListResponse(true, "Pedidos listados correctamente!", dtos);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public ListResponse filtrar(FilterRequest<PedidoDTO> request) {
        try {
            int page = G4D.toAdmissibleValue(request.getPage(), 0);
            int size = G4D.toAdmissibleValue(request.getSize(), 10);
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            PedidoDTO model = request.getFilterModel();
            String tipoEscenario = G4D.toAdmissibleValue(model.getTipoEscenario());
            String codCliente = G4D.toAdmissibleValue(model.getCodCliente());
            Boolean fueAtendido = model.getFueAtendido();
            LocalDateTime fechaHoraGeneracion = G4D.toAdmissibleValue(model.getFechaHoraGeneracion(), (LocalDateTime) null);
            LocalDateTime fechaHoraExpiracion = G4D.toAdmissibleValue(model.getFechaHoraExpiracion(), (LocalDateTime) null);
            List<DTO> dtos = new ArrayList<>();
            List<PedidoEntity> entities = pedidoRepository.filterBy(tipoEscenario, codCliente, fueAtendido, fechaHoraGeneracion, fechaHoraExpiracion, pageable).getContent();
            entities.forEach(entity -> dtos.add(pedidoMapper.toDTO(entity)));
            return new ListResponse(true, "Pedidos filtrados correctamente!", dtos);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - FILTRADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportRequest<PedidoDTO> request) {
        try {
            G4D.Logger.logln("Cargando pedido..");
            PedidoDTO dto = request.getDto();
            PedidoEntity pedido = pedidoMapper.toEntity(dto);
            pedido.setCodigo(this.obtenerNuevoCodigo(pedido.getDestino()));
            this.save(pedido);
            G4D.Logger.logln("[<] PEDIDO CARGADO!");
            return new GenericResponse(true, "Pedido importado correctamente!");
        } catch (Exception e) {
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportListRequest<PedidoDTO> request) {
        try {
            G4D.Logger.logln("Cargando pedidos..");
            List<PedidoDTO> dtos = request.getDtos();
            for(PedidoDTO dto : dtos) {
                PedidoEntity pedido = pedidoMapper.toEntity(dto);
                if(pedido != null) {
                    pedido.setCodigo(this.obtenerNuevoCodigo(pedido.getDestino()));
                    pedidos.put(pedido.getCodigo(), pedido);
                }
            }
            pedidos.values().forEach(this::save);
            G4D.Logger.logf("[<] PEDIDOS CARGADOS! ('%d')%n",  pedidos.size());
            return new GenericResponse(true, "Pedidos importados correctamente!");
        } catch (Exception e) {
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo, ImportFileRequest request) {
        try {
            G4D.Logger.logf("Cargando pedidos desde '%s'..%n",archivo.getName());
            LocalDateTime fechaHoraInicio = G4D.toAdmissibleValue(request.getFechaHoraInicio(), LocalDateTime.MIN);
            LocalDateTime fechaHoraFin = G4D.toAdmissibleValue(request.getFechaHoraFin(), LocalDateTime.MAX);
            if(fechaHoraFin.isBefore(fechaHoraInicio)) throw new Exception("Rango de tiempo inválido.");
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            int numLinea = 1;
            TipoEscenario escenario = G4D.toAdmissibleValue(request.getTipoEscenario(), TipoEscenario.class);
            boolean tieneNumeroDePedido = escenario.equals(TipoEscenario.SIMULACION);
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                PedidoEntity pedido = new PedidoEntity();
                String numPed = (tieneNumeroDePedido) ? lineaSC.next() : null;
                LocalDateTime fechaHoraGeneracionLocal = LocalDateTime.of(
                        G4D.toDate(lineaSC.nextInt()),
                        LocalTime.of(
                                lineaSC.nextInt(),
                                lineaSC.nextInt(),
                                0
                        )
                );
                String codDestino = lineaSC.next();
                AeropuertoEntity destino = aeropuertoService.obtenerPorCodigo(codDestino);
                if(destino != null) {
                    LocalDateTime fechaHoraGeneracionUTC = G4D.toUTC(fechaHoraGeneracionLocal, destino.getHusoHorario());
                    if(!fechaHoraGeneracionUTC.isBefore(fechaHoraInicio) && !fechaHoraGeneracionUTC.isAfter(fechaHoraFin)) {
                        if(numPed != null) {
                            pedido.setCodigo(destino.getCodigo() + numPed);
                            pedido.setFechaHoraProcesamientoLocal(fechaHoraGeneracionLocal);
                            pedido.setFechaHoraProcesamientoUTC(fechaHoraGeneracionUTC);
                        } else pedido.setCodigo(this.obtenerNuevoCodigo(destino));
                        pedido.setDestino(destino);
                        pedido.setCantidadSolicitada(lineaSC.nextInt());
                        ClienteEntity cliente = clienteService.obtenerPorCodigo(lineaSC.next());
                        pedido.setCliente(cliente);
                        pedido.setFechaHoraGeneracionLocal(fechaHoraGeneracionLocal);
                        pedido.setFechaHoraGeneracionUTC(fechaHoraGeneracionUTC);
                        pedido.setTipoEscenario(escenario);
                        pedidos.put(pedido.getCodigo(), pedido);
                    }
                } else throw new Exception(String.format("El destino del pedido de la linea #%d es inválido. ('%s')", numLinea, codDestino));
                lineaSC.close();
                numLinea++;
            }
            archivoSC.close();
            pedidos.values().forEach(this::save);
            G4D.Logger.logf("[<] PEDIDOS CARGADOS! ('%d')%n", pedidos.size());
            return new GenericResponse(true, "Pedidos importados correctamente!");
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", archivo.getName());
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } catch (Exception e) {
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        pedidos.clear();
        aeropuertoService.clearPools();
        clienteService.clearPools();
        pedidoMapper.clearPools();
    }
}
