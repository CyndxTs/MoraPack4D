package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.algorithm.GVNS;
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ParametersRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.PlanificationRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ImportResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.PlanificationResponse;
import com.pucp.dp1.grupo4d.morapack.service.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlgorithmService {

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private PlanService planService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private AdministradorService administradorService;

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
        }
        Problematica problematica = new Problematica();
        // ..
        GVNS gvns = new GVNS();
        // gvns.planificar(problematica);
        // gvns.getSolucionVNS();
        return new PlanificationResponse(true, "Planificación correctamene concluida.");
    }

    public void replanificar() {

    }
}
