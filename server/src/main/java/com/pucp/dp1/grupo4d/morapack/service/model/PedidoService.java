/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.PedidoMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportFileRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoPedido;
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

    public List<PedidoEntity> findAllByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoDePedidos) {
        return  pedidoRepository.findAllByDateTimeRange(fechaHoraInicio, fechaHoraFin, tipoDePedidos);
    }

    public List<PedidoEntity> findAllByDestino(AeropuertoEntity destino) {
        return pedidoRepository.findAllByDestino(destino);
    }

    private String obtenerNuevoCodigo(AeropuertoEntity destino) {
        OptionalInt maxCodigo = this.findAllByDestino(destino).stream().mapToInt(p -> Integer.parseInt(p.getCodigo().substring(4))).max();
        return String.format("%s%09d",destino.getCodigo(), maxCodigo.orElse(0) + 1);
    }

    public ListResponse listar(ListRequest request) {
        try {
            int page = (G4D.isAdmissible(request.getPage())) ? request.getPage() : 0;
            int size = (G4D.isAdmissible(request.getSize())) ? request.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            List<DTO> pedidosDTO = new ArrayList<>();
            List<PedidoEntity> pedidosEntity = this.findAll(pageable);
            pedidosEntity.forEach(p -> pedidosDTO.add(pedidoMapper.toDTO(p)));
            return new ListResponse(true, "Pedidos listados correctamente!", pedidosDTO);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportRequest<PedidoDTO> request) {
        try {
            G4D.Logger.logln("Cargando pedido..");
            PedidoDTO dto = request.getDto();
            PedidoEntity pedido = new PedidoEntity();
            String codCliente = dto.getCodCliente();
            String codDestino = dto.getCodDestino();
            AeropuertoEntity destino = aeropuertoService.obtenerPorCodigo(codDestino);
            if(destino != null) {
                String codPedido = obtenerNuevoCodigo(destino);
                pedido.setCodigo(codPedido);
                ClienteEntity cliente = clienteService.obtenerPorCodigo(codCliente);
                pedido.setCliente(cliente);
                pedido.setDestino(destino);
                pedido.setCantidadSolicitada(dto.getCantidadSolicitada());
                pedido.setFechaHoraGeneracionUTC(G4D.toDateTime(dto.getFechaHoraGeneracion()));
                pedido.setFechaHoraGeneracionLocal(G4D.toLocal(pedido.getFechaHoraGeneracionUTC(), destino.getHusoHorario()));
                pedido.setFechaHoraExpiracionUTC(null);
                pedido.setFechaHoraExpiracionLocal(null);
                pedido.setEstado(EstadoPedido.NO_ATENDIDO);
                this.save(pedido);
                G4D.Logger.logln("[<] PEDIDO CARGADO!");
                return new GenericResponse(true, "Pedido importado correctamente!");
            } else throw new Exception(String.format("El destino del pedido es inválido. ('%s')", codDestino));
        } catch (Exception e) {
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo, ImportFileRequest request) {
        try {
            G4D.Logger.logf("Cargando pedidos desde '%s'..%n",archivo.getName());
            LocalDateTime fechaHoraInicio = G4D.toDateTime(request.getFechaHoraInicio());
            LocalDateTime fechaHoraFin = G4D.toDateTime(request.getFechaHoraFin());
            if(fechaHoraFin.isBefore(fechaHoraInicio)) throw new Exception("Rango de tiempo inválido.");
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            int numLinea = 1;
            boolean tieneNumeroDePedido = request.getTipoArchivo().equals("SIMULACION");
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
                        pedido.setCodigo((numPed != null) ? destino.getCodigo() + numPed : this.obtenerNuevoCodigo(destino));
                        pedido.setDestino(destino);
                        pedido.setCantidadSolicitada(lineaSC.nextInt());
                        ClienteEntity cliente = clienteService.obtenerPorCodigo(lineaSC.next());
                        pedido.setCliente(cliente);
                        pedido.setFechaHoraGeneracionLocal(fechaHoraGeneracionLocal);
                        pedido.setFechaHoraGeneracionUTC(fechaHoraGeneracionUTC);
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
        aeropuertoService.clearPools();
        clienteService.clearPools();
        pedidoMapper.clearPools();
    }
}
