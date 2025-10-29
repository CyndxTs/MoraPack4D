/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GVNS.java
 [**/

package com.pucp.dp1.grupo4d.morapack.algorithm;

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
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Aeropuerto;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Cliente;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Lote;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Pedido;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Plan;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Ruta;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.Vuelo;
import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class GVNS {
    public static Integer L_MIN = 1;
    public static Integer L_MAX = 3;
    public static Integer K_MIN = 1;
    public static Integer K_MAX = 5;
    public static Integer T_MAX = 12;
    public static Integer MAX_INTENTOS = 12;
    private static final Random random = new Random();
    private Solucion solucionINI;
    private Solucion solucionVND;
    private Solucion solucionVNS;

    public GVNS() {
        this.solucionINI = null;
        this.solucionVND = null;
        this.solucionVNS = null;
    }
    //
    public void planificar(Problematica problematica) {
        G4D.Logger.Stats.set_global_start();
        // Declaracion de variables
        Solucion x = new Solucion();
        // Solución inicial (Nearest Neighbor)
        G4D.Logger.Stats.set_local_start();
        G4D.Logger.log("Generando solución inicial.. ");
        solucionInicial(problematica, x);
        G4D.Logger.Stats.set_local_duration();
        G4D.Logger.logf("[+] SOLUCION INICIAL GENERADA! (FITNESS: %.3f)%n", x.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x, "SolucionInicial.txt");
        this.solucionINI = x;
        /*
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
         */
        this.solucionVNS = x;
        G4D.Logger.Stats.set_global_duration();
        G4D.Logger.Stats.log_stat_global_sol();
    }
    // Nearest Neighbor
    private void solucionInicial(Problematica problematica, Solucion solucion) {
        G4D.Logger.logln("[NN]");
        // Declaracion de variables
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<Plan> planes = problematica.planes;
        List<Pedido> pedidos = problematica.pedidos;
        Set<Vuelo> vuelosEnTransito = new HashSet<>();
        Set<Ruta> rutasEnOperacion = new HashSet<>();
        //
        G4D.Logger.Stats.numPed = 1;
        G4D.Logger.Stats.set_process_start();
        for (Pedido pedido : pedidos) {
            G4D.Logger.Stats.numProd = 1;
            G4D.Logger.Stats.log_stat_ped();
            boolean pedidoAtendido = atenderPedido(pedido, origenes, planes, vuelosEnTransito, rutasEnOperacion);
            if(!pedidoAtendido) {
                G4D.Logger.Stats.log_err_stat();
                System.exit(1);
            }
            G4D.Logger.Stats.set_proccess_duration();
            G4D.Logger.Stats.next_ped();
        }
        //
        solucion.setPedidosAtendidos(pedidos);
        solucion.setVuelosEnTransito(vuelosEnTransito);
        solucion.setRutasEnOperacion(rutasEnOperacion);
        solucion.setFitness();
    }
    //
    private boolean atenderPedido(Pedido pedido, List<Aeropuerto> origenes, List<Plan> planes,
                                  Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        //
        int cantPorEnrutar = pedido.getCantidadSolicitada();
        LocalDateTime fechaHoraInicial = pedido.getFechaHoraGeneracionUTC();
        Aeropuerto destino = pedido.getDestino();
        List<Aeropuerto> origenesDisponibles = new ArrayList<>(origenes);
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        while(!origenesDisponibles.isEmpty()) {
            G4D.Logger.Stats.log_stat_prod();
            G4D.Logger.logf(">>> ATENDIENDO PEDIDO #%d | %d de '%d' productos enrutados.%n", G4D.Logger.Stats.numPed, G4D.Logger.Stats.numProd, pedido.getCantidadSolicitada());
            Aeropuerto origen = origenesDisponibles.get(random.nextInt(origenesDisponibles.size()));
            G4D.Logger.logf("Partiendo desde: %s%n", origen);
            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            LocalDateTime fechaHoraLimite = fechaHoraInicial.plusMinutes(tipoRuta.getMaxMinutosParaEntrega());
            Ruta ruta = buscarRutaVoraz(fechaHoraInicial, fechaHoraLimite, origen, destino, rutasAsignadas);
            G4D.Logger.delete_current_line();
            if(ruta == null) {
                ruta = construirRutaVoraz(fechaHoraInicial, fechaHoraLimite, origen, destino, planes, vuelosActivados);
                if(ruta == null) {
                    G4D.Logger.log("No es posible generar una ruta a partir de este origen.");
                    origenesDisponibles.remove(origen);
                    G4D.Logger.delete_lines(5);
                    continue;
                }
                ruta.setTipo(tipoRuta);
            }
            int rCapDisp = ruta.obtenerCapacidadDisponible();
            int cantEnrutables = Math.min(rCapDisp, cantPorEnrutar);
            G4D.Logger.logf("Enrutando %d productos.. {%s}", cantEnrutables, ruta.getCodigo());
            Lote lote = origen.generarLoteDeProductos(cantEnrutables);
            ruta.registraLoteDeProductos(lote, vuelosActivados, rutasAsignadas);
            pedido.getLotesPorRuta().put(ruta, lote);
            cantPorEnrutar -= cantEnrutables;
            G4D.Logger.Stats.set_proccess_duration();
            G4D.Logger.Stats.next_lot(cantEnrutables);
            G4D.Logger.log(" | [REALIZADO]");
            if(cantPorEnrutar == 0) {
                G4D.Logger.delete_lines(3);
                G4D.Logger.log(">>> PEDIDO ATENDIDO.");
                pedido.setFechaHoraExpiracion();
                vuelosEnTransito.addAll(vuelosActivados);
                rutasEnOperacion.addAll(rutasAsignadas);
                G4D.Logger.delete_lines(5);
                return true;
            }
            G4D.Logger.delete_lines(5);
        }
        return false;
    }
    //
    private Ruta buscarRutaVoraz(LocalDateTime fechaHoraInicial, LocalDateTime fechaHoraLimite,
                                 Aeropuerto origen, Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        G4D.Logger.log("Buscando ruta en operacion..");
        //
        if(origen.equals(destino)) {
            G4D.Logger.log(" [NO ENCONTRADA]");
            G4D.Logger.log_err(" | ERROR: {ORIG == DEST}");
            return null;
        }
        //
        List<Ruta> rutasPosibles = rutasAsignadas.stream().filter(r -> r.getOrigen().equals(origen) && r.getDestino().equals(destino)).toList();
        for(Ruta ruta : rutasPosibles) {
            LocalDateTime fechaHoraSalida = ruta.getFechaHoraSalidaUTC();
            if(fechaHoraSalida.isBefore(fechaHoraInicial)) continue;
            LocalDateTime fechaHoraLlegada = ruta.getFechaHoraLlegadaUTC();
            if(fechaHoraLlegada.isAfter(fechaHoraLimite)) continue;
            int rCapDisp = ruta.obtenerCapacidadDisponible();
            if(rCapDisp < 1) continue;
            G4D.Logger.log(" [ENCONTRADA]");
            return ruta;
        }
        G4D.Logger.log(" [NO ENCONTRADA]");
        return null;
    }
    //
    private Ruta construirRutaVoraz(LocalDateTime fechaHoraInicial, LocalDateTime fechaHoraLimite, Aeropuerto origen,
                                    Aeropuerto destino, List<Plan> planes, Set<Vuelo> vuelosActivados) {
        G4D.Logger.logln("Construyendo nueva ruta..");
        //
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = fechaHoraInicial;
        //
        if(actual.equals(destino)) {
            G4D.Logger.log_err(" | ERROR: {ORIG == DEST}");
            return null;
        }
        //
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            G4D.Logger.logf("> Vuelos asignados: %d%n",secuenciaDeVuelos.size());
            G4D.Logger.log("Buscando mejor plan de vuelo..");
            Plan mejorPlan = obtenerPlanMasProximo(actual, destino, fechaHoraActual, fechaHoraLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorPlan == null) {
                G4D.Logger.logln(" [NO ENCONTRADO] | Deadline..");
                G4D.Logger.delete_lines(4);
                return null;
            }
            G4D.Logger.logln(" [ENCONTRADO]");
            G4D.Logger.log("Bucando vuelo en tránsito..");
            Vuelo vuelo = mejorPlan.obtenerVueloActivo(fechaHoraActual, vuelosActivados);
            if(vuelo == null) {
                G4D.Logger.logln(" [NO_ENCONTRADO] | Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
                vuelo.instanciarHorarios(fechaHoraActual);
            } else G4D.Logger.logln(" [ENCONTRADO]");
            G4D.Logger.logf("> VUELO ASIGNADO: %s",vuelo.getCodigo());
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegadaUTC();
            actual = vuelo.getPlan().getDestino();
            G4D.Logger.delete_lines(4);
        }
        G4D.Logger.delete_upper_line();
        G4D.Logger.log("DESTINO ALCANZADO. Guardando ruta..");
        ruta.setOrigen(origen);
        ruta.setDestino(destino);
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.instanciarHorarios();
        ruta.setDuracion();
        ruta.setDistancia();
        G4D.Logger.delete_current_line();
        return ruta;
    }
    //
    private Plan obtenerPlanMasProximo(Aeropuerto origen, Aeropuerto destino, LocalDateTime fechaHoraActual,
                                       LocalDateTime fechaHoraLimite, List<Plan> planes, Set<Aeropuerto> visitados,
                                       Set<Vuelo> vuelosActivados) {
        Double menorLejania = Double.MAX_VALUE;
        Plan planMasProximo = null;
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

    // Búsqueda local: Variable Neighborhood Descent
    private void VND(Solucion solucion) {
        G4D.Logger.logln("[VND]");
        //
        boolean huboMejora;
        //
        for(int ele = L_MIN; ele <= L_MAX; ele++) {
            G4D.Logger.logf(">> Intensidad de busqueda: %d de '%d'%n", ele, L_MAX);
            int i = 1, j = 0;
            while (i <= 3) {
                Solucion solucionPropuesta = solucion.replicar();
                huboMejora = false;
                if(j == 3) {
                    j = 0;
                    G4D.Logger.delete_lines(4);
                }
                switch (i) {
                    case 1:
                        huboMejora = LSInsertar(solucionPropuesta, ele);
                        break;
                    case 2:
                        huboMejora = LSIntercambiar(solucionPropuesta,ele);
                        break;
                    case 3:
                        huboMejora = LSRealocar(solucionPropuesta,ele);
                        break;
                }
                j++;
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
    //
    private Boolean LSInsertar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando búsqueda local por 'Inserción'..");
        //
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness(), mejorFitness = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        //
        for (Pedido pedido : pedidos) {
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando aptitud del pedido.. ");
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            if (lotesPorRuta.size() < ele + 1) {
                G4D.Logger.log("[NO APTO]");
                G4D.Logger.delete_current_line();
                continue;
            } else G4D.Logger.log("[APTO]");
            Map<Ruta, Lote> lotesPorRutaAux = new HashMap<>(lotesPorRuta);
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(new ArrayList<>(lotesPorRuta.keySet()), ele);
            int posMejorComb = -1;
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                G4D.Logger.delete_current_line();
                G4D.Logger.log("Validando disponibilidad de rutas destino.. ");
                int combTotalProd = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
                List<Ruta> rutasDest = lotesPorRuta.keySet().stream().filter(r -> !rutasOrig.contains(r)).collect(Collectors.toList());
                int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
                if (capDispTotal < combTotalProd) {
                    G4D.Logger.log("[NO DISPONIBLES]");
                    continue;
                } else G4D.Logger.log("[DISPONIBLES]");
                G4D.Logger.delete_current_line();
                G4D.Logger.logf("Mejor fitness actual: %.3f | >> INSERTANDO..", mejorFitness);
                rutasDest.sort(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC));
                insertarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
                double fitnessObtenido = solucion.getFitness();
                G4D.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (fitnessObtenido < mejorFitness) {
                    G4D.Logger.log(" | ¡NUEVO MEJOR!");
                    mejorFitness = solucion.getFitness();
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4D.Logger.log(" | No fue mejor..");
                deshacerCambios(lotesPorRuta, lotesPorRutaAux, vuelosEnTransito, rutasEnOperacion);
            }
            if(posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                List<Ruta> rutasDest = lotesPorRuta.keySet().stream().filter(r -> !rutasOrig.contains(r)).collect(Collectors.toList());
                rutasDest.sort(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC));
                insertarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
            }
        }
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Inserción' : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4D.Logger.logln(" | {FITNESS OPTIMIZADO}");
        } else G4D.Logger.logln(" | [FITNESS MANTENIDO]");
        return huboMejora;
    }
    //
    private void insertarEleLotes(List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> lotesPorRuta, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        for(Ruta rOrig : rutasOrig) {
            Lote lOrig = lotesPorRuta.get(rOrig);
            int tamanioRestantePorFusionar = lOrig.getTamanio();
            rOrig.eliminarLoteDeProductos(lOrig, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.remove(rOrig);
            for(Ruta rDest : rutasDest) {
                if(tamanioRestantePorFusionar == 0) break;
                int rDestCapDisp = rDest.obtenerCapacidadDisponible();
                if(rDestCapDisp == 0) continue;
                int tamanioDeFusion = Math.min(tamanioRestantePorFusionar, rDestCapDisp);
                Lote lOld = lotesPorRuta.get(rDest);
                int tamanioDeConsolidado = lOld.getTamanio() + tamanioDeFusion;
                rDest.eliminarLoteDeProductos(lOld, vuelosEnTransito, rutasEnOperacion);
                lotesPorRuta.remove(rDest);
                Lote lNew = rDest.getOrigen().generarLoteDeProductos(tamanioDeConsolidado);
                rDest.registraLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
                lotesPorRuta.put(rDest, lNew);
                tamanioRestantePorFusionar -= tamanioDeFusion;
            }
        }
    }
    //
    private void deshacerCambios(Map<Ruta, Lote> lotesPorRuta, Map<Ruta, Lote> lotesPorRutaAux, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        for (Ruta r : lotesPorRuta.keySet()) {
            Lote l = lotesPorRuta.get(r);
            r.eliminarLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
        }
        lotesPorRuta.clear();
        lotesPorRuta.putAll(lotesPorRutaAux);
        for (Ruta r : lotesPorRuta.keySet()) {
            Lote l = lotesPorRuta.get(r);
            r.registraLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
        }
    }
    //
    private Boolean LSIntercambiar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando búsqueda local por 'Intercambio'..");
        //
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness();
        double mejorFitness = fitnessInicial;
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        //
        for (Pedido pedido : pedidos) {
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando aptitud del pedido.. ");
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            if (lotesPorRuta.size() < (2 * ele)) {
                G4D.Logger.log("[NO APTO]");
                continue;
            } else G4D.Logger.log("[APTO]");
            Map<Ruta, Lote> lotesPorRutaAux = new HashMap<>(lotesPorRuta);
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(new ArrayList<>(lotesPorRuta.keySet()), 2*ele);
            int posMejorComb = -1;
            for(int posComb = 0; posComb < combinaciones.size() - 1; posComb++) {
                List<Ruta> combinacion = combinaciones.get(posComb);
                List<Ruta> grupoA = combinacion.subList(0, ele);
                List<Ruta> grupoB = combinacion.subList(ele, combinacion.size());
                G4D.Logger.delete_current_line();
                G4D.Logger.log("Validando disponibilidad de rutas..");
                int capDispA = grupoA.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
                int capDispB = grupoB.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
                int tamanioA = grupoA.stream().mapToInt( r -> lotesPorRuta.get(r).getTamanio()).sum();
                int tamanioB = grupoB.stream().mapToInt( r -> lotesPorRuta.get(r).getTamanio()).sum();
                if (capDispA < tamanioB || capDispB < tamanioA) {
                    G4D.Logger.log("[NO DISPONIBLES]");
                    continue;
                } else G4D.Logger.log("[DISPONIBLES]");
                G4D.Logger.delete_current_line();
                G4D.Logger.logf("Mejor fitness actual: %.3f | >> INTERCAMBIANDO..", mejorFitness);
                grupoA.sort(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC));
                grupoB.sort(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC));
                intercambiarEleLotes(grupoA, grupoB, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
                double fitnessObtenido = solucion.getFitness();
                G4D.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (fitnessObtenido < mejorFitness) {
                    G4D.Logger.log(" | ¡NUEVO MEJOR!");
                    mejorFitness = fitnessObtenido;
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4D.Logger.log(" | No fue mejor. Revirtiendo..");
                deshacerCambios(lotesPorRuta, lotesPorRutaAux, vuelosEnTransito, rutasEnOperacion);
            }
            if(posMejorComb != -1) {
                List<Ruta> combinacion = combinaciones.get(posMejorComb);
                List<Ruta> grupoA = combinacion.subList(0, ele);
                List<Ruta> grupoB = combinacion.subList(ele, combinacion.size());
                grupoA.sort(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC));
                grupoB.sort(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC));
                intercambiarEleLotes(grupoA, grupoB, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
            }
        }
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Intercambio' : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora)
            G4D.Logger.logln(" | {FITNESS OPTIMIZADO}");
        else
            G4D.Logger.logln(" | [FITNESS MANTENIDO]");
        return huboMejora;
    }
    //
    private void intercambiarEleLotes(List<Ruta> grupoA, List<Ruta> grupoB, Map<Ruta, Lote> lotesPorRuta, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        int restanteA = grupoA.stream().mapToInt( r -> lotesPorRuta.get(r).getTamanio()).sum();
        int restanteB = grupoB.stream().mapToInt( r -> lotesPorRuta.get(r).getTamanio()).sum();
        for (Ruta r : grupoA) {
            Lote l = lotesPorRuta.get(r);
            r.eliminarLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.remove(r);
        }
        for (Ruta r : grupoB) {
            Lote l = lotesPorRuta.get(r);
            r.eliminarLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.remove(r);
        }
        for (Ruta r : grupoA) {
            if (restanteB == 0) break;
            int capDisp = r.obtenerCapacidadDisponible();
            if(capDisp == 0) continue;
            int asignar = Math.min(capDisp, restanteB);
            Lote lNew = r.getOrigen().generarLoteDeProductos(asignar);
            r.registraLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.put(r, lNew);
            restanteB -= asignar;
        }
        for (Ruta r : grupoB) {
            if (restanteA == 0) break;
            int capDisp = r.obtenerCapacidadDisponible();
            if(capDisp == 0) continue;
            int asignar = Math.min(capDisp, restanteA);
            Lote lNew = r.getOrigen().generarLoteDeProductos(asignar);
            r.registraLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.put(r, lNew);
            restanteA -= asignar;
        }
    }
    //
    private Boolean LSRealocar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando búsqueda local por 'Realocación'..");
        //
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness();
        double mejorFitness = fitnessInicial;
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        //
        for (Pedido pedido : pedidos) {
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando aptitud del pedido.. ");
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            if (lotesPorRuta.size() < ele) {
                G4D.Logger.log("[NO APTO]");
                continue;
            } else G4D.Logger.log("[APTO]");
            Map<Ruta, Lote> lotesPorRutaAux = new HashMap<>(lotesPorRuta);
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(rutas, ele);
            int posMejorComb = -1;
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                G4D.Logger.delete_current_line();
                G4D.Logger.log("Validando disponibilidad de rutas destino..");
                int totalRealocar = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
                List<Ruta> rutasDest = rutasEnOperacion.stream()
                        .filter(r -> !rutas.contains(r))
                        .filter(r -> r.getDestino().equals(pedido.getDestino()))
                        .filter(r -> !r.getFechaHoraSalidaUTC().isBefore(pedido.getFechaHoraGeneracionUTC()) && !r.getFechaHoraLlegadaUTC().isAfter(pedido.getFechaHoraExpiracionUTC())) // si tienes este método o un criterio temporal
                        .filter(r -> r.obtenerCapacidadDisponible() > 0)
                        .sorted(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC))
                        .collect(Collectors.toList());
                int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
                if (capDispTotal < totalRealocar) {
                    G4D.Logger.log("[NO DISPONIBLES]");
                    continue;
                } else G4D.Logger.log("[DISPONIBLES]");
                G4D.Logger.delete_current_line();
                G4D.Logger.logf("Mejor fitness actual: %.3f | >> REALOCANDO..", mejorFitness);
                realocarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
                double fitnessObtenido = solucion.getFitness();
                G4D.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (fitnessObtenido < mejorFitness) {
                    G4D.Logger.log(" | ¡NUEVO MEJOR!");
                    mejorFitness = fitnessObtenido;
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4D.Logger.log(" | No fue mejor. Revirtiendo..");
                deshacerCambios(lotesPorRuta, lotesPorRutaAux, vuelosEnTransito, rutasEnOperacion);
            }
            if(posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                List<Ruta> rutasDest = rutasEnOperacion.stream()
                        .filter(r -> !rutasOrig.contains(r))
                        .filter(r -> r.getDestino().equals(pedido.getDestino()))
                        .filter(r -> !r.getFechaHoraSalidaUTC().isBefore(pedido.getFechaHoraGeneracionUTC()) && !r.getFechaHoraLlegadaUTC().isAfter(pedido.getFechaHoraExpiracionUTC())) // si tienes este método o un criterio temporal
                        .filter(r -> r.obtenerCapacidadDisponible() > 0)
                        .sorted(Comparator.comparing(Ruta::getFechaHoraLlegadaUTC))
                        .collect(Collectors.toList());
                realocarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
            }
        }
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Realocación' : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora)
            G4D.Logger.logln(" | {FITNESS OPTIMIZADO}");
        else
            G4D.Logger.logln(" | [FITNESS MANTENIDO]");
        return huboMejora;
    }
    //
    private void realocarEleLotes(List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> lotesPorRuta, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        int restante = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
        for (Ruta rOrig : rutasOrig) {
            Lote l = lotesPorRuta.get(rOrig);
            rOrig.eliminarLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.remove(rOrig);
        }
        for (Ruta rDest : rutasDest) {
            if (restante == 0) break;
            int capDisp = rDest.obtenerCapacidadDisponible();
            if (capDisp == 0) continue;
            int asignar = Math.min(capDisp, restante);
            Lote lNew = rDest.getOrigen().generarLoteDeProductos(asignar);
            rDest.registraLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
            lotesPorRuta.put(rDest, lNew);
            restante -= asignar;
        }
    }
    //
    private void Shaking(Solucion solucion, G4D.IntegerWrapper k) {
        G4D.Logger.logln("[RAND]");
        int j = 0;
        double fitnessInicial = solucion.getFitness();
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
                    TInsertar(solucion, ele);
                    break;
                case 1:
                    TIntercambiar(solucion, ele);
                    break;
                case 2:
                    TRealocar(solucion, ele);
                    break;
            }
            j++;
        }
        G4D.Logger.delete_lines(3 + j);
        G4D.Logger.logf("> 'Agitación' : %.3f -> %.3f", fitnessInicial, solucion.getFitness());
    }
    //
    private void TInsertar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando perturbación por 'Inserción'..");
        //
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        //
        for (Pedido pedido : pedidos) {
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando aptitud del pedido.. ");
            if (pedido.getLotesPorRuta().size() < ele + 1) {
                G4D.Logger.log("[NO APTO]");
                G4D.Logger.delete_current_line();
                continue;
            } else G4D.Logger.log("[APTO]");
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(new ArrayList<>(lotesPorRuta.keySet()), ele);
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            int combTotalProd = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
            List<Ruta> rutasDest = lotesPorRuta.keySet().stream().filter(r -> !rutasOrig.contains(r)).collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando disponibilidad de rutas destino.. ");
            if (capDispTotal < combTotalProd) {
                G4D.Logger.log("[NO DISPONIBLES]");
                continue;
            } else G4D.Logger.log("[DISPONIBLES]");
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Insertando..");
            Collections.shuffle(rutasDest);
            insertarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
        }
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Inserción' : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }
    //
    private void TIntercambiar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando perturbación por 'Intercambio'..");
        //
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        //
        for (Pedido pedido : pedidos) {
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando aptitud del pedido.. ");
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            if (lotesPorRuta.size() < (2 * ele)) {
                G4D.Logger.log("[NO APTO]");
                continue;
            } else G4D.Logger.log("[APTO]");
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(new ArrayList<>(lotesPorRuta.keySet()), 2*ele);
            List<Ruta> combinacion = combinaciones.get(random.nextInt(combinaciones.size()));
            List<Ruta> grupoA = combinacion.subList(0, ele);
            List<Ruta> grupoB = combinacion.subList(ele, combinacion.size());
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando disponibilidad de rutas..");
            int capDispA = grupoA.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            int capDispB = grupoB.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            int tamanioA = grupoA.stream().mapToInt( r -> lotesPorRuta.get(r).getTamanio()).sum();
            int tamanioB = grupoB.stream().mapToInt( r -> lotesPorRuta.get(r).getTamanio()).sum();
            if (capDispA < tamanioB || capDispB < tamanioA) {
                G4D.Logger.log("[NO DISPONIBLES]");
                continue;
            } else G4D.Logger.log("[DISPONIBLES]");
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Intercambiando..");
            Collections.shuffle(grupoA);
            Collections.shuffle(grupoB);
            intercambiarEleLotes(grupoA, grupoB, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
        }
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Intercambio' : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }
    //
    private void TRealocar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando perturbación por 'Realocación'..");
        //
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        //
        for (Pedido pedido : pedidos) {
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando aptitud del pedido.. ");
            Map<Ruta, Lote> lotesPorRuta = pedido.getLotesPorRuta();
            if (lotesPorRuta.size() < ele) {
                G4D.Logger.log("[NO APTO]");
                continue;
            } else G4D.Logger.log("[APTO]");
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(rutas, ele);
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Analizando rutas origen...");
            int totalRealocar = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
            List<Ruta> rutasDest = rutasEnOperacion.stream()
                    .filter(r -> !rutas.contains(r))
                    .filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalidaUTC().isBefore(pedido.getFechaHoraGeneracionUTC()) && !r.getFechaHoraLlegadaUTC().isAfter(pedido.getFechaHoraExpiracionUTC())) // si tienes este método o un criterio temporal
                    .filter(r -> r.obtenerCapacidadDisponible() > 0)
                    .collect(Collectors.toList());
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando disponibilidad de rutas destino..");
            int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            if (capDispTotal < totalRealocar) {
                G4D.Logger.log("[NO DISPONIBLES]");
                continue;
            } else G4D.Logger.log("[DISPONIBLES]");
            Collections.shuffle(rutasDest);
            realocarEleLotes(rutasOrig, rutasDest, lotesPorRuta, vuelosEnTransito, rutasEnOperacion);
        }
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Realocación' : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }
    //
    private void VNS(Solucion solucion) {
        Solucion x_best = solucion.replicar();
        G4D.IntegerWrapper t = new G4D.IntegerWrapper(), t_best = new G4D.IntegerWrapper();
        Instant start = Instant.now();
        do {
            G4D.IntegerWrapper k = new G4D.IntegerWrapper(K_MIN);
            solucion.reasignar(x_best.replicar());
            while (k.value <= K_MAX) {
                Solucion x_prima = new Solucion();
                boolean solucionValida = false;
                int intentos = 1;
                // Realizacion de Agitaciones Aleatorias Continuas hasta una Posible Solucion
                while (true) {
                    x_prima = solucion.replicar();
                    G4D.Logger.log("Agitando.. ");
                    Shaking(x_prima, k);
                    if (x_prima.getFitness() < solucion.obtenerUmbralDeAberracion()) {
                        G4D.Logger.logln(" | >> POSIBLE MEJOR SOLUCIÓN");
                        solucionValida = true;
                        break;
                    } else {
                        G4D.Logger.logln(" | >> ABERRACIÓN");
                        if (intentos >= MAX_INTENTOS) {
                            G4D.Logger.log("LIMITE DE INTENTOS ALCANZADO.");
                            break;
                        }
                        intentos++;
                    }
                }

                if (!solucionValida) {
                    k.value++;
                    G4D.Logger.delete_lines(1 + intentos);
                    continue;
                }

                Solucion x_prima_doble = x_prima.replicar();
                G4D.Logger.log("Reoptimizando.. ");
                VND(x_prima_doble);
                G4D.Logger.delete_upper_line();
                G4D.Logger.logf("> 'Reoptimización' : %.3f -> %.3f%n", x_prima.getFitness(), x_prima_doble.getFitness());
                //
                Instant end = Instant.now();
                t.value = (int) Duration.between(start, end).getSeconds();
                G4D.Logger.log("Validando nuevo vencindario.. ");
                NeighborhoodChange(solucion, x_prima_doble, x_best, k, t, t_best);
                G4D.Logger.delete_lines(2 + intentos);
            }
            Instant end = Instant.now();
            Duration duracion = Duration.between(start, end);
            t.value = (int) duracion.getSeconds();
        } while (t.value < T_MAX);
        solucion.reasignar(x_best);
    }
    //
    private void NeighborhoodChange(Solucion solucionAux,
                                    Solucion x_prima_doble, Solucion x_best,
                                    G4D.IntegerWrapper k, G4D.IntegerWrapper t,
                                    G4D.IntegerWrapper t_best) {
        G4D.Logger.log("[NeighborhoodChange]");
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
    //
    public void imprimirSolucionINI(String rutaArchivo) { imprimirSolucion(this.solucionINI, rutaArchivo); }
    //
    public void imprimirSolucionVND(String rutaArchivo) { imprimirSolucion(this.solucionVND, rutaArchivo); }
    //
    public void imprimirSolucionVNS(String rutaArchivo) { imprimirSolucion(this.solucionVNS, rutaArchivo); }
    //
    private void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        G4D.Logger.logf("Cargando archivo 'Solucion' a la ruta '%s'..",rutaArchivo);
        // Declaracion de variables
        int dimLinea = 181;
        // Carga de datos
        try {
            // Inicializaion
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
