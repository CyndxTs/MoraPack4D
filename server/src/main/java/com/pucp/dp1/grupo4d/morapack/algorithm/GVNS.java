/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GVNS.java
 [**/

package com.pucp.dp1.grupo4d.morapack.algorithm;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.pucp.dp1.grupo4d.morapack.model.algorithm.*;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoLote;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoRuta;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoRuta;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class GVNS {
    private static final Random random = new Random();
    public Double dMin;                                 // Diferencia mínima considerable de fitness
    public Integer iMax;                                // Número máximo de iteraciones de exploración inicial
    public Integer eleMin;                              // Nivel mínimo de búsqueda local
    public Integer eleMax;                              // Nivel máximo de búsqueda local
    public Integer kMin;                                // Nivel mínimo de perturbación
    public Integer kMax;                                // Nivel máximo de perturbación
    public Integer nMax;                                // Número de máximo de intentos por nivel de perturbación
    public Integer tMax;                                // Tiempo máximo esperado de exploración global
    public Solucion solucion;

    public GVNS() {
        this.solucion = new Solucion();
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
        VND(problematica, x);
        G4DUtility.Logger.Stats.set_local_duration();
        G4DUtility.Logger.logf("[+] OPTIMIZACION INICIAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        this.solucion = x;
        // Optimización final (Variable Neighborhood Search)
        G4DUtility.Logger.Stats.set_local_start();
        G4DUtility.Logger.logln("Realizando optimización final.. ");
        VNS(problematica, x);
        G4DUtility.Logger.Stats.set_local_duration();
        G4DUtility.Logger.logf("[+] OPTIMIZACION FINAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        this.solucion = x;
         */
        // Limpieza de datos sobrantes (Garbage Collection)
        G4DUtility.Logger.Stats.set_local_start();
        G4DUtility.Logger.logln("Realizando limpieza final.. ");
        limpiezaFinal(problematica, x);
        G4DUtility.Logger.Stats.set_local_duration();
        G4DUtility.Logger.logf("[*] LIMPIEZA FINAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        G4DUtility.Logger.Stats.set_global_duration();
        G4DUtility.Logger.Stats.log_stat_global_sol();
    }

    private void solucionInicial(Problematica problematica, Solucion solucion) {
        G4DUtility.Logger.logln("[NN]");
        // Declaración & inicialización de variables
        boolean errorDeEnrutamiento, haySolucion = false;
        Problematica pAux = new Problematica();
        // Iteración de exploraciones iniciales para encontrar 1 solución
        for(int i = 0; i < iMax; i++) {
            System.out.printf(">> Iteración: %d de '%d':%n", i + 1, iMax);
            pAux = problematica.replicar();
            List<Aeropuerto> origenes = new ArrayList<>(pAux.origenes.values());
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
            prepararReplanificacion(pAux, pedidos);
            // Atención & reatención de pedidos
            G4DUtility.Logger.Stats.set_process_start();
            for (Pedido pedido : pedidos) {
                G4DUtility.Logger.Stats.numProd = 1;
                G4DUtility.Logger.Stats.log_stat_ped();
                // Validación por estado de atención de pedido
                if(!pedido.getFueAtendido()) {
                    boolean pedidoAtendido = atenderPedido(pAux, pedido, origenes, planes, vuelosEnTransito, rutasEnOperacion);
                    if(!pedidoAtendido) {
                        G4DUtility.Logger.Stats.log_err_stat();
                        errorDeEnrutamiento = true;
                        break;
                    }
                } else {
                    boolean pedidoReatendido = reatenderPedido(pAux, pedido, planes, vuelosEnTransito, rutasEnOperacion);
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
                System.out.println("Error de enrutamiento...");
                continue;
            }
            // Actualización de solución
            solucion.setPedidosAtendidos(pedidos);
            solucion.setVuelosEnTransito(vuelosEnTransito);
            solucion.setRutasEnOperacion(rutasEnOperacion);
            solucion.getAeropuertosTransitados().addAll(origenes);
            solucion.getAeropuertosTransitados().addAll(destinos);
            solucion.setFitness(problematica);
            haySolucion = true;
            break;
        }
        // Validación por inexistencia de solución
        if(!problematica.pedidos.isEmpty()) {
            if(!haySolucion) {
                System.out.printf("ERROR: No fue posible enrutar todos los pedidos en '%d' iteraciones.%n", iMax);
                solucion.setFitness(-9999.99);
                this.solucion = null;
            } else problematica.reasignar(pAux);
        } else solucion.setFitness(-9999.99);
    }

    private void prepararReplanificacion(Problematica problematica, List<Pedido> pedidos) {
        System.out.println(">> PREPARANDO CONTEXTO DE REPLANIFICACION..");
        // Declaracion & inicialización de Variables
        problematica.puntosDeReplanificacion = new ArrayList<>();
        List<Pedido> pedidosReplanificables = pedidos.stream().filter(Pedido::getFueAtendido).toList();
        // Preparación de pedidos replanificables
        for(Pedido pReplanificable : pedidosReplanificables) {
            Map<Ruta, Lote> sVigente = pReplanificable.obtenerSegementacionVigente().getLotesPorRuta();
            for(Map.Entry<Ruta, Lote> entry : sVigente.entrySet()) {
                Ruta ruta = entry.getKey();
                Lote lote = entry.getValue();
                boolean replanificarAleatoriamente = random.nextDouble() < problematica.probabilidadDeReplanificacion;
                if(ruta.getEstado().equals(EstadoRuta.REVISION_PENDIENTE)) {
                    List<Vuelo> vuelos = ruta.getVuelos();
                    if (ruta.getFechaHoraLlegada().isAfter(problematica.umbralDeReplanificacion) && vuelos.getLast().getFechaHoraSalida().isAfter(problematica.umbralDeReplanificacion)) {
                        Vuelo vAux = vuelos.stream().filter(v -> v.getFechaHoraSalida().isAfter(problematica.umbralDeReplanificacion) && v.getPlan().getOrigen().obtenerRegistroDeLoteDeProductos(lote) != null).findFirst().orElse(vuelos.getFirst());
                        int posVAux = vuelos.indexOf(vAux);
                        List<Vuelo> vuelosFijos = new ArrayList<>(vuelos.subList(0, posVAux));
                        List<Vuelo> vuelosPorReplanificar = new ArrayList<>(vuelos.subList(posVAux, vuelos.size()));
                        Aeropuerto aeropuertoDeConexion = vAux.getPlan().getOrigen();
                        LocalDateTime umbralDeConexion;
                        if(!vuelosFijos.isEmpty()) {
                            umbralDeConexion = (LocalDateTime) G4DUtility.Calculator.getMax(vuelosFijos.getLast().getFechaHoraLlegada(),problematica.umbralDeReplanificacion);
                        } else umbralDeConexion = (LocalDateTime) G4DUtility.Calculator.getMax(pReplanificable.getFechaHoraProcesamiento(), problematica.umbralDeReplanificacion);
                        if(vuelosPorReplanificar.stream().anyMatch(v -> v.esProblematico(problematica))) {
                            ruta.setEstado(EstadoRuta.DESHABILITADA);
                        } else ruta.setEstado(EstadoRuta.OPERATIVA);
                        PuntoDeReplanificacion pdr = new PuntoDeReplanificacion();
                        pdr.setRutaInicial(ruta);
                        pdr.setAeropuertoDeConexion(aeropuertoDeConexion);
                        pdr.setUmbralDeConexion(umbralDeConexion);
                        pdr.setVueloReplanificado(vuelosPorReplanificar.getFirst());
                        pdr.setVuelosFijos(vuelosFijos);
                        problematica.puntosDeReplanificacion.add(pdr);
                    } else ruta.setEstado(EstadoRuta.FINALIZADA);
                }
                PuntoDeReplanificacion pdr = problematica.puntosDeReplanificacion.stream().filter(p -> ruta.equals(p.getRutaInicial())).findFirst().orElse(null);
                switch (ruta.getEstado()) {
                    case OPERATIVA -> {
                        if(replanificarAleatoriamente) {
                            lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                            pdr.getLotes().add(lote);
                        }
                    }
                    case DESHABILITADA -> {
                        lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                        pdr.getLotes().add(lote);
                    }
                    case FINALIZADA -> {
                        lote.setEstado(EstadoLote.ENTREGADO);
                    }
                }
            }
        }
        System.out.println(">> CONTEXTO DE REPLANIFICACIÓN CARGADO!");
    }

    private boolean atenderPedido(Problematica problematica, Pedido pedido, List<Aeropuerto> origenes, List<Plan> planes,
                                  Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        System.out.printf(">> ATENDIENDO PEDIDO #%d de '%d' | '%d' para enrutar!.%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.totalPed, pedido.getCantidadSolicitada());
        // Declaración & inicialización de variables
        int cantPorEnrutar = pedido.getCantidadSolicitada();
        LocalDateTime instanteDeGeneracion = pedido.getFechaHoraGeneracion();
        LocalDateTime instanteDeProcesamiento = pedido.getFechaHoraProcesamiento();
        Aeropuerto destino = pedido.getDestino();
        List<Aeropuerto> origenesDisponibles = new ArrayList<>(origenes);
        origenesDisponibles.remove(destino);
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        Map<Ruta, Lote> lotesPorRuta = new HashMap<>();
        // Exploración aleatoria de orígenes
        while(!origenesDisponibles.isEmpty()) {
            Aeropuerto origen = origenesDisponibles.get(random.nextInt(origenesDisponibles.size()));
            System.out.printf("> Origen:  '%s'%n", origen.getCodigo());
            TipoRuta tipoRuta = (origen.getContinente().equals(destino.getContinente())) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            LocalDateTime instanteLimite = instanteDeGeneracion.plusMinutes(tipoRuta.getMaxMinutosParaEntrega(problematica));
            System.out.printf("> Destino: '%s'%n", destino.getCodigo());
            // Búsqueda de ruta reutilizable
            Ruta ruta = buscarRutaVoraz(problematica, instanteDeProcesamiento, instanteLimite, origen, destino, rutasAsignadas);
            if(ruta == null) {
                // Construcción de nueva ruta
                ruta = construirRutaVoraz(problematica, instanteDeProcesamiento, instanteDeProcesamiento, LocalDateTime.MAX, instanteLimite, origen, destino, planes, vuelosActivados);
                if(ruta == null) {
                    System.out.println("ERROR: No fue posible generar una ruta a partir de este origen.");
                    origenesDisponibles.remove(origen);
                    continue;
                }
                System.out.printf("> Nueva ruta construida! '%s': '%s'", ruta.getCodigo(), ruta.getOrigen().getCodigo());
            } else System.out.printf("> Se encontró una ruta! '%s': '%s'", ruta.getCodigo(), ruta.getOrigen().getCodigo());
            ruta.getVuelos().forEach(v -> System.out.printf(" -> V -> '%s'", v.getPlan().getDestino().getCodigo()));
            System.out.println("]");
            // Segmentación de pedido respecto a disponibilidad de ruta
            int rCapDisp = ruta.obtenerCapacidadDisponible(problematica);
            int cantEnrutables = Math.min(rCapDisp, cantPorEnrutar);
            System.out.printf("Enrutando %d de '%d' productos..%n", cantEnrutables, cantPorEnrutar);
            // Producción y registro de segmento de pedido
            Lote lote = origen.generarLoteDeProductos(cantEnrutables);
            ruta.agregarRegistroDeLoteDeProductos(problematica, lote, vuelosActivados, rutasAsignadas);
            lotesPorRuta.put(ruta, lote);
            cantPorEnrutar -= cantEnrutables;
            System.out.printf("Quedan '%d' por enrutar!%n", cantPorEnrutar);
            G4DUtility.Logger.Stats.next_lot(cantEnrutables);
            // Validación por atención completa de pedido
            if(cantPorEnrutar == 0) {
                Segmentacion segmentacion = new Segmentacion();
                segmentacion.setFechaHoraAplicacion(instanteDeProcesamiento);
                segmentacion.setLotesPorRuta(lotesPorRuta);
                pedido.getSegmentaciones().add(segmentacion);
                pedido.setFechaHoraExpiracion(problematica);
                pedido.setFueAtendido(true);
                System.out.printf(">>> PEDIDO #%d ATENDIDO! | '%d' de '%d' productos enrutados!%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.numProd-1, pedido.getCantidadSolicitada());
                vuelosEnTransito.addAll(vuelosActivados);
                rutasEnOperacion.addAll(rutasAsignadas);
                return true;
            }
        }
        return false;
    }

    private Ruta buscarRutaVoraz(Problematica problematica, LocalDateTime instanteInicial, LocalDateTime instanteLimite,
                                 Aeropuerto origen, Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        System.out.println("Buscando ruta voraz en operación..");
        // Búsqueda de ruta reutilizable
        List<Ruta> rutasPosibles = rutasAsignadas.stream().filter(r -> r.getOrigen().equals(origen) && r.getDestino().equals(destino) && r.getEstado().equals(EstadoRuta.OPERATIVA)).toList();
        for(Ruta ruta : rutasPosibles) {
            if(!ruta.esAlcanzable(problematica, instanteInicial, instanteLimite)) continue;
            return ruta;
        }
        System.out.println("> No se pudo encontrar una ruta.");
        return null;
    }

    private Ruta construirRutaVoraz(Problematica problematica, LocalDateTime instanteInicial, LocalDateTime instanteInicialMinimoDeEgreso, LocalDateTime instanteInicialMaximoDeEgreso,
                                    LocalDateTime instanteLimite, Aeropuerto origen, Aeropuerto destino, List<Plan> planes,
                                    Set<Vuelo> vuelosActivados) {
        System.out.println("> Construyendo nueva ruta voraz..");
        // Declaración & inicialización de variables
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime instanteActual = instanteInicial;
        LocalDateTime origInstanteMinimoDeEgreso = instanteInicialMinimoDeEgreso;
        LocalDateTime origInstanteMaximoDeEgreso = instanteInicialMaximoDeEgreso;
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        // Construcción de nueva ruta
        while (!actual.equals(destino)) {
            G4DUtility.Logger.logf("- Ubicación actual: %s%n", actual);
            aeropuertosVisitados.add(actual);
            // Búsqueda de plan de vuelo más próximo
            G4DUtility.Logger.log("- Buscando vuelo más próximo..");
            Vuelo mejorVuelo = obtenerVueloMasProximo(problematica, null, actual, destino, instanteActual, instanteActual, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorVuelo == null) {
                System.out.println("> No se pudo construir la ruta.");
                return null;
            }
            // Asignación de vuelo
            G4DUtility.Logger.logf("- Vuelo asignado: %s -> %s%n", mejorVuelo.getPlan().getOrigen().getCodigo(), mejorVuelo.getPlan().getDestino().getCodigo());
            secuenciaDeVuelos.add(mejorVuelo);
            actual = mejorVuelo.getPlan().getDestino();
            instanteActual = mejorVuelo.getFechaHoraLlegada();
            origInstanteMinimoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*problematica.minHorasDeEstancia));
            origInstanteMaximoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*problematica.maxHorasDeEstancia));
        }
        // Generacion de ruta
        Ruta ruta = new Ruta();
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.instanciarAtributos();
        return ruta;
    }

    private Vuelo obtenerVueloMasProximo(Problematica problematica, Vuelo vueloReplanificado, Aeropuerto origen, Aeropuerto destino,
                                         LocalDateTime instanteActual, LocalDateTime origInstanteDeIngreso, LocalDateTime origInstanteMinimoDeEgreso, LocalDateTime origInstanteMaximoDeEgreso,
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
        while(instanteDeReferenciaActual.isBefore(instanteLimite) && instanteDeReferenciaActual.isBefore(origInstanteMaximoDeEgreso)) {
            for(Plan plan : planesPosibles) {
                if(!plan.esAlcanzable(problematica, vueloReplanificado, instanteDeReferenciaActual, origInstanteDeIngreso, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, destino, vuelosActivados)) continue;
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
        G4DUtility.Logger.log("- Bucando vuelo en tránsito..");
        Vuelo vuelo = planMasProximo.obtenerVueloActivo(instanteDeReferenciaActual, vuelosActivados);
        if(vuelo == null) {
            G4DUtility.Logger.logln("- Activando nuevo vuelo..");
            vuelo = new Vuelo();
            vuelo.setPlan(planMasProximo);
            vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
            vuelo.instanciarAtributos(instanteDeReferenciaActual);
        }
        return vuelo;
    }

    private boolean reatenderPedido(Problematica problematica, Pedido pedido, List<Plan> planes,
                                    Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        System.out.printf(">> REATENDIENDO PEDIDO #%d de '%d'.%n", G4DUtility.Logger.Stats.numPed, G4DUtility.Logger.Stats.totalPed);
        // Declaración & inicialización de variables
        Segmentacion sVigente = pedido.obtenerSegementacionVigente();
        List<Lote> lotesPorReplanificar = sVigente.getLotesPorRuta().values().stream().filter(l -> l.getEstado().equals(EstadoLote.POR_REPLANIFICAR)).collect(Collectors.toList());
        if (lotesPorReplanificar.isEmpty()) return true; // Validación de inexistencia de lotes por replanificar
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        Segmentacion sNew = new Segmentacion(sVigente);
        sVigente.getLotesPorRuta().keySet().stream().filter(r -> sVigente.getLotesPorRuta().get(r).getEstado().equals(EstadoLote.POR_REPLANIFICAR)).forEach(sNew.getLotesPorRuta()::remove);
        while(!lotesPorReplanificar.isEmpty()) {
            HashMap<Lote, Ruta> enrutamientosNew = new HashMap<>(), enrutamientosOld = new HashMap<>();
            HashMap<Lote, Aeropuerto> conexionesNew = new HashMap<>(), conexionesOld = new HashMap<>();
            Lote lReplanificar = lotesPorReplanificar.get(random.nextInt(lotesPorReplanificar.size()));
            int restantePorReplanificar = lReplanificar.getTamanio();
            PuntoDeReplanificacion pdr = problematica.puntosDeReplanificacion.stream().filter(p -> p.getLotes().contains(lReplanificar)).findFirst().orElse(null);
            List<Vuelo> vuelosFijos = pdr.getVuelosFijos();
            Vuelo vueloReplanificado = pdr.getVueloReplanificado();
            Aeropuerto aeropuertoDeConexion = pdr.getAeropuertoDeConexion();
            Ruta rutaInicial = pdr.getRutaInicial();
            LocalDateTime umbralDeConexion = pdr.getUmbralDeConexion();
            LocalDateTime instanteInicialDeIngreso = pedido.getFechaHoraProcesamiento();
            LocalDateTime instanteInicialMinimoDeEgreso = pedido.getFechaHoraProcesamiento();
            LocalDateTime instanteInicialMaximoDeEgreso = LocalDateTime.MAX;
            Registro rReplanificar = aeropuertoDeConexion.obtenerRegistroDeLoteDeProductos(lReplanificar);
            if(rReplanificar != null) {
                instanteInicialDeIngreso = rReplanificar.getFechaHoraIngreso();
                instanteInicialMinimoDeEgreso = rReplanificar.getFechaHoraIngreso().plusMinutes((long)(60*problematica.minHorasDeEstancia));
                instanteInicialMaximoDeEgreso = rReplanificar.getFechaHoraIngreso().plusMinutes((long)(60*problematica.maxHorasDeEstancia));
            }
            System.out.printf("[*] Replanificando lote %s(%d) desde '%s' hasta '%s'%n", lReplanificar.getCodigo(), lReplanificar.getTamanio(), aeropuertoDeConexion.getCodigo(), pedido.getDestino().getCodigo());
            System.out.printf(" :  Ruta inicial ('%s'): ['%s'", rutaInicial.getCodigo(), rutaInicial.getOrigen().getCodigo());
            rutaInicial.getVuelos().forEach(v -> System.out.printf(" -> V -> '%s'", v.getPlan().getDestino().getCodigo()));
            System.out.println("]");
            System.out.print(" :  Vuelos fijos: [");
            if(!vuelosFijos.isEmpty()) {
                System.out.printf(" '%s'", vuelosFijos.getFirst().getPlan().getOrigen().getCodigo());
                vuelosFijos.forEach(v -> System.out.printf(" V -> '%s'", v.getPlan().getDestino().getCodigo()));
            }
            System.out.println("]");
            rutaInicial.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lReplanificar, aeropuertoDeConexion, true);
            enrutamientosOld.put(lReplanificar, rutaInicial);
            conexionesOld.put(lReplanificar, aeropuertoDeConexion);
            // Replanificación de productos de lote
            while(restantePorReplanificar > 0) {
                // Búsqueda de ruta reutilizable
                Ruta ruta = buscarRutaVoraz(problematica, rutaInicial, vueloReplanificado, vuelosFijos, aeropuertoDeConexion, umbralDeConexion, instanteInicialMinimoDeEgreso, instanteInicialMaximoDeEgreso, pedido.getFechaHoraGeneracion(), pedido.getDestino(), rutasAsignadas);
                if(ruta == null) {
                    // Construcción de nueva ruta
                    ruta = construirRutaVoraz(problematica, vueloReplanificado, vuelosFijos, aeropuertoDeConexion, umbralDeConexion, instanteInicialDeIngreso, instanteInicialMinimoDeEgreso, instanteInicialMaximoDeEgreso, pedido.getFechaHoraGeneracion(), pedido.getDestino(), planes, vuelosActivados);
                    if(ruta == null) {
                        if(!rutaInicial.getEstado().equals(EstadoRuta.DESHABILITADA)) {
                            System.out.println("> Revirtiendo hacia estado anterior..");
                            lReplanificar.setEstado(EstadoLote.PLANIFICADO);
                            lotesPorReplanificar.remove(lReplanificar);
                            enrutamientosNew.forEach((l, r) -> {
                                r.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(l, conexionesNew.get(l));
                                sNew.getLotesPorRuta().remove(r);
                            });
                            enrutamientosOld.forEach((l, r) -> {
                                r.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, l, conexionesOld.get(l), vuelosEnTransito, rutasEnOperacion, true);
                                l.setEstado(EstadoLote.PLANIFICADO);
                                sNew.getLotesPorRuta().put(r, l);
                            });
                            break;
                        } else return false;
                    }
                    System.out.printf("> Nueva ruta construida! '%s': ['%s'", ruta.getCodigo(), ruta.getOrigen().getCodigo());
                } else System.out.printf("> Se encontró una ruta! '%s': ['%s'", ruta.getCodigo(), ruta.getOrigen().getCodigo());
                ruta.getVuelos().forEach(v -> System.out.printf(" -> V -> '%s'", v.getPlan().getDestino().getCodigo()));
                System.out.println("]");
                // Segmentación de lote respecto a disponibilidad de ruta
                int rCapDisp = ruta.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aeropuertoDeConexion);
                int cantEnrutables = Math.min(rCapDisp, restantePorReplanificar);
                System.out.printf("Enrutando %d de '%d' productos..%n", cantEnrutables, restantePorReplanificar);
                // Producción y registro de segmento de pedido
                Lote lote = ruta.getOrigen().generarLoteDeProductos(cantEnrutables);
                if(sNew.getLotesPorRuta().containsKey(ruta)) {
                    Lote lConsolidar = sNew.getLotesPorRuta().get(ruta);
                    System.out.printf("CONSOLIDACIÓN-INI:       Consolidando lote '%s(%d)' en '%s(%d)' desde '%s'%n", lConsolidar.getCodigo(),lConsolidar.getTamanio(),lote.getCodigo(),lote.getTamanio(),aeropuertoDeConexion.getCodigo());
                    ruta.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lConsolidar, aeropuertoDeConexion, true);
                    enrutamientosOld.put(lConsolidar, ruta);
                    conexionesOld.put(lConsolidar, aeropuertoDeConexion);
                    lConsolidar.setEstado(EstadoLote.REPLANIFICADO);
                    lote.setTamanio(lote.getTamanio() + lConsolidar.getTamanio());
                    sNew.getLotesPorRuta().remove(ruta);
                    System.out.printf("CONSOLIDACIÓN-FIN:       El lote '%s' ahora tiene un tamanio de '%d'%n", lote.getCodigo(),lote.getTamanio());
                }
                ruta.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aeropuertoDeConexion, vuelosActivados, rutasAsignadas);
                enrutamientosNew.put(lote, ruta);
                conexionesNew.put(lote, aeropuertoDeConexion);
                sNew.getLotesPorRuta().put(ruta, lote);
                restantePorReplanificar -= cantEnrutables;
                System.out.printf("Quedan '%d' por enrutar!%n", restantePorReplanificar);
                // Validación por atención completa de pedido
                if(restantePorReplanificar == 0) {
                    vuelosEnTransito.addAll(vuelosActivados);
                    rutasEnOperacion.addAll(rutasAsignadas);
                    lReplanificar.setEstado(EstadoLote.REPLANIFICADO);
                    lotesPorReplanificar.remove(lReplanificar);
                    pdr.getLotes().addAll(enrutamientosNew.keySet());
                    pdr.getLotes().addAll(enrutamientosOld.keySet());
                }
            }
        }
        sVigente.setFechaHoraSustitucion(problematica.umbralDeReplanificacion);
        sNew.setFechaHoraAplicacion(problematica.umbralDeReplanificacion);
        pedido.getSegmentaciones().add(sNew);
        pedido.setFechaHoraExpiracion(problematica);
        System.out.printf(">>> PEDIDO #%d REATENDIDO!%n", G4DUtility.Logger.Stats.numPed);
        return true;
    }

    private Ruta buscarRutaVoraz(Problematica problematica, Ruta rutaInicial, Vuelo vueloReplanificado, List<Vuelo> vuelosFijos, Aeropuerto aeropuertoDeConexion, LocalDateTime umbralDeConexion,
                                 LocalDateTime instanteInicialMinimoDeEgreso, LocalDateTime instanteInicialMaximoDeEgreso, LocalDateTime instanteDeGeneracion,
                                 Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        System.out.println("> Buscando ruta voraz en operación..");
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
                LocalDateTime instanteLimite = instanteDeGeneracion.plusMinutes(tipoRuta.getMaxMinutosParaEntrega(problematica));
                if(!ruta.esAlcanzableDesdeAeropuerto(problematica, aeropuertoDeConexion, umbralDeConexion, instanteInicialMinimoDeEgreso, instanteInicialMaximoDeEgreso, instanteLimite)) continue;
                ruta.setTipo(tipoRuta);
                return ruta;
            }
        }
        System.out.println("> No se pudo encontrar una ruta.");
        return null;
    }


    private Ruta construirRutaVoraz(Problematica problematica, Vuelo vueloReplanificado, List<Vuelo> vuelosFijos, Aeropuerto aeropuertoDeConexion, LocalDateTime umbralDeConexion,
                                    LocalDateTime instanteInicialDeIngreso, LocalDateTime instanteInicialMinimoDeEgreso, LocalDateTime instanteInicialMaximoDeEgreso, LocalDateTime instanteDeGeneracion,
                                    Aeropuerto destino, List<Plan> planes, Set<Vuelo> vuelosActivados) {
        System.out.println("> Construyendo nueva ruta voraz..");
        // Declaración & inicialización de variables
        List<Vuelo> secuenciaDeVuelos = (!vuelosFijos.isEmpty()) ? new ArrayList<>(vuelosFijos) : new ArrayList<>();
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        secuenciaDeVuelos.forEach(v -> aeropuertosVisitados.add(v.getPlan().getOrigen()));
        TipoRuta tipoRuta;
        if(!vuelosFijos.isEmpty()) {
            tipoRuta = (vuelosFijos.getFirst().getPlan().getOrigen().getContinente().equals(destino.getContinente())) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
        } else tipoRuta = (aeropuertoDeConexion.getContinente().equals(destino.getContinente())) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
        LocalDateTime instanteLimite = instanteDeGeneracion.plusMinutes(tipoRuta.getMaxMinutosParaEntrega(problematica));
        LocalDateTime instanteActual = umbralDeConexion;
        LocalDateTime origInstanteDeIngreso = instanteInicialDeIngreso;
        LocalDateTime origInstanteMinimoDeEgreso = instanteInicialMinimoDeEgreso;
        LocalDateTime origInstanteMaximoDeEgreso = instanteInicialMaximoDeEgreso;
        Aeropuerto actual = aeropuertoDeConexion;
        while (!actual.equals(destino)) {
            G4DUtility.Logger.logf("- Ubicación actual: %s%n", actual);
            aeropuertosVisitados.add(actual);
            // Búsqueda de plan de vuelo más próximo
            G4DUtility.Logger.log("- Buscando vuelo más próximo..");
            Vuelo mejorVuelo = obtenerVueloMasProximo(problematica, vueloReplanificado, actual, destino, instanteActual, origInstanteDeIngreso, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorVuelo == null) {
                System.out.println("> No se pudo construir la ruta.");
                return null;
            }
            // Asignación de vuelo
            G4DUtility.Logger.logf("- Vuelo asignado: %s -> %s%n", mejorVuelo.getPlan().getOrigen().getCodigo(), mejorVuelo.getPlan().getDestino().getCodigo());
            secuenciaDeVuelos.add(mejorVuelo);
            actual = mejorVuelo.getPlan().getDestino();
            instanteActual = mejorVuelo.getFechaHoraLlegada();
            origInstanteDeIngreso = mejorVuelo.getFechaHoraLlegada();
            origInstanteMinimoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*problematica.minHorasDeEstancia));
            origInstanteMaximoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*problematica.maxHorasDeEstancia));
        }
        // Actualización de ruta construida
        Ruta ruta = new Ruta();
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.instanciarAtributos();
        return ruta;
    }

    private void VND(Problematica problematica, Solucion solucion) {
        G4DUtility.Logger.logln("[VND]");
        // Declaración & inicialización de variables
        boolean huboMejora;
        // Búsqueda local de soluciones por nivel de intensidad
        for(int ele = eleMin; ele <= eleMax; ele++) {
            G4DUtility.Logger.logf(">> Intensidad de busqueda: %d de '%d'%n", ele, eleMax);
            int i = 1;
            // Iteración de vecindarios a visitar
            while (i <= 3) {
                Object[] contextoPropuesto = replicarContexto(problematica, solucion);
                Problematica pAux =  (Problematica) contextoPropuesto[0];
                Solucion sAux = (Solucion) contextoPropuesto[1];
                huboMejora = false;
                switch (i) {
                    case 1:
                        huboMejora = LSCompactar(pAux, sAux, ele);
                        break;
                    case 2:
                        // huboMejora = LSFusionar(pAux, sAux, ele);
                        break;
                    case 3:
                        // huboMejora = LSRealocar(pAux, sAux, ele);
                        break;
                }
                // Validación por mejora obtenida
                if (huboMejora) {
                    problematica.reasignar(pAux);
                    solucion.reasignar(sAux);
                    i = 1;
                } else {
                    i++;
                }
            }
        }
    }

    private Boolean LSCompactar(Problematica problematica, Solucion solucion, int ele) {
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
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            // Validación de aptitud
            G4DUtility.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, pedidos.size());
            if (segmentacionModificable.size() < ele + 1) {
                G4DUtility.Logger.logln(" [NO APTO]");
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
                        .filter(r -> r.obtenerCapacidadDisponible(problematica) > 0)
                        .filter(r -> r.respetaPuntosDeReplanificacion(rutasOrig, puntosDeReplanificacion))
                        .collect(Collectors.toList());
                int capDispTotal = rutasDest.stream().mapToInt(r -> r.obtenerCapacidadDisponible(problematica)).sum();
                if (capDispTotal < totalCompactar) {
                    G4DUtility.Logger.logln(" [INVALIDA]");
                    continue;
                } else G4DUtility.Logger.logln("[VALIDA]");
                // Compactación
                G4DUtility.Logger.logf(": Mejor fitness completed: %.3f | >> COMPACTANDO..", mejorFitness);
                rutasDest.sort(Comparator.comparing(Ruta::getFechaHoraLlegada));
                compactarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
                solucion.setFitness(problematica);
                double fitnessObtenido = solucion.getFitness();
                G4DUtility.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, dMin)) {
                    G4DUtility.Logger.logln(" | ¡NUEVO MEJOR!");
                    mejorFitness = solucion.getFitness();
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4DUtility.Logger.logln();
                // Reversión de cambios
                revertirCambios(problematica, segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
            }
            // Aplicar mejor combinación
            if (posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                List<Ruta> rutasDest = segmentacionModificable.keySet().stream().filter(r -> !rutasOrig.contains(r))
                        .filter(r -> r.obtenerCapacidadDisponible(problematica) > 0)
                        .filter(r -> r.respetaPuntosDeReplanificacion(rutasOrig, puntosDeReplanificacion))
                        .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                        .collect(Collectors.toList());
                compactarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
            }
        }
        solucion.setFitness(problematica);
        G4DUtility.Logger.logf("> 'Compactación' : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4DUtility.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4DUtility.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private void compactarSegmentacion(Problematica problematica, List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> segmentacion, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        // Iteración por orígenes para compactar
        for(Ruta rOrig : rutasOrig) {
            Lote lOrig = segmentacion.get(rOrig);
            int restante = lOrig.getTamanio();
            Aeropuerto aConexion = null;
            if(puntosDeReplanificacion.get(rOrig) != null) {
                puntosDeReplanificacion.get(rOrig).getLotes().remove(lOrig);
                aConexion = puntosDeReplanificacion.get(rOrig).getAeropuertoDeConexion();
            }
            rOrig.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lOrig, aConexion, true);
            segmentacion.remove(rOrig);
            // Compactación en destinos seleccionados
            for(Ruta rDest : rutasDest) {
                if(restante == 0) break;
                int rDestCapDisp = rDest.obtenerCapacidadDisponible(problematica);
                if(rDestCapDisp == 0) continue;
                int asignable = Math.min(restante, rDestCapDisp);
                Lote lOld = segmentacion.get(rDest);
                int consolidado = lOld.getTamanio() + asignable;
                rDest.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lOld, aConexion, true);
                segmentacion.remove(rDest);
                Lote lNew = rDest.getOrigen().generarLoteDeProductos(consolidado);
                rDest.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lNew, aConexion, vuelosEnTransito, rutasEnOperacion);
                if(puntosDeReplanificacion.get(rOrig) != null) puntosDeReplanificacion.get(rOrig).getLotes().add(lNew);
                segmentacion.put(rDest, lNew);
                restante -= asignable;
            }
        }
    }

    private void revertirCambios(Problematica problematica, Map<Ruta, Lote> segmentacion, Map<Ruta, Lote> segmentacionAux, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        // Eliminación de registros actualizados
        for (Ruta r : segmentacion.keySet()) {
            Lote l = segmentacion.get(r);
            Aeropuerto aConexion = null;
            for(PuntoDeReplanificacion pdr : puntosDeReplanificacion.values()) {
                if(pdr != null && pdr.getLotes().remove(l)) {
                    aConexion = pdr.getAeropuertoDeConexion();
                    break;
                }
            }
            r.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(l, aConexion);
        }
        segmentacion.clear();
        // Agregación de registros antiguos
        segmentacion.putAll(segmentacionAux);
        for (Ruta r : segmentacion.keySet()) {
            Lote l = segmentacion.get(r);
            Aeropuerto aConexion = null;
            if(puntosDeReplanificacion.containsKey(r) && puntosDeReplanificacion.get(r) != null) {
                puntosDeReplanificacion.get(r).getLotes().add(l);
                aConexion = puntosDeReplanificacion.get(r).getAeropuertoDeConexion();
            }
            r.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, l, aConexion, vuelosEnTransito, rutasEnOperacion, true);
        }
    }

    private Boolean LSFusionar(Problematica problematica, Solucion solucion, int ele) {
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
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
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
                    continue;
                } else G4DUtility.Logger.logln(" [VALIDA]");
                for(int posConexionFin = saFin.size() - 2, numConexion = 1; posConexionFin > 0; posConexionFin--, numConexion++) {
                    G4DUtility.Logger.logf(": Validando conexion #%d de '%d'..", numConexion, saFin.size());
                    Aeropuerto aConexion = saFin.get(posConexionFin);
                    List<Aeropuerto> saIni = rIni.obtenerSecuenciaDeAeropuertos();
                    int posConexionIni = saIni.indexOf(aConexion);
                    if(posConexionIni == -1) {
                        G4DUtility.Logger.logln(" [INVALIDA]");
                        continue;
                    }
                    // Validación de aptitud de fusión por existencia de disponibilidad de ruta
                    List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posConexionIni));
                    LocalDateTime fechaHoraLlegadaAConexion = svIni.getLast().getFechaHoraLlegada();
                    List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posConexionFin, rFin.getVuelos().size()));
                    LocalDateTime fechaHoraSalidaDesdeConexion = svFin.getFirst().getFechaHoraSalida();
                    if(fechaHoraLlegadaAConexion.isAfter(fechaHoraSalidaDesdeConexion) || fechaHoraLlegadaAConexion.plusMinutes((long)(60*problematica.maxHorasDeEstancia)).isAfter(fechaHoraSalidaDesdeConexion)) {
                        G4DUtility.Logger.logln(" [INVALIDA]");
                        continue;
                    }
                    List<Vuelo> svNew = new ArrayList<>();
                    svNew.addAll(svIni);
                    svNew.addAll(svFin);
                    Ruta rNew = new Ruta(rIni);
                    rNew.setVuelos(svNew);
                    if(!rNew.respetaPuntosDeReplanificacion(List.of(rIni), puntosDeReplanificacion)) {
                        G4DUtility.Logger.logln(" [INVALIDA]");
                        continue;
                    }
                    Lote lote = segmentacion.get(rIni);
                    rIni.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aConexion, true);
                    if(rNew.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aConexion) < lote.getTamanio()) {
                        G4DUtility.Logger.logln(" [INVALIDA]");
                        rIni.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion, true);
                        continue;
                    } else G4DUtility.Logger.logln(" [VALIDA]");
                    // Fusión
                    G4DUtility.Logger.logf(": Mejor fitness completed: %.3f | >> FUSIONANDO..", mejorFitness);
                    segmentacion.remove(rIni);
                    rNew.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion);
                    segmentacion.put(rNew, lote);
                    solucion.setFitness(problematica);
                    double fitnessObtenido = solucion.getFitness();
                    G4DUtility.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                    // Validación por mejora de fitness
                    if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, dMin)) {
                        G4DUtility.Logger.logln(" | ¡NUEVO MEJOR!");
                        mejorFitness = fitnessObtenido;
                        huboMejora = true;
                        posMejorComb = posComb;
                        posMejorConexionIni = posConexionIni;
                        posMejorConexionFin = posConexionFin;
                    } else G4DUtility.Logger.logln();
                    // Reversión de cambios [Nuevos]
                    rNew.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aConexion);
                    segmentacion.remove(rNew);
                    rIni.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion, true);
                    segmentacion.put(rIni, lote);
                }
            }
            // Validación de existencia de mejora por fusión
            if(posMejorComb != -1) {
                List<Ruta> combinacion = combinaciones.get(posMejorComb);
                Ruta rIni = combinacion.get(0);
                Ruta rFin = combinacion.get(1);
                Aeropuerto aConexion = rFin.obtenerSecuenciaDeAeropuertos().get(posMejorConexionFin);
                List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posMejorConexionIni));
                List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posMejorConexionFin, rFin.getVuelos().size()));
                List<Vuelo> svNew = new ArrayList<>();
                svNew.addAll(svIni);
                svNew.addAll(svFin);
                Ruta rNew = new Ruta(rIni);
                rNew.setVuelos(svNew);
                Lote lote = segmentacion.get(rIni);
                rIni.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aConexion, true);
                segmentacion.remove(rIni);
                rNew.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion);
                segmentacion.put(rNew, lote);
            }
        }
        // Actualización de solución
        solucion.setFitness(problematica);
        G4DUtility.Logger.logf("> 'Fusión'       : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4DUtility.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4DUtility.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private Boolean LSRealocar(Problematica problematica, Solucion solucion, int ele) {
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
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            // Validación de aptitud
            G4DUtility.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, pedidos.size());
            if (segmentacionModificable.size() < ele) {
                G4DUtility.Logger.logln(" [NO APTO]");
                continue;
            } else G4DUtility.Logger.logln("[APTO]");
            List<Ruta> rutas = new ArrayList<>(segmentacionModificable.keySet());
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(rutas, ele);
            List<Ruta> rutasDestPosibles = rutasEnOperacion.stream().filter(r -> !rutas.contains(r))
                    .filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalida().isBefore(pedido.getFechaHoraProcesamiento()) && !r.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion()))
                    .filter(r -> r.obtenerCapacidadDisponible(problematica) > 0)
                    .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                    .toList();
            int posMejorComb = -1;
            // Iteración de combinaciones
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                G4DUtility.Logger.logf(": Validando combinación #%d de '%d'..", posComb+1, combinaciones.size());
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                List<Ruta> rutasDest = rutasDestPosibles.stream().filter(r -> r.respetaPuntosDeReplanificacion(rutasOrig, puntosDeReplanificacion)).collect(Collectors.toList());
                int capDispTotal = rutasDest.stream().mapToInt(r -> r.obtenerCapacidadDisponible(problematica)).sum();
                int totalRealocar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
                if (capDispTotal < totalRealocar) {
                    G4DUtility.Logger.logln(" [INVALIDA]");
                    continue;
                } else G4DUtility.Logger.logln(" [VALIDA]");
                // Realocación
                G4DUtility.Logger.logf(": Mejor fitness completed: %.3f | >> REALOCANDO..", mejorFitness);
                realocarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
                solucion.setFitness(problematica);
                double fitnessObtenido = solucion.getFitness();
                G4DUtility.Logger.logf(" | >> Fitness obtenido: %.3f", fitnessObtenido);
                if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, dMin)) {
                    G4DUtility.Logger.logln(" | ¡NUEVO MEJOR!");
                    mejorFitness = fitnessObtenido;
                    posMejorComb = posComb;
                    huboMejora = true;
                } else G4DUtility.Logger.logln();
                // Reversión de cambios
                revertirCambios(problematica, segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
            }
            // Aplicar mejor combinación
            if(posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                List<Ruta> rutasDest = rutasDestPosibles.stream().filter(r -> r.respetaPuntosDeReplanificacion(rutasOrig, puntosDeReplanificacion)).collect(Collectors.toList());
                realocarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
            }
        }
        solucion.setFitness(problematica);
        G4DUtility.Logger.logf("> 'Realocación'  : %.3f -> %.3f", fitnessInicial, mejorFitness);
        if (huboMejora) {
            G4DUtility.Logger.logln(" | FITNESS OPTIMIZADO!");
        } else G4DUtility.Logger.logln(" | Fitness mantenido..");
        return huboMejora;
    }

    private void realocarSegmentacion(Problematica problematica, List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> segmentacion, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        // Iteración por orígenes para realocar
        for(Ruta rOrig : rutasOrig) {
            Lote lOrig = segmentacion.get(rOrig);
            int restante = lOrig.getTamanio();
            Aeropuerto aConexion = null;
            if(puntosDeReplanificacion.get(rOrig) != null) {
                puntosDeReplanificacion.get(rOrig).getLotes().remove(lOrig);
                aConexion = puntosDeReplanificacion.get(rOrig).getAeropuertoDeConexion();
            }
            rOrig.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lOrig, aConexion, true);
            segmentacion.remove(rOrig);
            // Realocación en destinos seleccionados
            for(Ruta rDest : rutasDest) {
                if(restante == 0) break;
                int rDestCapDisp = rDest.obtenerCapacidadDisponible(problematica);
                int asignable = Math.min(restante, rDestCapDisp);
                Lote lNew = rDest.getOrigen().generarLoteDeProductos(asignable);
                rDest.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lNew, aConexion, vuelosEnTransito, rutasEnOperacion);
                if(puntosDeReplanificacion.get(rOrig) != null) puntosDeReplanificacion.get(rOrig).getLotes().add(lNew);
                segmentacion.put(rDest, lNew);
                restante -= asignable;
            }
        }
    }

    private void VNS(Problematica problematica, Solucion solucion) {
        G4DUtility.Logger.logln("[VNS]");
        // Declaración & inicialización de variables
        Object[] ctx_best = replicarContexto(problematica, solucion);
        Problematica p_best = (Problematica) ctx_best[0];
        Solucion x_best = (Solucion)  ctx_best[1];
        G4DUtility.IntegerWrapper t = new G4DUtility.IntegerWrapper(), t_best = new G4DUtility.IntegerWrapper();
        Instant start = Instant.now();
        // Búsqueda global de soluciones por nivel de perturbación
        do {
            G4DUtility.IntegerWrapper k = new G4DUtility.IntegerWrapper(kMin);
            ctx_best = replicarContexto(p_best, x_best);
            problematica.reasignar((Problematica) ctx_best[0]);
            solucion.reasignar((Solucion) ctx_best[1]);
            while (t.value < tMax && k.value <= kMax) {
                Problematica p_prima;
                Solucion x_prima;
                boolean solucionValida = false;
                int intentos = 1;
                // Realización de agitaciones aleatorias continuas
                while (true) {
                    Object[] ctx_prima = replicarContexto(problematica, solucion);
                    p_prima = (Problematica) ctx_prima[0];
                    x_prima = (Solucion) ctx_prima[1];
                    G4DUtility.Logger.log("Agitando.. ");
                    Shaking(p_prima, x_prima, k);
                    boolean huboAlteracion = G4DUtility.Calculator.areProximatelyEqual(solucion.getFitness(), x_prima.getFitness(), dMin);
                    // Validación de solución por umbral de aberración
                    if (huboAlteracion  && x_prima.getFitness() < solucion.obtenerUmbralDeAberracion()) {
                        G4DUtility.Logger.logln(" | >> POSIBLE MEJOR SOLUCIÓN");
                        solucionValida = true;
                        break;
                    } else {
                        if(!huboAlteracion) {
                            G4DUtility.Logger.logln(" | >> SIN ALTERACIÓN");
                        } else G4DUtility.Logger.logln(" | >> ABERRACIÓN");
                        if (intentos >= nMax) {
                            G4DUtility.Logger.log("LIMITE DE INTENTOS ALCANZADO.");
                            break;
                        }
                        intentos++;
                    }
                }
                // Validación de existencia de solución
                if (!solucionValida) {
                    k.value++;
                    continue;
                }
                // Reoptimización de posible mejor solución
                Object[] ctx_prima_doble = replicarContexto(p_prima, x_prima);
                Problematica p_prima_doble = (Problematica)  ctx_prima_doble[0];
                Solucion x_prima_doble = (Solucion) ctx_prima_doble[1];
                G4DUtility.Logger.log("Reoptimizando.. ");
                VND(p_prima_doble, x_prima_doble);
                G4DUtility.Logger.logf("> 'Reoptimización' : %.3f -> %.3f%n", x_prima.getFitness(), x_prima_doble.getFitness());
                // Actualización de tiempo transcurrido
                Instant end = Instant.now();
                t.value = (int) Duration.between(start, end).getSeconds();
                // Actualización de vecindario
                G4DUtility.Logger.log("Validando nuevo vencindario.. ");
                NeighborhoodChange(problematica, solucion, p_prima_doble, x_prima_doble, p_best, x_best, k, t, t_best);
            }
            // Actualización de tiempo transcurrido
            Instant end = Instant.now();
            Duration duracion = Duration.between(start, end);
            t.value = (int) duracion.getSeconds();
        } while (t.value < tMax);
        solucion.reasignar(x_best);
    }

    private void Shaking(Problematica problematica, Solucion solucion, G4DUtility.IntegerWrapper k) {
        G4DUtility.Logger.logln("[RAND]");
        // Declaración & inicialización de variables
        double fitnessInicial = solucion.getFitness();
        // Perturbación aleatoria de solución por nivel de intesidad
        G4DUtility.Logger.logf(">> Intensidad de perturbación: %d de '%d'%n", k.value, kMax);
        for (int i = 0; i < k.value; ++i) {
            int neighborhood = random.nextInt(3);
            int ele = eleMin + random.nextInt(eleMax - eleMin + 1);
            switch (neighborhood) {
                case 0:
                    TCompactar(problematica, solucion, ele);
                    break;
                case 1:
                    // TFusionar(problematica, solucion, ele);
                    break;
                case 2:
                    // TRealocar(problematica, solucion, ele);
                    break;
            }
        }
        G4DUtility.Logger.logf("> 'Agitación'    : %.3f -> %.3f", fitnessInicial, solucion.getFitness());
    }

    private void TCompactar(Problematica problematica, Solucion solucion, int ele) {
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
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            // Validación por aptitud de pedido para compactar
            G4DUtility.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, pedidos.size());
            if (segmentacionModificable.size() < ele + 1) {
                G4DUtility.Logger.logln(" [NO APTO]");
                continue;
            } else G4DUtility.Logger.logln(" [APTO]");
            // Validación por aptitud de combinación
            G4DUtility.Logger.log(": Validando combinación aleatoria.. ");
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(new ArrayList<>(segmentacionModificable.keySet()), ele);
            if (combinaciones.isEmpty()) {
                G4DUtility.Logger.logln("[INVALIDA]");
                continue;
            }
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            int totalCompactar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
            List<Ruta> rutasDest = segmentacionModificable.keySet().stream().filter(r -> !rutasOrig.contains(r))
                    .filter(r -> r.obtenerCapacidadDisponible(problematica) > 0)
                    .filter(r -> r.respetaPuntosDeReplanificacion(rutasOrig, puntosDeReplanificacion))
                    .collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(r -> r.obtenerCapacidadDisponible(problematica)).sum();
            if (capDispTotal < totalCompactar) {
                G4DUtility.Logger.logln("[INVALIDA]");
                continue;
            } else G4DUtility.Logger.logln("[VALIDA]");
            // Compactación
            G4DUtility.Logger.logln(": Compactando..");
            Collections.shuffle(rutasDest);
            compactarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
        }
        // Actualización de solución
        solucion.setFitness(problematica);
        G4DUtility.Logger.logf("> 'Compactación' : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void TFusionar(Problematica problematica, Solucion solucion, int ele) {
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
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
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
                continue;
            }
            // Validación de fusión por existencia de conexión en ruta
            int posConexionFin = random.nextInt(1, saFin.size() - 1);
            Aeropuerto aConexion = saFin.get(posConexionFin);
            List<Aeropuerto> saIni = rIni.obtenerSecuenciaDeAeropuertos();
            int posConexionIni = saIni.indexOf(aConexion);
            if(posConexionIni == -1) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                continue;
            }
            // Validación de fusión por existencia de disponibilidad en ruta
            List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posConexionIni));
            LocalDateTime fechaHoraLlegadaAConexion = svIni.getLast().getFechaHoraLlegada();
            List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posConexionFin, rFin.getVuelos().size()));
            LocalDateTime fechaHoraSalidaDesdeConexion = svIni.getFirst().getFechaHoraSalida();
            if(fechaHoraLlegadaAConexion.isAfter(fechaHoraSalidaDesdeConexion) || fechaHoraLlegadaAConexion.plusMinutes((long)(60*problematica.maxHorasDeEstancia)).isAfter(fechaHoraSalidaDesdeConexion)) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                continue;
            }
            List<Vuelo> svNew = new ArrayList<>();
            svNew.addAll(svIni);
            svNew.addAll(svFin);
            Ruta rNew = new Ruta(rIni);
            rNew.setVuelos(svNew);
            if(!rNew.respetaPuntosDeReplanificacion(List.of(rIni), puntosDeReplanificacion)) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                continue;
            }
            Lote lote = segmentacion.get(rIni);
            rIni.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aConexion, true);
            segmentacion.remove(rIni);
            if(rNew.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aConexion) < lote.getTamanio()) {
                G4DUtility.Logger.logln(" [INVALIDA]");
                rIni.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion, true);
                segmentacion.put(rIni, lote);
                continue;
            } else G4DUtility.Logger.logln(" [VALIDA]");
            // Fusión
            G4DUtility.Logger.logln(": Fusionando..");
            rNew.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion);
            segmentacion.put(rNew, lote);
        }
        // Actualización de solución
        solucion.setFitness(problematica);
        G4DUtility.Logger.logf("> 'Fusión'       : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void TRealocar(Problematica problematica, Solucion solucion, int ele) {
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
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            // Validación por aptitud de pedido para realocar
            G4DUtility.Logger.logf("- Validando aptitud del pedido #%d de '%d'..", posPedido+1, pedidos.size());
            if (segmentacion.size() < ele) {
                G4DUtility.Logger.logln("[NO APTO]");
                continue;
            } else G4DUtility.Logger.logln("[APTO]");
            // Validación por aptitud de combinación
            G4DUtility.Logger.log(": Validando combinación aleatoria.. ");
            List<Ruta> rutas = new ArrayList<>(segmentacionModificable.keySet());
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(rutas, ele);
            if (combinaciones.isEmpty()) {
                G4DUtility.Logger.logln("[INVALIDA]");
                continue;
            }
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            List<Ruta> rutasDest = rutasEnOperacion.stream().filter(r -> !rutas.contains(r))
                    .filter(r -> r.getDestino().equals(pedido.getDestino()))
                    .filter(r -> !r.getFechaHoraSalida().isBefore(pedido.getFechaHoraGeneracion()) && !r.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion()))
                    .filter(r -> r.obtenerCapacidadDisponible(problematica) > 0)
                    .filter(r -> r.respetaPuntosDeReplanificacion(rutas, puntosDeReplanificacion))
                    .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                    .collect(Collectors.toList());
            int capDispTotal = rutasDest.stream().mapToInt(r -> r.obtenerCapacidadDisponible(problematica)).sum();
            int totalRealocar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
            if (capDispTotal < totalRealocar) {
                G4DUtility.Logger.logln("[INVALIDA]");
                continue;
            } else G4DUtility.Logger.logln("[VALIDA]");
            // Realocación
            G4DUtility.Logger.logln(": Realocando..");
            Collections.shuffle(rutasDest);
            realocarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
        }
        // Actualización de solución
        solucion.setFitness(problematica);
        G4DUtility.Logger.logf("> 'Realocación'  : %.3f -> %.3f%n", fitnessInicial, solucion.getFitness());
    }

    private void NeighborhoodChange(Problematica problematica, Solucion solucion,
                                    Problematica p_prima_doble, Solucion x_prima_doble,
                                    Problematica p_best, Solucion x_best,
                                    G4DUtility.IntegerWrapper k, G4DUtility.IntegerWrapper t,
                                    G4DUtility.IntegerWrapper t_best) {
        G4DUtility.Logger.log("[NeighborhoodChange]");
        // Validación por mejor vecindario
        if (x_prima_doble.getFitness() < x_best.getFitness()) {
            G4DUtility.Logger.logf("| > NUEVO MEJOR [%.3f]", x_prima_doble.getFitness());
            Object[] ctx_best = replicarContexto(p_prima_doble, x_prima_doble);
            p_best.reasignar((Problematica) ctx_best[0]);
            x_best.reasignar((Solucion) ctx_best[1]);
            problematica.reasignar(p_prima_doble);
            solucion.reasignar(x_prima_doble);
            k.value = kMin;
            t_best.value = t.value;
        } else {
            G4DUtility.Logger.log("| No es mejor.");
            k.value++;
        }
    }

    public void limpiezaFinal(Problematica problematica, Solucion solucion) {
        G4DUtility.Logger.logln("[GC]");
        // Limpieza de elementos sin uso
        List<Vuelo> vuelosEliminar = problematica.vuelos.stream().filter(v -> Objects.equals(v.getCapacidadDisponible(), v.getPlan().getCapacidad())).toList();
        vuelosEliminar.forEach(problematica.vuelos::remove);
        vuelosEliminar.forEach(solucion.getVuelosEnTransito()::remove);
        List<Ruta> rutasEliminar = problematica.rutas.stream().filter(r -> r.getEstado().equals(EstadoRuta.OPERATIVA) && Objects.equals(r.obtenerCapacidadDisponible(problematica), r.obtenerCapacidadMaxima())).toList();
        rutasEliminar.forEach(problematica.rutas::remove);
        rutasEliminar.forEach(solucion.getRutasEnOperacion()::remove);
    }

    private Object[] replicarContexto(Problematica problematica, Solucion solucion) {
        Object[] contexto = new Object[2];
        Map<String, Cliente> poolClientes = new HashMap<>();
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Map<String, Lote> poolLotes = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        Map<String, Plan> poolPlanes = new HashMap<>();
        contexto[0] = problematica.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes);
        contexto[1] = solucion.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes);
        return contexto;
    }
}
