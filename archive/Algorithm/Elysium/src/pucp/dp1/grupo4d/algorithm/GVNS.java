/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GVNS.java 
[**/

package pucp.dp1.grupo4d.algorithm;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import pucp.dp1.grupo4d.enums.TipoRuta;
import pucp.dp1.grupo4d.model.Aeropuerto;
import pucp.dp1.grupo4d.model.Cliente;
import pucp.dp1.grupo4d.model.Lote;
import pucp.dp1.grupo4d.model.Pedido;
import pucp.dp1.grupo4d.model.Plan;
import pucp.dp1.grupo4d.model.Ruta;
import pucp.dp1.grupo4d.model.Vuelo;
import pucp.dp1.grupo4d.util.G4D;

public class GVNS {
    public static Double D_MIN = 0.001;                 // Diferencia mínima considerable de fitness
    public static Integer I_MAX = 1;                    // Número máximo de iteraciones de exploración inicial
    public static Integer L_MIN = 1;                    // Nivel mínimo de búsqueda local
    public static Integer L_MAX = 3;                    // Nivel máximo de búsqueda local
    public static Integer K_MIN = 3;                    // Nivel mínimo de perturbación
    public static Integer K_MAX = 5;                    // Nivel máximo de perturbación
    public static Integer T_MAX = 60;                   // Tiempo máximo esperado de exploración global
    public static Integer MAX_INTENTOS = 5;             // Número de máximo de intentos por nivel de perturbación
    private static final Random random = new Random();
    private Solucion solucionINI;
    private Solucion solucionVND;
    private Solucion solucionVNS;

    public GVNS() {
        this.solucionINI = null;
        this.solucionVND = null;
        this.solucionVNS = null;
    }
    
    public void planificar(Problematica problematica) {
        G4D.Logger.Stats.set_global_start();
        // Declaracion & inicialización de variables
        Solucion x = new Solucion();
        // Solución inicial (Nearest Neighbor)
        G4D.Logger.Stats.set_local_start();
        G4D.Logger.log("Generando solución inicial.. ");
        solucionInicial(problematica, x);
        G4D.Logger.Stats.set_local_duration();
        if(x.getFitness() < 0) return;
        G4D.Logger.logf("[+] SOLUCION INICIAL GENERADA! (FITNESS: %.3f)%n", x.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x, "SolucionInicial.txt");
        this.solucionINI = x;
        // Optimización inicial (Variable Neighborhood Descent)
        G4D.Logger.Stats.set_local_start();
        G4D.Logger.log("Realizando optimización inicial.. ");
        VND(x);
        G4D.Logger.Stats.set_local_duration();
        G4D.Logger.logf("[+] OPTIMIZACION INICIAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x, "SolucionVND.txt");
        this.solucionVND = x;
        // Optimización final (Variable Neighborhood Search)
        G4D.Logger.Stats.set_local_start();
        G4D.Logger.logln("Realizando optimización final.. [VNS]");
        VNS(x);
        G4D.Logger.Stats.set_local_duration();
        G4D.Logger.logf("[+] OPTIMIZACION FINAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x, "SolucionGVNS.txt");
        this.solucionVNS = x;
        G4D.Logger.Stats.set_global_duration();
        G4D.Logger.Stats.log_stat_global_sol();
    }

    private void solucionInicial(Problematica problematica, Solucion solucion) {
        G4D.Logger.logln("[NN]");
        // Declaración & inicialización de variables
        Boolean errorDeEnrutamiento = false, haySolucion = false;
        Solucion sAux = new Solucion();
        Double mejorFitness = sAux.getFitness();
        solucion.reasignar(sAux);
        // Iteración de exploraciones iniciales
        for(int i = 0; i < GVNS.I_MAX; i++) {
            Problematica pAux = problematica.replicar();
            List<Aeropuerto> origenes = pAux.origenes;
            List<Plan> planes = pAux.planes;
            List<Pedido> pedidos = pAux.pedidos;
            Set<Vuelo> vuelosEnTransito = pAux.vuelosEnTransito;
            Set<Ruta> rutasEnOperacion = pAux.rutasEnOperacion;
            G4D.Logger.logf(">> Iteración: %d de '%d':%n", i + 1, I_MAX);
            errorDeEnrutamiento = false;
            G4D.Logger.Stats.numPed = 1;
            G4D.Logger.Stats.posPed = 0;
            G4D.Logger.Stats.posProd = 0;
            // Atención de pedidos
            G4D.Logger.Stats.set_process_start();
            for (Pedido pedido : pedidos) {
                G4D.Logger.Stats.numProd = 1;
                G4D.Logger.Stats.log_stat_ped();
                boolean pedidoAtendido = atenderPedido(pedido, origenes, planes, vuelosEnTransito, rutasEnOperacion);
                if(!pedidoAtendido) {
                    G4D.Logger.Stats.log_err_stat();
                    errorDeEnrutamiento = true;
                    break;
                }
                G4D.Logger.Stats.set_proccess_duration();
                G4D.Logger.Stats.next_ped();
            }
            // Validación por error de enrutamiento
            if(errorDeEnrutamiento) {
                G4D.Logger.delete_lines(8);
                continue;
            }
            // Actualización de solución
            sAux.setPedidosAtendidos(pedidos);
            sAux.setVuelosEnTransito(vuelosEnTransito);
            sAux.setRutasEnOperacion(rutasEnOperacion);
            sAux.setFitness();
            double fitnessObtenido = sAux.getFitness();
            G4D.Logger.logf(":: Mejor fitness actual: %.3f | Fitness obtenido: %.3f", mejorFitness, fitnessObtenido);
            // Validación por mejor solución
            if(fitnessObtenido < mejorFitness) {
                G4D.Logger.logln(" | >> Nuevo mejor!");
                mejorFitness = sAux.getFitness();
                solucion.reasignar(sAux);
                haySolucion = true;
            } else G4D.Logger.logln();
            G4D.Logger.delete_lines(3);
        }
        // Validación por inexistencia de solución
        if(!haySolucion) {
            G4D.Logger.logf_err("ERROR: No fue posible enrutar todos los pedidos en '%d' iteraciones.%n", I_MAX);
            this.solucionINI = null;
            solucion.setFitness(-9999.99);
        }
    }
    //
    private boolean atenderPedido(Pedido pedido, List<Aeropuerto> origenes, List<Plan> planes,
                                  Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Declaración & inicialización de variables
        int cantPorEnrutar = pedido.getCantidadSolicitada();
        LocalDateTime fechaHoraInicial = pedido.getFechaHoraGeneracionUTC();
        Aeropuerto destino = pedido.getDestino();
        List<Aeropuerto> origenesDisponibles = new ArrayList<>(origenes);
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        // Exploración aleatoria de orígenes
        while(!origenesDisponibles.isEmpty()) {
            G4D.Logger.Stats.log_stat_prod();
            G4D.Logger.logf(">>> ATENDIENDO PEDIDO #%d | %d de '%d' productos enrutados.%n", G4D.Logger.Stats.numPed, G4D.Logger.Stats.posProd, pedido.getCantidadSolicitada());
            Aeropuerto origen = origenesDisponibles.get(random.nextInt(origenesDisponibles.size()));
            G4D.Logger.logf("> ORIGEN:  %s%n> DESTINO: %s%n", origen, destino);
            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            LocalDateTime fechaHoraLimite = fechaHoraInicial.plusMinutes(tipoRuta.getMaxMinutosParaEntrega());
            // Búsqueda de ruta reutilizable
            Ruta ruta = buscarRutaVoraz(fechaHoraInicial, fechaHoraLimite, origen, destino, rutasAsignadas);
            G4D.Logger.delete_upper_line();
            if(ruta == null) {
                // Construcción de nueva ruta
                ruta = construirRutaVoraz(fechaHoraInicial, fechaHoraLimite, origen, destino, planes, vuelosActivados);
                if(ruta == null) {
                    G4D.Logger.logln_err("ERROR: No fue posible generar una ruta a partir de este origen.");
                    origenesDisponibles.remove(origen);
                    if(origenesDisponibles.size() > 0) G4D.Logger.delete_lines(8);
                    else G4D.Logger.delete_lines(6);
                    continue;
                } else G4D.Logger.delete_upper_line();
                ruta.setTipo(tipoRuta);
            }
            G4D.Logger.logf("> RUTA:    %s -> '%d' intermedios -> %s%n", origen.getCodigo(), ruta.getVuelos().size() - 1, destino.getCodigo());
            // Segmentación de pedido respecto a disponibilidad de ruta
            int rCapDisp = ruta.obtenerCapacidadDisponible();
            int cantEnrutables = Math.min(rCapDisp, cantPorEnrutar);
            G4D.Logger.logf("Enrutando %d productos..", cantEnrutables);
            // Producción y registro de segmento de pedido
            Lote lote = origen.generarLoteDeProductos(cantEnrutables);
            ruta.registraLoteDeProductos(lote, vuelosActivados, rutasAsignadas);
            pedido.getLotesPorRuta().put(ruta, lote);
            cantPorEnrutar -= cantEnrutables;
            G4D.Logger.Stats.set_proccess_duration();
            G4D.Logger.Stats.next_lot(cantEnrutables);
            G4D.Logger.logln(" | [REALIZADO]");
            // Validación por atención completa de pedido
            if(cantPorEnrutar == 0) {
                G4D.Logger.delete_lines(6);
                G4D.Logger.logf(">>> PEDIDO #%d ATENDIDO! | '%d' de '%d' productos enrutados!%n", G4D.Logger.Stats.numPed, G4D.Logger.Stats.posProd, pedido.getCantidadSolicitada());
                pedido.setFechaHoraExpiracion();
                vuelosEnTransito.addAll(vuelosActivados);
                rutasEnOperacion.addAll(rutasAsignadas);
                G4D.Logger.delete_lines(6);
                return true;
            }
            G4D.Logger.delete_lines(8);
        }
        return false;
    }

    private Ruta buscarRutaVoraz(LocalDateTime fechaHoraInicial, LocalDateTime fechaHoraLimite,
                                 Aeropuerto origen, Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        G4D.Logger.log("Buscando ruta en operacion..");
        // Validación por origen factible
        if(origen.equals(destino)) {
            G4D.Logger.log(" [NO ENCONTRADA]");
            G4D.Logger.logln_err(" | ERROR: ORIGEN == DESTINO");
            return null;
        }
        // Búsqueda de ruta reutilizable
        List<Ruta> rutasPosibles = rutasAsignadas.stream().filter(r -> r.getOrigen().equals(origen) && r.getDestino().equals(destino)).toList();
        for(Ruta ruta : rutasPosibles) {
            if(!ruta.esAlcanzable(fechaHoraInicial, fechaHoraLimite)) continue;
            G4D.Logger.logln(" [ENCONTRADA]");
            return ruta;
        }
        G4D.Logger.logln(" [NO ENCONTRADA]");
        return null;
    }

    private Ruta construirRutaVoraz(LocalDateTime fechaHoraInicial, LocalDateTime fechaHoraLimite, Aeropuerto origen,
                                    Aeropuerto destino, List<Plan> planes, Set<Vuelo> vuelosActivados) {
        G4D.Logger.log("Construyendo nueva ruta..");
        // Validación por origen factible
        if(origen.equals(destino)) {
            G4D.Logger.logln_err(" | ERROR: ORIGEN == DESTINO");
            return null;
        } else G4D.Logger.logln();
        // Declaración & inicialización de variables
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = fechaHoraInicial;
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        // Construcción de nueva ruta
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            G4D.Logger.logf("- Num. vuelos asignados: %d%n", secuenciaDeVuelos.size());
            G4D.Logger.logf("- Aeropuerto actual: %s%n", actual);
            // Búsqueda de plan de vuelo más próximo
            G4D.Logger.log(": Buscando mejor plan de vuelo..");
            Plan mejorPlan = obtenerPlanMasProximo(actual, destino, fechaHoraActual, fechaHoraLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorPlan == null) {
                G4D.Logger.log(" [NO ENCONTRADO]");
                G4D.Logger.delete_lines(4);
                G4D.Logger.log("Construyendo nueva ruta..");
                G4D.Logger.logln_err(" | ERROR: Deadline..");
                return null;
            } else G4D.Logger.logln(" [ENCONTRADO]");
            // Búsqueda de vuelo activo con el mejor plan
            G4D.Logger.log(": Bucando vuelo en tránsito..");
            Vuelo vuelo = mejorPlan.obtenerVueloActivo(fechaHoraActual, vuelosActivados);
            if(vuelo == null) {
                G4D.Logger.logln(" [NO_ENCONTRADO] | Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
                vuelo.instanciarHorarios(fechaHoraActual);
            } else G4D.Logger.logln(" [ENCONTRADO]");
            // Asignación de vuelo
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegadaUTC();
            actual = vuelo.getPlan().getDestino();
            G4D.Logger.logf(": Vuelo asignado: %s -> %s%n", vuelo.getPlan().getOrigen().getCodigo(), actual.getCodigo());
            G4D.Logger.delete_lines(6);
        }
        G4D.Logger.delete_upper_line();
        // Actualización de ruta construida
        G4D.Logger.logln("DESTINO ALCANZADO! | Guardando ruta..");
        ruta.setOrigen(origen);
        ruta.setDestino(destino);
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.instanciarHorarios();
        ruta.setDuracion();
        ruta.setDistancia();
        return ruta;
    }

    private Plan obtenerPlanMasProximo(Aeropuerto origen, Aeropuerto destino, LocalDateTime fechaHoraActual,
                                              LocalDateTime fechaHoraLimite, List<Plan> planes, Set<Aeropuerto> visitados,
                                              Set<Vuelo> vuelosActivados) {
        // Declaración & inicialización de variables
        Double menorLejania = Double.MAX_VALUE;
        Plan planMasProximo = null;
        // Búsqueda de plan de vuelo más próximo
        List<Plan> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                                                          .filter(p -> !visitados.contains(p.getDestino()))
                                                          .toList();
        for(Plan plan : planesPosibles) {
            if(!plan.esAlcanzable(fechaHoraActual, fechaHoraLimite, destino, vuelosActivados)) continue;
            Double lejania = plan.obtenerLejania(fechaHoraActual, destino);
            if(lejania < menorLejania) {
                menorLejania = lejania;
                planMasProximo = plan;
            }
        }
        return planMasProximo;
    }

    private void VND(Solucion solucion) {
        G4D.Logger.logln("[VND]");
        // Declaración & inicialización de variables
        boolean huboMejora;
        // Búsqueda local de soluciones por nivel de intensidad
        for(int ele = L_MIN; ele <= L_MAX; ele++) {
            G4D.Logger.logf(">> Intensidad de busqueda: %d de '%d'%n", ele, L_MAX);
            int i = 1, j = 0;
            // Iteración de vecindarios a visitar
            while (i <= 3) {
                Solucion solucionPropuesta = solucion.replicar();
                huboMejora = false;
                if(j == 3) {
                    j = 0;
                    G4D.Logger.delete_lines(4);
                }
                switch (i) {
                    case 1:
                        huboMejora = LSCompactar(solucionPropuesta, ele);
                        break; 
                    case 2:
                        huboMejora = LSFusionar(solucionPropuesta, ele);
                        break;
                    case 3:
                        huboMejora = LSRealocar(solucionPropuesta, ele);
                        break;
                }
                j++;
                // Validación por mejora obtenida
                if (huboMejora) {
                    solucion.reasignar(solucionPropuesta);
                    i = 1;
                } else {
                    i++;
                }
            }
            G4D.Logger.delete_lines(2 + j);
        }
    }

    private Boolean LSCompactar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando búsqueda local por 'Compactación'..");
        // Declaración & inicialización de variables
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness(), mejorFitness = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Compactación de pedidos
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            // Validación de aptitud de pedido para compactación
            G4D.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, G4D.Logger.Stats.totalPed);
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            if (lotesPorRuta.size() < ele + 1) {
                G4D.Logger.logln(" [NO APTO]");
                G4D.Logger.delete_upper_line();
                continue;
            } else G4D.Logger.logln(" [APTO]");
            Map<Ruta, Lote> lotesPorRutaAux = new HashMap<>(lotesPorRuta);
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(new ArrayList<>(lotesPorRuta.keySet()), ele);
            int posMejorComb = -1;
            // Iteración de combinaciones para compactación de pedidos
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                // Validación de aptitud de combinación
                G4D.Logger.logf(": Validando combinación #%d de '%d'..", posComb+1, combinaciones.size());
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                int combTotalProd = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
                List<Ruta> rutasDest = lotesPorRuta.keySet().stream().filter(r -> !rutasOrig.contains(r)).collect(Collectors.toList());
                int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
                if (capDispTotal < combTotalProd) {
                    G4D.Logger.logln(" [INVALIDA]");
                    G4D.Logger.delete_upper_line();
                    continue;
                } else G4D.Logger.logln("[VALIDA]");
                // Compactación
                G4D.Logger.logf(": Mejor fitness actual: %.3f | >> COMPACTANDO..", mejorFitness);
                rutasDest.sort(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC));
                compactarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
                // Validación por mejor fitness
                double fitnessObtenido = solucion.getFitness();
                G4D.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (fitnessObtenido < mejorFitness) {
                    G4D.Logger.logln(" | ¡NUEVO MEJOR!");
                    mejorFitness = solucion.getFitness();
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4D.Logger.logln();
                // Reversión de cambios
                deshacerCambios(lotesPorRuta, lotesPorRutaAux, vuelosEnTransito, rutasEnOperacion);
                G4D.Logger.delete_lines(3);
            }
            // Validación de existencia de mejora por compactación
            if(posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                List<Ruta> rutasDest = lotesPorRuta.keySet().stream().filter(r -> !rutasOrig.contains(r)).collect(Collectors.toList());
                rutasDest.sort(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC));
                compactarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
            }
            G4D.Logger.delete_upper_line();
        }
        // Actualización de solución
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Compactación' : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4D.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4D.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private void compactarEleLotes(List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> lotesPorRuta, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Iteración por orígenes para compactar
        for(Ruta rOrig : rutasOrig) {
            Lote lOrig = lotesPorRuta.get(rOrig);
            int tamanioRestantePorFusionar = lOrig.getTamanio();
            rOrig.eliminarRegistroDeLoteDeProductos(lOrig, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.remove(rOrig);
            // Compactación en destinos seleccionados
            for(Ruta rDest : rutasDest) {
                if(tamanioRestantePorFusionar == 0) break;
                int rDestCapDisp = rDest.obtenerCapacidadDisponible();
                if(rDestCapDisp == 0) continue;
                int tamanioDeFusion = Math.min(tamanioRestantePorFusionar, rDestCapDisp);
                Lote lOld = lotesPorRuta.get(rDest);
                int tamanioDeConsolidado = lOld.getTamanio() + tamanioDeFusion;
                rDest.eliminarRegistroDeLoteDeProductos(lOld, vuelosEnTransito, rutasEnOperacion);
                lotesPorRuta.remove(rDest);
                Lote lNew = rDest.getOrigen().generarLoteDeProductos(tamanioDeConsolidado);
                rDest.registraLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
                lotesPorRuta.put(rDest, lNew);
                tamanioRestantePorFusionar -= tamanioDeFusion;
            }
        }
    }

    private void deshacerCambios(Map<Ruta, Lote> lotesPorRuta, Map<Ruta, Lote> lotesPorRutaAux, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Eliminación de registros actualizados
        for (Ruta r : lotesPorRuta.keySet()) {
            Lote l = lotesPorRuta.get(r);
            r.eliminarRegistroDeLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
        }
        lotesPorRuta.clear();
        // Agregación de registros antiguos
        lotesPorRuta.putAll(lotesPorRutaAux);
        for (Ruta r : lotesPorRuta.keySet()) {
            Lote l = lotesPorRuta.get(r);
            r.registraLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
        }
    }


    /*
     *   P  -> RA , RB , RC , RD, RE  => A -> > V3 -> A3-> V3->D
     * ....
     */

    private Boolean LSFusionar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando búsqueda local por 'Fusión'..");
        // Declaración & inicialización de variables
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness();
        double mejorFitness = fitnessInicial;
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Fusión de rutas de pedidos
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            G4D.Logger.logf("- Evaluando rutas del pedido #%d de '%d'..%n", posPedido+1, G4D.Logger.Stats.totalPed);
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            List<Ruta> rutasIni = new ArrayList<>(lotesPorRuta.keySet());
            Ruta rMejorIni = null;
            List<Vuelo> rMejorSvNew = null;
            // Iteración de combinaciones para fusión de rutas [INI + FIN]
            for(int posRutaIni = 0; posRutaIni < rutasIni.size(); posRutaIni++) {
                G4D.Logger.logf(": Evaluando ruta #%d de '%d'..%n", posRutaIni+1, rutasIni.size());
                Ruta rIni = rutasIni.get(posRutaIni);
                Lote lote = lotesPorRuta.get(rIni);
                List<Ruta> rutasFin = rutasEnOperacion.stream()
                    .filter(r -> !r.equals(rIni))
                    .filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalidaUTC().isBefore(pedido.getFechaHoraGeneracionUTC()) && !r.getFechaHoraLlegadaUTC().isAfter(pedido.getFechaHoraExpiracionUTC()))
                    .sorted(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC))
                    .collect(Collectors.toList());
                rIni.eliminarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                lotesPorRuta.remove(rIni);
                for(int posRutaFin = 0; posRutaFin < rutasFin.size(); posRutaFin++) {
                    Ruta rFin = rutasFin.get(posRutaFin);
                    // Validación de aptitud de fusión por tamanio ruta
                    G4D.Logger.logf(": Validando fusión #%d de '%d'..", posRutaFin+1, rutasFin.size());
                    List<Aeropuerto> saFin = rFin.obtenerSecuenciaDeAeropuertos();
                    if(saFin.size() < ele + 1) {
                        G4D.Logger.logln(" [INVALIDA]");
                        G4D.Logger.delete_upper_line();
                        continue;
                    }
                    // Validación de aptitud de fusión por existencia de conexión de ruta
                    int posConexionFin = saFin.size() - ele - 1;
                    Aeropuerto aConexion = saFin.get(posConexionFin);
                    List<Aeropuerto> saIni = rIni.obtenerSecuenciaDeAeropuertos();
                    int posConexionIni = saIni.indexOf(aConexion);
                    if(posConexionIni == -1) {
                        G4D.Logger.logln(" [INVALIDA]");
                        G4D.Logger.delete_upper_line();
                        continue;
                    }
                    // Validación de aptitud de fusión por existencia de disponibilidad de ruta
                    List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posConexionIni));
                    List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posConexionFin, rFin.getVuelos().size()));
                    List<Vuelo> svNew = new ArrayList<>();
                    svNew.addAll(svIni);
                    svNew.addAll(svFin);
                    Ruta rNew = new Ruta(rIni);
                    rNew.setVuelos(svNew);
                    if(rNew.obtenerCapacidadDisponible() < lote.getTamanio()) {
                        G4D.Logger.logln(" [INVALIDA]");
                        G4D.Logger.delete_upper_line();
                        continue;
                    } else G4D.Logger.logln(" [VALIDA]");
                    // Fusión
                    rNew.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                    lotesPorRuta.put(rNew, lote);
                    G4D.Logger.logf(": Mejor fitness actual: %.3f | >> FUSIONANDO..", mejorFitness);
                    solucion.setFitness();
                    // Validación por mejor fitness
                    double fitnessObtenido = solucion.getFitness();
                    G4D.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                    if (fitnessObtenido < mejorFitness) {
                        G4D.Logger.logln(" | ¡NUEVO MEJOR!");
                        mejorFitness = fitnessObtenido;
                        huboMejora = true;
                        rMejorIni = rIni;
                        rMejorSvNew = svNew;
                    } else G4D.Logger.logln();
                    // Reversión de cambios [Nuevos]
                    rNew.eliminarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                    lotesPorRuta.remove(rNew);
                    G4D.Logger.delete_lines(3);
                }
                // Reversión de cambios [Iniciales]
                rIni.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                lotesPorRuta.put(rIni, lote);
                G4D.Logger.delete_upper_line();
            }
            // Validación de existencia de mejora por fusión
            if(rMejorIni != null && rMejorSvNew != null) {
                Lote lote = lotesPorRuta.get(rMejorIni);
                rMejorIni.eliminarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                rMejorIni.setVuelos(rMejorSvNew);
                rMejorIni.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
            }
            G4D.Logger.delete_upper_line();
        }
        // Actualización de solución
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Fusión'       : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4D.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4D.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private Boolean LSRealocar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando búsqueda local por 'Realocación'..");
        // Declaración & inicialización de variables
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness();
        double mejorFitness = fitnessInicial;
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Realocación de lotes de pedidos
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            // Validación de aptitud de pedido para realocación
            G4D.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, G4D.Logger.Stats.totalPed);
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            if (lotesPorRuta.size() < ele) {
                G4D.Logger.logln(" [NO APTO]");
                G4D.Logger.delete_upper_line();
                continue;
            } else G4D.Logger.logln("[APTO]");
            Map<Ruta, Lote> lotesPorRutaAux = new HashMap<>(lotesPorRuta);
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(rutas, ele);
            List<Ruta> rutasDest = rutasEnOperacion.stream()
                    .filter(r -> !rutas.contains(r))
                    .filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalidaUTC().isBefore(pedido.getFechaHoraGeneracionUTC()) && !r.getFechaHoraLlegadaUTC().isAfter(pedido.getFechaHoraExpiracionUTC()))
                    .filter(r -> r.obtenerCapacidadDisponible() > 0)
                    .sorted(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC))
                    .collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            int posMejorComb = -1;
            // Iteración de combinaciones para realocación de lotes
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                G4D.Logger.logf(": Validando combinación #%d de '%d'..", posComb+1, combinaciones.size());
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                int totalRealocar = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
                if (capDispTotal < totalRealocar) {
                    G4D.Logger.logln(" [INVALIDA]");
                    G4D.Logger.delete_upper_line();
                    continue;
                } else G4D.Logger.logln(" [VALIDA]");
                // Realocación
                G4D.Logger.logf(": Mejor fitness actual: %.3f | >> REALOCANDO..", mejorFitness);
                realocarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
                // Validación por mejor fitness
                double fitnessObtenido = solucion.getFitness();
                G4D.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (fitnessObtenido < mejorFitness) {
                    G4D.Logger.logln(" | ¡NUEVO MEJOR!");
                    mejorFitness = fitnessObtenido;
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4D.Logger.logln();
                // Reversión de cambios
                deshacerCambios(lotesPorRuta, lotesPorRutaAux, vuelosEnTransito, rutasEnOperacion);
                G4D.Logger.delete_lines(3);
            }
            // Validación de existencia de mejora por realocación
            if(posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                realocarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
            }
            G4D.Logger.delete_upper_line();
        }
        // Actualización de solución
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Realocación'  : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4D.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4D.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private void realocarEleLotes(List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> lotesPorRuta, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Eliminación de registros de origenes
        int restante = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
        for (Ruta rOrig : rutasOrig) {
            Lote l = lotesPorRuta.get(rOrig);
            rOrig.eliminarRegistroDeLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.remove(rOrig);
        }
        // Realocación en destinos seleccionados
        for (Ruta rDest : rutasDest) {
            if (restante == 0) break;
            int capDisp = rDest.obtenerCapacidadDisponible();
            int asignar = Math.min(capDisp, restante);
            Lote lNew = rDest.getOrigen().generarLoteDeProductos(asignar);
            rDest.registraLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.put(rDest, lNew);
            restante -= asignar;
        }
    }

    private void VNS(Solucion solucion) {
        // Declaración & inicialización de variables
        Solucion x_best = solucion.replicar();
        G4D.IntegerWrapper t = new G4D.IntegerWrapper(), t_best = new G4D.IntegerWrapper();
        Instant start = Instant.now();
        // Búsqueda global de soluciones por nivel de perturbación
        do {
            G4D.IntegerWrapper k = new G4D.IntegerWrapper(K_MIN);
            solucion.reasignar(x_best.replicar());
            while (t.value < T_MAX && k.value <= K_MAX) {
                Solucion x_prima = new Solucion();
                boolean solucionValida = false;
                int intentos = 1;
                // Realización de agitaciones aleatorias continuas
                while (true) {
                    x_prima = solucion.replicar();
                    G4D.Logger.log("Agitando.. ");
                    Shaking(x_prima, k);
                    Boolean huboAlteracion = Math.abs(solucion.getFitness() - x_prima.getFitness()) > D_MIN;
                    // Validación de solución por umbral de aberración
                    if (huboAlteracion  && x_prima.getFitness() < solucion.obtenerUmbralDeAberracion()) {
                        G4D.Logger.logln(" | >> POSIBLE MEJOR SOLUCIÓN");
                        solucionValida = true;
                        break;
                    } else {
                        if(!huboAlteracion) {
                            G4D.Logger.logln(" | >> SIN ALTERACIÓN");
                        } else G4D.Logger.logln(" | >> ABERRACIÓN");
                        if (intentos >= MAX_INTENTOS) {
                            G4D.Logger.log("LIMITE DE INTENTOS ALCANZADO.");
                            break;
                        }
                        intentos++;
                    }
                }
                // Validación de existencia de solución
                if (!solucionValida) {
                    k.value++;
                    G4D.Logger.delete_lines(1 + intentos);
                    continue;
                }
                // Reoptimización de posible mejor solución
                Solucion x_prima_doble = x_prima.replicar();
                G4D.Logger.log("Reoptimizando.. ");
                VND(x_prima_doble);
                G4D.Logger.delete_upper_line();
                G4D.Logger.logf("> 'Reoptimización' : %.3f -> %.3f%n", x_prima.getFitness(), x_prima_doble.getFitness());
                // Actualización de tiempo transcurrido
                Instant end = Instant.now();
                t.value = (int) Duration.between(start, end).getSeconds();
                // Actualización de vecindario
                G4D.Logger.log("Validando nuevo vencindario.. ");
                NeighborhoodChange(solucion, x_prima_doble, x_best, k, t, t_best);
                G4D.Logger.delete_lines(2 + intentos);
            }
            // Actualización de tiempo transcurrido
            Instant end = Instant.now();
            Duration duracion = Duration.between(start, end);
            t.value = (int) duracion.getSeconds();
        } while (t.value < T_MAX);
        solucion.reasignar(x_best);
    }

    private void Shaking(Solucion solucion, G4D.IntegerWrapper k) {
        G4D.Logger.logln("[RAND]");
        // Declaración & inicialización de variables
        int j = 0;
        double fitnessInicial = solucion.getFitness();
        // Perturbación aleatoria de solución por nivel de intesidad
        G4D.Logger.logf(">> Intensidad de perturbación: %d de '%d'%n", k.value, K_MAX);
        for (int i = 0; i < k.value; ++i) {
            int neighborhood = random.nextInt(3);
            int ele = L_MIN + random.nextInt(L_MAX - L_MIN + 1);
            if(j == 3) {
                j = 0;
                G4D.Logger.delete_lines(4);
            }
            switch (neighborhood) {
                case 0:
                    TCompactar(solucion, ele);
                    break;
                case 1:
                    TFusionar(solucion, ele);
                    break;
                case 2:
                    TRealocar(solucion, ele);
                    break;
            }
            j++;
        }
        G4D.Logger.delete_lines(3 + j);
        G4D.Logger.logf("> 'Agitación'    : %.3f -> %.3f", fitnessInicial, solucion.getFitness());
    }

    private void TCompactar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando perturbación por 'Compactación'..");
        // Declaración & inicialización de variables
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Compactación de pedidos
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            // Validación por aptitud de pedido para compactar
            G4D.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, G4D.Logger.Stats.totalPed);
            if (pedido.getLotesPorRuta().size() < ele + 1) {
                G4D.Logger.logln(" [NO APTO]");
                G4D.Logger.delete_upper_line();
                continue;
            } else G4D.Logger.logln(" [APTO]");
            // Validación por aptitud de combinación
            G4D.Logger.log(": Validando combinación aleatoria.. ");
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(new ArrayList<>(lotesPorRuta.keySet()), ele);
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            int combTotalProd = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
            List<Ruta> rutasDest = lotesPorRuta.keySet().stream().filter(r -> !rutasOrig.contains(r)).collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            if (capDispTotal < combTotalProd) {
                G4D.Logger.logln("[INVALIDA]");
                G4D.Logger.delete_lines(3);
                continue;
            } else G4D.Logger.logln("[VALIDA]");
            // Compactación
            G4D.Logger.logln(": Compactando..");
            Collections.shuffle(rutasDest);
            compactarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
            G4D.Logger.delete_lines(4);
        }
        // Actualización de solución
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Compactación' : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void TFusionar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando perturbación por 'Fusión'..");
        // Declaración & inicialización de variables
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Fusión de rutas
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            List<Ruta> rutasIni = new ArrayList<>(lotesPorRuta.keySet());
            int posRutaIni = random.nextInt(rutasIni.size());
            Ruta rIni = rutasIni.get(posRutaIni);
            Lote lote = lotesPorRuta.get(rIni);
            G4D.Logger.logf("- Evaluando ruta aleatoria del pedido #%d de '%d'..%n", posPedido+1, G4D.Logger.Stats.totalPed);
            List<Ruta> rutasFin = rutasEnOperacion.stream()
                .filter(r -> !r.equals(rIni))
                .filter(r -> r.getDestino().equals(pedido.getDestino()))
                .filter(r -> !r.getFechaHoraSalidaUTC().isBefore(pedido.getFechaHoraGeneracionUTC()) && !r.getFechaHoraLlegadaUTC().isAfter(pedido.getFechaHoraExpiracionUTC()))
                .collect(Collectors.toList());
            // Validación por existencia de rutas para fusionar
            if(rutasFin.isEmpty()) {
                G4D.Logger.delete_upper_line();
                continue;
            }
            Collections.shuffle(rutasFin);
            // Validación de fusión por tamanio de ruta
            int posRutaFin = random.nextInt(rutasFin.size());
            G4D.Logger.log(": Validando fusión aleatoria..");
            Ruta rFin = rutasFin.get(posRutaFin);
            List<Aeropuerto> saFin = rFin.obtenerSecuenciaDeAeropuertos();
            if(saFin.size() < ele + 1) {
                G4D.Logger.logln(" [INVALIDA]");
                G4D.Logger.delete_lines(3);
                continue;
            }
            // Validación de fusión por existencia de conexión en ruta
            int posConexionFin = saFin.size() - ele - 1;
            Aeropuerto aConexion = saFin.get(posConexionFin);
            List<Aeropuerto> saIni = rIni.obtenerSecuenciaDeAeropuertos();
            int posConexionIni = saIni.indexOf(aConexion);
            if(posConexionIni == -1) {
                G4D.Logger.logln(" [INVALIDA]");
                G4D.Logger.delete_lines(3);
                continue;
            }
            // Validación de fusión por existencia de disponibilidad en ruta
            List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posConexionIni));
            List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posConexionFin, rFin.getVuelos().size()));
            List<Vuelo> svNew = new ArrayList<>();
            svNew.addAll(svIni);
            svNew.addAll(svFin);
            Ruta rNew = new Ruta(rIni);
            rNew.setVuelos(svNew);
            rIni.eliminarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.remove(rIni);
            if(rNew.obtenerCapacidadDisponible() < lote.getTamanio()) {
                G4D.Logger.logln(" [INVALIDA]");
                rIni.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                lotesPorRuta.put(rIni, lote);
                G4D.Logger.delete_lines(3);
                continue;
            } else G4D.Logger.logln(" [VALIDA]");
            // Fusión
            G4D.Logger.logln(": Fusionando..");
            rNew.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.put(rNew, lote);
            G4D.Logger.delete_lines(4);
        }
        // Actualización de solución
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Fusión'       : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void TRealocar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando perturbación por 'Realocación'..");
        // Declaración & inicialización de variables
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Realocación de lotes
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            // Validación por aptitud de pedido para realocar
            G4D.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, G4D.Logger.Stats.totalPed);
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            if (lotesPorRuta.size() < ele) {
                G4D.Logger.logln("[NO APTO]");
                G4D.Logger.delete_upper_line();
                continue;
            } else G4D.Logger.logln("[APTO]");
            // Validación por aptitud de combinación
            G4D.Logger.log(": Validando combinación aleatoria.. ");
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(rutas, ele);
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            int totalRealocar = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
            List<Ruta> rutasDest = rutasEnOperacion.stream()
                .filter(r -> !rutas.contains(r))
                .filter(r -> r.getDestino().equals(pedido.getDestino()))
                .filter(r -> !r.getFechaHoraSalidaUTC().isBefore(pedido.getFechaHoraGeneracionUTC()) && !r.getFechaHoraLlegadaUTC().isAfter(pedido.getFechaHoraExpiracionUTC())) // si tienes este método o un criterio temporal
                .filter(r -> r.obtenerCapacidadDisponible() > 0)
                .collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            if (capDispTotal < totalRealocar) {
                G4D.Logger.logln("[INVALIDA]");
                G4D.Logger.delete_lines(3);
                continue;
            } else G4D.Logger.logln("[VALIDA]");
            // Realocación
            G4D.Logger.logln(": Realocando..");
            Collections.shuffle(rutasDest);
            realocarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
            G4D.Logger.delete_lines(4);
        }
        // Actualización de solución
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Realocación'  : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void NeighborhoodChange(Solucion solucionAux,
                                    Solucion x_prima_doble, Solucion x_best,
                                    G4D.IntegerWrapper k, G4D.IntegerWrapper t, 
                                    G4D.IntegerWrapper t_best) {
        G4D.Logger.log("[NeighborhoodChange]");
        // Validación por mejor vecindario
        if (x_prima_doble.getFitness() < x_best.getFitness()) {
            G4D.Logger.logf("| > NUEVO MEJOR [%.3f]", x_prima_doble.getFitness());
            x_best.reasignar(x_prima_doble.replicar());
            solucionAux.reasignar(x_prima_doble);
            k.value = K_MIN;
            t_best.value = t.value;
        } else {
            G4D.Logger.log("| No es mejor.");
            k.value++;
        }
    }

    public void imprimirSolucionINI(String rutaArchivo) { imprimirSolucion(this.solucionINI, rutaArchivo); }

    public void imprimirSolucionVND(String rutaArchivo) { imprimirSolucion(this.solucionVND, rutaArchivo); }

    public void imprimirSolucionVNS(String rutaArchivo) { imprimirSolucion(this.solucionVNS, rutaArchivo); }

    private void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        G4D.Logger.logf("Cargando archivo 'Solucion' a la ruta '%s'..",rutaArchivo);
        // Declaración & inicialización de variables
        int dimLinea = 181;
        try {
            // Inicialización de archivo
            G4D.Printer.open(rutaArchivo);
            // Impresion de reporte
            G4D.Printer.fill_line('=', dimLinea);
            G4D.Printer.print_centered("FITNESS DE LA SOLUCIÓN", dimLinea);
            G4D.Printer.print_centered(
                String.format("%.2f",
                    solucion.getFitness()
                ), dimLinea);
            G4D.Printer.println();
            G4D.Printer.print_centered(
                String.format("%s%35s%39s",
                "UTILIZACION TEMPORAL",
                    "DESVIACION ESPACIAL",
                    "DISPOSICION OPERACIONAL"
                ), dimLinea);
            G4D.Printer.print_centered(
                String.format("%s%35s%37s",
                    String.format("%.2f%%", 100*solucion.getRatioPromedioDeUtilizacionTemporal()),
                    String.format("%.2f%%", 100*solucion.getRatioPromedioDeDesviacionEspacial()),
                    String.format("%.2f%%", 100*solucion.getRatioPromedioDeDisposicionOperacional())
                ), dimLinea);
            G4D.Printer.fill_line('=', dimLinea);
            List<Pedido> sol_pedidos = solucion.getPedidosAtendidos();
            int cantPedidos = sol_pedidos.size();
            for (int posPedido = 0; posPedido < cantPedidos; posPedido++) {
                Pedido pedido = sol_pedidos.get(posPedido);
                double ped_duracionActivaTotal = 0.0;
                double ped_duracionPasivaTotal = 0.0;
                double ped_tiempoOptimizado = 0.0;
                LocalDateTime ped_fechaHoraGeneracion = pedido.getFechaHoraGeneracionUTC();
                LocalDateTime ped_fechaHoraExpiracion = pedido.getFechaHoraExpiracionUTC();
                Cliente ped_cli = pedido.getCliente();
                Aeropuerto ped_aDest = pedido.getDestino();
                G4D.Printer.print_centered(
                    String.format(
                        "PEDIDO #%d",
                        posPedido + 1
                    ), dimLinea);
                G4D.Printer.fill_line('-', dimLinea, 4);
                G4D.Printer.printf(
                    "%4s%-50s%8s%-30s%27s%28s%30s%n",
                    " ",
                    "CLIENTE",
                    " ",
                    "DESTINO",
                    "CANT. PRODUCTOS MPE",
                    "INSTANTE DE REGISTRO",
                    "INSTANTE DE EXPIRACION"
                );
                G4D.Printer.printf(
                    "%4s%-50s%8s%-30s%19s%34s%29s%n",
                    " ",
                    ped_cli,
                    " ",
                    ped_aDest,
                    String.format("%03d", pedido.getCantidadSolicitada()),
                    G4D.toDisplayString(ped_fechaHoraGeneracion),
                    G4D.toDisplayString(ped_fechaHoraExpiracion)
                );
                G4D.Printer.println();
                G4D.Printer.print_centered(">> RUTAS PLANIFICADAS PARA EL PEDIDO <<", dimLinea);
                G4D.Printer.println();
                G4D.Printer.fill_line('*', dimLinea, 8);
                List<Ruta> ped_rutas = new ArrayList<>(pedido.getLotesPorRuta().keySet());
                ped_rutas.sort(Comparator.comparing(Ruta::getFechaHoraSalidaUTC));
                int cantRutas = ped_rutas.size();
                for (int posRuta = 0; posRuta < cantRutas; posRuta++) {
                    Ruta ruta = ped_rutas.get(posRuta);
                    int rut_numProdAsignados = pedido.obtenerCantidadDeProductosEnRuta(ruta);
                    double rut_duracionActivaTotalInd = ruta.obtenerDuracionActivaTotal();
                    double rut_duracionActivaTotalLot = rut_duracionActivaTotalInd*rut_numProdAsignados;
                    double rut_duracionPasivaTotalInd = ruta.obtenerDuracionPasivaTotal(ped_fechaHoraGeneracion);
                    double rut_duracionPasivaTotalLot = rut_duracionPasivaTotalInd*rut_numProdAsignados;
                    double rut_tiempoOptimizadoInd = G4D.getElapsedHours(ruta.getFechaHoraLlegadaUTC(), ped_fechaHoraExpiracion);
                    double rut_tiempoOptimizadoLot = rut_tiempoOptimizadoInd*rut_numProdAsignados;
                    Aeropuerto rut_aOrig = ruta.getOrigen();
                    G4D.Printer.printf("%10s RUTA #%s | ORIGEN: %-30s | TIPO DE ENVIO: %s | INSTANTE DE ENTREGA: %s | CANTIDAD ASIGNADA DE PRODUCTOS: %3d%n",
                        ">>",
                        String.format("%03d", posRuta + 1),
                        rut_aOrig,
                        ruta.getTipo(),
                        G4D.toDisplayString(ruta.getFechaHoraLlegadaUTC()),
                        rut_numProdAsignados
                    );
                    G4D.Printer.println();
                    G4D.Printer.printf(
                            "%39s%4s%-30s%54s%3s%s%n",
                             "INSTANTE DE SALIDA",
                             " ",
                              "ORIGEN",
                              "INSTANTE DE LLEGADA",
                              " ",
                              "DESTINO"
                        );
                    List<Vuelo> vuelos = ruta.getVuelos();
                    for (Vuelo vuelo : vuelos) {
                        G4D.Printer.print_centered(
                            String.format("[%s]    %-30s            > > > > > >            [%s]    %-30s",
                            G4D.toDisplayString(vuelo.getFechaHoraSalidaUTC()),    
                            vuelo.getPlan().getOrigen(),
                                G4D.toDisplayString(vuelo.getFechaHoraLlegadaUTC()), 
                                vuelo.getPlan().getDestino()
                            ), dimLinea);
                    }
                    G4D.Printer.fill_line('.', dimLinea, 8);
                    G4D.Printer.printf("%27s%22s%16s%n", "Resumen de la ruta:","INDIVIDUAL","LOTE");
                    G4D.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Duración 'Activa':", G4D.toTimeDisplayString(rut_duracionActivaTotalInd), G4D.toTimeDisplayString(rut_duracionActivaTotalLot));
                    G4D.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Duración 'Pasiva':", G4D.toTimeDisplayString(rut_duracionPasivaTotalInd), G4D.toTimeDisplayString(rut_duracionPasivaTotalLot));
                    G4D.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Tiempo optimizado:", G4D.toTimeDisplayString(rut_tiempoOptimizadoInd), G4D.toTimeDisplayString(rut_tiempoOptimizadoLot));
                    if (posRuta != cantRutas - 1) G4D.Printer.fill_line('*', dimLinea, 8);
                    ped_duracionActivaTotal += rut_duracionActivaTotalLot;
                    ped_duracionPasivaTotal += rut_duracionPasivaTotalLot;
                    ped_tiempoOptimizado += rut_tiempoOptimizadoLot;
                }
                G4D.Printer.fill_line('-', dimLinea, 4);
                G4D.Printer.printf("%23s%23s%n", "Resumen del pedido:","TOTAL");
                G4D.Printer.printf("%4s%-30s%15s%n", " ", ">> Duración 'Activa':", G4D.toTimeDisplayString(ped_duracionActivaTotal));
                G4D.Printer.printf("%4s%-30s%15s%n", " ", ">> Duración 'Pasiva':", G4D.toTimeDisplayString(ped_duracionPasivaTotal));
                G4D.Printer.printf("%4s%-30s%15s%n", " ", ">> Tiempo optimizado:", G4D.toTimeDisplayString(ped_tiempoOptimizado));
                G4D.Printer.fill_line('=', dimLinea);
            }
            G4D.Printer.flush();
            G4D.Printer.close();
            G4D.Logger.delete_current_line();
            G4D.Logger.logf("[>] ARCHIVO 'SOLUCION' GENERADO! (RUTA: '%s')%n", rutaArchivo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Solucion getSolucionINI() {
        return solucionINI;
    }

    public Solucion getSolucionVND() {
        return solucionVND;
    }

    public Solucion getSolucionVNS() {
        return solucionVNS;
    }
}
