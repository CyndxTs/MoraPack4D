/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PedidoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.repository.PedidoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
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

    public Optional<PedidoEntity> findByCodigo(String codigo) {
        return pedidoRepository.findByCodigo(codigo);
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

    public boolean existsByCodigo(String codigo) {
        return pedidoRepository.findByCodigo(codigo).isPresent();
    }

    public void importarDesdeArchivo(MultipartFile archivo) {
        String linea;
        Scanner archivoSC = null, lineaSC;
        List<PedidoEntity> pedidos = new ArrayList<>();
        try {
            G4D.Logger.logf("Cargando pedidos desde '%s'..%n",archivo.getName());
            archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                PedidoEntity pedido = new PedidoEntity();
                LocalDateTime fechaHoraCreacionLocal = LocalDateTime.of(
                        LocalDate.now().withDayOfMonth(lineaSC.nextInt()),
                        LocalTime.of(
                                lineaSC.nextInt(),
                                lineaSC.nextInt(),
                                0
                        )
                );
                AeropuertoEntity aDest = aeropuertoService.findByCodigo(lineaSC.next()).orElse(null);
                if(aDest != null) {
                    pedido.setDestino(aDest);
                    pedido.setCantidadSolicitada(lineaSC.nextInt());
                    ClienteEntity cliente = clienteService.findByCodigo(lineaSC.next()).orElse(null);
                    if(cliente != null) {
                        pedido.setCliente(cliente);
                        pedido.setFechaHoraGeneracionLocal(fechaHoraCreacionLocal);
                        pedido.setFechaHoraGeneracionUTC(G4D.toUTC(fechaHoraCreacionLocal, pedido.getDestino().getHusoHorario()));
                        pedido.setCodigo(G4D.Generator.getUniqueString("PED"));
                        pedidos.add(pedido);
                    }
                }
                lineaSC.close();
            }
            pedidos.sort(Comparator.comparing(PedidoEntity::getFechaHoraGeneracionUTC));
            pedidos.forEach(this::save);
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
        G4D.Logger.logf("[<] PEDIDOS CARGADOS! ('%d' pedidos )%n", pedidos.size());
    }
}
