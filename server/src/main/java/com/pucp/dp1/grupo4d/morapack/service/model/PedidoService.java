/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.PedidoMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.PedidoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final AeropuertoService aeropuertoService;
    private final ClienteService clienteService;
    private final PedidoMapper pedidoMapper;
    private final List<PedidoEntity> pedidos =  new ArrayList<>();

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

    public Optional<PedidoEntity> findByCodigoEscenario(String codigo, String tipoEscenario) {
        TipoEscenario escenario = G4DUtility.Convertor.toAdmissible(tipoEscenario, TipoEscenario.class);
        return pedidoRepository.findByCodigoEscenario(codigo, escenario);
    }

    public boolean existsByCodigoEscenario(String codigo, String tipoEscenario) {
        TipoEscenario escenario = G4DUtility.Convertor.toAdmissible(tipoEscenario, TipoEscenario.class);
        return pedidoRepository.findByCodigoEscenario(codigo, escenario).isPresent();
    }

    public List<PedidoEntity> findAllByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario) {
        TipoEscenario escenario = G4DUtility.Convertor.toAdmissible(tipoEscenario, TipoEscenario.class);
        return pedidoRepository.findAllByDateTimeRange(fechaHoraInicio, fechaHoraFin, escenario);
    }

    public List<PedidoEntity> findAllByDestino(AeropuertoEntity destino) {
        return pedidoRepository.findAllByDestino(destino);
    }

    public String obtenerNuevoCodigo(AeropuertoEntity destino) {
        OptionalInt maxCodigo = OptionalInt.empty();
        if(!pedidos.isEmpty()) {
            maxCodigo = pedidos.stream().filter(entity -> entity.getDestino().getCodigo().equals(destino.getCodigo())).mapToInt(entity -> Integer.parseInt(entity.getCodigo().substring(4))).max();
        }
        if(maxCodigo.isEmpty()) {
            maxCodigo = this.findAllByDestino(destino).stream().mapToInt(entity -> Integer.parseInt(entity.getCodigo().substring(4))).max();
        }
        return String.format("%s%09d", destino.getCodigo(), maxCodigo.orElse(0) + 1);
    }

    public ListResponse listar(ListRequest request) throws Exception{
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
            List<DTO> dtos = new ArrayList<>();
            List<PedidoEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(pedidoMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Pedidos listados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public ListResponse filtrar(FilterRequest<PedidoDTO> request) throws Exception {
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
            PedidoDTO modelo = request.getModelo();
            TipoEscenario tipoEscenario = G4DUtility.Convertor.toAdmissible(modelo.getTipoEscenario(),  TipoEscenario.class);
            String codCliente = G4DUtility.Convertor.toAdmissible(modelo.getCodCliente());
            Boolean fueAtendido = modelo.getFueAtendido();
            LocalDateTime fechaHoraGeneracion = G4DUtility.Convertor.toAdmissible(modelo.getFechaHoraGeneracion(), (LocalDateTime) null);
            LocalDateTime fechaHoraExpiracion = G4DUtility.Convertor.toAdmissible(modelo.getFechaHoraExpiracion(), (LocalDateTime) null);
            List<DTO> dtos = new ArrayList<>();
            List<PedidoEntity> entities = pedidoRepository.filterBy(tipoEscenario, codCliente, fueAtendido, fechaHoraGeneracion, fechaHoraExpiracion, pageable).getContent();
            entities.forEach(entity -> dtos.add(pedidoMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Pedidos filtrados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportRequest<PedidoDTO> request) throws Exception{
        try {
            System.out.println("Importando pedido..");
            PedidoDTO dto = request.getDto();
            PedidoEntity pedido = pedidoMapper.toEntity(dto);
            pedido.setCodigo(this.obtenerNuevoCodigo(pedido.getDestino()));
            this.save(pedido);
            System.out.println("[<] PEDIDO IMPORTADO!");
            return new GenericResponse(true, "Pedido importado correctamente!");
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo, ImportFileRequest request) throws Exception {
        try {
            G4DUtility.Logger.logf("Importando pedidos desde '%s'..%n",archivo.getName());
            LocalDateTime fechaHoraInicio = G4DUtility.Convertor.toAdmissible(request.getFechaHoraInicio(), LocalDateTime.MIN);
            LocalDateTime fechaHoraFin = G4DUtility.Convertor.toAdmissible(request.getFechaHoraFin(), LocalDateTime.MAX);
            if(fechaHoraFin.isBefore(fechaHoraInicio)) {
                throw new G4DException("Rango de tiempo inválido.");
            }
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4DUtility.Reader.getFileCharset(archivo));
            TipoEscenario escenario = G4DUtility.Convertor.toAdmissible(request.getTipoEscenario(), TipoEscenario.class);
            boolean tieneNumeroDePedido = escenario.equals(TipoEscenario.SIMULACION);
            int numLinea = 1;
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                PedidoEntity pedido = new PedidoEntity();
                String numPed = (tieneNumeroDePedido) ? lineaSC.next() : null;
                LocalDateTime fechaHoraGeneracionLocal = LocalDateTime.of(
                        G4DUtility.Convertor.toDate(lineaSC.nextInt()),
                        LocalTime.of(
                                lineaSC.nextInt(),
                                lineaSC.nextInt(),
                                0
                        )
                );
                String codDestino = lineaSC.next();
                AeropuertoEntity destino = aeropuertoService.obtenerPorCodigo(codDestino);
                if(destino != null) {
                    LocalDateTime fechaHoraGeneracionUTC = G4DUtility.Convertor.toUTC(fechaHoraGeneracionLocal, destino.getHusoHorario());
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
                        pedidos.add(pedido);
                    }
                } else throw new G4DException(String.format("El destino '%s' del pedido de la linea #%d es inválido.", codDestino, numLinea));
                lineaSC.close();
                numLinea++;
            }
            archivoSC.close();
            pedidos.stream().filter(entity -> !this.existsByCodigoEscenario(entity.getCodigo(), escenario.toString())).forEach(this::save);
            System.out.printf("[<] PEDIDOS IMPORTADOS! ('%d')%n", pedidos.size());
            return new GenericResponse(true, String.format("Pedidos importados correctamente! ('%d')", pedidos.size()));
        } catch (NoSuchElementException | FileNotFoundException e) {
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado o está vacío.", archivo.getName()));
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
