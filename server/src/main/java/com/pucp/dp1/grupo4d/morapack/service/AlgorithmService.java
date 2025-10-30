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
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ParametersRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.PlanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ImportResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.PlanificationResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ProblematicResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.SolutionResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
    private ClienteAdapter clienteAdapter;

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

    public ImportResponse importarDesdeArchivo(MultipartFile file, String type) {
        try {
            switch (type.toUpperCase()) {
                case "PEDIDOS":
                    pedidoService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Pedidos importados correctamente!");
                case "CLIENTES":
                    clienteService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Clientes importados correctamente!");
                case "PLANES":
                    planService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Planes importados correctamente!");
                case "AEROPUERTOS":
                    aeropuertoService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Aeropuertos importados correctamente!");
                case "ADMINISTRADORES":
                    administradorService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Administradores importados correctamente!");
                default:
                    return new ImportResponse(false, "Tipo de archivo inválido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ImportResponse(false, "ERROR - IMPORT: " + e.getMessage());
        }
    }

    public PlanificationResponse planificar(PlanificationRequest request) {
        if(request.getReparametrizar()) {
            ParametersRequest parameters = request.getParameters();
            Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL = parameters.getMaxDiasEntregaIntercontinental();
            Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL = parameters.getMaxDiasEntregaIntercontinental();
            Problematica.MAX_HORAS_RECOJO = parameters.getMaxHorasRecojo();
            Problematica.MAX_HORAS_ESTANCIA = parameters.getMaxHorasEstancia();
            Problematica.MIN_HORAS_ESTANCIA = parameters.getMinHorasEstancia();
            Problematica.CODIGOS_DE_ORIGENES = parameters.getCodigosDeOrigenes();
            GVNS.L_MIN = parameters.getEleMin();
            GVNS.L_MAX = parameters.getEleMax();
            GVNS.K_MIN = parameters.getKMin();
            GVNS.K_MAX = parameters.getKMax();
            GVNS.T_MAX = parameters.getTMax();
            GVNS.MAX_INTENTOS = parameters.getMaxIntentos();
            Solucion.f_UA = parameters.getFactorDeUmbralDeAberracion();
            Solucion.f_UT = parameters.getFactorDeUtilizacionTemporal();
            Solucion.f_DE = parameters.getFactorDeDesviacionEspacial();
            Solucion.f_DO = parameters.getFactorDeDisposicionOperacional();
        }
        Problematica problematica = new Problematica(
                aeropuertoService, clienteService, planService, pedidoService,
                aeropuertoAdapter, clienteAdapter, planAdapter, pedidoAdapter
        );
        problematica.cargarDatos();
        ProblematicResponse pAux = new ProblematicResponse(problematica);
        GVNS gvns = new GVNS();
        gvns.planificar(problematica);
        Solucion solucion = gvns.getSolucionVNS();
        SolutionResponse sAux = new SolutionResponse(solucion);
        almacenarSolucion(solucion, problematica);
        problematica.limpiarPools();
        vueloAdapter.clearPools();
        rutaAdapter.clearPools();
        loteAdapter.clearPools();
        return new PlanificationResponse(true, "Planificación correctamente concluida.", sAux, pAux);
    }

    @Transactional
    public void almacenarSolucion(Solucion solucion, Problematica problematica) {
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
}
