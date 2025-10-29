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

import java.util.HashMap;
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
    @Autowired
    private RegistroAdapter registroAdapter;

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
            Problematica.MAX_DIAS_ENTREGA_INTRACONTINENTAL = parameters.getMaxDiasEntregaIntercontinental();
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

        Problematica problematica = new Problematica(
                aeropuertoService, clienteService, planService, pedidoService,
                aeropuertoAdapter, clienteAdapter, planAdapter, pedidoAdapter
        );
        problematica.cargarDatos();

        GVNS gvns = new GVNS();
        gvns.planificar(problematica);

        Solucion solucion = gvns.getSolucionINI();
        actualizarPorSolucion(solucion, problematica);

        // Limpiar pools después de persistir
        problematica.limpiarPools();
        vueloAdapter.clearPools();
        rutaAdapter.clearPools();
        loteAdapter.clearPools();

        return new PlanificationResponse(true, "Planificación correctamente concluida.");
    }

    @Transactional
    public void actualizarPorSolucion(Solucion solucion, Problematica problematica) {
        if (solucion == null || solucion.getPedidosAtendidos() == null) {
            return;
        }

        System.out.println("\n════════════════════════════════════════════════");
        System.out.println("   INICIANDO PERSISTENCIA DE SOLUCIÓN");
        System.out.println("════════════════════════════════════════════════\n");

        // ============================================
        // FASE 1: PERSISTIR VUELOS
        // ============================================
        System.out.println("┌─ FASE 1: Persistiendo Vuelos");
        System.out.println("│");

        int contadorVuelos = 0;
        for (Vuelo vueloAlg : solucion.getVuelosEnTransito()) {
            VueloEntity vueloEntity = vueloAdapter.toEntity(vueloAlg);
            if (vueloEntity != null && vueloEntity.getId() == null) {
                vueloService.save(vueloEntity);
                contadorVuelos++;
                System.out.println("│  ✓ Vuelo: " + vueloEntity.getCodigo());
            }
        }

        System.out.println("│");
        System.out.println("└─ Total vuelos: " + contadorVuelos);
        System.out.println();

        // ============================================
        // FASE 2: PERSISTIR RUTAS (con vuelos ya persistidos)
        // ============================================
        System.out.println("┌─ FASE 2: Persistiendo Rutas");
        System.out.println("│");

        int contadorRutas = 0;
        for (Ruta rutaAlg : solucion.getRutasEnOperacion()) {
            RutaEntity rutaEntity = rutaAdapter.toEntity(rutaAlg);
            if (rutaEntity != null) {
                // Asignar vuelos (ya tienen ID del pool)
                rutaEntity.getVuelos().clear();
                for (Vuelo vuelo : rutaAlg.getVuelos()) {
                    VueloEntity vueloEntity = vueloAdapter.toEntity(vuelo);
                    if (vueloEntity != null) {
                        rutaEntity.getVuelos().add(vueloEntity);
                    }
                }

                if (rutaEntity.getId() == null) {
                    rutaService.save(rutaEntity);
                    contadorRutas++;
                    System.out.println("│  ✓ Ruta: " + rutaEntity.getCodigo() +
                            " (vuelos: " + rutaEntity.getVuelos().size() + ")");
                }
            }
        }

        System.out.println("│");
        System.out.println("└─ Total rutas: " + contadorRutas);
        System.out.println();

        // ============================================
        // FASE 3: PERSISTIR PEDIDOS CON LOTES (todo junto)
        // ============================================
        System.out.println("┌─ FASE 3: Persistiendo Pedidos con Lotes");
        System.out.println("│");

        int contadorPedidos = 0;
        int contadorLotes = 0;

        for (Pedido pedidoAlg : solucion.getPedidosAtendidos()) {
            System.out.println("│  → Procesando: " + pedidoAlg.getCodigo());

            // Convertir pedido (sin relaciones todavía)
            PedidoEntity pedidoEntity = pedidoService.findByCodigo(pedidoAlg.getCodigo()).orElse(null);
            if (pedidoEntity == null) {
                System.out.println("│    ✗ No encontrado en BD");
                continue;
            }

            // Actualizar fechas de expiración
            pedidoEntity.setFechaHoraExpiracionLocal(pedidoAlg.getFechaHoraExpiracionLocal());
            pedidoEntity.setFechaHoraExpiracionUTC(pedidoAlg.getFechaHoraExpiracionUTC());

            // Limpiar relaciones anteriores
            pedidoEntity.getRutas().clear();
            pedidoEntity.getLotes().clear();

            // Procesar cada lote por ruta
            for (Map.Entry<Ruta, Lote> entry : pedidoAlg.getLotesPorRuta().entrySet()) {
                Ruta rutaAlg = entry.getKey();
                Lote loteAlg = entry.getValue();

                // Obtener ruta del pool (ya persistida con ID)
                RutaEntity rutaEntity = rutaAdapter.toEntity(rutaAlg);
                if (rutaEntity == null || rutaEntity.getId() == null) {
                    System.out.println("│    ✗ Ruta sin ID: " + rutaAlg.getCodigo());
                    continue;
                }

                // Convertir lote (sin persistir todavía)
                LoteEntity loteEntity = loteAdapter.toEntity(loteAlg);
                if (loteEntity == null) {
                    System.out.println("│    ✗ Lote no pudo convertirse");
                    continue;
                }

                // Establecer relaciones bidireccionales
                loteEntity.setRuta(rutaEntity);
                loteEntity.setPedido(pedidoEntity);

                // Agregar a las colecciones del pedido
                if (!pedidoEntity.getRutas().contains(rutaEntity)) {
                    pedidoEntity.getRutas().add(rutaEntity);
                }
                pedidoEntity.getLotes().add(loteEntity);

                contadorLotes++;
            }

            // Persistir pedido (con cascade a lotes)
            pedidoService.save(pedidoEntity);
            contadorPedidos++;

            System.out.println("│  ✓ Pedido: " + pedidoEntity.getCodigo() +
                    " (rutas: " + pedidoEntity.getRutas().size() +
                    ", lotes: " + pedidoEntity.getLotes().size() + ")");
        }

        System.out.println("│");
        System.out.println("└─ Total pedidos: " + contadorPedidos + " | Total lotes: " + contadorLotes);
        System.out.println();

        // ============================================
        // FASE 4: PERSISTIR REGISTROS
        // ============================================
        System.out.println("┌─ FASE 4: Persistiendo Registros");
        System.out.println("│");

        int contadorRegistros = 0;

        for (Aeropuerto aeropuertoAlg : problematica.destinos) {
            if (aeropuertoAlg == null || aeropuertoAlg.getRegistros().isEmpty()) {
                continue;
            }

            System.out.println("│  → Procesando aeropuerto: " + aeropuertoAlg.getCodigo());

            // Buscar aeropuerto en BD (no usar adapter)
            AeropuertoEntity aeropuertoEntity = aeropuertoService.findByCodigo(aeropuertoAlg.getCodigo()).orElse(null);
            if (aeropuertoEntity == null) {
                System.out.println("│    ✗ Aeropuerto no encontrado en BD");
                continue;
            }

            aeropuertoEntity.getRegistros().clear();

            for (Registro registroAlg : aeropuertoAlg.getRegistros()) {
                // Crear RegistroEntity directamente (no usar adapter)
                RegistroEntity registroEntity = new RegistroEntity();
                registroEntity.setCodigo(registroAlg.getCodigo());
                registroEntity.setFechaHoraIngresoLocal(registroAlg.getFechaHoraIngresoLocal());
                registroEntity.setFechaHoraIngresoUTC(registroAlg.getFechaHoraIngresoUTC());
                registroEntity.setFechaHoraEgresoLocal(registroAlg.getFechaHoraEgresoLocal());
                registroEntity.setFechaHoraEgresoUTC(registroAlg.getFechaHoraEgresoUTC());
                registroEntity.setAeropuerto(aeropuertoEntity);

                // ⚠️ CRÍTICO: Buscar lote DESDE LA BD, no del pool
                String codigoLote = registroAlg.getLote().getCodigo();
                LoteEntity loteEntity = loteService.findByCodigo(codigoLote).orElse(null);

                if (loteEntity == null) {
                    System.out.println("│    ✗ Lote no encontrado en BD: " + codigoLote);
                    continue;
                }

                registroEntity.setLote(loteEntity);
                aeropuertoEntity.getRegistros().add(registroEntity);
                contadorRegistros++;
            }

            if (!aeropuertoEntity.getRegistros().isEmpty()) {
                aeropuertoService.save(aeropuertoEntity);
                System.out.println("│  ✓ Aeropuerto guardado con " +
                        aeropuertoEntity.getRegistros().size() + " registros");
            }
        }

        System.out.println("│");
        System.out.println("└─ Total registros: " + contadorRegistros);
        System.out.println();

        System.out.println("════════════════════════════════════════════════");
        System.out.println("   PERSISTENCIA COMPLETADA");
        System.out.println("════════════════════════════════════════════════\n");
    }
}
