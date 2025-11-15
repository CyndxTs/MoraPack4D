/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AlgorithmService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.adapter.*;
import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.algorithm.Solucion;
import com.pucp.dp1.grupo4d.morapack.mapper.*;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FileImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.PlanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AlgorithmService {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoAdapter pedidoAdapter;

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private AeropuertoAdapter aeropuertoAdapter;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private UsuarioAdapter usuarioAdapter;

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanAdapter planAdapter;

    @Autowired
    private LoteService loteService;

    @Autowired
    private LoteAdapter loteAdapter;

    @Autowired
    private RutaService rutaService;

    @Autowired
    private RutaAdapter rutaAdapter;

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private VueloAdapter vueloAdapter;

    @Autowired
    private VueloService vueloService;

    @Autowired
    private RegistroAdapter registroAdapter;

    @Autowired
    private ParametrosService parametrosService;

    @Autowired
    private ParametrosMapper parametrosMapper;

    @Autowired
    private PedidoMapper pedidoMapper;

    @Autowired
    private AeropuertoMapper aeropuertoMapper;

    @Autowired
    private VueloMapper vueloMapper;

    @Autowired
    private RutaMapper rutaMapper;

    public GenericResponse importarDesdeArchivo(MultipartFile file, FileImportRequest request) {
        try {
            switch (request.getTipoArchivo()) {
                case "PEDIDOS":
                    String fechaHoraInicio = request.getFechaHoraInicio();
                    LocalDateTime fechaHoraInicioImport = (fechaHoraInicio == null || fechaHoraInicio.isBlank()) ? G4D.toDateTime("1999-12-31 23:59:59") : G4D.toDateTime(fechaHoraInicio);
                    String fechaHoraFin = request.getFechaHoraFin();
                    LocalDateTime fechaHoraFinImport = (fechaHoraFin == null || fechaHoraFin.isBlank()) ? LocalDateTime.now() :  G4D.toDateTime(fechaHoraFin);
                    Integer desfaseTemporal = request.getDesfaseTemporal();
                    desfaseTemporal = (desfaseTemporal == null || desfaseTemporal < 0) ? 0 : desfaseTemporal;
                    fechaHoraInicioImport = fechaHoraInicioImport.minusDays(desfaseTemporal);
                    fechaHoraFinImport = fechaHoraFinImport.plusDays(desfaseTemporal);
                    pedidoService.importar(file, fechaHoraInicioImport, fechaHoraFinImport);
                    return new GenericResponse(true, "Pedidos importados correctamente!");
                case "CLIENTES":
                    clienteService.importar(file);
                    return new GenericResponse(true, "Clientes importados correctamente!");
                case "PLANES":
                    planService.importar(file);
                    return new GenericResponse(true, "Planes de vuelo importados correctamente!");
                case "AEROPUERTOS":
                    aeropuertoService.importar(file);
                    return new GenericResponse(true, "Aeropuertos importados correctamente!");
                case "ADMINISTRADORES":
                    administradorService.importar(file);
                    return new GenericResponse(true, "Administradores importados correctamente!");
                default:
                    return new GenericResponse(false, "Tipo de archivo inválido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        }
    }

    public GenericResponse importarDesdeLista(ListImportRequest request) {
        try {
            switch (request.getTipoDtos()) {
                case "PEDIDOS":
                    pedidoService.importar(request.getDtos());
                    return new GenericResponse(true, "Pedidos importados correctamente!");
                default:
                    return new GenericResponse(false, "Tipo de archivo inválido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        }
    }

    public SolutionResponse planificar(PlanificationRequest request) {
        try {
            boolean guardarPlanificacion = request.getGuardarPlanificacion();
            boolean replanificar = request.getReplanificar();
            if(request.getReparametrizar()) {
                ParametrosDTO parametrosDTO = request.getParameters();
                parametrosMapper.toAlgorithm(parametrosDTO);
                if(request.getGuardarParametrizacion()) {
                    ParametrosEntity parametrosEntity = parametrosMapper.toEntity(parametrosDTO);
                    parametrosService.save(parametrosEntity);
                }
            } else {
                ParametrosEntity parametrosEntity = parametrosService.findById(1);
                parametrosMapper.toAlgorithm(parametrosEntity);
                if(request.getGuardarParametrizacion()) {
                    parametrosService.save(parametrosEntity);
                }
            }
            Problematica problematica = new Problematica();
            problematica.cargarDatos(
                    aeropuertoService, aeropuertoAdapter,
                    clienteService, usuarioAdapter,
                    planService, planAdapter,
                    pedidoService, pedidoAdapter,
                    rutaService, rutaAdapter,
                    vueloService, vueloAdapter
            );
            GVNS gvns = new GVNS();
            gvns.planificar(problematica);
            if(gvns.getSolucionINI() == null) {
                return new SolutionResponse(false, "COLAPSO!");
            }
            Solucion solucion = gvns.getSolucionVNS();
            if(guardarPlanificacion) {
                guardarSolucion(solucion, problematica);
            }
            return devolverSolucion(solucion);
        } catch(Exception e) {
            e.printStackTrace();
            return new SolutionResponse(false, "ERROR - PLANIFICACIÓN: " + e.getMessage());
        } finally {
            limpiarPools();
        }
    }

    @Transactional
    public void guardarSolucion(Solucion solucion, Problematica problematica) {
        if (solucion == null || solucion.getPedidosAtendidos() == null) {
            return;
        }
        System.out.println("\nAlmacenando solución..\n");
        // Vuelos
        for (Vuelo vueloAlg : solucion.getVuelosEnTransito()) {
            VueloEntity vueloEntity = vueloAdapter.toEntity(vueloAlg);
            if (vueloEntity != null && vueloEntity.getId() == null) {
                vueloService.save(vueloEntity);
                System.out.println("[+] VUELO: " + vueloEntity.getCodigo());
            }
        }
        // Rutas
        for (Ruta rutaAlg : solucion.getRutasEnOperacion()) {
            RutaEntity rutaEntity = rutaAdapter.toEntity(rutaAlg);
            if (rutaEntity != null) {
                rutaEntity.getVuelos().clear();
                for (Vuelo vuelo : rutaAlg.getVuelos()) {
                    VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
                    if (vueloEntity != null) {
                        rutaEntity.getVuelos().add(vueloEntity);
                    }
                }
                if (rutaEntity.getId() == null) {
                    rutaService.save(rutaEntity);
                    System.out.println("[+] RUTA: " + rutaEntity.getCodigo() + " ('" + rutaEntity.getVuelos().size() + "' vuelos)");
                }
            }
        }
        // Pedidos & Lotes
        for (Pedido pedidoAlg : solucion.getPedidosAtendidos()) {
            PedidoEntity pedidoEntity = pedidoService.findByCodigo(pedidoAlg.getCodigo()).orElse(null);
            if (pedidoEntity == null) {
                continue;
            }
            pedidoEntity.setFechaHoraExpiracionLocal(pedidoAlg.getFechaHoraExpiracionLocal());
            pedidoEntity.setFechaHoraExpiracionUTC(pedidoAlg.getFechaHoraExpiracionUTC());
            pedidoEntity.getRutas().clear();
            pedidoEntity.getLotes().clear();
            for (Map.Entry<Ruta, Lote> entry : pedidoAlg.getLotesPorRuta().entrySet()) {
                Ruta rutaAlg = entry.getKey();
                Lote loteAlg = entry.getValue();
                RutaEntity rutaEntity = rutaAdapter.toEntity(rutaAlg);
                if (rutaEntity == null || rutaEntity.getId() == null) {
                    continue;
                }
                LoteEntity loteEntity = loteAdapter.toEntity(loteAlg);
                if (loteEntity == null) {
                    continue;
                }
                loteEntity.setRuta(rutaEntity);
                loteEntity.setPedido(pedidoEntity);
                if (!pedidoEntity.getRutas().contains(rutaEntity)) {
                    pedidoEntity.getRutas().add(rutaEntity);
                }
                pedidoEntity.getLotes().add(loteEntity);
            }
            pedidoService.save(pedidoEntity);
            System.out.println("[*] PEDIDO: " + pedidoEntity.getCodigo() + " ('" + pedidoEntity.getRutas().size() + "' rutas | '" + pedidoEntity.getLotes().size() + "' lotes)");
        }
        // Registros
        for (Aeropuerto aeropuertoAlg : problematica.destinos) {
            if (aeropuertoAlg == null || aeropuertoAlg.getRegistros().isEmpty()) {
                continue;
            }
            AeropuertoEntity aeropuertoEntity = aeropuertoService.findByCodigo(aeropuertoAlg.getCodigo()).orElse(null);
            if (aeropuertoEntity == null) {
                continue;
            }
            aeropuertoEntity.getRegistros().clear();
            for (Registro registroAlg : aeropuertoAlg.getRegistros()) {
                RegistroEntity registroEntity = new RegistroEntity();
                registroEntity.setCodigo(registroAlg.getCodigo());
                registroEntity.setFechaHoraIngresoLocal(registroAlg.getFechaHoraIngresoLocal());
                registroEntity.setFechaHoraIngresoUTC(registroAlg.getFechaHoraIngresoUTC());
                registroEntity.setFechaHoraEgresoLocal(registroAlg.getFechaHoraEgresoLocal());
                registroEntity.setFechaHoraEgresoUTC(registroAlg.getFechaHoraEgresoUTC());
                registroEntity.setAeropuerto(aeropuertoEntity);
                String codigoLote = registroAlg.getLote().getCodigo();
                LoteEntity loteEntity = loteService.findByCodigo(codigoLote).orElse(null);
                if (loteEntity == null) {
                    continue;
                }
                registroEntity.setLote(loteEntity);
                aeropuertoEntity.getRegistros().add(registroEntity);
            }
            if (!aeropuertoEntity.getRegistros().isEmpty()) {
                aeropuertoService.save(aeropuertoEntity);
                System.out.println("[*] AEROPUERTO: " + aeropuertoEntity.getCodigo() + " ('" + aeropuertoEntity.getRegistros().size() + "' registros)");
            }
        }
        System.out.println("\nSOLUCIÓN ALMACENADA!");
    }

    private SolutionResponse devolverSolucion(Solucion solucion) {
        List<PedidoDTO> pedidosDTO = new ArrayList<>();
        solucion.getPedidosAtendidos().forEach(p -> pedidosDTO.add(pedidoMapper.toDTO(p)));
        List<AeropuertoDTO> aeropuertosDTO = new ArrayList<>();
        aeropuertoService.findAll().forEach(a ->  aeropuertosDTO.add(aeropuertoMapper.toDTO(a)));
        List<VueloDTO> vuelosDTO = new ArrayList<>();
        solucion.getVuelosEnTransito().forEach(v -> vuelosDTO.add(vueloMapper.toDTO(v)));
        List<RutaDTO> rutasDTO = new ArrayList<>();
        solucion.getRutasEnOperacion().forEach(r -> rutasDTO.add(rutaMapper.toDTO(r)));
        return new SolutionResponse(true, "Planificación correctamente concluida!", pedidosDTO, aeropuertosDTO, vuelosDTO, rutasDTO);
    }

    private void limpiarPools() {
        vueloAdapter.clearPools();
        vueloMapper.clearPools();
        rutaAdapter.clearPools();
        rutaMapper.clearPools();
        loteAdapter.clearPools();
        pedidoAdapter.clearPools();
        pedidoMapper.clearPools();
        aeropuertoAdapter.clearPools();
        aeropuertoMapper.clearPools();
        usuarioAdapter.clearPools();
        planAdapter.clearPools();
        registroAdapter.clearPools();
    }
}
