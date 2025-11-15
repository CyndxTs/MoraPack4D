/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PedidoDTO;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.repository.PedidoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class PedidoService {

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private ClienteService clienteService;

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<PedidoEntity> findAll() {
        return pedidoRepository.findAll();
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

    public List<PedidoEntity> findByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, Integer desfaseDeDias) {
        return  pedidoRepository.findByDateTimeRange(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
    }

    public void importar(MultipartFile archivo, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        List<PedidoEntity> pedidos = new ArrayList<>();
        try {
            G4D.Logger.logf("Cargando pedidos desde '%s'..%n",archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                PedidoEntity pedido = new PedidoEntity();
                String numPed = lineaSC.next();
                LocalDateTime fechaHoraGeneracionLocal = LocalDateTime.of(
                        G4D.toDate(lineaSC.nextInt()),
                        LocalTime.of(
                                lineaSC.nextInt(),
                                lineaSC.nextInt(),
                                0
                        )
                );
                AeropuertoEntity aDest = aeropuertoService.findByCodigo(lineaSC.next()).orElse(null);
                if(aDest != null) {
                    LocalDateTime fechaHoraGeneracionUTC = G4D.toUTC(fechaHoraGeneracionLocal, aDest.getHusoHorario());
                    if(!fechaHoraGeneracionUTC.isBefore(fechaHoraInicio) && !fechaHoraGeneracionUTC.isAfter(fechaHoraFin)) {
                        pedido.setCodigo(aDest.getCodigo() + numPed);
                        pedido.setDestino(aDest);
                        pedido.setCantidadSolicitada(lineaSC.nextInt());
                        String codCliente = lineaSC.next();
                        ClienteEntity cliente = clienteService.findByCodigo(codCliente).orElse(null);
                        if(cliente == null) {
                            cliente = new ClienteEntity();
                            cliente.setCodigo(codCliente);
                            cliente.setNombre(G4D.Generator.getUniqueName());
                            String correo = G4D.Generator.getUniqueEmail();
                            boolean existeCorreo = clienteService.existsByCorreo(correo);
                            if(existeCorreo) {
                                String newCorreo = "";
                                while (existeCorreo) {
                                    newCorreo = G4D.Generator.addRandomInteger(correo, correo.indexOf('@'));
                                    existeCorreo = clienteService.existsByCorreo(newCorreo);
                                }
                                cliente.setCorreo(newCorreo);
                            } else cliente.setCorreo(correo);
                            cliente.setContrasenia("12345678");
                            clienteService.save(cliente);
                        }
                        pedido.setCliente(cliente);
                        pedido.setFechaHoraGeneracionLocal(fechaHoraGeneracionLocal);
                        pedido.setFechaHoraGeneracionUTC(fechaHoraGeneracionUTC);
                        pedidos.add(pedido);
                    }
                }
                lineaSC.close();
            }
            archivoSC.close();
            pedidos.removeIf(p -> this.existsByCodigo(p.getCodigo()));
            pedidos.sort(Comparator.comparing(PedidoEntity::getFechaHoraGeneracionUTC));
            pedidos.forEach(this::save);
            G4D.Logger.logf("[<] PEDIDOS CARGADOS! ('%d')%n", pedidos.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void importar(List<DTO> dtos) {
        List<PedidoEntity> pedidos = new ArrayList<>();
        try {
            G4D.Logger.logln("Cargando pedidos desde lista..");
            for (DTO dto : dtos) {
                PedidoDTO pedidoDTO = (PedidoDTO)  dto;
                PedidoEntity pedido = new PedidoEntity();
                pedido.setCodigo(pedidoDTO.getCodigo());
                String codCliente = pedidoDTO.getCodCliente();
                ClienteEntity cliente = clienteService.findByCodigo(codCliente).orElse(null);
                if(cliente != null) {
                    String codDestino = pedidoDTO.getCodDestino();
                    AeropuertoEntity destino = aeropuertoService.findByCodigo(codDestino).orElse(null);
                    if(destino != null) {
                        pedido.setCliente(cliente);
                        pedido.setDestino(destino);
                        pedido.setCantidadSolicitada(pedidoDTO.getCantidadSolicitada());
                        pedido.setFechaHoraGeneracionUTC(G4D.toDateTime(pedidoDTO.getFechaHoraGeneracion()));
                        pedido.setFechaHoraGeneracionLocal(G4D.toLocal(pedido.getFechaHoraGeneracionUTC(), destino.getHusoHorario()));
                        pedido.setFechaHoraExpiracionUTC(null);
                        pedido.setFechaHoraExpiracionLocal(null);
                        pedido.setFueAtendido(false);
                        pedidos.add(pedido);
                    }
                }
            }
            pedidos.removeIf(p -> this.existsByCodigo(p.getCodigo()));
            pedidos.sort(Comparator.comparing(PedidoEntity::getFechaHoraGeneracionUTC));
            pedidos.forEach(this::save);
            G4D.Logger.logf("[<] PEDIDOS CARGADOS! ('%d')%n", pedidos.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
