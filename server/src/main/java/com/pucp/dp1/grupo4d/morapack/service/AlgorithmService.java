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
import com.pucp.dp1.grupo4d.morapack.model.entity.*;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public ImportResponse importarDesdeArchivo(MultipartFile file, String type) {
        try {
            switch (type.toUpperCase()) {
                case "AEROPUERTOS":
                    aeropuertoService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Aeropuertos importados correctamente.");

                case "PLANES":
                    planService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Planes importados correctamente");

                case "CLIENTES":
                    clienteService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Clientes importados correctamente");

                case "PEDIDOS":
                    pedidoService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Pedidos importados correctamente");

                case "ADMINISTRADORES":
                    administradorService.importarDesdeArchivo(file);
                    return new ImportResponse(true, "Administradores importados correctamente");
                default:
                    return new ImportResponse(false, "Tipo de archivo no inválido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ImportResponse(false, "ERROR - IMPORT: " + e.getMessage());
        }
    }

    public PlanificationResponse planificar(PlanificationRequest request) {
        if(request.getReparametrizar()) {
            ParametersRequest parameters = request.getParameters();
            Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL = parameters.getMaxDiasEntregaIntracontinental();
            Problematica.MAX_DIAS_ENTREGA_INTERCONTINENTAL = parameters.getMaxDiasEntregaIntercontinental();
            Problematica.MAX_HORAS_RECOJO = parameters.getMaxHorasRecojo();
            Problematica.MAX_HORAS_ESTANCIA = parameters.getMaxHorasEstancia();
            Problematica.MIN_HORAS_ESTANCIA = parameters.getMinHorasEstancia();
            GVNS.L_MIN = parameters.getEleMin();
            GVNS.L_MAX = parameters.getEleMax();
            GVNS.K_MIN = parameters.getkMin();
            GVNS.K_MAX = parameters.getkMax();
            GVNS.T_MAX = parameters.gettMax();
            GVNS.MAX_INTENTOS = parameters.getMaxIntentos();
            Solucion.f_UA = parameters.getFactorDeUmbralDeAberracion();
            Solucion.f_UT = parameters.getFactorDeUtilizacionTemporal();
            Solucion.f_DE = parameters.getFactorDeDesviacionEspacial();
            Solucion.f_DO = parameters.getFactorDeDisposicionOperacional();
        }
        Problematica problematica = new Problematica(aeropuertoService,clienteService,planService,pedidoService,aeropuertoAdapter,clienteAdapter,planAdapter,pedidoAdapter);
        problematica.cargarDatos();
        GVNS gvns = new GVNS();
        gvns.planificar(problematica);
        Solucion sAux = gvns.getSolucionINI();
        actualizarPorSolucion(sAux);
        return new PlanificationResponse(true, "Planificación correctamene concluida.");
    }

    @Transactional
    public void actualizarPorSolucion(Solucion solucion) {
        if (solucion == null || solucion.getPedidosAtendidos() == null) {
            return;
        }

        for (Pedido pedidoAlg : solucion.getPedidosAtendidos()) {
            System.out.println("CONVIRTIENDO PEDIDO");
            PedidoEntity pedidoEntity = pedidoAdapter.toEntity(pedidoAlg);
            if (pedidoEntity == null) {
                continue;
            }
            System.out.println("GUARDANDO PEDIDO");
            pedidoService.save(pedidoEntity);
            System.out.println("PEDIDO GUARDADO");
        }

        System.out.println("Solución actualizada correctamente en la base de datos.");
    }
}
