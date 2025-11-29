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
import java.util.stream.Stream;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoLote;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEvento;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class GVNS {
    public static Double D_MIN = 0.005;                 // Diferencia mínima considerable de fitness
    public static Integer I_MAX = 1;                    // Número máximo de iteraciones de exploración inicial
    public static Integer L_MIN = 1;                    // Nivel mínimo de búsqueda local
    public static Integer L_MAX = 3;                    // Nivel máximo de búsqueda local
    public static Integer K_MIN = 3;                    // Nivel mínimo de perturbación
    public static Integer K_MAX = 5;                    // Nivel máximo de perturbación
    public static Integer T_MAX = 15;                   // Tiempo máximo esperado de exploración global
    public static Integer MAX_INTENTOS = 5;             // Número de máximo de intentos por nivel de perturbación
    private static final Random random = new Random();
    private Solucion solucion;

    public GVNS() {
        this.solucion = null;
    }

    public void planificar(Problematica problematica) {
        G4DUtility.Logger.Stats.set_global_start();
        // Declaracion & inicialización de variables
        Solucion x = new Solucion();
        // Solución inicial (Nearest Neighbor)
        G4DUtility.Logger.Stats.set_local_start();
        G4DUtility.Logger.log("Generando solución inicial.. ");
        solucionInicial(problematica, x);
        G4DUtility.Logger.Stats.set_local_duration();
        if(x.getFitness() < 0) return;
        G4DUtility.Logger.logf("[+] SOLUCION INICIAL GENERADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x, "SolucionInicial.txt");
        this.solucion = x;
        // Optimización inicial (Variable Neighborhood Descent)
        G4DUtility.Logger.Stats.set_local_start();
        G4DUtility.Logger.log("Realizando optimización inicial.. ");
        VND(x);
        G4DUtility.Logger.Stats.set_local_duration();
        G4DUtility.Logger.logf("[+] OPTIMIZACION INICIAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x, "SolucionVND.txt");
        this.solucion = x;
        // Optimización final (Variable Neighborhood Search)
        G4DUtility.Logger.Stats.set_local_start();
        G4DUtility.Logger.logln("Realizando optimización final.. [VNS]");
        VNS(x);
        G4DUtility.Logger.Stats.set_local_duration();
        G4DUtility.Logger.logf("[+] OPTIMIZACION FINAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x, "SolucionGVNS.txt");
        this.solucion = x;
        G4DUtility.Logger.Stats.set_global_duration();
        G4DUtility.Logger.Stats.log_stat_global_sol();
    }

    private void solucionInicial(Problematica problematica, Solucion solucion) {
        G4DUtility.Logger.logln("[NN]");
        // Declaración & inicialización de variables
        Boolean errorDeEnrutamiento = false, haySolucion = false;
        Solucion sAux = new Solucion();
        Double mejorFitness = sAux.getFitness();
        solucion.reasignar(sAux);
        // Iteración de exploraciones iniciales
        for(int i = 0; i < GVNS.I_MAX; i++) {
            Problematica pAux = problematica.replicar();
            List<Aeropuerto> origenes = pAux.origenes;
            List<Aeropuerto> destinos = pAux.destinos;
            List<Plan> planes = pAux.planes;
            List<Pedido> pedidos = pAux.pedidos;
            Set<Vuelo> vuelosEnTransito = pAux.vuelos;
            Set<Ruta> rutasEnOperacion = pAux.rutas;
            G4DUtility.Logger.logf(">> Iteración: %d de '%d':%n", i + 1, I_MAX);
            errorDeEnrutamiento = false;
            G4DUtility.Logger.Stats.numPed = 1;
            G4DUtility.Logger.Stats.posPed = 0;
            G4DUtility.Logger.Stats.posProd = 0;
            //
            prepararReplanificacion(pedidos, planes);
            G4DUtility.Logger.Stats.set_process_start();
            for (Pedido pedido : pedidos) {
                G4DUtility.Logger.Stats.numProd = 1;
                G4DUtility.Logger.Stats.log_stat_ped();

                if(!pedido.getFueAtendido()) {
                    boolean pedidoAtendido = atenderPedido(pedido, origenes, planes, vuelosEnTransito, rutasEnOperacion);
                    if(!pedidoAtendido) {
                        G4DUtility.Logger.Stats.log_err_stat();
                        errorDeEnrutamiento = true;
                        break;
                    }
                } else {
                    boolean pedidoReatendido = reatenderPedido(pedido, origenes, planes, vuelosEnTransito, rutasEnOperacion);
                    if(!pedidoReatendido) {
                        G4DUtility.Logger.Stats.log_err_stat();
                        errorDeEnrutamiento = true;
                        break;
                    }
                }
                G4DUtility.Logger.Stats.set_proccess_duration();
                G4DUtility.Logger.Stats.next_ped();
            }
            // Validación por error de enrutamiento
            if(errorDeEnrutamiento) {
                G4DUtility.Logger.delete_lines(8);
                continue;
            }
            // Actualización de solución
            sAux.setPedidosAtendidos(pedidos);
            sAux.setVuelosEnTransito(vuelosEnTransito);
            sAux.setRutasEnOperacion(rutasEnOperacion);
            sAux.getAeropuertosTransitados().addAll(origenes);
            sAux.getAeropuertosTransitados().addAll(destinos);
            sAux.setFitness();
            double fitnessObtenido = sAux.getFitness();
            G4DUtility.Logger.logf(":: Mejor fitness completed: %.3f | Fitness obtenido: %.3f", mejorFitness, fitnessObtenido);
            // Validación por mejor solución
            if(fitnessObtenido < mejorFitness) {
                G4DUtility.Logger.logln(" | >> Nuevo mejor!");
                mejorFitness = sAux.getFitness();
                solucion.reasignar(sAux);
                haySolucion = true;
            } else G4DUtility.Logger.logln();
            G4DUtility.Logger.delete_lines(3);
        }
        // Validación por inexistencia de solución
        if(!haySolucion) {
            G4DUtility.Logger.logf_err("ERROR: No fue posible enrutar todos los pedidos en '%d' iteraciones.%n", I_MAX);
            this.solucion = null;
            solucion.setFitness(-9999.99);
        }
    }

    private void prepararReplanificacion(List<Pedido> pedidos, List<Plan> planes) {
        Problematica.PUNTOS_REPLANIFICACION = new ArrayList<>();
        boolean hayReplanificacion = !Problematica.UMBRAL_REPLANIFICACION.isBefore(Problematica.INICIO_PLANIFICACION);
        if (!hayReplanificacion) return;
        List<Pedido> pedidosReplanificables = pedidos.stream().filter(Pedido::getFueAtendido).toList();
        List<Plan> planesProblematicos = planes.stream().filter(Plan::esProblematico).toList();
        for(Pedido pedido : pedidosReplanificables) {
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            for(Map.Entry<Ruta, Lote> entry : segmentacion.entrySet()) {
                Ruta ruta = entry.getKey();
                Lote lote = entry.getValue();
                if(ruta.getEstaOperativa()) {
                    if (!ruta.getFechaHoraLlegada().isAfter(Problematica.UMBRAL_REPLANIFICACION)) {
                        continue;
                    }
                    Vuelo vNodo = ruta.getVuelos().stream().filter(v -> planesProblematicos.contains(v.getPlan())).findFirst().orElse(null);
                    boolean replanificadoAleatoriamente = false;
                    if(vNodo == null && random.nextBoolean()) {
                        if (ruta.getVuelos().size() > 2) {
                            vNodo = ruta.getVuelos().get(random.nextInt(1, ruta.getVuelos().size() - 1));
                            replanificadoAleatoriamente = true;
                        }
                    }
                    if(vNodo != null) {
                        if(!replanificadoAleatoriamente) {
                            ruta.setEstaOperativa(false);
                        }
                        Aeropuerto aNodo = vNodo.getPlan().getOrigen();
                        lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                        ruta.eliminarRegistroDeLoteDeProductos(lote, aNodo, true);
                        int posVNodo = ruta.getVuelos().indexOf(vNodo);
                        LocalDateTime inicio;
                        if(posVNodo > 0) {
                            Vuelo vAnt = ruta.getVuelos().get(posVNodo - 1);
                            inicio = vAnt.getFechaHoraLlegada().isBefore(Problematica.UMBRAL_REPLANIFICACION) ? Problematica.UMBRAL_REPLANIFICACION : vAnt.getFechaHoraLlegada();
                        } else {
                            inicio = Problematica.UMBRAL_REPLANIFICACION.isBefore(pedido.getFechaHoraGeneracion()) ? pedido.getFechaHoraGeneracion(): Problematica.UMBRAL_REPLANIFICACION;
                        }
                        PuntoDeReplanificacion pdr = new PuntoDeReplanificacion();
                        pdr.setRuta(ruta);
                        pdr.setAeropuerto(aNodo);
                        pdr.setFechaHoraInicio(inicio);
                        pdr.getLotes().add(lote);
                        Problematica.PUNTOS_REPLANIFICACION.add(pdr);
                    }
                } else {
                    PuntoDeReplanificacion pdr = Problematica.PUNTOS_REPLANIFICACION.stream().filter(p -> p.getRuta().equals(ruta)).findFirst().orElse(null);
                    if(pdr != null) {
                        lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                        ruta.eliminarRegistroDeLoteDeProductos(lote, pdr.getAeropuerto(), true);
                        pdr.getLotes().add(lote);
                    }
                }
            }
        }
    }


    private boolean atenderPedido(Pedido pedido, List<Aeropuerto> origenes, List<Plan> planes,
                                  Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Declaración & inicialización de variables
        int cantPorEnrutar = pedido.getCantidadSolicitada();
        LocalDateTime fechaHoraGeneracion = pedido.getFechaHoraGeneracion();
        LocalDateTime fechaHoraProcesamiento = pedido.getFechaHoraProcesamiento();
        Aeropuerto destino = pedido.getDestino();
        List<Aeropuerto> origenesDisponibles = new ArrayList<>(origenes);
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        // Exploración aleatoria de orígenes
        while(!origenesDisponibles.isEmpty()) {
            G4DUtility.Logger.Stats.log_stat_prod();
            G4DUtility.Logger.logf(">>> ATENDIENDO PEDIDO #%d | %d de '%d' productos enrutados.%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.numProd - 1, pedido.getCantidadSolicitada());
            Aeropuerto origen = origenesDisponibles.get(random.nextInt(origenesDisponibles.size()));
            G4DUtility.Logger.logf("> ORIGEN:  %s%n> DESTINO: %s%n", origen, destino);
            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            LocalDateTime fechaHoraLimite = fechaHoraGeneracion.plusMinutes(tipoRuta.getMaxMinutosParaEntrega());
            // Búsqueda de ruta reutilizable
            Ruta ruta = buscarRutaVoraz(fechaHoraProcesamiento, fechaHoraLimite, origen, destino, rutasAsignadas);
            G4DUtility.Logger.delete_upper_line();
            if(ruta == null) {
                // Construcción de nueva ruta
                ruta = construirRutaVoraz(fechaHoraProcesamiento, fechaHoraLimite, origen, destino, planes, vuelosActivados);
                if(ruta == null) {
                    G4DUtility.Logger.logln_err("ERROR: No fue posible generar una ruta a partir de este origen.");
                    origenesDisponibles.remove(origen);
                    if(!origenesDisponibles.isEmpty()) G4DUtility.Logger.delete_lines(8);
                    else G4DUtility.Logger.delete_lines(6);
                    continue;
                } else G4DUtility.Logger.delete_upper_line();
                ruta.setTipo(tipoRuta);
            }
            G4DUtility.Logger.logf("> RUTA:    %s -> '%d' intermedios -> %s%n", origen.getCodigo(), ruta.getVuelos().size() - 1, destino.getCodigo());
            // Segmentación de pedido respecto a disponibilidad de ruta
            int rCapDisp = ruta.obtenerCapacidadDisponible();
            int cantEnrutables = Math.min(rCapDisp, cantPorEnrutar);
            G4DUtility.Logger.logf("Enrutando %d productos..", cantEnrutables);
            // Producción y registro de segmento de pedido
            Lote lote = origen.generarLoteDeProductos(cantEnrutables);
            ruta.registraLoteDeProductos(lote, vuelosActivados, rutasAsignadas);
            Segmentacion segmentacion = pedido.obtenerSegementacionVigente();
            if(segmentacion == null) {
                segmentacion = new Segmentacion();
                segmentacion.setFechaHoraAplicacion(fechaHoraProcesamiento);
                pedido.getSegmentaciones().add(segmentacion);
            }
            segmentacion.getLotesPorRuta().put(ruta, lote);
            cantPorEnrutar -= cantEnrutables;
            G4DUtility.Logger.Stats.set_proccess_duration();
            G4DUtility.Logger.Stats.next_lot(cantEnrutables);
            G4DUtility.Logger.logln(" | [REALIZADO]");
            // Validación por atención completa de pedido
            if(cantPorEnrutar == 0) {
                pedido.setFueAtendido(true);
                G4DUtility.Logger.delete_lines(6);
                G4DUtility.Logger.logf(">>> PEDIDO #%d ATENDIDO! | '%d' de '%d' productos enrutados!%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.numProd, pedido.getCantidadSolicitada());
                pedido.setFechaHoraExpiracion();
                vuelosEnTransito.addAll(vuelosActivados);
                rutasEnOperacion.addAll(rutasAsignadas);
                G4DUtility.Logger.delete_lines(6);
                return true;
            }
            G4DUtility.Logger.delete_lines(8);
        }
        return false;
    }



    private Ruta buscarRutaVoraz(LocalDateTime fechaHoraInicial, LocalDateTime fechaHoraLimite,
                                 Aeropuerto origen, Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        G4DUtility.Logger.log("Buscando ruta en operacion..");
        // Validación por origen factible
        if(origen.equals(destino)) {
            G4DUtility.Logger.log(" [NO ENCONTRADA]");
            G4DUtility.Logger.logln_err(" | ERROR: ORIGEN == DESTINO");
            return null;
        }
        // Búsqueda de ruta reutilizable
        List<Ruta> rutasPosibles = rutasAsignadas.stream().filter(r -> r.getOrigen().equals(origen) && r.getDestino().equals(destino)).filter(Ruta::getEstaOperativa).toList();
        for(Ruta ruta : rutasPosibles) {
            if(!ruta.esAlcanzable(fechaHoraInicial, fechaHoraLimite)) continue;
            G4DUtility.Logger.logln(" [ENCONTRADA]");
            return ruta;
        }
        G4DUtility.Logger.logln(" [NO ENCONTRADA]");
        return null;
    }

    private Ruta construirRutaVoraz(LocalDateTime fechaHoraInicial, LocalDateTime fechaHoraLimite, Aeropuerto origen,
                                    Aeropuerto destino, List<Plan> planes, Set<Vuelo> vuelosActivados) {
        G4DUtility.Logger.log("Construyendo nueva ruta..");
        // Validación por origen factible
        if(origen.equals(destino)) {
            G4DUtility.Logger.logln_err(" | ERROR: ORIGEN == DESTINO");
            return null;
        } else G4DUtility.Logger.logln();
        // Declaración & inicialización de variables
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = fechaHoraInicial;
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        // Construcción de nueva ruta
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            G4DUtility.Logger.logf("- Num. vuelos asignados: %d%n", secuenciaDeVuelos.size());
            G4DUtility.Logger.logf("- Aeropuerto completed: %s%n", actual);
            // Búsqueda de plan de vuelo más próximo
            G4DUtility.Logger.log(": Buscando mejor plan de vuelo..");
            Plan mejorPlan = obtenerPlanMasProximo(actual, destino, fechaHoraActual, fechaHoraLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorPlan == null) {
                G4DUtility.Logger.log(" [NO ENCONTRADO]");
                G4DUtility.Logger.delete_lines(4);
                G4DUtility.Logger.log("Construyendo nueva ruta..");
                G4DUtility.Logger.logln_err(" | ERROR: Deadline..");
                return null;
            } else G4DUtility.Logger.logln(" [ENCONTRADO]");
            // Búsqueda de vuelo activo con el mejor plan
            G4DUtility.Logger.log(": Bucando vuelo en tránsito..");
            Vuelo vuelo = mejorPlan.obtenerVueloActivo(fechaHoraActual, vuelosActivados);
            if(vuelo == null) {
                G4DUtility.Logger.logln(" [NO_ENCONTRADO] | Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
                vuelo.instanciarHorarios(fechaHoraActual);
            } else G4DUtility.Logger.logln(" [ENCONTRADO]");
            // Asignación de vuelo
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegada();
            actual = vuelo.getPlan().getDestino();
            G4DUtility.Logger.logf(": Vuelo asignado: %s -> %s%n", vuelo.getPlan().getOrigen().getCodigo(), actual.getCodigo());
            G4DUtility.Logger.delete_lines(6);
        }
        G4DUtility.Logger.delete_upper_line();
        // Actualización de ruta construida
        G4DUtility.Logger.logln("DESTINO ALCANZADO! | Guardando ruta..");
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
                .flatMap(p -> {
                    boolean hayEventosValidos = p.getEventos().stream().anyMatch(e -> !fechaHoraActual.isBefore(e.getFechaHoraInicio()) && !fechaHoraActual.isAfter(e.getFechaHoraFin()));
                    if (!hayEventosValidos) {
                        return Stream.of(p);
                    }
                    List<Plan> planesGenerados = p.getEventos().stream().filter(e -> e.getTipo().equals(TipoEvento.REPROGRAMACION))
                            .filter(e -> !fechaHoraActual.isBefore(e.getFechaHoraSalida()) && !fechaHoraActual.isAfter(e.getFechaHoraFin()))
                            .map(e -> {
                                Plan nuevo = new Plan(p);
                                nuevo.setHoraSalida(e.getFechaHoraSalida().toLocalTime());
                                nuevo.setHoraLlegada(e.getFechaHoraSalida().toLocalTime());
                                nuevo.setEventos(new ArrayList<>());
                                return nuevo;
                            }).toList();
                    return planesGenerados.stream();
                }).toList();
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

    private boolean reatenderPedido(Pedido pedido, List<Aeropuerto> origenes, List<Plan> planes,
                                    Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
        List<Lote> lotesPorReplanificar = segmentacion.values().stream().filter(l -> l.getEstado().equals(EstadoLote.POR_REPLANIFICAR)).toList();
        if (lotesPorReplanificar.isEmpty()) {
            return true;
        }
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        Aeropuerto destino = pedido.getDestino();
        LocalDateTime fechaHoraLimite = pedido.getFechaHoraExpiracion();
        Map<Ruta, Lote> nuevaSegmentacion = new HashMap<>(segmentacion);
        segmentacion.keySet().stream().filter(r -> segmentacion.get(r).getEstado().equals(EstadoLote.POR_REPLANIFICAR)).toList().forEach(nuevaSegmentacion::remove);
        while(!lotesPorReplanificar.isEmpty()) {
            Lote lReplanificar = lotesPorReplanificar.get(random.nextInt(lotesPorReplanificar.size()));
            int restantePorReplanificar = lReplanificar.getTamanio();
            PuntoDeReplanificacion pdr = Problematica.PUNTOS_REPLANIFICACION.stream().filter(p -> p.getLotes().contains(lReplanificar)).findFirst().orElse(null);
            Ruta rutaAsignadaInicial = segmentacion.keySet().stream().filter(r -> segmentacion.get(r).equals(lReplanificar)).findFirst().orElse(null);
            while(restantePorReplanificar > 0) {
                // Búsqueda de ruta reutilizable
                Ruta ruta = buscarRutaVoraz(pdr, fechaHoraLimite, destino, rutasAsignadas);
                if(ruta == null) {
                    // Construcción de nueva ruta
                    ruta = construirRutaVoraz(rutaAsignadaInicial, pdr, fechaHoraLimite, destino, planes, vuelosActivados);
                    if(ruta == null) {
                        return false;
                    }
                    ruta.setTipo(rutaAsignadaInicial.getTipo());
                }
                // Segmentación de pedido respecto a disponibilidad de ruta
                int rCapDisp = ruta.obtenerCapacidadDisponible();
                int cantEnrutables = Math.min(rCapDisp, restantePorReplanificar);
                G4DUtility.Logger.logf("Enrutando %d productos..", cantEnrutables);
                // Producción y registro de segmento de pedido
                Lote lote = ruta.getOrigen().generarLoteDeProductos(cantEnrutables);
                ruta.registraLoteDeProductos(lote, vuelosActivados, rutasAsignadas);
                if(nuevaSegmentacion.containsKey(ruta)) {
                    Lote lPorConsolidar = nuevaSegmentacion.get(ruta);
                    lPorConsolidar.setEstado(EstadoLote.REPLANIFICADO);
                    lote.setTamanio(lote.getTamanio() + lPorConsolidar.getTamanio());
                    nuevaSegmentacion.remove(ruta);
                }
                nuevaSegmentacion.put(ruta, lote);
                restantePorReplanificar -= cantEnrutables;
                G4DUtility.Logger.Stats.set_proccess_duration();
                G4DUtility.Logger.Stats.next_lot(cantEnrutables);
                G4DUtility.Logger.logln(" | [REALIZADO]");
                // Validación por atención completa de pedido
                if(restantePorReplanificar == 0) {
                    vuelosEnTransito.addAll(vuelosActivados);
                    rutasEnOperacion.addAll(rutasAsignadas);
                    lReplanificar.setEstado(EstadoLote.REPLANIFICADO);
                    lotesPorReplanificar.remove(lReplanificar);
                }
            }
        }
        Segmentacion nuevaSeg = new Segmentacion();
        pedido.obtenerSegementacionVigente().setFechaHoraSustitucion(Problematica.UMBRAL_REPLANIFICACION);
        nuevaSeg.setFechaHoraAplicacion(Problematica.UMBRAL_REPLANIFICACION);
        nuevaSeg.setLotesPorRuta(nuevaSegmentacion);
        pedido.getSegmentaciones().add(nuevaSeg);
        pedido.setFechaHoraExpiracion();
        return true;
    }

    private Ruta buscarRutaVoraz(PuntoDeReplanificacion pdr, LocalDateTime fechaHoraLimite, Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        Aeropuerto origen = pdr.getAeropuerto();
        if(origen.equals(destino)) {
            G4DUtility.Logger.log(" [NO ENCONTRADA]");
            G4DUtility.Logger.logln_err(" | ERROR: ORIGEN == DESTINO");
            return null;
        }
        // Búsqueda de ruta reutilizable
        LocalDateTime fechaHoraInicial = pdr.getFechaHoraInicio();
        List<Ruta> rutasPosibles = rutasAsignadas.stream().filter(r -> r.getDestino().equals(destino))
                                                          .filter(Ruta::getEstaOperativa)
                                                          .filter(r -> {
                                                              List<Aeropuerto> secuencia = r.obtenerSecuenciaDeAeropuertos();
                                                              int posOrigen = secuencia.indexOf(origen);
                                                              if(posOrigen == -1) return false;
                                                              if(posOrigen >= r.getVuelos().size()) return false;
                                                              Vuelo vueloDesdeOrigen = r.getVuelos().get(posOrigen);
                                                              return !vueloDesdeOrigen.getFechaHoraSalida().isBefore(fechaHoraInicial);
                                                          }).toList();
        for(Ruta ruta : rutasPosibles) {
            if(!ruta.esAlcanzable(fechaHoraInicial, fechaHoraLimite)) continue;
            G4DUtility.Logger.logln(" [ENCONTRADA]");
            return ruta;
        }
        G4DUtility.Logger.logln(" [NO ENCONTRADA]");
        return null;
    }


    private Ruta construirRutaVoraz(Ruta rutaAsignadaInicial, PuntoDeReplanificacion pdr, LocalDateTime fechaHoraLimite, Aeropuerto destino, List<Plan> planes, Set<Vuelo> vuelosActivados) {
        G4DUtility.Logger.log("Construyendo nueva ruta..");
        Aeropuerto origen = pdr.getAeropuerto();
        // Validación por origen factible
        if(origen.equals(destino)) {
            G4DUtility.Logger.logln_err(" | ERROR: ORIGEN == DESTINO");
            return null;
        } else G4DUtility.Logger.logln();
        // Declaración & inicialización de variables
        Ruta ruta = new Ruta();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = pdr.getFechaHoraInicio();
        List<Aeropuerto> saInicial = rutaAsignadaInicial.obtenerSecuenciaDeAeropuertos();
        int posConexion = saInicial.indexOf(origen);
        List<Vuelo> secuenciaDeVuelos = (posConexion > 0) ? new ArrayList<>(rutaAsignadaInicial.getVuelos().subList(0, posConexion)) : new ArrayList<>();
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>(saInicial);
        // Construcción de nueva ruta
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            G4DUtility.Logger.logf("- Num. vuelos asignados: %d%n", secuenciaDeVuelos.size());
            G4DUtility.Logger.logf("- Aeropuerto completed: %s%n", actual);
            // Búsqueda de plan de vuelo más próximo
            G4DUtility.Logger.log(": Buscando mejor plan de vuelo..");
            Plan mejorPlan = obtenerPlanMasProximo(actual, destino, fechaHoraActual, fechaHoraLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorPlan == null) {
                G4DUtility.Logger.log(" [NO ENCONTRADO]");
                G4DUtility.Logger.delete_lines(4);
                G4DUtility.Logger.log("Construyendo nueva ruta..");
                G4DUtility.Logger.logln_err(" | ERROR: Deadline..");
                return null;
            } else G4DUtility.Logger.logln(" [ENCONTRADO]");
            // Búsqueda de vuelo activo con el mejor plan
            G4DUtility.Logger.log(": Bucando vuelo en tránsito..");
            Vuelo vuelo = mejorPlan.obtenerVueloActivo(fechaHoraActual, vuelosActivados);
            if(vuelo == null) {
                G4DUtility.Logger.logln(" [NO_ENCONTRADO] | Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
                vuelo.instanciarHorarios(fechaHoraActual);
            } else G4DUtility.Logger.logln(" [ENCONTRADO]");
            // Asignación de vuelo
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegada();
            actual = vuelo.getPlan().getDestino();
            G4DUtility.Logger.logf(": Vuelo asignado: %s -> %s%n", vuelo.getPlan().getOrigen().getCodigo(), actual.getCodigo());
            G4DUtility.Logger.delete_lines(6);
        }
        G4DUtility.Logger.delete_upper_line();
        // Actualización de ruta construida
        G4DUtility.Logger.logln("DESTINO ALCANZADO! | Guardando ruta..");
        ruta.setOrigen(origen);
        ruta.setDestino(destino);
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.instanciarHorarios();
        ruta.setDuracion();
        ruta.setDistancia();
        return ruta;
    }


    private void VND(Solucion solucion) {
        G4DUtility.Logger.logln("[VND]");
        // Declaración & inicialización de variables
        boolean huboMejora;
        // Búsqueda local de soluciones por nivel de intensidad
        for(int ele = L_MIN; ele <= L_MAX; ele++) {
            G4DUtility.Logger.logf(">> Intensidad de busqueda: %d de '%d'%n", ele, L_MAX);
            int i = 1, j = 0;
            // Iteración de vecindarios a visitar
            while (i <= 3) {
                Solucion solucionPropuesta = solucion.replicar();
                huboMejora = false;
                if(j == 3) {
                    j = 0;
                    G4DUtility.Logger.delete_lines(4);
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
            G4DUtility.Logger.delete_lines(2 + j);
        }
    }

    private Boolean LSCompactar(Solucion solucion, int ele) {
        G4DUtility.Logger.logln("> Realizando búsqueda local por 'Compactación'..");
        // Declaración & inicialización de variables
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness(), mejorFitness = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Compactación de segmentación de pedidos
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionAux = new HashMap<>(segmentacion);
            Map<Ruta, Lote> segmentacionModificable = new HashMap<>();
            Map<Ruta, List<Aeropuerto>> secuenciasIntocables = new HashMap<>();
            // Carga de restricciones por replanificación
            pedido.cargarRestriccionesDeReplanificacion(segmentacionModificable, secuenciasIntocables);
            // Validación de aptitud
            G4DUtility.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, pedidos.size());
            if (segmentacionModificable.size() < ele + 1) {
                G4DUtility.Logger.logln(" [NO APTO]");
                G4DUtility.Logger.delete_upper_line();
                continue;
            } else G4DUtility.Logger.logln(" [APTO]");
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(new ArrayList<>(segmentacionModificable.keySet()), ele);
            int posMejorComb = -1;
            // Iteración de combinaciones
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                G4DUtility.Logger.logf(": Validando combinación #%d de '%d'..", posComb+1, combinaciones.size());
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                int totalCompactar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
                List<Ruta> rutasDest = segmentacionModificable.keySet().stream().filter(r -> !rutasOrig.contains(r))
                        .filter(r -> r.obtenerCapacidadDisponible() > 0)
                        .filter(r -> r.respetaSecuenciasInalterables(rutasOrig, secuenciasIntocables))
                        .collect(Collectors.toList());
                int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
                if (capDispTotal < totalCompactar) {
                    G4DUtility.Logger.logln(" [INVALIDA]");
                    G4DUtility.Logger.delete_upper_line();
                    continue;
                } else G4DUtility.Logger.logln("[VALIDA]");
                // Compactación
                G4DUtility.Logger.logf(": Mejor fitness completed: %.3f | >> COMPACTANDO..", mejorFitness);
                rutasDest.sort(Comparator.comparing(Ruta::getFechaHoraLlegada));
                compactarSegmentacion(rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
                double fitnessObtenido = solucion.getFitness();
                G4DUtility.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, D_MIN)) {
                    G4DUtility.Logger.logln(" | ¡NUEVO MEJOR!");
                    mejorFitness = solucion.getFitness();
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4DUtility.Logger.logln();
                // Reversión de cambios
                revertirCambios(segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion);
                G4DUtility.Logger.delete_lines(3);
            }
            // Aplicar mejor combinación
            if (posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                List<Ruta> rutasDest = segmentacionModificable.keySet().stream().filter(r -> !rutasOrig.contains(r))
                        .filter(r -> r.obtenerCapacidadDisponible() > 0)
                        .filter(r -> r.respetaSecuenciasInalterables(rutasOrig, secuenciasIntocables))
                        .collect(Collectors.toList());
                rutasDest.sort(Comparator.comparing(Ruta::getFechaHoraLlegada));
                compactarSegmentacion(rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion);
            }
            G4DUtility.Logger.delete_upper_line();
        }
        solucion.setFitness();
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Compactación' : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4DUtility.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4DUtility.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private void compactarSegmentacion(List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> segmentacion, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Iteración por orígenes para compactar
        for(Ruta rOrig : rutasOrig) {
            Lote lOrig = segmentacion.get(rOrig);
            int tamanioRestantePorFusionar = lOrig.getTamanio();
            rOrig.eliminarRegistroDeLoteDeProductos(lOrig);
            segmentacion.remove(rOrig);
            // Compactación en destinos seleccionados
            for(Ruta rDest : rutasDest) {
                if(tamanioRestantePorFusionar == 0) break;
                int rDestCapDisp = rDest.obtenerCapacidadDisponible();
                if(rDestCapDisp == 0) continue;
                int tamanioDeFusion = Math.min(tamanioRestantePorFusionar, rDestCapDisp);
                Lote lOld = segmentacion.get(rDest);
                int tamanioDeConsolidado = lOld.getTamanio() + tamanioDeFusion;
                rDest.eliminarRegistroDeLoteDeProductos(lOld);
                segmentacion.remove(rDest);
                Lote lNew = rDest.getOrigen().generarLoteDeProductos(tamanioDeConsolidado);
                rDest.registraLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
                segmentacion.put(rDest, lNew);
                tamanioRestantePorFusionar -= tamanioDeFusion;
            }
        }
    }

    private void revertirCambios(Map<Ruta, Lote> segmentacion, Map<Ruta, Lote> segmentacionAux, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Eliminación de registros actualizados
        for (Ruta r : segmentacion.keySet()) {
            Lote l = segmentacion.get(r);
            r.eliminarRegistroDeLoteDeProductos(l);
        }
        segmentacion.clear();
        // Agregación de registros antiguos
        segmentacion.putAll(segmentacionAux);
        for (Ruta r : segmentacion.keySet()) {
            Lote l = segmentacion.get(r);
            r.registraLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
        }
    }

    private Boolean LSFusionar(Solucion solucion, int ele) {
        G4DUtility.Logger.logln("> Realizando búsqueda local por 'Fusión'..");
        // Declaración & inicialización de variables
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness();
        double mejorFitness = fitnessInicial;
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Fusión de rutas de pedidos
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            G4DUtility.Logger.logf("- Evaluando rutas del pedido #%d de '%d'..%n", posPedido+1, pedidos.size());
            Pedido pedido = pedidos.get(posPedido);
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionModificable = new HashMap<>();
            Map<Ruta, List<Aeropuerto>> secuenciasIntocables = new HashMap<>();
            // Carga de restricciones por replanificación
            pedido.cargarRestriccionesDeReplanificacion(segmentacionModificable, secuenciasIntocables);
            List<Ruta> rutasIni = new ArrayList<>(segmentacionModificable.keySet());
            List<Ruta> rutasFin = rutasEnOperacion.stream().filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalida().isBefore(pedido.getFechaHoraGeneracion()) && !r.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion()))
                    .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                    .collect(Collectors.toList());
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getCrossedCombinations(rutasIni, rutasFin, 1, 1);
            int cantCombinaciones = Math.min(ele, combinaciones.size());
            int posMejorComb = -1, posMejorConexionIni = -1, posMejorConexionFin = -1;
            // Combinaciones
            for(int posComb = 0; posComb < cantCombinaciones; posComb++) {
                // Validación de aptitud de combinación
                G4DUtility.Logger.logf(": Validando combinación #%d de '%d'..", posComb+1, cantCombinaciones);
                List<Ruta> combinacion = combinaciones.get(posComb);
                Ruta rIni = combinacion.get(0);
                Ruta rFin = combinacion.get(1);
                List<Aeropuerto> saFin = rFin.obtenerSecuenciaDeAeropuertos();
                if(saFin.size() < 3) {
                    G4DUtility.Logger.logln(" [INVALIDA]");
                    G4DUtility.Logger.delete_upper_line();
                    continue;
                } else G4DUtility.Logger.logln(" [VALIDA]");
                for(int posConexionFin = saFin.size() - 2, numConexion = 1; posConexionFin > 0; posConexionFin--, numConexion++) {
                    G4DUtility.Logger.logf(": Validando conexion #%d de '%d'..", numConexion, saFin.size());
                    Aeropuerto aConexion = saFin.get(posConexionFin);
                    List<Aeropuerto> saIni = rIni.obtenerSecuenciaDeAeropuertos();
                    int posConexionIni = saIni.indexOf(aConexion);
                    if(posConexionIni == -1) {
                        G4DUtility.Logger.logln(" [INVALIDA]");
                        G4DUtility.Logger.delete_upper_line();
                        continue;
                    }
                    // Validación de aptitud de fusión por existencia de disponibilidad de ruta
                    List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posConexionIni + 1));
                    LocalDateTime fechaHoraLlegadaAConexion = svIni.getLast().getFechaHoraLlegada();
                    List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posConexionFin + 1, rFin.getVuelos().size()));
                    LocalDateTime fechaHoraSalidaDesdeConexion = svIni.getFirst().getFechaHoraSalida();
                    if(fechaHoraLlegadaAConexion.isAfter(fechaHoraSalidaDesdeConexion) || fechaHoraLlegadaAConexion.plusMinutes((long)(60*Problematica.MAX_HORAS_ESTANCIA)).isAfter(fechaHoraSalidaDesdeConexion)) {
                        G4DUtility.Logger.logln(" [INVALIDA]");
                        G4DUtility.Logger.delete_upper_line();
                        continue;
                    }
                    List<Vuelo> svNew = new ArrayList<>();
                    svNew.addAll(svIni);
                    svNew.addAll(svFin);
                    Ruta rNew = new Ruta(rIni);
                    rNew.setVuelos(svNew);
                    if(!rNew.respetaSecuenciasInalterables(List.of(rIni), secuenciasIntocables)) {
                        G4DUtility.Logger.logln(" [INVALIDA]");
                        G4DUtility.Logger.delete_upper_line();
                        continue;
                    }
                    Lote lote = segmentacion.get(rIni);
                    rIni.eliminarRegistroDeLoteDeProductos(lote);
                    if(rNew.obtenerCapacidadDisponible() < lote.getTamanio()) {
                        G4DUtility.Logger.logln(" [INVALIDA]");
                        rIni.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                        G4DUtility.Logger.delete_upper_line();
                        continue;
                    } else G4DUtility.Logger.logln(" [VALIDA]");
                    // Fusión
                    G4DUtility.Logger.logf(": Mejor fitness completed: %.3f | >> FUSIONANDO..", mejorFitness);
                    segmentacion.remove(rIni);
                    rNew.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                    segmentacion.put(rNew, lote);
                    solucion.setFitness();
                    double fitnessObtenido = solucion.getFitness();
                    G4DUtility.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                    // Validación por mejora de fitness
                    if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, D_MIN)) {
                        G4DUtility.Logger.logln(" | ¡NUEVO MEJOR!");
                        mejorFitness = fitnessObtenido;
                        huboMejora = true;
                        posMejorComb = posComb;
                        posMejorConexionIni = posConexionIni;
                        posMejorConexionFin = posConexionFin;
                    } else G4DUtility.Logger.logln();
                    // Reversión de cambios [Nuevos]
                    rNew.eliminarRegistroDeLoteDeProductos(lote);
                    segmentacion.remove(rNew);
                    rIni.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                    segmentacion.put(rIni, lote);
                    G4DUtility.Logger.delete_lines(3);
                }
                G4DUtility.Logger.delete_upper_line();
            }
            // Validación de existencia de mejora por fusión
            if(posMejorComb != -1) {
                List<Ruta> combinacion = combinaciones.get(posMejorComb);
                Ruta rIni = combinacion.get(0);
                Ruta rFin = combinacion.get(1);
                int posConexionIni = posMejorConexionIni;
                int posConexionFin = posMejorConexionFin;
                List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posConexionIni));
                List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posConexionFin, rFin.getVuelos().size()));
                List<Vuelo> svNew = new ArrayList<>();
                svNew.addAll(svIni);
                svNew.addAll(svFin);
                Ruta rNew = new Ruta(rIni);
                rNew.setVuelos(svNew);
                Lote lote = segmentacion.get(rIni);
                rIni.eliminarRegistroDeLoteDeProductos(lote);
                segmentacion.remove(rIni);
                rNew.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                segmentacion.put(rNew, lote);
            }
            G4DUtility.Logger.delete_upper_line();
        }
        // Actualización de solución
        solucion.setFitness();
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Fusión'       : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4DUtility.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4DUtility.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private Boolean LSRealocar(Solucion solucion, int ele) {
        G4DUtility.Logger.logln("> Realizando búsqueda local por 'Realocación'..");
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness();
        double mejorFitness = fitnessInicial;
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionAux = new HashMap<>(segmentacion);
            Map<Ruta, Lote> segmentacionModificable = new HashMap<>();
            Map<Ruta, List<Aeropuerto>> secuenciasIntocables = new HashMap<>();
            pedido.cargarRestriccionesDeReplanificacion(segmentacionModificable, secuenciasIntocables);
            // Validación de aptitud
            G4DUtility.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, pedidos.size());
            if (segmentacionModificable.size() < ele) {
                G4DUtility.Logger.logln(" [NO APTO]");
                G4DUtility.Logger.delete_upper_line();
                continue;
            } else G4DUtility.Logger.logln("[APTO]");
            List<Ruta> rutas = new ArrayList<>(segmentacionModificable.keySet());
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(rutas, ele);
            List<Ruta> rutasDest = rutasEnOperacion.stream().filter(r -> !rutas.contains(r))
                    .filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalida().isBefore(pedido.getFechaHoraGeneracion()) && !r.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion()))
                    .filter(r -> r.obtenerCapacidadDisponible() > 0)
                    .filter(r -> r.respetaSecuenciasInalterables(rutas, secuenciasIntocables))
                    .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                    .collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            int posMejorComb = -1;
            // Iteración de combinaciones
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                G4DUtility.Logger.logf(": Validando combinación #%d de '%d'..", posComb+1, combinaciones.size());
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                int totalRealocar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
                if (capDispTotal < totalRealocar) {
                    G4DUtility.Logger.logln(" [INVALIDA]");
                    G4DUtility.Logger.delete_upper_line();
                    continue;
                } else G4DUtility.Logger.logln(" [VALIDA]");
                // Realocación
                G4DUtility.Logger.logf(": Mejor fitness completed: %.3f | >> REALOCANDO..", mejorFitness);
                realocarSegmentacion(rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion);
                solucion.setFitness();
                double fitnessObtenido = solucion.getFitness();
                G4DUtility.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, D_MIN)) {
                    G4DUtility.Logger.logln(" | ¡NUEVO MEJOR!");
                    mejorFitness = fitnessObtenido;
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4DUtility.Logger.logln();
                // Reversión de cambios
                revertirCambios(segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion);
                G4DUtility.Logger.delete_lines(3);
            }
            // Aplicar mejor combinación
            if(posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                realocarSegmentacion(rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion);
            }
            G4DUtility.Logger.delete_upper_line();
        }
        solucion.setFitness();
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Realocación'  : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4DUtility.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4DUtility.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private void realocarSegmentacion(List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> lotesPorRuta, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Eliminación de registros de origenes
        int restante = rutasOrig.stream().mapToInt(r -> lotesPorRuta.get(r).getTamanio()).sum();
        for (Ruta rOrig : rutasOrig) {
            Lote l = lotesPorRuta.get(rOrig);
            rOrig.eliminarRegistroDeLoteDeProductos(l);
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
        G4DUtility.IntegerWrapper t = new G4DUtility.IntegerWrapper(), t_best = new G4DUtility.IntegerWrapper();
        Instant start = Instant.now();
        // Búsqueda global de soluciones por nivel de perturbación
        do {
            G4DUtility.IntegerWrapper k = new G4DUtility.IntegerWrapper(K_MIN);
            solucion.reasignar(x_best.replicar());
            while (t.value < T_MAX && k.value <= K_MAX) {
                Solucion x_prima = new Solucion();
                boolean solucionValida = false;
                int intentos = 1;
                // Realización de agitaciones aleatorias continuas
                while (true) {
                    x_prima = solucion.replicar();
                    G4DUtility.Logger.log("Agitando.. ");
                    Shaking(x_prima, k);
                    Boolean huboAlteracion = G4DUtility.Calculator.areProximatelyEqual(solucion.getFitness(), x_prima.getFitness(), D_MIN);
                    // Validación de solución por umbral de aberración
                    if (huboAlteracion  && x_prima.getFitness() < solucion.obtenerUmbralDeAberracion()) {
                        G4DUtility.Logger.logln(" | >> POSIBLE MEJOR SOLUCIÓN");
                        solucionValida = true;
                        break;
                    } else {
                        if(!huboAlteracion) {
                            G4DUtility.Logger.logln(" | >> SIN ALTERACIÓN");
                        } else G4DUtility.Logger.logln(" | >> ABERRACIÓN");
                        if (intentos >= MAX_INTENTOS) {
                            G4DUtility.Logger.log("LIMITE DE INTENTOS ALCANZADO.");
                            break;
                        }
                        intentos++;
                    }
                }
                // Validación de existencia de solución
                if (!solucionValida) {
                    k.value++;
                    G4DUtility.Logger.delete_lines(1 + intentos);
                    continue;
                }
                // Reoptimización de posible mejor solución
                Solucion x_prima_doble = x_prima.replicar();
                G4DUtility.Logger.log("Reoptimizando.. ");
                VND(x_prima_doble);
                G4DUtility.Logger.delete_upper_line();
                G4DUtility.Logger.logf("> 'Reoptimización' : %.3f -> %.3f%n", x_prima.getFitness(), x_prima_doble.getFitness());
                // Actualización de tiempo transcurrido
                Instant end = Instant.now();
                t.value = (int) Duration.between(start, end).getSeconds();
                // Actualización de vecindario
                G4DUtility.Logger.log("Validando nuevo vencindario.. ");
                NeighborhoodChange(solucion, x_prima_doble, x_best, k, t, t_best);
                G4DUtility.Logger.delete_lines(2 + intentos);
            }
            // Actualización de tiempo transcurrido
            Instant end = Instant.now();
            Duration duracion = Duration.between(start, end);
            t.value = (int) duracion.getSeconds();
        } while (t.value < T_MAX);
        solucion.reasignar(x_best);
    }

    private void Shaking(Solucion solucion, G4DUtility.IntegerWrapper k) {
        G4DUtility.Logger.logln("[RAND]");
        // Declaración & inicialización de variables
        int j = 0;
        double fitnessInicial = solucion.getFitness();
        // Perturbación aleatoria de solución por nivel de intesidad
        G4DUtility.Logger.logf(">> Intensidad de perturbación: %d de '%d'%n", k.value, K_MAX);
        for (int i = 0; i < k.value; ++i) {
            int neighborhood = random.nextInt(3);
            int ele = L_MIN + random.nextInt(L_MAX - L_MIN + 1);
            if(j == 3) {
                j = 0;
                G4DUtility.Logger.delete_lines(4);
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
        G4DUtility.Logger.delete_lines(3 + j);
        G4DUtility.Logger.logf("> 'Agitación'    : %.3f -> %.3f", fitnessInicial, solucion.getFitness());
    }

    private void TCompactar(Solucion solucion, int ele) {
        G4DUtility.Logger.logln("> Realizando perturbación por 'Compactación'..");
        // Declaración & inicialización de variables
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Compactación de pedidos
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionModificable = new HashMap<>();
            Map<Ruta, List<Aeropuerto>> secuenciasIntocables = new HashMap<>();
            // Carga de secuencias intocables por replanificación
            pedido.cargarRestriccionesDeReplanificacion(segmentacionModificable, secuenciasIntocables);
            // Validación por aptitud de pedido para compactar
            G4DUtility.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, pedidos.size());
            if (segmentacionModificable.size() < ele + 1) {
                G4DUtility.Logger.logln(" [NO APTO]");
                G4DUtility.Logger.delete_upper_line();
                continue;
            } else G4DUtility.Logger.logln(" [APTO]");
            // Validación por aptitud de combinación
            G4DUtility.Logger.log(": Validando combinación aleatoria.. ");
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(new ArrayList<>(segmentacionModificable.keySet()), ele);
            if (combinaciones.isEmpty()) {
                G4DUtility.Logger.logln("[INVALIDA]");
                G4DUtility.Logger.delete_lines(3);
                continue;
            }
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            int totalCompactar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
            List<Ruta> rutasDest = segmentacionModificable.keySet().stream().filter(r -> !rutasOrig.contains(r))
                    .filter(r -> r.obtenerCapacidadDisponible() > 0)
                    .filter(r -> r.respetaSecuenciasInalterables(rutasOrig, secuenciasIntocables))
                    .collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            if (capDispTotal < totalCompactar) {
                G4DUtility.Logger.logln("[INVALIDA]");
                G4DUtility.Logger.delete_lines(3);
                continue;
            } else G4DUtility.Logger.logln("[VALIDA]");
            // Compactación
            G4DUtility.Logger.logln(": Compactando..");
            Collections.shuffle(rutasDest);
            compactarSegmentacion(rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion);
            G4DUtility.Logger.delete_lines(4);
        }
        // Actualización de solución
        solucion.setFitness();
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Compactación' : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void TFusionar(Solucion solucion, int ele) {
        G4DUtility.Logger.logln("> Realizando perturbación por 'Fusión'..");
        // Declaración & inicialización de variables
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Fusión de rutas
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionModificable = new HashMap<>();
            Map<Ruta, List<Aeropuerto>> secuenciasIntocables = new HashMap<>();
            // Carga de restricciones por replanificación
            pedido.cargarRestriccionesDeReplanificacion(segmentacionModificable, secuenciasIntocables);
            List<Ruta> rutasIni = new ArrayList<>(segmentacionModificable.keySet());
            List<Ruta> rutasFin = rutasEnOperacion.stream().filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalida().isBefore(pedido.getFechaHoraGeneracion()) && !r.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion()))
                    .collect(Collectors.toList());
            Collections.shuffle(rutasFin);
            G4DUtility.Logger.logf("- Evaluando ruta aleatoria del pedido #%d de '%d'..%n", posPedido+1, pedidos.size());
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getCrossedCombinations(rutasIni, rutasFin, 1, 1);
            int cantCombinaciones = Math.min(ele, combinaciones.size());
            // Validación por existencia de rutas para fusionar
            if(combinaciones.isEmpty()) {
                G4DUtility.Logger.delete_upper_line();
                continue;
            }
            List<Ruta> combinacion = combinaciones.get(random.nextInt(cantCombinaciones));
            Ruta rIni = combinacion.get(0);
            Ruta rFin = combinacion.get(1);
            // Validación de fusión por tamanio de ruta
            G4DUtility.Logger.log(": Validando fusión aleatoria..");
            List<Aeropuerto> saFin = rFin.obtenerSecuenciaDeAeropuertos();
            if(saFin.size() < 3) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                G4DUtility.Logger.delete_lines(3);
                continue;
            }
            // Validación de fusión por existencia de conexión en ruta
            int posConexionFin = random.nextInt(1, saFin.size() - 1);
            Aeropuerto aConexion = saFin.get(posConexionFin);
            List<Aeropuerto> saIni = rIni.obtenerSecuenciaDeAeropuertos();
            int posConexionIni = saIni.indexOf(aConexion);
            if(posConexionIni == -1) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                G4DUtility.Logger.delete_lines(3);
                continue;
            }
            // Validación de fusión por existencia de disponibilidad en ruta
            List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posConexionIni + 1));
            LocalDateTime fechaHoraLlegadaAConexion = svIni.getLast().getFechaHoraLlegada();
            List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posConexionFin + 1, rFin.getVuelos().size()));
            LocalDateTime fechaHoraSalidaDesdeConexion = svIni.getFirst().getFechaHoraSalida();
            if(fechaHoraLlegadaAConexion.isAfter(fechaHoraSalidaDesdeConexion) || fechaHoraLlegadaAConexion.plusMinutes((long)(60*Problematica.MAX_HORAS_ESTANCIA)).isAfter(fechaHoraSalidaDesdeConexion)) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                G4DUtility.Logger.delete_lines(3);
                continue;
            }
            List<Vuelo> svNew = new ArrayList<>();
            svNew.addAll(svIni);
            svNew.addAll(svFin);
            Ruta rNew = new Ruta(rIni);
            rNew.setVuelos(svNew);
            if(!rNew.respetaSecuenciasInalterables(List.of(rIni), secuenciasIntocables)) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                G4DUtility.Logger.delete_lines(3);
                continue;
            }
            Lote lote = segmentacion.get(rIni);
            rIni.eliminarRegistroDeLoteDeProductos(lote);
            segmentacion.remove(rIni);
            if(rNew.obtenerCapacidadDisponible() < lote.getTamanio()) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                rIni.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                segmentacion.put(rIni, lote);
                G4DUtility.Logger.delete_lines(3);
                continue;
            } else G4DUtility.Logger.logln(" [VALIDA]");
            // Fusión
            G4DUtility.Logger.logln(": Fusionando..");
            rNew.registraLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
            segmentacion.put(rNew, lote);
            G4DUtility.Logger.delete_lines(4);
        }
        // Actualización de solución
        solucion.setFitness();
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Fusión'       : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void TRealocar(Solucion solucion, int ele) {
        G4DUtility.Logger.logln("> Realizando perturbación por 'Realocación'..");
        // Declaración & inicialización de variables
        double fitnessInicial = solucion.getFitness();
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Realocación de lotes
        for (int posPedido = 0; posPedido < pedidos.size(); posPedido++) {
            Pedido pedido = pedidos.get(posPedido);
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionModificable = new HashMap<>();
            Map<Ruta, List<Aeropuerto>> secuenciasIntocables = new HashMap<>();
            // Carga de restricciones por replanificacion
            pedido.cargarRestriccionesDeReplanificacion(segmentacionModificable, secuenciasIntocables);
            // Validación por aptitud de pedido para realocar
            G4DUtility.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, pedidos.size());
            if (segmentacion.size() < ele) {
                G4DUtility.Logger.logln("[NO APTO]");
                G4DUtility.Logger.delete_upper_line();
                continue;
            } else G4DUtility.Logger.logln("[APTO]");
            // Validación por aptitud de combinación
            G4DUtility.Logger.log(": Validando combinación aleatoria.. ");
            List<Ruta> rutas = new ArrayList<>(segmentacionModificable.keySet());
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(rutas, ele);
            if (combinaciones.isEmpty()) {
                G4DUtility.Logger.logln("[INVALIDA]");
                G4DUtility.Logger.delete_lines(3);
                continue;
            }
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            List<Ruta> rutasDest = rutasEnOperacion.stream().filter(r -> !rutas.contains(r))
                    .filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalida().isBefore(pedido.getFechaHoraGeneracion()) && !r.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion()))
                    .filter(r -> r.obtenerCapacidadDisponible() > 0)
                    .filter(r -> r.respetaSecuenciasInalterables(rutas, secuenciasIntocables))
                    .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                    .collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(Ruta::obtenerCapacidadDisponible).sum();
            int totalRealocar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
            if (capDispTotal < totalRealocar) {
                G4DUtility.Logger.logln("[INVALIDA]");
                G4DUtility.Logger.delete_lines(3);
                continue;
            } else G4DUtility.Logger.logln("[VALIDA]");
            // Realocación
            G4DUtility.Logger.logln(": Realocando..");
            Collections.shuffle(rutasDest);
            realocarSegmentacion(rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion);
            G4DUtility.Logger.delete_lines(4);
        }
        // Actualización de solución
        solucion.setFitness();
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Realocación'  : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void NeighborhoodChange(Solucion solucionAux,
                                    Solucion x_prima_doble, Solucion x_best,
                                    G4DUtility.IntegerWrapper k, G4DUtility.IntegerWrapper t,
                                    G4DUtility.IntegerWrapper t_best) {
        G4DUtility.Logger.log("[NeighborhoodChange]");
        // Validación por mejor vecindario
        if (x_prima_doble.getFitness() < x_best.getFitness()) {
            G4DUtility.Logger.logf("| > NUEVO MEJOR [%.3f]", x_prima_doble.getFitness());
            x_best.reasignar(x_prima_doble.replicar());
            solucionAux.reasignar(x_prima_doble);
            k.value = K_MIN;
            t_best.value = t.value;
        } else {
            G4DUtility.Logger.log("| No es mejor.");
            k.value++;
        }
    }

    public void imprimirSolucion(String rutaArchivo) { imprimirSolucion(this.solucion, rutaArchivo); }

    private void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        G4DUtility.Logger.logf("Cargando archivo 'Solucion' a la ruta '%s'..",rutaArchivo);
        // Declaración & inicialización de variables
        int dimLinea = 181;
        try {
            // Inicialización de archivo
            G4DUtility.Printer.open(rutaArchivo);
            // Impresion de reporte
            G4DUtility.Printer.fill_line('=', dimLinea);
            G4DUtility.Printer.print_centered("FITNESS DE LA SOLUCIÓN", dimLinea);
            G4DUtility.Printer.print_centered(
                    String.format("%.2f",
                            solucion.getFitness()
                    ), dimLinea);
            G4DUtility.Printer.println();
            G4DUtility.Printer.print_centered(
                    String.format("%s%35s%39s",
                            "UTILIZACION TEMPORAL",
                            "DESVIACION ESPACIAL",
                            "DISPOSICION OPERACIONAL"
                    ), dimLinea);
            G4DUtility.Printer.print_centered(
                    String.format("%s%35s%37s",
                            String.format("%.2f%%", 100*solucion.getRatioPromedioDeUtilizacionTemporal()),
                            String.format("%.2f%%", 100*solucion.getRatioPromedioDeDesviacionEspacial()),
                            String.format("%.2f%%", 100*solucion.getRatioPromedioDeDisposicionOperacional())
                    ), dimLinea);
            G4DUtility.Printer.fill_line('=', dimLinea);
            List<Pedido> sol_pedidos = solucion.getPedidosAtendidos();
            sol_pedidos.sort(Comparator.comparing(Pedido::getFechaHoraGeneracion));
            int cantPedidos = sol_pedidos.size();
            for (int posPedido = 0; posPedido < cantPedidos; posPedido++) {
                Pedido pedido = sol_pedidos.get(posPedido);
                double ped_duracionActivaTotal = 0.0;
                double ped_duracionPasivaTotal = 0.0;
                double ped_tiempoOptimizado = 0.0;
                LocalDateTime ped_fechaHoraGeneracion = pedido.getFechaHoraGeneracion();
                LocalDateTime ped_fechaHoraExpiracion = pedido.getFechaHoraExpiracion();
                Cliente ped_cli = pedido.getCliente();
                Aeropuerto ped_aDest = pedido.getDestino();
                G4DUtility.Printer.print_centered(
                        String.format(
                                "PEDIDO #%d",
                                posPedido + 1
                        ), dimLinea);
                G4DUtility.Printer.fill_line('-', dimLinea, 4);
                G4DUtility.Printer.printf(
                        "%4s%-50s%8s%-30s%27s%28s%30s%n",
                        " ",
                        "CLIENTE",
                        " ",
                        "DESTINO",
                        "CANT. PRODUCTOS MPE",
                        "INSTANTE DE REGISTRO",
                        "INSTANTE DE EXPIRACION"
                );
                G4DUtility.Printer.printf(
                        "%4s%-50s%8s%-30s%19s%34s%29s%n",
                        " ",
                        ped_cli,
                        " ",
                        ped_aDest,
                        String.format("%03d", pedido.getCantidadSolicitada()),
                        G4DUtility.Convertor.toDisplayString(ped_fechaHoraGeneracion),
                        G4DUtility.Convertor.toDisplayString(ped_fechaHoraExpiracion)
                );
                G4DUtility.Printer.println();
                G4DUtility.Printer.print_centered(">> RUTAS PLANIFICADAS PARA EL PEDIDO <<", dimLinea);
                G4DUtility.Printer.println();
                G4DUtility.Printer.fill_line('*', dimLinea, 8);
                List<Ruta> ped_rutas = new ArrayList<>(pedido.obtenerSegementacionVigente().getLotesPorRuta().keySet());
                ped_rutas.sort(Comparator.comparing(Ruta::getFechaHoraSalida));
                int cantRutas = ped_rutas.size();
                for (int posRuta = 0; posRuta < cantRutas; posRuta++) {
                    Ruta ruta = ped_rutas.get(posRuta);
                    int rut_numProdAsignados = pedido.obtenerCantidadDeProductosEnRuta(ruta);
                    double rut_duracionActivaTotalInd = ruta.obtenerDuracionActivaTotal();
                    double rut_duracionActivaTotalLot = rut_duracionActivaTotalInd*rut_numProdAsignados;
                    double rut_duracionPasivaTotalInd = ruta.obtenerDuracionPasivaTotal(ped_fechaHoraGeneracion);
                    double rut_duracionPasivaTotalLot = rut_duracionPasivaTotalInd*rut_numProdAsignados;
                    double rut_tiempoOptimizadoInd = G4DUtility.Calculator.getElapsedHours(ruta.getFechaHoraLlegada(), ped_fechaHoraExpiracion);
                    double rut_tiempoOptimizadoLot = rut_tiempoOptimizadoInd*rut_numProdAsignados;
                    Aeropuerto rut_aOrig = ruta.getOrigen();
                    G4DUtility.Printer.printf("%10s RUTA #%s | ORIGEN: %-30s | TIPO DE ENVIO: %s | INSTANTE DE ENTREGA: %s | CANTIDAD ASIGNADA DE PRODUCTOS: %3d%n",
                            ">>",
                            String.format("%03d", posRuta + 1),
                            rut_aOrig,
                            ruta.getTipo(),
                            G4DUtility.Convertor.toDisplayString(ruta.getFechaHoraLlegada()),
                            rut_numProdAsignados
                    );
                    G4DUtility.Printer.println();
                    G4DUtility.Printer.printf(
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
                        G4DUtility.Printer.print_centered(
                                String.format("[%s]    %-30s            > > > > > >            [%s]    %-30s",
                                        G4DUtility.Convertor.toDisplayString(vuelo.getFechaHoraSalida()),
                                        vuelo.getPlan().getOrigen(),
                                        G4DUtility.Convertor.toDisplayString(vuelo.getFechaHoraLlegada()),
                                        vuelo.getPlan().getDestino()
                                ), dimLinea);
                    }
                    G4DUtility.Printer.fill_line('.', dimLinea, 8);
                    G4DUtility.Printer.printf("%27s%22s%16s%n", "Resumen de la ruta:","INDIVIDUAL","LOTE");
                    G4DUtility.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Duración 'Activa':", G4DUtility.Convertor.toDisplayString(rut_duracionActivaTotalInd), G4DUtility.Convertor.toDisplayString(rut_duracionActivaTotalLot));
                    G4DUtility.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Duración 'Pasiva':", G4DUtility.Convertor.toDisplayString(rut_duracionPasivaTotalInd), G4DUtility.Convertor.toDisplayString(rut_duracionPasivaTotalLot));
                    G4DUtility.Printer.printf("%8s%-30s%11s%20s%n", " ", ">> Tiempo optimizado:", G4DUtility.Convertor.toDisplayString(rut_tiempoOptimizadoInd), G4DUtility.Convertor.toDisplayString(rut_tiempoOptimizadoLot));
                    if (posRuta != cantRutas - 1) {
                        G4DUtility.Printer.fill_line('*', dimLinea, 8);
                    }
                    ped_duracionActivaTotal += rut_duracionActivaTotalLot;
                    ped_duracionPasivaTotal += rut_duracionPasivaTotalLot;
                    ped_tiempoOptimizado += rut_tiempoOptimizadoLot;
                }
                G4DUtility.Printer.fill_line('-', dimLinea, 4);
                G4DUtility.Printer.printf("%23s%23s%n", "Resumen del pedido:","TOTAL");
                G4DUtility.Printer.printf("%4s%-30s%15s%n", " ", ">> Duración 'Activa':", G4DUtility.Convertor.toDisplayString(ped_duracionActivaTotal));
                G4DUtility.Printer.printf("%4s%-30s%15s%n", " ", ">> Duración 'Pasiva':", G4DUtility.Convertor.toDisplayString(ped_duracionPasivaTotal));
                G4DUtility.Printer.printf("%4s%-30s%15s%n", " ", ">> Tiempo optimizado:", G4DUtility.Convertor.toDisplayString(ped_tiempoOptimizado));
                G4DUtility.Printer.fill_line('=', dimLinea);
            }
            G4DUtility.Printer.flush();
            G4DUtility.Printer.close();
            G4DUtility.Logger.delete_current_line();
            G4DUtility.Logger.logf("[>] ARCHIVO 'SOLUCION' GENERADO! (RUTA: '%s')%n", rutaArchivo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Solucion getSolucion() {
        return solucion;
    }
}
