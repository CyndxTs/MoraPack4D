/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GVNS.java 
[**/

package pucp.dp1.grupo4d.algoritmo;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import pucp.dp1.grupo4d.modelo.Aeropuerto;
import pucp.dp1.grupo4d.modelo.Cliente;
import pucp.dp1.grupo4d.modelo.LoteDeProductos;
import pucp.dp1.grupo4d.modelo.Pedido;
import pucp.dp1.grupo4d.modelo.PlanDeVuelo;
import pucp.dp1.grupo4d.modelo.Problematica;
import pucp.dp1.grupo4d.modelo.Ruta;
import pucp.dp1.grupo4d.modelo.Solucion;
import pucp.dp1.grupo4d.modelo.TipoRuta;
import pucp.dp1.grupo4d.modelo.Vuelo;
import pucp.dp1.grupo4d.util.G4D;

public class GVNS {
    private static final Integer L_MIN = 1;
    private static final Integer L_MAX = 3;
    private static final Integer K_MIN = 3;
    private static final Integer K_MAX = 5;
    private static final Integer T_MAX = 12;
    private static final Integer MAX_INTENTOS = 7;
    private static final Random random = new Random();
    private Solucion solucion;

    public GVNS() {
        this.solucion = null;
    }
    //
    public void planificar(Problematica problematica) {
        G4D.Logger.Stats.set_global_start();
        // Declaracion de variables
        Solucion solucionAux = new Solucion();
        Solucion x_best = new Solucion();
        G4D.IntegerWrapper t = new G4D.IntegerWrapper(), t_best = new G4D.IntegerWrapper();
        // Solución inicial (Nearest Neighbor)
        G4D.Logger.Stats.set_local_start();
        G4D.Logger.log("Generando solución inicial.. ");
        solucionInicial(problematica, solucionAux);
        G4D.Logger.Stats.set_local_duration();
        G4D.Logger.logf("[+] SOLUCION INICIAL GENERADA! (FITNESS: %.3f)%n", solucionAux.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(solucionAux, "SolucionInicial.txt");
        // Optimización inicial (Variable Neighborhood Descent)
        G4D.Logger.Stats.set_local_start();
        G4D.Logger.log("Realizando optimización inicial.. ");
        VND(problematica, solucionAux);
        G4D.Logger.Stats.set_local_duration();
        G4D.Logger.logf("[+] OPTIMIZACION INICIAL REALIZADA! (FITNESS: %.3f)%n", solucionAux.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(solucionAux, "SolucionVND.txt");
        // Optimización final (Variable Neighborhood Search)
        G4D.Logger.Stats.set_local_start();
        G4D.Logger.logln("Realizando optimización final.. [VNS]");
        x_best = solucionAux.replicar();
        Instant start = Instant.now();
        do {
            G4D.IntegerWrapper k = new G4D.IntegerWrapper(K_MIN);
            solucionAux = x_best.replicar();
            //
            while (k.value <= K_MAX && t.value < T_MAX) {
                Solucion x_prima = new Solucion();
                boolean solucionValida = false;
                int intentos = 0;
                // Realizacion de Agitaciones Aleatorias Continuas hasta una Posible Solucion
                while (true) {
                    x_prima = solucionAux.replicar();
                    G4D.Logger.delete_current_line();
                    Shaking(x_prima, k);
                    if (x_prima.getFitness() < Solucion.PEOR_FITNESS) {
                        solucionValida = true;
                        break;
                    } else {
                        G4D.Logger.logf("%s%n", " [ABERRACION]");
                        intentos++;
                        G4D.Logger.delete_current_line();
                        if (intentos >= MAX_INTENTOS) break;
                    }
                }
                
                if (!solucionValida) {
                    k.value++;
                    continue;
                }
                Solucion x_prima_doble = new Solucion(x_prima);
                G4D.Logger.log("Reoptimizando.. ");
                VND(problematica, x_prima_doble);
                // Actualizar tiempo
                Instant end = Instant.now();
                t.value = (int) Duration.between(start, end).getSeconds();
                // Neighborhood Change
                NeighborhoodChange(problematica, solucionAux, x_prima_doble, x_best, k, t, t_best);
            }
            G4D.Logger.delete_lines(1 + k.value);
            // Actualizar tiempo para condición del bucle externo
            Instant end = Instant.now();
            Duration duracion = Duration.between(start, end);
            t.value = (int) duracion.getSeconds();
        } while (t.value < T_MAX);
        this.solucion = x_best;
        G4D.Logger.Stats.set_local_duration();
        G4D.Logger.logf("[+] OPTIMIZACION FINAL REALIZADA! (FITNESS: %.3f)%n", x_best.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x_best, "SolucionGVNS.txt");
        G4D.Logger.Stats.set_global_duration();
        G4D.Logger.Stats.log_stat_global_sol();
    }
    // Nearest Neighbor
    private void solucionInicial(Problematica problematica, Solucion solucion) {
        G4D.Logger.logln("[NN]");
        // Declaracion de variables
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Pedido> pedidos = problematica.pedidos;
        Set<Aeropuerto> aeropuertosEnUso = new HashSet<>();
        Set<Vuelo> vuelosEnTransito = new HashSet<>();
        Set<Ruta> rutasEnOperacion = new HashSet<>();
        //
        G4D.Logger.Stats.numPed = 1;
        for (Pedido pedido : pedidos) {
            G4D.Logger.Stats.numProd = 1;
            G4D.Logger.Stats.log_stat_ped();
            G4D.Logger.Stats.set_process_start();
            boolean pedidoAtendido = atenderPedido(pedido, origenes, planes, aeropuertosEnUso, vuelosEnTransito, rutasEnOperacion);
            if(!pedidoAtendido) {
                G4D.Logger.Stats.log_err_stat();
                System.exit(1);
            }
            G4D.Logger.Stats.set_proccess_duration();
            G4D.Logger.Stats.next_ped();
        }
        // 
        solucion.setPedidosAtendidos(pedidos);
        solucion.setAeropuertosEnUso(aeropuertosEnUso);
        solucion.setVuelosEnTransito(vuelosEnTransito);
        solucion.setRutasEnOperacion(rutasEnOperacion);
        solucion.setFitness();
    }
    //
    private boolean atenderPedido(Pedido pedido, List<Aeropuerto> origenes, List<PlanDeVuelo> planes,
                                  Set<Aeropuerto> aeropuertosEnUso, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        //
        int cantPorEnrutar = pedido.getCantidadDeProductosSolicitados();
        LocalDateTime fechaHoraInicial = pedido.getFechaHoraGeneracionUTC();
        Aeropuerto destino = pedido.getDestino();
        List<Aeropuerto> origenesDisponibles = new ArrayList<>(origenes);
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        while(!origenesDisponibles.isEmpty()) {
            G4D.Logger.Stats.log_stat_prod();
            G4D.Logger.logf(">>> ATENDIENDO PEDIDO #%d | %d de '%d' productos enrutados.%n", G4D.Logger.Stats.numPed, G4D.Logger.Stats.numProd, pedido.getCantidadDeProductosSolicitados());
            Aeropuerto origen = origenesDisponibles.get(random.nextInt(origenesDisponibles.size()));
            G4D.Logger.logf("Partiendo desde: %s%n", origen);
            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            LocalDateTime fechaHoraLimite = fechaHoraInicial.plusMinutes(tipoRuta.getMaxMinutosParaEntrega());
            boolean rutaReasignada = false;
            Ruta ruta = buscarRutaVoraz(fechaHoraInicial, fechaHoraLimite, origen, destino, rutasAsignadas);
            G4D.Logger.delete_current_line();
            if(ruta == null) {
                Set<Aeropuerto> aeropuertosVisitadosEnRuta = new HashSet<>();
                ruta = construirRutaVoraz(fechaHoraInicial, fechaHoraLimite, origen, destino, planes, aeropuertosVisitadosEnRuta, vuelosActivados);
                if(ruta == null) {
                    G4D.Logger.log("No es posible generar una ruta a partir de este origen.");
                    origenesDisponibles.remove(origen);
                    G4D.Logger.delete_lines(5);
                    continue;
                }
                ruta.setTipo(tipoRuta);
                aeropuertosVisitados.addAll(aeropuertosVisitadosEnRuta);
            } else rutaReasignada = true;
            int rCapDisp = ruta.obtenerCapacidadDisponible();
            int cantEnrutables = Math.min(rCapDisp, cantPorEnrutar);
            G4D.Logger.logf("Enrutando %d productos.. {%s}", cantEnrutables, ruta.getId());
            LoteDeProductos lote = origen.generarLoteDeProductos(cantEnrutables);
            if(rutaReasignada) ruta.agregarLoteDeProductos(lote);
            else ruta.registraLoteDeProductos(lote, vuelosActivados, rutasAsignadas);
            pedido.getLotesPorRuta().put(ruta, lote);
            cantPorEnrutar -= cantEnrutables;
            G4D.Logger.Stats.set_proccess_duration();
            G4D.Logger.Stats.next_lot(cantEnrutables);
            G4D.Logger.log(" | [REALIZADO]");
            if(cantPorEnrutar == 0) {
                G4D.Logger.delete_lines(3);
                G4D.Logger.log(">>> PEDIDO ATENDIDO.");
                pedido.setFechaHoraExpiracion();
                aeropuertosEnUso.addAll(aeropuertosVisitados);
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
                                    Aeropuerto destino, List<PlanDeVuelo> planes, Set<Aeropuerto> aeropuertosVisitados,
                                    Set<Vuelo> vuelosActivados) {
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
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            G4D.Logger.logf("> Vuelos asignados: %d%n",secuenciaDeVuelos.size());
            G4D.Logger.log("Buscando mejor plan de vuelo..");
            PlanDeVuelo mejorPlan = obtenerPlanMasProximo(actual, destino, fechaHoraActual, fechaHoraLimite, planes, aeropuertosVisitados, vuelosActivados);
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
                vuelo.setDuracion();
                vuelo.setDistancia();
            } else G4D.Logger.logln(" [ENCONTRADO]");
            G4D.Logger.logf("> VUELO ASIGNADO: %s",vuelo.getId());
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegadaUTC();
            actual = vuelo.getPlan().getDestino();
            G4D.Logger.delete_lines(4);
        }
        G4D.Logger.delete_upper_line();
        G4D.Logger.log("DESTINO ALCANZADO. Guardando ruta..");
        aeropuertosVisitados.add(actual);
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
    private PlanDeVuelo obtenerPlanMasProximo(Aeropuerto origen, Aeropuerto destino, LocalDateTime fechaHoraActual,
                                              LocalDateTime fechaHoraLimite, List<PlanDeVuelo> planes, Set<Aeropuerto> visitados,
                                              Set<Vuelo> vuelosActivados) {
        Double menorLejania = Double.MAX_VALUE;
        PlanDeVuelo planMasProximo = null;
        List<PlanDeVuelo> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                                                          .filter(p -> !visitados.contains(p.getDestino()))
                                                          .toList();
        for(PlanDeVuelo plan : planesPosibles) {
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
    private void VND(Problematica problematica, Solucion solucion) {
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
                switch (i) {
                    case 1:
                        huboMejora = LSFusionar(solucionPropuesta, ele);
                        j++;
                        break; 
                    case 2:
                        // huboMejora = LSIntercambiar(solucionPropuesta,ele);
                        break;
                    case 3:
                        // huboMejora = LSRealocar(solucionPropuesta,ele);
                        break;
                }
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
    private Boolean LSFusionar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando búsqueda local por 'Fusión'..");
        boolean huboMejora = false;
        Solucion mejorSolucion = solucion.replicar();
        int cantPed = solucion.getPedidosAtendidos().size();
        for (int posPed = 0; posPed < cantPed; posPed++) {
            Pedido pedido = mejorSolucion.getPedidosAtendidos().get(posPed);
            G4D.Logger.log("Validando aptitud del pedido.. ");
            if (pedido.getLotesPorRuta().size() < ele + 1) {
                G4D.Logger.log("[NO APTO]");
                G4D.Logger.delete_current_line();
                continue;
            } else G4D.Logger.log("[APTO]");
            Map<Ruta, LoteDeProductos> lotesPorRuta = pedido.getLotesPorRuta();
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(rutas, ele);
            for (List<Ruta> rutasOrig : combinaciones) {
                int combTotalProd = 0;
                for (Ruta ruta : rutasOrig) combTotalProd += lotesPorRuta.get(ruta).getTamanio();
                int capDispTotal = 0;
                for(Ruta ruta : rutas) {
                    if(rutasOrig.contains(ruta)) continue;
                    capDispTotal += ruta.obtenerCapacidadDisponible();      
                }
                G4D.Logger.delete_current_line();
                G4D.Logger.log("Validando disponibilidad de rutas destino.. ");
                if (capDispTotal < combTotalProd) {
                    G4D.Logger.log("[NO DISPONIBLES]");
                    continue;
                } else G4D.Logger.log("[DISPONIBLES]");
                G4D.Logger.delete_current_line();
                G4D.Logger.logf("Mejor fitness actual: %.3f", mejorSolucion.getFitness());
                Solucion solucionAux = mejorSolucion.replicar();
                Set<Vuelo> vuelosEnTransitoAux = solucionAux.getVuelosEnTransito();
                Set<Ruta> rutasEnOperacionAux = solucionAux.getRutasEnOperacion();
                Pedido pedAux = solucionAux.getPedidosAtendidos().get(posPed);
                Map<Ruta, LoteDeProductos> lotesPorRutaAux = pedAux.getLotesPorRuta();
                List<Ruta> rutasAux = new ArrayList<>(lotesPorRutaAux.keySet());
                List<Ruta> rutasOrigAux = new ArrayList<>();
                List<Ruta> rutasDestAux = new ArrayList<>();
                for (Ruta rutaAux : rutasAux) {
                    if (rutasOrig.contains(rutaAux)) {
                        rutasOrigAux.add(rutaAux);
                    } else rutasDestAux.add(rutaAux);
                }
                for(Ruta rutaOrigAux : rutasOrigAux) {
                    LoteDeProductos loteOrig = lotesPorRutaAux.get(rutaOrigAux);
                    int tamanioRestantePorFusionar = loteOrig.getTamanio();
                    rutaOrigAux.eliminarLoteDeProductos(loteOrig, vuelosEnTransitoAux, rutasEnOperacionAux);
                    lotesPorRutaAux.remove(rutaOrigAux);
                    for(int posRutaDest = 0; tamanioRestantePorFusionar > 0; posRutaDest++) {
                        Ruta rutaDestAux = rutasDestAux.get(posRutaDest);
                        int rCapDisp = rutaDestAux.obtenerCapacidadDisponible();
                        if(rCapDisp == 0) continue;
                        int tamanioDeFusion = Math.min(tamanioRestantePorFusionar, rCapDisp);
                        LoteDeProductos loteDest = lotesPorRutaAux.get(rutaDestAux);
                        int tamanioDeConsolidado = loteDest.getTamanio() + tamanioDeFusion;
                        rutaDestAux.eliminarLoteDeProductos(loteDest, vuelosEnTransitoAux, rutasEnOperacionAux);
                        lotesPorRutaAux.remove(rutaDestAux);
                        LoteDeProductos loteAux = rutaDestAux.getOrigen().generarLoteDeProductos(tamanioDeConsolidado);
                        rutaDestAux.registraLoteDeProductos(loteAux, vuelosEnTransitoAux, rutasEnOperacionAux);
                        lotesPorRutaAux.put(rutaDestAux, loteAux);
                        tamanioRestantePorFusionar -= tamanioDeFusion;
                    }
                }
                solucionAux.setFitness();
                G4D.Logger.logf(" | Fitness obtenido: %.3f", solucionAux.getFitness());
                if (solucionAux.getFitness() < mejorSolucion.getFitness()) {
                    G4D.Logger.log("| {NUEVO MEJOR}");
                    mejorSolucion = solucionAux.replicar();
                    huboMejora = true;
                    break;
                }
            }
        }
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Realocación' : %.3f -> %.3f", solucion.getFitness(), mejorSolucion.getFitness());
        if (huboMejora) {
            G4D.Logger.logln(" | {FITNESS OPTIMIZADO}");
            solucion.reasignar(mejorSolucion);
        } else G4D.Logger.logln(" | [FITNESS MANTENIDO]");
        return huboMejora;
    }
    //
    private void Shaking(Solucion solucion, G4D.IntegerWrapper k) {
        G4D.Logger.logln("Agitando..");
        int j = 0;
        // Perturbar la solución actual para diversificación
        for (int i = 0; i < k.value; ++i) {
            int neighborhood = random.nextInt(3);
            int ele = L_MIN + random.nextInt(L_MAX - L_MIN + 1);
            switch (neighborhood) {
                case 0:
                    TFusionar(solucion, ele);
                    j++;
                    break;
                case 1:
                    // TRealocar(solucion, ele);
                    break;
                case 2:
                    // TIntercambiar(solucion, ele);
                    break;
            }
        }
        G4D.Logger.delete_lines(1 + j);
    }
    //
    private void TFusionar(Solucion solucion, int ele) {
        G4D.Logger.logln("> Realizando perturbación por 'Fusión'..");
        Solucion mejorSolucion = solucion.replicar();
        double fitnessInicial = solucion.getFitness();
        int cantPed = solucion.getPedidosAtendidos().size();
        for (int posPed = 0; posPed < cantPed; posPed++) {
            Pedido pedido = mejorSolucion.getPedidosAtendidos().get(posPed);
            G4D.Logger.log("Validando aptitud del pedido.. ");
            if (pedido.getLotesPorRuta().size() < ele + 1) {
                G4D.Logger.log("[NO APTO]");
                G4D.Logger.delete_current_line();
                continue;
            } else G4D.Logger.log("[APTO]");
            Map<Ruta, LoteDeProductos> lotesPorRuta = pedido.getLotesPorRuta();
            List<Ruta> rutas = new ArrayList<>(lotesPorRuta.keySet());
            List<List<Ruta>> combinaciones = G4D.getPossibleCombinations(rutas, ele);
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            int combTotalProd = 0;
            for (Ruta ruta : rutasOrig) combTotalProd += lotesPorRuta.get(ruta).getTamanio();
            int capDispTotal = 0;
            for(Ruta ruta : rutas) {
                if(rutasOrig.contains(ruta)) continue;
                capDispTotal += ruta.obtenerCapacidadDisponible();      
            }
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Validando disponibilidad de rutas destino.. ");
            if (capDispTotal < combTotalProd) {
                G4D.Logger.log("[NO DISPONIBLES]");
                continue;
            } else G4D.Logger.log("[DISPONIBLES]");
            G4D.Logger.delete_current_line();
            G4D.Logger.log("Fusionando..");
            Solucion solucionAux = mejorSolucion.replicar();
            Set<Vuelo> vuelosEnTransitoAux = solucionAux.getVuelosEnTransito();
            Set<Ruta> rutasEnOperacionAux = solucionAux.getRutasEnOperacion();
            Pedido pedAux = solucionAux.getPedidosAtendidos().get(posPed);
            Map<Ruta, LoteDeProductos> lotesPorRutaAux = pedAux.getLotesPorRuta();
            List<Ruta> rutasAux = new ArrayList<>(lotesPorRutaAux.keySet());
            List<Ruta> rutasOrigAux = new ArrayList<>();
            List<Ruta> rutasDestAux = new ArrayList<>();
            for (Ruta rutaAux : rutasAux) {
                if (rutasOrig.contains(rutaAux)) {
                    rutasOrigAux.add(rutaAux);
                } else rutasDestAux.add(rutaAux);
            }
            for(Ruta rutaOrigAux : rutasOrigAux) {
                LoteDeProductos loteOrig = lotesPorRutaAux.get(rutaOrigAux);
                int tamanioRestantePorFusionar = loteOrig.getTamanio();
                rutaOrigAux.eliminarLoteDeProductos(loteOrig, vuelosEnTransitoAux, rutasEnOperacionAux);
                lotesPorRutaAux.remove(rutaOrigAux);
                for(int posRutaDest = 0; tamanioRestantePorFusionar > 0; posRutaDest++) {
                    Ruta rutaDestAux = rutasDestAux.get(posRutaDest);
                    int rCapDisp = rutaDestAux.obtenerCapacidadDisponible();
                    if(rCapDisp == 0) continue;
                    int tamanioDeFusion = Math.min(tamanioRestantePorFusionar, rCapDisp);
                    LoteDeProductos loteDest = lotesPorRutaAux.get(rutaDestAux);
                    int tamanioDeConsolidado = loteDest.getTamanio() + tamanioDeFusion;
                    rutaDestAux.eliminarLoteDeProductos(loteDest, vuelosEnTransitoAux, rutasEnOperacionAux);
                    lotesPorRutaAux.remove(rutaDestAux);
                    LoteDeProductos loteAux = rutaDestAux.getOrigen().generarLoteDeProductos(tamanioDeConsolidado);
                    rutaDestAux.registraLoteDeProductos(loteAux, vuelosEnTransitoAux, rutasEnOperacionAux);
                    lotesPorRutaAux.put(rutaDestAux, loteAux);
                    tamanioRestantePorFusionar -= tamanioDeFusion;
                }
            }
            G4D.Logger.delete_current_line();
        }
        solucion.setFitness();
        G4D.Logger.delete_upper_line();
        G4D.Logger.logf("> 'Fusión' : %.3f%n", fitnessInicial, solucion.getFitness());
    }
    //
    private void NeighborhoodChange(Problematica problematica, Solucion solucionAux,
                                    Solucion x_prima_doble, Solucion x_best,
                                    G4D.IntegerWrapper k, G4D.IntegerWrapper t, 
                                    G4D.IntegerWrapper mejorT) {
        G4D.Logger.log("Validando cambio de vencindario.. ");
        if (x_prima_doble.getFitness() < x_best.getFitness()) {
            x_best.reasignar(x_prima_doble.replicar());
            solucionAux.reasignar(x_prima_doble);
            k.value = K_MIN;
            mejorT.value = t.value;
            G4D.Logger.logf("| > NUEVO MEJOR [%.3f]", x_best.getFitness());
        } else {
            G4D.Logger.log("| No es mejor.");
            k.value++;
        }
    }
    //
    public void imprimirSolucion(String rutaArchivo) { imprimirSolucion(this.solucion, rutaArchivo); }
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
                    String.format("%03d", pedido.getCantidadDeProductosSolicitados()),
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
}
