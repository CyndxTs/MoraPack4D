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

import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoLote;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoRuta;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class GVNS {
    private static final Random random = new Random();
    public static Double D_MIN;                         // Diferencia mínima considerable de fitness
    public static Integer I_MAX;                        // Número máximo de iteraciones de exploración inicial
    public static Integer L_MIN;                        // Nivel mínimo de búsqueda local
    public static Integer L_MAX;                        // Nivel máximo de búsqueda local
    public static Integer K_MIN;                        // Nivel mínimo de perturbación
    public static Integer K_MAX;                        // Nivel máximo de perturbación
    public static Integer N_MAX;                        // Número de máximo de intentos por nivel de perturbación
    public static Integer T_MAX;                        // Tiempo máximo esperado de exploración global
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
        G4DUtility.Logger.logf("[+] SOLUCION INICIAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        this.solucion = x;
        /*
        // Optimización inicial (Variable Neighborhood Descent)
        G4DUtility.Logger.Stats.set_local_start();
        G4DUtility.Logger.log("Realizando optimización inicial.. ");
        VND(x);
        G4DUtility.Logger.Stats.set_local_duration();
        G4DUtility.Logger.logf("[+] OPTIMIZACION INICIAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        this.solucion = x;
        // Optimización final (Variable Neighborhood Search)
        G4DUtility.Logger.Stats.set_local_start();
        G4DUtility.Logger.logln("Realizando optimización final.. [VNS]");
        VNS(x);
        G4DUtility.Logger.Stats.set_local_duration();
        G4DUtility.Logger.logf("[+] OPTIMIZACION FINAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        this.solucion = x;
         */
        G4DUtility.Logger.Stats.set_global_duration();
        G4DUtility.Logger.Stats.log_stat_global_sol();
    }

    private void solucionInicial(Problematica problematica, Solucion solucion) {
        G4DUtility.Logger.logln("[NN]");
        // Declaración & inicialización de variables
        boolean errorDeEnrutamiento, haySolucion = false;
        Problematica pAux = new Problematica();
        // Iteración de exploraciones iniciales para encontrar 1 solución
        for(int i = 0; i < I_MAX; i++) {
            G4DUtility.Logger.logf(">> Iteración: %d de '%d':%n", i + 1, I_MAX);
            pAux = problematica.replicar();
            List<Aeropuerto> origenes = pAux.origenes;
            List<Aeropuerto> destinos = pAux.destinos;
            List<Plan> planes = pAux.planes;
            List<Pedido> pedidos = pAux.pedidos;
            Set<Vuelo> vuelosEnTransito = pAux.vuelos;
            Set<Ruta> rutasEnOperacion = pAux.rutas;
            G4DUtility.Logger.Stats.totalPed = pedidos.size();
            pedidos.forEach(p -> G4DUtility.Logger.Stats.totalProd += p.getCantidadSolicitada());
            G4DUtility.Logger.Stats.numPed = 1;
            G4DUtility.Logger.Stats.posPed = 0;
            G4DUtility.Logger.Stats.posProd = 0;
            errorDeEnrutamiento = false;
            // Preparación de replanificación
            prepararReplanificacion(pedidos, planes);
            // Atención & reatención de pedidos
            G4DUtility.Logger.Stats.set_process_start();
            for (Pedido pedido : pedidos) {
                G4DUtility.Logger.Stats.numProd = 1;
                G4DUtility.Logger.Stats.log_stat_ped();
                // Validación por estado de atención de pedido
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
                continue;
            }
            // Actualización de solución
            solucion.setPedidosAtendidos(pedidos);
            solucion.setVuelosEnTransito(vuelosEnTransito);
            solucion.setRutasEnOperacion(rutasEnOperacion);
            solucion.getAeropuertosTransitados().addAll(origenes);
            solucion.getAeropuertosTransitados().addAll(destinos);
            solucion.setFitness();
            haySolucion = true;
            break;
        }
        // Validación por inexistencia de solución
        if(!problematica.pedidos.isEmpty()) {
            if(!haySolucion) {
                G4DUtility.Logger.logf_err("ERROR: No fue posible enrutar todos los pedidos en '%d' iteraciones.%n", I_MAX);
                solucion.setFitness(-9999.99);
                this.solucion = null;
            } else problematica.reasignar(pAux);
        } else solucion.setFitness(-9999.99);
    }

    private void prepararReplanificacion(List<Pedido> pedidos, List<Plan> planes) {
        G4DUtility.Logger.logln(">> Preparando replanificación..");
        // Declaracion & inicialización de Variables
        Problematica.PUNTOS_REPLANIFICACION = new ArrayList<>();
        List<Pedido> pedidosReplanificables = pedidos.stream().filter(Pedido::getFueAtendido).toList();
        // Preparación de pedidos replanificables
        for(Pedido pReplanificable : pedidosReplanificables) {
            Map<Ruta, Lote> sVigente = pReplanificable.obtenerSegementacionVigente().getLotesPorRuta();
            for(Map.Entry<Ruta, Lote> entry : sVigente.entrySet()) {
                Ruta ruta = entry.getKey();
                Lote lote = entry.getValue();
                switch (ruta.getEstado()) {
                    case REVISION_PENDIENTE -> {
                        if (!ruta.getFechaHoraLlegada().isAfter(Problematica.UMBRAL_REPLANIFICACION)) {
                            ruta.setEstado(EstadoRuta.FINALIZADA);
                            continue;
                        }
                        // Extracción de vuelo a replanificar
                        boolean huboReplanificacionAleatoria = false;
                        List<Vuelo> vuelos = ruta.getVuelos();
                        Vuelo vReplanificar = vuelos.stream().filter(v -> v.esProblematico() && v.getFechaHoraSalida().isAfter(Problematica.UMBRAL_REPLANIFICACION) && v.getPlan().getOrigen().obtenerRegistroDeLoteDeProductos(lote) != null).findFirst().orElse(null);
                        // Validación de existencia de vuelo problemático para realizar extracción aleatoria
                        if(vReplanificar == null && random.nextDouble() < Problematica.PROBABILIDAD_REPLANIFICACION) {
                            Vuelo vAux = vuelos.stream().filter(v -> v.getFechaHoraSalida().isAfter(Problematica.UMBRAL_REPLANIFICACION) && v.getPlan().getOrigen().obtenerRegistroDeLoteDeProductos(lote) != null).findFirst().orElse(null);
                            if(vAux != null) {
                                int posVAux = vuelos.indexOf(vAux);
                                if(posVAux < vuelos.size() - 1) {
                                    vReplanificar = vuelos.get(random.nextInt(posVAux, vuelos.size()));
                                } else vReplanificar = vuelos.get(posVAux);
                                huboReplanificacionAleatoria = true;
                            }
                        }
                        // Validación de existencia de vuelo a replanificar
                        if(vReplanificar != null) {
                            int posVReplanficar = vuelos.indexOf(vReplanificar);
                            List<Vuelo> vuelosFijos = new ArrayList<>();
                            if(posVReplanficar > 1) {
                                vuelosFijos = new ArrayList<>(vuelos.subList(0, posVReplanficar));
                            } else if(posVReplanficar == 1) vuelosFijos.add(vuelos.getFirst());
                            Aeropuerto aeropuertoDeConexion = vReplanificar.getPlan().getOrigen();
                            LocalDateTime umbralDeConexion;
                            if(!vuelosFijos.isEmpty()) {
                                umbralDeConexion = (vuelosFijos.getLast().getFechaHoraLlegada().isAfter(Problematica.UMBRAL_REPLANIFICACION)) ? vuelosFijos.getLast().getFechaHoraLlegada() : Problematica.UMBRAL_REPLANIFICACION;
                            } else umbralDeConexion = (pReplanificable.getFechaHoraProcesamiento().isAfter(Problematica.UMBRAL_REPLANIFICACION)) ? pReplanificable.getFechaHoraProcesamiento() : Problematica.UMBRAL_REPLANIFICACION;
                            if(huboReplanificacionAleatoria) {
                                ruta.setEstado(EstadoRuta.OPERATIVA);
                            } else ruta.setEstado(EstadoRuta.DESHABILITADA);
                            lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                            PuntoDeReplanificacion pdr = new PuntoDeReplanificacion();
                            pdr.setRutaInicial(ruta);
                            pdr.setAeropuertoDeConexion(aeropuertoDeConexion);
                            pdr.setUmbralDeConexion(umbralDeConexion);
                            pdr.setVueloReplanificado(vReplanificar);
                            pdr.setVuelosFijos(vuelosFijos);
                            pdr.getLotes().add(lote);
                            Problematica.PUNTOS_REPLANIFICACION.add(pdr);
                        } else ruta.setEstado(EstadoRuta.OPERATIVA);
                    }
                    case OPERATIVA -> {
                        if(random.nextDouble() < Problematica.PROBABILIDAD_REPLANIFICACION) {
                            List<Vuelo> vuelos = ruta.getVuelos();
                            Vuelo vAux = vuelos.stream().filter(v -> v.getFechaHoraSalida().isAfter(Problematica.UMBRAL_REPLANIFICACION) && v.getPlan().getOrigen().obtenerRegistroDeLoteDeProductos(lote) != null).findFirst().orElse(null);
                            if(vAux != null) {
                                int posVAux = vuelos.indexOf(vAux);
                                int posVReplanficar = (posVAux < vuelos.size() - 1) ? random.nextInt(posVAux, vuelos.size()) : posVAux;
                                Vuelo vReplanificar = vuelos.get(posVReplanficar);
                                List<Vuelo> vuelosFijos = new ArrayList<>();
                                if(posVReplanficar > 1) {
                                    vuelosFijos = new ArrayList<>(vuelos.subList(0, posVReplanficar));
                                } else if(posVReplanficar == 1) vuelosFijos.add(vuelos.getFirst());
                                Aeropuerto aeropuertoDeConexion = vReplanificar.getPlan().getOrigen();
                                LocalDateTime umbralDeConexion;
                                if(!vuelosFijos.isEmpty()) {
                                    umbralDeConexion = (vuelosFijos.getLast().getFechaHoraLlegada().isAfter(Problematica.UMBRAL_REPLANIFICACION)) ? vuelosFijos.getLast().getFechaHoraLlegada() : Problematica.UMBRAL_REPLANIFICACION;
                                } else {
                                    umbralDeConexion = (pReplanificable.getFechaHoraProcesamiento().isAfter(Problematica.UMBRAL_REPLANIFICACION)) ? pReplanificable.getFechaHoraProcesamiento() : Problematica.UMBRAL_REPLANIFICACION;
                                }
                                lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                                PuntoDeReplanificacion pdr = new PuntoDeReplanificacion();
                                pdr.setRutaInicial(ruta);
                                pdr.setAeropuertoDeConexion(aeropuertoDeConexion);
                                pdr.setUmbralDeConexion(umbralDeConexion);
                                pdr.setVueloReplanificado(vReplanificar);
                                pdr.setVuelosFijos(vuelosFijos);
                                pdr.getLotes().add(lote);
                                Problematica.PUNTOS_REPLANIFICACION.add(pdr);
                            }
                        }
                    }
                    case DESHABILITADA -> {
                        PuntoDeReplanificacion pdr = Problematica.PUNTOS_REPLANIFICACION.stream().filter(p -> ruta.getEstado().equals(EstadoRuta.DESHABILITADA) && ruta.equals(p.getRutaInicial())).findFirst().orElse(null);
                        if(pdr != null) {
                            lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                            pdr.getLotes().add(lote);
                        }
                    }
                }
            }
        }
        G4DUtility.Logger.logln(">> PUNTOS DE REPLANIFICACIÓN CARGADOS!");
    }

    private boolean atenderPedido(Pedido pedido, List<Aeropuerto> origenes, List<Plan> planes,
                                  Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        G4DUtility.Logger.logf(">> ATENDIENDO PEDIDO #%d de '%d' | '%d' para enrutar!.%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.totalPed, pedido.getCantidadSolicitada());
        // Declaración & inicialización de variables
        int cantPorEnrutar = pedido.getCantidadSolicitada();
        LocalDateTime instanteDeGeneracion = pedido.getFechaHoraGeneracion();
        LocalDateTime instanteDeProcesamiento = pedido.getFechaHoraProcesamiento();
        Aeropuerto destino = pedido.getDestino();
        List<Aeropuerto> origenesDisponibles = new ArrayList<>(origenes);
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        Map<Ruta, Lote> lotesPorRuta = new HashMap<>();
        // Exploración aleatoria de orígenes
        while(!origenesDisponibles.isEmpty()) {
            Aeropuerto origen = origenesDisponibles.get(random.nextInt(origenesDisponibles.size()));
            G4DUtility.Logger.logf("> Probando origen:  %s%n", origen);
            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            LocalDateTime instanteLimite = instanteDeGeneracion.plusMinutes(tipoRuta.getMaxMinutosParaEntrega());
            G4DUtility.Logger.logf("> Tipo de ruta hasta '%s': '%s'%n", destino, tipoRuta.toString());
            // Búsqueda de ruta reutilizable
            Ruta ruta = buscarRutaVoraz(instanteDeProcesamiento, instanteLimite, origen, destino, rutasAsignadas);
            G4DUtility.Logger.delete_upper_line();
            if(ruta == null) {
                // Construcción de nueva ruta
                ruta = construirRutaVoraz(instanteDeProcesamiento, instanteDeProcesamiento, null, instanteLimite, origen, destino, planes, vuelosActivados);
                if(ruta == null) {
                    G4DUtility.Logger.logln_err("ERROR: No fue posible generar una ruta a partir de este origen.");
                    origenesDisponibles.remove(origen);
                    if(!origenesDisponibles.isEmpty()) {
                        G4DUtility.Logger.delete_lines(8);
                    } else G4DUtility.Logger.delete_lines(6);
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
            ruta.agregarRegistroDeLoteDeProductos(lote, vuelosActivados, rutasAsignadas);
            lotesPorRuta.put(ruta, lote);
            cantPorEnrutar -= cantEnrutables;
            G4DUtility.Logger.Stats.set_proccess_duration();
            G4DUtility.Logger.Stats.next_lot(cantEnrutables);
            G4DUtility.Logger.logln(" | [REALIZADO]");
            // Validación por atención completa de pedido
            if(cantPorEnrutar == 0) {
                Segmentacion segmentacion = new Segmentacion();
                segmentacion.setFechaHoraAplicacion(instanteDeProcesamiento);
                segmentacion.setLotesPorRuta(lotesPorRuta);
                pedido.getSegmentaciones().add(segmentacion);
                pedido.setFechaHoraExpiracion();
                pedido.setFueAtendido(true);
                G4DUtility.Logger.delete_lines(6);
                G4DUtility.Logger.logf(">>> PEDIDO #%d ATENDIDO! | '%d' de '%d' productos enrutados!%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.numProd-1, pedido.getCantidadSolicitada());
                vuelosEnTransito.addAll(vuelosActivados);
                rutasEnOperacion.addAll(rutasAsignadas);
                G4DUtility.Logger.delete_lines(6);
                return true;
            }
            G4DUtility.Logger.delete_lines(8);
        }
        return false;
    }

    private Ruta buscarRutaVoraz(LocalDateTime instanteInicial, LocalDateTime instanteLimite,
                                 Aeropuerto origen, Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        G4DUtility.Logger.logln("Buscando ruta voraz en operación..");
        // Validación por origen factible
        if(origen.equals(destino)) {
            G4DUtility.Logger.log(" [NO ENCONTRADA]");
            G4DUtility.Logger.logln_err(" | ERROR: ORIGEN == DESTINO");
            return null;
        }
        // Búsqueda de ruta reutilizable
        List<Ruta> rutasPosibles = rutasAsignadas.stream().filter(r -> r.getOrigen().equals(origen) && r.getDestino().equals(destino) && r.getEstado().equals(EstadoRuta.OPERATIVA)).toList();
        for(Ruta ruta : rutasPosibles) {
            if(!ruta.esAlcanzable(instanteInicial, instanteLimite)) continue;
            G4DUtility.Logger.logf("Se encontró la ruta '%s'", ruta.getCodigo());
            return ruta;
        }
        G4DUtility.Logger.logln("No se encontró.");
        return null;
    }

    private Ruta construirRutaVoraz(LocalDateTime instanteInicial, LocalDateTime instanteMinimoDeEgreso, LocalDateTime instanteMaximoDeEgreso,
                                    LocalDateTime instanteLimite, Aeropuerto origen, Aeropuerto destino, List<Plan> planes,
                                    Set<Vuelo> vuelosActivados) {
        G4DUtility.Logger.logln("Construyendo nueva ruta voraz..");
        // Validación por origen factible
        if(origen.equals(destino)) {
            G4DUtility.Logger.logln_err(" | ERROR: ORIGEN == DESTINO");
            return null;
        } else G4DUtility.Logger.logln();
        // Declaración & inicialización de variables
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime instanteActual = instanteInicial;
        LocalDateTime origInstanteMinimoDeEgreso = instanteMinimoDeEgreso;
        LocalDateTime origInstanteMaximoDeEgreso = instanteMaximoDeEgreso;
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        // Construcción de nueva ruta
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            G4DUtility.Logger.logf("- Ubicación actual: %s%n", actual);
            // Búsqueda de plan de vuelo más próximo
            G4DUtility.Logger.log("- Buscando vuelo más próximo..");
            Vuelo mejorVuelo = obtenerVueloMasProximo(null, actual, destino, instanteActual, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorVuelo == null) {
                G4DUtility.Logger.logln("- No se pudo encontrar ni activar un vuelo.");
                return null;
            }
            G4DUtility.Logger.logf("- Vuelo asignado: %s -> %s%n", mejorVuelo.getPlan().getOrigen().getCodigo(), mejorVuelo.getPlan().getDestino().getCodigo());
            // Asignación de vuelo
            secuenciaDeVuelos.add(mejorVuelo);
            actual = mejorVuelo.getPlan().getDestino();
            instanteActual = mejorVuelo.getFechaHoraLlegada();
            origInstanteMinimoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*Problematica.MIN_HORAS_ESTANCIA));
            origInstanteMaximoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*Problematica.MAX_HORAS_ESTANCIA));
        }
        // Actualización de ruta construida
        G4DUtility.Logger.logln("Guardando ruta..");
        ruta.setOrigen(origen);
        ruta.setDestino(destino);
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.instanciarAtributos();
        ruta.setDuracion();
        ruta.setDistancia();
        return ruta;
    }

    private Vuelo obtenerVueloMasProximo(Vuelo vueloReplanificado, Aeropuerto origen, Aeropuerto destino, LocalDateTime instanteActual,
                                       LocalDateTime origInstanteMinimoDeEgreso, LocalDateTime origInstanteMaximoDeEgreso,
                                       LocalDateTime instanteLimite, List<Plan> planes, Set<Aeropuerto> aeropuertosVisitados,
                                       Set<Vuelo> vuelosActivados) {
        // Declaración & inicialización de variables
        double menorLejania = Double.MAX_VALUE;
        Plan planMasProximo = null;
        // Búsqueda de plan de vuelo más próximo
        List<Plan> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                                                   .filter(p -> !p.getDestino().getEsSede())
                                                   .filter(p -> !aeropuertosVisitados.contains(p.getDestino())).toList();
        LocalDateTime instanteDeReferenciaActual = instanteActual;
        while(instanteDeReferenciaActual.isBefore(instanteLimite)) {
            for(Plan plan : planesPosibles) {
                if(!plan.esAlcanzable(vueloReplanificado, instanteActual, instanteDeReferenciaActual, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, destino, vuelosActivados)) continue;
                double lejania = plan.obtenerLejania(instanteActual, destino);
                if(lejania < menorLejania) {
                    menorLejania = lejania;
                    planMasProximo = plan;
                }
            }
            if(planMasProximo != null) break;
            instanteDeReferenciaActual = instanteDeReferenciaActual.plusDays(1);
        }
        if(planMasProximo == null) {
            return null;
        }
        G4DUtility.Logger.log(": Bucando vuelo en tránsito..");
        Vuelo vuelo = planMasProximo.obtenerVueloActivo(instanteDeReferenciaActual, vuelosActivados);
        if(vuelo == null) {
            G4DUtility.Logger.logln(": Activando nuevo vuelo..");
            vuelo = new Vuelo();
            vuelo.setPlan(planMasProximo);
            vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
            vuelo.instanciarAtributos(instanteActual);
        }
        return vuelo;
    }

    private boolean reatenderPedido(Pedido pedido, List<Aeropuerto> origenes, List<Plan> planes,
                                    Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        System.out.printf(">> REATENDIENDO PEDIDO #%d de '%d'.%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.totalPed);
        // Declaración & inicialización de variables
        Segmentacion sVigente = pedido.obtenerSegementacionVigente();
        List<Lote> lotesPorReplanificar = sVigente.getLotesPorRuta().values().stream().filter(l -> l.getEstado().equals(EstadoLote.POR_REPLANIFICAR)).collect(Collectors.toList());
        if (lotesPorReplanificar.isEmpty()) return true; // Validación de inexistencia de lotes por replanificar
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        Aeropuerto destino = pedido.getDestino();
        Segmentacion sNew = new Segmentacion(sVigente);
        sVigente.getLotesPorRuta().keySet().stream().filter(r -> sVigente.getLotesPorRuta().get(r).getEstado().equals(EstadoLote.POR_REPLANIFICAR)).forEach(sNew.getLotesPorRuta()::remove);
        HashMap<Lote, Ruta> enrutamientosNew = new HashMap<>(), enrutamientosOld = new HashMap<>();
        HashMap<Lote, Aeropuerto> conexionesNew = new HashMap<>(), conexionesOld = new HashMap<>();
        while(!lotesPorReplanificar.isEmpty()) {
            Lote lReplanificar = lotesPorReplanificar.get(random.nextInt(lotesPorReplanificar.size()));
            PuntoDeReplanificacion pdr = Problematica.PUNTOS_REPLANIFICACION.stream().filter(p -> p.getLotes().contains(lReplanificar)).findFirst().orElse(null);
            List<Vuelo> vuelosFijos = pdr.getVuelosFijos();
            Vuelo vueloReplanificado = pdr.getVueloReplanificado();
            Aeropuerto aeropuertoDeConexion = pdr.getAeropuertoDeConexion();
            Ruta rutaInicial = pdr.getRutaInicial();
            LocalDateTime umbralDeConexion = pdr.getUmbralDeConexion();
            LocalDateTime instanteMinimoDeEgreso = pedido.getFechaHoraProcesamiento();
            LocalDateTime instanteMaximoDeEgreso = pedido.getFechaHoraProcesamiento().plusMinutes((long)(60*Problematica.MAX_HORAS_ESTANCIA));
            Registro rReplanificar = aeropuertoDeConexion.obtenerRegistroDeLoteDeProductos(lReplanificar);
            if(rReplanificar != null) {
                instanteMinimoDeEgreso = rReplanificar.getFechaHoraIngreso().plusMinutes((long)(60*Problematica.MIN_HORAS_ESTANCIA));
                instanteMaximoDeEgreso = rReplanificar.getFechaHoraIngreso().plusMinutes((long)(60*Problematica.MAX_HORAS_ESTANCIA));
            }
            enrutamientosOld.put(lReplanificar, rutaInicial);
            conexionesOld.put(lReplanificar, aeropuertoDeConexion);
            rutaInicial.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lReplanificar, aeropuertoDeConexion, true);
            int restantePorReplanificar = lReplanificar.getTamanio();
            // Replanificación de productos de lote
            while(restantePorReplanificar > 0) {
                // Búsqueda de ruta reutilizable
                Ruta ruta = buscarRutaVoraz(rutaInicial, vueloReplanificado, vuelosFijos, aeropuertoDeConexion, umbralDeConexion, instanteMinimoDeEgreso, instanteMaximoDeEgreso, pedido.getFechaHoraGeneracion(), destino, rutasAsignadas);
                if(ruta == null) {
                    // Construcción de nueva ruta
                    ruta = construirRutaVoraz(vueloReplanificado, vuelosFijos, aeropuertoDeConexion, umbralDeConexion, instanteMinimoDeEgreso, instanteMaximoDeEgreso, pedido.getFechaHoraGeneracion(), origenes, destino, planes, vuelosActivados);
                    if(ruta == null) {
                        lotesPorReplanificar.forEach(l -> l.setEstado(EstadoLote.PLANIFICADO));
                        if(!rutaInicial.getEstado().equals(EstadoRuta.DESHABILITADA)) {
                            System.out.println("Reutilizando ruta inicial..");
                            enrutamientosNew.forEach((l, r) -> r.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(l, conexionesNew.get(l), false));
                            enrutamientosOld.forEach((l, r) -> {
                                r.agregarRegistroDeLoteDeProductosDesdeAeropuerto(l, conexionesOld.get(l), vuelosEnTransito, rutasEnOperacion);
                                l.setEstado(EstadoLote.PLANIFICADO);
                            });
                            return true;
                        } else return false;
                    }
                }
                // Segmentación de lote respecto a disponibilidad de ruta
                int rCapDisp = ruta.obtenerCapacidadDisponibleDesdeAeropuerto(aeropuertoDeConexion);
                int cantEnrutables = Math.min(rCapDisp, restantePorReplanificar);
                System.out.printf("Enrutando %d productos..%n", cantEnrutables);
                // Producción y registro de segmento de pedido
                Lote lote = ruta.getOrigen().generarLoteDeProductos(cantEnrutables);
                if(sNew.getLotesPorRuta().containsKey(ruta)) {
                    Lote lConsolidar = sNew.getLotesPorRuta().get(ruta);
                    System.out.printf("CONSOLIDACIÓN-INI:       Consolidando lote '%s(%d)' en '%s(%d)' desde '%s'%n", lConsolidar.getCodigo(),lConsolidar.getTamanio(),lote.getCodigo(),lote.getTamanio(),aeropuertoDeConexion.getCodigo());
                    enrutamientosOld.put(lConsolidar, ruta);
                    conexionesOld.put(lConsolidar, aeropuertoDeConexion);
                    ruta.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lConsolidar, aeropuertoDeConexion, true);
                    lote.setTamanio(lote.getTamanio() + lConsolidar.getTamanio());
                    sNew.getLotesPorRuta().remove(ruta);
                    lConsolidar.setEstado(EstadoLote.REPLANIFICADO);
                    System.out.printf("CONSOLIDACIÓN-FIN:       El lote '%s' ahora tiene un tamanio de '%d'%n", lote.getCodigo(),lote.getTamanio());
                }
                enrutamientosNew.put(lote, ruta);
                conexionesNew.put(lote, aeropuertoDeConexion);
                ruta.agregarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aeropuertoDeConexion, vuelosActivados, rutasAsignadas);
                sNew.getLotesPorRuta().put(ruta, lote);
                restantePorReplanificar -= cantEnrutables;
                System.out.printf("Quedan '%d' por enrutar!%n", restantePorReplanificar);
                // Validación por atención completa de pedido
                if(restantePorReplanificar == 0) {
                    vuelosEnTransito.addAll(vuelosActivados);
                    rutasEnOperacion.addAll(rutasAsignadas);
                    lReplanificar.setEstado(EstadoLote.REPLANIFICADO);
                    lotesPorReplanificar.remove(lReplanificar);
                }
            }
        }
        sVigente.setFechaHoraSustitucion(Problematica.UMBRAL_REPLANIFICACION);
        sNew.setFechaHoraAplicacion(Problematica.UMBRAL_REPLANIFICACION);
        pedido.getSegmentaciones().add(sNew);
        pedido.setFechaHoraExpiracion();
        G4DUtility.Logger.logf(">>> PEDIDO #%d REATENDIDO! | '%d' de '%d' productos enrutados!%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.numProd-1, pedido.getCantidadSolicitada());
        return true;
    }

    private Ruta buscarRutaVoraz(Ruta rutaInicial, Vuelo vueloReplanificado, List<Vuelo> vuelosFijos, Aeropuerto aeropuertoDeConexion, LocalDateTime umbralDeConexion,
                                 LocalDateTime instanteMinimoDeEgreso, LocalDateTime instanteMaximoDeEgreso, LocalDateTime instanteDeGeneracion,
                                 Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        System.out.println("Buscando ruta voraz en operación..");
        // Búsqueda de ruta reutilizable
        List<Ruta> rutasPosibles = rutasAsignadas.stream().filter(r -> r.getDestino().equals(destino))
                                                          .filter(r -> !r.equals(rutaInicial))
                                                          .filter(r -> r.getEstado().equals(EstadoRuta.OPERATIVA))
                                                          .filter(r -> {
                                                              if(aeropuertoDeConexion == null) {
                                                                  return r.getOrigen().getEsSede();
                                                              }
                                                              List<Aeropuerto> sa = r.obtenerSecuenciaDeAeropuertos();
                                                              int posConexion = sa.indexOf(aeropuertoDeConexion);
                                                              if(posConexion == -1) return false;
                                                              return posConexion < sa.size() - 1;
                                                          }).toList();
        for(Ruta ruta : rutasPosibles) {
            boolean esValida = false;
            if(!vuelosFijos.isEmpty()) {
                for(int i = 0; i < vuelosFijos.size(); i++) {
                    if(!vuelosFijos.get(i).equals(ruta.getVuelos().get(i))) {
                        break;
                    }
                    if(i == vuelosFijos.size() - 1) {
                        if(ruta.getVuelos().size() > vuelosFijos.size() ) {
                            if(!ruta.getVuelos().get(i + 1).equals(vueloReplanificado)) {
                                esValida = true;
                                break;
                            }
                        }
                    }
                }
            } else if(!ruta.getVuelos().getFirst().equals(vueloReplanificado)) {
                esValida = true;
            }
            if(esValida) {
                Aeropuerto origen = ruta.getOrigen();
                TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
                LocalDateTime instanteLimite = instanteDeGeneracion.plusMinutes(tipoRuta.getMaxMinutosParaEntrega());
                if(!ruta.esAlcanzableDesdeAeropuerto(aeropuertoDeConexion, umbralDeConexion, instanteMinimoDeEgreso, instanteMaximoDeEgreso, instanteLimite)) continue;
                ruta.setTipo(tipoRuta);
                System.out.printf("Se encontró la ruta '%s': '%s'", ruta.getCodigo(), ruta.getOrigen().getCodigo());
                ruta.getVuelos().forEach(v -> System.out.printf(" '%s'", v.getPlan().getDestino().getCodigo()));
                System.out.println();
                return ruta;
            }
        }
        System.out.println("No se encontró.");
        return null;
    }


    private Ruta construirRutaVoraz(Vuelo vueloReplanificado, List<Vuelo> vuelosFijos, Aeropuerto aeropuertoDeConexion, LocalDateTime umbralDeConexion,
                                    LocalDateTime instanteMinimoDeEgreso, LocalDateTime instanteMaximoDeEgreso, LocalDateTime instanteDeGeneracion,
                                    List<Aeropuerto> origenes, Aeropuerto destino, List<Plan> planes, Set<Vuelo> vuelosActivados) {
        System.out.println("Construyendo nueva ruta voraz..");
        // Declaración & inicialización de variables
        List<Vuelo> secuenciaDeVuelos = (!vuelosFijos.isEmpty()) ? new ArrayList<>(vuelosFijos) : new ArrayList<>();
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        secuenciaDeVuelos.forEach(v -> aeropuertosVisitados.add(v.getPlan().getOrigen()));
        TipoRuta tipoRuta;
        if(!vuelosFijos.isEmpty()) {
            tipoRuta = (vuelosFijos.getFirst().getPlan().getOrigen().getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
        } else tipoRuta = (aeropuertoDeConexion.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
        LocalDateTime instanteLimite = instanteDeGeneracion.plusMinutes(tipoRuta.getMaxMinutosParaEntrega());
        LocalDateTime instanteActual = umbralDeConexion;
        LocalDateTime origInstanteMinimoDeEgreso = instanteMinimoDeEgreso;
        LocalDateTime origInstanteMaximoDeEgreso = instanteMaximoDeEgreso;
        Aeropuerto actual = aeropuertoDeConexion;
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            G4DUtility.Logger.logf("- Aeropuerto completed: %s%n", actual);
            // Búsqueda de plan de vuelo más próximo
            G4DUtility.Logger.logln("- Buscando vuelo más próximo..");
            Vuelo mejorVuelo = obtenerVueloMasProximo(vueloReplanificado, actual, destino, instanteActual, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorVuelo == null) {
                G4DUtility.Logger.logln("- No se pudo encontrar ni activar un vuelo.");
                System.out.println("No se pudo construir la ruta.");
                return null;
            }
            G4DUtility.Logger.logf("- Vuelo asignado: %s -> %s%n", mejorVuelo.getPlan().getOrigen().getCodigo(), mejorVuelo.getPlan().getDestino().getCodigo());
            // Asignación de vuelo
            secuenciaDeVuelos.add(mejorVuelo);
            actual = mejorVuelo.getPlan().getDestino();
            instanteActual = mejorVuelo.getFechaHoraLlegada();
            origInstanteMinimoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*Problematica.MIN_HORAS_ESTANCIA));
            origInstanteMaximoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*Problematica.MAX_HORAS_ESTANCIA));
        }
        // Actualización de ruta construida
        G4DUtility.Logger.logln("Guardando ruta..");
        Ruta ruta = new Ruta();
        ruta.setOrigen(secuenciaDeVuelos.getFirst().getPlan().getOrigen());
        ruta.setDestino(destino);
        ruta.setTipo(tipoRuta);
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.instanciarAtributos();
        ruta.setDuracion();
        ruta.setDistancia();
        System.out.printf("Nueva ruta construida '%s': '%s'", ruta.getCodigo(), ruta.getOrigen().getCodigo());
        ruta.getVuelos().forEach(v -> System.out.printf(" '%s'", v.getPlan().getDestino().getCodigo()));
        System.out.println();
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
                rDest.agregarRegistroDeLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
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
            r.agregarRegistroDeLoteDeProductos(l, vuelosEnTransito, rutasEnOperacion);
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
                        rIni.agregarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                        G4DUtility.Logger.delete_upper_line();
                        continue;
                    } else G4DUtility.Logger.logln(" [VALIDA]");
                    // Fusión
                    G4DUtility.Logger.logf(": Mejor fitness completed: %.3f | >> FUSIONANDO..", mejorFitness);
                    segmentacion.remove(rIni);
                    rNew.agregarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
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
                    rIni.agregarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
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
                rNew.agregarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
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
            rDest.agregarRegistroDeLoteDeProductos(lNew, vuelosEnTransito, rutasEnOperacion);
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
                        if (intentos >= N_MAX) {
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
                rIni.agregarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
                segmentacion.put(rIni, lote);
                G4DUtility.Logger.delete_lines(3);
                continue;
            } else G4DUtility.Logger.logln(" [VALIDA]");
            // Fusión
            G4DUtility.Logger.logln(": Fusionando..");
            rNew.agregarRegistroDeLoteDeProductos(lote, vuelosEnTransito, rutasEnOperacion);
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
