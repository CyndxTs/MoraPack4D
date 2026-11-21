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
    public static final String MOTIVO_SIN_RUTA = "SIN_RUTA";        // No se encontró ruta para atender el pedido
    public static final String MOTIVO_EVENTO = "EVENTO";            // Un evento obligó a reatender y no se pudo
    private static final int MAX_MEJORAS_VND = 100;                 // Corte de seguridad del VND
    private static final Random random = new Random();
    public Double dMin;                                 // Diferencia mínima considerable de fitness
    public Integer iMax;                                // Número máximo de iteraciones de exploración inicial
    public Integer eleMin;                              // Nivel mínimo de búsqueda local
    public Integer eleMax;                              // Nivel máximo de búsqueda local
    public Integer kMin;                                // Nivel mínimo de perturbación
    public Integer kMax;                                // Nivel máximo de perturbación
    public Integer nMax;                                // Número de máximo de intentos por nivel de perturbación
    public Integer tMax;                                // Tiempo máximo esperado de exploración global
    public Solucion solucion;                           // Al inicio sirve de plantilla de factores (f_UA, f_UT, f_DE, f_DO)

    public GVNS() {
        this.solucion = new Solucion();
    }

    // Nueva solución con los factores parametrizados
    private Solucion nuevaSolucion() {
        Solucion s = new Solucion();
        s.copiarFactores(this.solucion);
        return s;
    }

    public void planificar(Problematica problematica) {
        G4DUtility.Logger.Stats.set_global_start();
        G4DUtility.Logger.logln("Generando solución inicial.. [NN]");
        G4DUtility.Logger.Stats.set_local_start();
        Solucion x = solucionInicial(problematica);
        G4DUtility.Logger.Stats.set_local_duration();
        G4DUtility.Logger.logf("[+] SOLUCION INICIAL REALIZADA! (FITNESS: %.3f | NO ATENDIDOS: %d)%n", x.getFitness(), x.getPedidosNoAtendidos().size());
        G4DUtility.Logger.Stats.log_stat_local_sol();
        this.solucion = x;
        if(!x.getPedidosAtendidos().isEmpty()) {
            G4DUtility.Logger.logln("Realizando optimización inicial.. [VND]");
            G4DUtility.Logger.Stats.set_local_start();
            VND(problematica, x);
            G4DUtility.Logger.Stats.set_local_duration();
            G4DUtility.Logger.logf("[+] OPTIMIZACION INICIAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
            G4DUtility.Logger.Stats.log_stat_local_sol();
            G4DUtility.Logger.logln("Realizando optimización final.. [VNS]");
            G4DUtility.Logger.Stats.set_local_start();
            VNS(problematica, x);
            G4DUtility.Logger.Stats.set_local_duration();
            G4DUtility.Logger.logf("[+] OPTIMIZACION FINAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
            G4DUtility.Logger.Stats.log_stat_local_sol();
        }
        sincronizar(problematica, x);
        G4DUtility.Logger.logln("Realizando limpieza final.. [GC]");
        limpiezaFinal(problematica, x);
        G4DUtility.Logger.logf("[~] LIMPIEZA FINAL REALIZADA! (FITNESS: %.3f)%n", x.getFitness());
        this.solucion = x;
        G4DUtility.Logger.Stats.set_global_duration();
        G4DUtility.Logger.Stats.log_stat_global_sol();
    }

    // Exploración inicial: en cada iteración se atienden/reatienden todos los pedidos posibles, guardando los que
    // fallan. Si una iteración no tiene fallidos se toma de inmediato; si ninguna lo logra, se toma la de menos
    // fallidos (en empate, la más reciente).
    private Solucion solucionInicial(Problematica problematica) {
        Solucion mejorSolucion = null;
        Problematica mejorProblematica = null;
        int menorCantidadDeFallidos = Integer.MAX_VALUE;
        for(int i = 0; i < iMax; i++) {
            G4DUtility.Logger.logf(">> Iteración: %d de '%d':%n", i + 1, iMax);
            Problematica pAux = problematica.replicar();
            List<Aeropuerto> origenes = new ArrayList<>(pAux.origenes.values());
            List<Aeropuerto> destinos = pAux.destinos;
            List<Plan> planes = pAux.planes;
            List<Pedido> pedidos = pAux.pedidos;
            Set<Vuelo> vuelosEnTransito = pAux.vuelos;
            Set<Ruta> rutasEnOperacion = pAux.rutas;
            Map<Pedido, String> fallidos = new LinkedHashMap<>();
            G4DUtility.Logger.Stats.reset_ped_count(pedidos.size());
            // Preparación de replanificación (rutas en curso respecto del umbral)
            prepararReplanificacion(pAux, pedidos);
            // Atención & reatención de pedidos
            for (Pedido pedido : pedidos) {
                G4DUtility.Logger.Stats.reset_prod_count(pedido.getCantidadSolicitada());
                G4DUtility.Logger.Stats.log_stat_ped();
                if(!pedido.getFueAtendido()) {
                    if(!atenderPedido(pAux, pedido, origenes, planes, vuelosEnTransito, rutasEnOperacion)) {
                        G4DUtility.Logger.Stats.log_err_stat();
                        fallidos.put(pedido, MOTIVO_SIN_RUTA);
                    }
                } else if(!reatenderPedido(pAux, pedido, planes, vuelosEnTransito, rutasEnOperacion)) {
                    G4DUtility.Logger.Stats.log_err_stat();
                    fallidos.put(pedido, MOTIVO_EVENTO);
                }
                G4DUtility.Logger.delete_lines(3);
                G4DUtility.Logger.Stats.next_ped();
            }
            // Construcción de la solución de la iteración
            Solucion sAux = nuevaSolucion();
            sAux.setPedidosAtendidos(pedidos.stream().filter(Pedido::getFueAtendido).collect(Collectors.toList()));
            sAux.setPedidosNoAtendidos(fallidos);
            sAux.setVuelosEnTransito(vuelosEnTransito);
            sAux.setRutasEnOperacion(rutasEnOperacion);
            sAux.getAeropuertosTransitados().addAll(origenes);
            sAux.getAeropuertosTransitados().addAll(destinos);
            sAux.setFitness(pAux);
            if(fallidos.size() <= menorCantidadDeFallidos) {
                menorCantidadDeFallidos = fallidos.size();
                mejorSolucion = sAux;
                mejorProblematica = pAux;
            }
            if(fallidos.isEmpty()) break;
            G4DUtility.Logger.logf_warn("[WARNING] Iteración #%d con '%d' pedidos no atendidos.%n", i + 1, fallidos.size());
        }
        if(mejorProblematica == null) {
            return nuevaSolucion();
        }
        problematica.reasignar(mejorProblematica);
        return mejorSolucion;
    }

    // Para cada pedido ya atendido, decide qué pasa con cada lote respecto del umbral de replanificación:
    //  - FINALIZADA  : el último vuelo ya salió -> lote ENTREGADO.
    //  - OPERATIVA   : se puede replanificar al azar (probabilidad de replanificación).
    //  - DESHABILITADA: algún vuelo posterior al umbral es problemático (evento) -> replanificación obligatoria.
    private void prepararReplanificacion(Problematica problematica, List<Pedido> pedidos) {
        G4DUtility.Logger.logln(">> Preparando contexto de replanificación..");
        problematica.puntosDeReplanificacion = new ArrayList<>();
        // Toda ruta en curso se vuelve a revisar en cada planificación
        problematica.rutas.stream().filter(r -> r.getEstado() == EstadoRuta.OPERATIVA || r.getEstado() == EstadoRuta.DESHABILITADA).forEach(r -> r.setEstado(EstadoRuta.REVISION_PENDIENTE));
        List<Pedido> pedidosReplanificables = pedidos.stream().filter(Pedido::getFueAtendido).toList();
        for(Pedido p : pedidosReplanificables) {
            Segmentacion sv = p.obtenerSegementacionVigente();
            if(sv == null) continue;
            sv.getLotesPorRuta().forEach((r, l) -> {
                problematica.rutas.add(r);
                problematica.vuelos.addAll(r.getVuelos());
                if(l.getEstado() != EstadoLote.ENTREGADO && (r.getEstado() == EstadoRuta.OPERATIVA || r.getEstado() == EstadoRuta.DESHABILITADA)) r.setEstado(EstadoRuta.REVISION_PENDIENTE);
            });
        }
        for(Pedido pReplanificable : pedidosReplanificables) {
            Segmentacion sVigente = pReplanificable.obtenerSegementacionVigente();
            if(sVigente == null) continue;
            for(Map.Entry<Ruta, Lote> entry : sVigente.getLotesPorRuta().entrySet()) {
                Ruta ruta = entry.getKey();
                Lote lote = entry.getValue();
                if(lote.getEstado() == EstadoLote.ENTREGADO) continue;
                if(ruta.getEstado() == EstadoRuta.REVISION_PENDIENTE) {
                    List<Vuelo> vuelos = ruta.getVuelos();
                    if (ruta.getFechaHoraLlegada().isAfter(problematica.umbralDeReplanificacion) && vuelos.getLast().getFechaHoraSalida().isAfter(problematica.umbralDeReplanificacion)) {
                        // Primer vuelo aún no volado desde donde está el lote (una sede no tiene registro del lote)
                        Vuelo vAux = vuelos.stream().filter(v -> v.getFechaHoraSalida().isAfter(problematica.umbralDeReplanificacion))
                                                    .filter(v -> v.getPlan().getOrigen().getEsSede() || v.getPlan().getOrigen().obtenerRegistroDeLoteDeProductos(lote) != null)
                                                    .findFirst().orElse(vuelos.getLast());
                        int posVAux = vuelos.indexOf(vAux);
                        List<Vuelo> vuelosFijos = new ArrayList<>(vuelos.subList(0, posVAux));
                        List<Vuelo> vuelosPorReplanificar = new ArrayList<>(vuelos.subList(posVAux, vuelos.size()));
                        Aeropuerto aeropuertoDeConexion = vAux.getPlan().getOrigen();
                        LocalDateTime umbralDeConexion;
                        if(!vuelosFijos.isEmpty()) {
                            umbralDeConexion = (LocalDateTime) G4DUtility.Calculator.getMax(vuelosFijos.getLast().getFechaHoraLlegada(), problematica.umbralDeReplanificacion);
                        } else umbralDeConexion = (LocalDateTime) G4DUtility.Calculator.getMax(pReplanificable.getFechaHoraProcesamiento(), problematica.umbralDeReplanificacion);
                        boolean hayProblema = false;
                        for(Vuelo v : vuelosPorReplanificar) {
                            if(v.esProblematico()) hayProblema = true;
                        }
                        ruta.setEstado(hayProblema ? EstadoRuta.DESHABILITADA : EstadoRuta.OPERATIVA);
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
                        if(pdr == null) break;
                        // Todo lote en curso queda asociado a su punto de replanificación (así las búsquedas locales respetan lo ya volado)
                        pdr.getLotes().add(lote);
                        if(random.nextDouble() < problematica.probabilidadDeReplanificacion) {
                            lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                        }
                    }
                    case DESHABILITADA -> {
                        if(pdr == null) break;
                        pdr.getLotes().add(lote);
                        lote.setEstado(EstadoLote.POR_REPLANIFICAR);
                    }
                    case FINALIZADA -> lote.setEstado(EstadoLote.ENTREGADO);
                    default -> { }
                }
            }
        }
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf(">> Contexto de replanificación cargado! ('%d' puntos de replanificación)%n", problematica.puntosDeReplanificacion.size());
        G4DUtility.Logger.delete_upper_line();
    }

    private boolean atenderPedido(Problematica problematica, Pedido pedido, List<Aeropuerto> origenes, List<Plan> planes,
                                  Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        G4DUtility.Logger.logf(" >>  ATENDIENDO PEDIDO #%d | ('%d' productos)%n", G4DUtility.Logger.Stats.posPed + 1, G4DUtility.Logger.Stats.cantProd);
        // Declaración & inicialización de variables
        int fallosDeEnrutamiento = 0;
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
            TipoRuta tipoRuta = (origen.getContinente().equals(destino.getContinente())) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            G4DUtility.Logger.logf(" >   Origen : '%s' | > 'RUTA %s' > | '%s' : Destino%n", origen, tipoRuta.toString(), destino);
            LocalDateTime instanteLimite = instanteDeGeneracion.plusMinutes(tipoRuta.getMaxMinutosParaEntrega(problematica));
            // Búsqueda de ruta reutilizable
            Ruta ruta = buscarRutaVoraz(problematica, instanteDeProcesamiento, instanteLimite, origen, destino, rutasAsignadas);
            if(ruta == null) {
                // Construcción de nueva ruta
                ruta = construirRutaVoraz(problematica, instanteDeProcesamiento, instanteDeProcesamiento, LocalDateTime.MAX, instanteLimite, origen, destino, planes, vuelosActivados);
                if(ruta == null) {
                    G4DUtility.Logger.delete_upper_line();
                    G4DUtility.Logger.logf_warn("[WARNING] No fue posible enrutar productos desde '%s' hacia '%s'.%n", origen, destino);
                    origenesDisponibles.remove(origen);
                    fallosDeEnrutamiento++;
                    continue;
                }
                G4DUtility.Logger.delete_upper_line();
                G4DUtility.Logger.logln(" >   Construyendo nueva ruta voraz.. [CONSTRUIDA]");
            } else {
                G4DUtility.Logger.delete_current_line();
                G4DUtility.Logger.logln(" >   Buscando ruta voraz en operación.. [ENCONTRADA]");
            }
            G4DUtility.Logger.logf(" >   Ruta asignada: '%s' : '%s'", ruta, ruta.getOrigen());
            ruta.getVuelos().forEach(v -> G4DUtility.Logger.logf(" -> '%s'", v.getPlan().getDestino()));
            G4DUtility.Logger.logln();
            // Segmentación de pedido respecto a disponibilidad de ruta
            int rCapDisp = ruta.obtenerCapacidadDisponible(problematica);
            int cantEnrutables = Math.min(rCapDisp, cantPorEnrutar);
            G4DUtility.Logger.logf("-  Enrutando %d de '%d' productos..%n", cantEnrutables, cantPorEnrutar);
            // Producción y registro de segmento de pedido
            Lote lote = origen.generarLoteDeProductos(cantEnrutables);
            ruta.agregarRegistroDeLoteDeProductos(problematica, lote, vuelosActivados, rutasAsignadas);
            if(lotesPorRuta.containsKey(ruta)) {
                // Misma ruta reutilizada dos veces para el mismo pedido: se consolida en un solo lote
                Lote lPrevio = lotesPorRuta.get(ruta);
                ruta.eliminarRegistroDeLoteDeProductos(lPrevio);
                ruta.eliminarRegistroDeLoteDeProductos(lote);
                lote.setTamanio(lote.getTamanio() + lPrevio.getTamanio());
                ruta.agregarRegistroDeLoteDeProductos(problematica, lote, vuelosActivados, rutasAsignadas);
            }
            lotesPorRuta.put(ruta, lote);
            cantPorEnrutar -= cantEnrutables;
            G4DUtility.Logger.Stats.next_prod_lot(cantEnrutables);
            G4DUtility.Logger.logf("-  Quedan '%d' productos por enrutar!%n", cantPorEnrutar);
            // Validación por atención completa de pedido
            if(cantPorEnrutar == 0) {
                Segmentacion segmentacion = new Segmentacion();
                segmentacion.setFechaHoraAplicacion(instanteDeProcesamiento);
                segmentacion.setLotesPorRuta(lotesPorRuta);
                pedido.getSegmentaciones().add(segmentacion);
                pedido.setFechaHoraExpiracion(problematica);
                pedido.setFueAtendido(true);
                vuelosEnTransito.addAll(vuelosActivados);
                rutasEnOperacion.addAll(rutasAsignadas);
                G4DUtility.Logger.delete_lines(fallosDeEnrutamiento);
                return true;
            }
            G4DUtility.Logger.delete_lines(3 + fallosDeEnrutamiento);
            fallosDeEnrutamiento = 0;
        }
        // Reversión: el pedido no se pudo atender completo, se liberan las capacidades que tomó parcialmente
        lotesPorRuta.forEach((r, l) -> r.eliminarRegistroDeLoteDeProductos(l));
        G4DUtility.Logger.delete_lines(2 + fallosDeEnrutamiento);
        return false;
    }

    private Ruta buscarRutaVoraz(Problematica problematica, LocalDateTime instanteInicial, LocalDateTime instanteLimite,
                                 Aeropuerto origen, Aeropuerto destino, Set<Ruta> rutasAsignadas) {
        G4DUtility.Logger.log(" >   Buscando ruta voraz en operación..");
        // Búsqueda de ruta reutilizable
        List<Ruta> rutasPosibles = rutasAsignadas.stream().filter(r -> r.getOrigen().equals(origen) && r.getDestino().equals(destino) && r.getEstado().equals(EstadoRuta.OPERATIVA)).toList();
        for(Ruta ruta : rutasPosibles) {
            if(!ruta.esAlcanzable(problematica, instanteInicial, instanteLimite)) continue;
            return ruta;
        }
        G4DUtility.Logger.logln(" [NO ENCONTRADA]");
        return null;
    }

    private Ruta construirRutaVoraz(Problematica problematica, LocalDateTime instanteInicial, LocalDateTime instanteInicialMinimoDeEgreso, LocalDateTime instanteInicialMaximoDeEgreso,
                                    LocalDateTime instanteLimite, Aeropuerto origen, Aeropuerto destino, List<Plan> planes,
                                    Set<Vuelo> vuelosActivados) {
        G4DUtility.Logger.logln(" >   Construyendo nueva ruta voraz..");
        // Declaración & inicialización de variables
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime instanteActual = instanteInicial;
        LocalDateTime origInstanteMinimoDeEgreso = instanteInicialMinimoDeEgreso;
        LocalDateTime origInstanteMaximoDeEgreso = instanteInicialMaximoDeEgreso;
        Set<Aeropuerto> aeropuertosVisitados = new HashSet<>();
        // Construcción de nueva ruta
        while (!actual.equals(destino)) {
            G4DUtility.Logger.logf("-  Ubicación actual: '%s'%n", actual);
            aeropuertosVisitados.add(actual);
            // Búsqueda de vuelo más próximo
            Vuelo mejorVuelo = obtenerVueloMasProximo(problematica, null, actual, destino, instanteActual, instanteActual, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, planes, aeropuertosVisitados, vuelosActivados);
            if(mejorVuelo == null) {
                G4DUtility.Logger.delete_lines(4);
                G4DUtility.Logger.logln(" >   Construyendo nueva ruta voraz.. [NO ES POSIBLE]");
                return null;
            }
            // Asignación de vuelo
            G4DUtility.Logger.logf("-  Vuelo asignado: '%s' -> '%s'%n", mejorVuelo.getPlan().getOrigen(), mejorVuelo.getPlan().getDestino());
            secuenciaDeVuelos.add(mejorVuelo);
            actual = mejorVuelo.getPlan().getDestino();
            instanteActual = mejorVuelo.getFechaHoraLlegada();
            origInstanteMinimoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*problematica.minHorasDeEstancia));
            origInstanteMaximoDeEgreso = mejorVuelo.getFechaHoraLlegada().plusMinutes((long)(60*problematica.maxHorasDeEstancia));
            G4DUtility.Logger.delete_lines(5);
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
        G4DUtility.Logger.log("-  Buscando mejor plan de vuelo..");
        // Declaración & inicialización de variables
        double menorLejania = Double.MAX_VALUE;
        Plan planMasProximo = null;
        double factorDeLejania = (problematica.factorDeLejania != null) ? problematica.factorDeLejania : 0.0085;
        // Búsqueda de plan de vuelo más próximo (las rutas no pasan por otras sedes)
        List<Plan> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                                                   .filter(p -> !p.getDestino().getEsSede())
                                                   .filter(p -> !aeropuertosVisitados.contains(p.getDestino())).toList();
        LocalDateTime instanteDeReferenciaActual = instanteActual;
        while(instanteDeReferenciaActual.isBefore(instanteLimite) && instanteDeReferenciaActual.isBefore(origInstanteMaximoDeEgreso)) {
            for(Plan plan : planesPosibles) {
                if(!plan.esAlcanzable(problematica, vueloReplanificado, instanteDeReferenciaActual, origInstanteDeIngreso, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, destino, vuelosActivados)) continue;
                double lejania = plan.obtenerLejania(instanteDeReferenciaActual, destino, factorDeLejania);
                if(lejania < menorLejania) {
                    menorLejania = lejania;
                    planMasProximo = plan;
                }
            }
            if(planMasProximo != null) break;
            instanteDeReferenciaActual = instanteDeReferenciaActual.plusDays(1);
        }
        if(planMasProximo == null) {
            G4DUtility.Logger.logln(" [NO ENCONTRADO]");
            return null;
        }
        G4DUtility.Logger.log("- Asignando vuelo..");
        Vuelo vuelo = planMasProximo.obtenerVueloActivo(instanteDeReferenciaActual, vuelosActivados);
        if(vuelo == null) {
            G4DUtility.Logger.logln(" [NUEVO VUELO ACTIVADO]");
            vuelo = new Vuelo();
            vuelo.setPlan(planMasProximo);
            vuelo.instanciarAtributos(instanteDeReferenciaActual);
        } else G4DUtility.Logger.logln(" [VUELO REUTILIZADO]");
        return vuelo;
    }

    // Reatención de los lotes POR_REPLANIFICAR de un pedido desde su punto de replanificación.
    //  - Replanificación aleatoria que falla: se revierte al estado anterior (no es error).
    //  - Replanificación por evento (ruta DESHABILITADA) que falla: el lote se queda en su aeropuerto de conexión,
    //    se dan de baja (soft delete) sus registros posteriores y el pedido cuenta como no atendido (devuelve false).
    private boolean reatenderPedido(Problematica problematica, Pedido pedido, List<Plan> planes,
                                    Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion) {
        // Declaración & inicialización de variables
        Segmentacion sVigente = pedido.obtenerSegementacionVigente();
        if (sVigente == null) return true;
        List<Lote> lotesPorReplanificar = sVigente.getLotesPorRuta().values().stream().filter(l -> l.getEstado().equals(EstadoLote.POR_REPLANIFICAR)).collect(Collectors.toList());
        if (lotesPorReplanificar.isEmpty()) return true; // Validación de inexistencia de lotes por replanificar
        G4DUtility.Logger.logf(" >>  REATENDIENDO PEDIDO #%d | ('%d' lotes por replanificar)%n", G4DUtility.Logger.Stats.posPed + 1, lotesPorReplanificar.size());
        boolean huboFalloPorEvento = false, huboCambios = false;
        Set<Vuelo> vuelosActivados = new HashSet<>(vuelosEnTransito);
        Set<Ruta> rutasAsignadas = new HashSet<>(rutasEnOperacion);
        Segmentacion sNew = new Segmentacion(sVigente);
        sNew.setCodigo(G4DUtility.Generator.getUniqueString("SEG"));
        sNew.setFechaHoraSustitucion(null);
        sVigente.getLotesPorRuta().keySet().stream().filter(r -> sVigente.getLotesPorRuta().get(r).getEstado().equals(EstadoLote.POR_REPLANIFICAR)).toList().forEach(sNew.getLotesPorRuta()::remove);
        while(!lotesPorReplanificar.isEmpty()) {
            HashMap<Lote, Ruta> enrutamientosNew = new HashMap<>(), enrutamientosOld = new HashMap<>();
            HashMap<Lote, Aeropuerto> conexionesNew = new HashMap<>(), conexionesOld = new HashMap<>();
            Lote lReplanificar = lotesPorReplanificar.get(random.nextInt(lotesPorReplanificar.size()));
            int restantePorReplanificar = lReplanificar.getTamanio();
            PuntoDeReplanificacion pdr = problematica.puntosDeReplanificacion.stream().filter(p -> p.getLotes().contains(lReplanificar)).findFirst().orElse(null);
            if(pdr == null) {
                lReplanificar.setEstado(EstadoLote.PLANIFICADO);
                lotesPorReplanificar.remove(lReplanificar);
                Ruta rOriginal = sVigente.getLotesPorRuta().entrySet().stream().filter(e -> e.getValue().equals(lReplanificar)).map(Map.Entry::getKey).findFirst().orElse(null);
                if(rOriginal != null) sNew.getLotesPorRuta().put(rOriginal, lReplanificar);
                continue;
            }
            List<Vuelo> vuelosFijos = pdr.getVuelosFijos();
            Vuelo vueloReplanificado = pdr.getVueloReplanificado();
            Aeropuerto aeropuertoDeConexion = pdr.getAeropuertoDeConexion();
            Ruta rutaInicial = pdr.getRutaInicial();
            boolean esPorEvento = rutaInicial.getEstado().equals(EstadoRuta.DESHABILITADA);
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
            G4DUtility.Logger.logf(" >   Replanificando lote '%s'(%d) desde '%s' hasta '%s' [%s]%n", lReplanificar, lReplanificar.getTamanio(), aeropuertoDeConexion, pedido.getDestino(), esPorEvento ? "EVENTO" : "ALEATORIO");
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
                        // Reversión de lo enrutado parcialmente
                        enrutamientosNew.forEach((l, r) -> {
                            r.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(l, conexionesNew.get(l));
                            sNew.getLotesPorRuta().remove(r);
                        });
                        if(!esPorEvento) {
                            G4DUtility.Logger.logln(" >   Sin alternativa: se mantiene la ruta original.");
                            enrutamientosOld.forEach((l, r) -> {
                                r.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, l, conexionesOld.get(l), vuelosEnTransito, rutasEnOperacion, true);
                                l.setEstado(EstadoLote.PLANIFICADO);
                                sNew.getLotesPorRuta().put(r, l);
                            });
                        } else {
                            G4DUtility.Logger.logf_warn("[WARNING] Lote '%s' sin alternativa tras un evento: queda en '%s'.%n", lReplanificar, aeropuertoDeConexion);
                            // Los lotes consolidados vuelven a su ruta; el lote afectado se queda en la conexión
                            enrutamientosOld.forEach((l, r) -> {
                                if(l.equals(lReplanificar)) return;
                                r.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, l, conexionesOld.get(l), vuelosEnTransito, rutasEnOperacion, true);
                                l.setEstado(EstadoLote.PLANIFICADO);
                                sNew.getLotesPorRuta().put(r, l);
                            });
                            if(rReplanificar != null) {
                                aeropuertoDeConexion.agregarRegistroDeLoteDeProductos(lReplanificar, rReplanificar.getFechaHoraIngreso(), instanteInicialMaximoDeEgreso, false);
                            }
                            lReplanificar.setEstado(EstadoLote.POR_REPLANIFICAR);
                            sNew.getLotesPorRuta().put(rutaInicial, lReplanificar);
                            huboFalloPorEvento = true;
                            huboCambios = true;
                        }
                        lotesPorReplanificar.remove(lReplanificar);
                        break;
                    }
                    G4DUtility.Logger.logf(" >   Nueva ruta construida! '%s'%n", ruta);
                } else G4DUtility.Logger.logf(" >   Se encontró una ruta! '%s'%n", ruta);
                // Segmentación de lote respecto a disponibilidad de ruta
                int rCapDisp = ruta.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aeropuertoDeConexion);
                int cantEnrutables = Math.min(rCapDisp, restantePorReplanificar);
                // Producción y registro de segmento de pedido
                Lote lote = new Lote();
                lote.setTamanio(cantEnrutables);
                if(sNew.getLotesPorRuta().containsKey(ruta)) {
                    Lote lConsolidar = sNew.getLotesPorRuta().get(ruta);
                    Aeropuerto conexionConsolidar = conexionesNew.getOrDefault(lConsolidar, aeropuertoDeConexion);
                    ruta.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lConsolidar, conexionConsolidar, true);
                    enrutamientosOld.put(lConsolidar, ruta);
                    conexionesOld.put(lConsolidar, conexionConsolidar);
                    lConsolidar.setEstado(EstadoLote.REPLANIFICADO);
                    lote.setTamanio(lote.getTamanio() + lConsolidar.getTamanio());
                    sNew.getLotesPorRuta().remove(ruta);
                }
                ruta.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aeropuertoDeConexion, vuelosActivados, rutasAsignadas);
                enrutamientosNew.put(lote, ruta);
                conexionesNew.put(lote, aeropuertoDeConexion);
                sNew.getLotesPorRuta().put(ruta, lote);
                restantePorReplanificar -= cantEnrutables;
                // Validación por reatención completa del lote
                if(restantePorReplanificar == 0) {
                    vuelosEnTransito.addAll(vuelosActivados);
                    rutasEnOperacion.addAll(rutasAsignadas);
                    lReplanificar.setEstado(EstadoLote.REPLANIFICADO);
                    lotesPorReplanificar.remove(lReplanificar);
                    pdr.getLotes().addAll(enrutamientosNew.keySet());
                    pdr.getLotes().addAll(enrutamientosOld.keySet());
                    huboCambios = true;
                }
            }
        }
        // Solo se genera una nueva segmentación si algo cambió
        if(huboCambios) {
            // La nueva segmentación aplica desde el umbral, pero nunca antes que la vigente (puede aplicar después del umbral)
            LocalDateTime instanteDeAplicacion = sVigente.getFechaHoraAplicacion() != null && sVigente.getFechaHoraAplicacion().isAfter(problematica.umbralDeReplanificacion)
                                               ? sVigente.getFechaHoraAplicacion() : problematica.umbralDeReplanificacion;
            sVigente.setFechaHoraSustitucion(instanteDeAplicacion);
            sNew.setFechaHoraAplicacion(instanteDeAplicacion);
            pedido.getSegmentaciones().add(sNew);
            pedido.setFechaHoraExpiracion(problematica);
        }
        G4DUtility.Logger.delete_upper_line();
        return !huboFalloPorEvento;
    }

    private Ruta buscarRutaVoraz(Problematica problematica, Ruta rutaInicial, Vuelo vueloReplanificado, List<Vuelo> vuelosFijos, Aeropuerto aeropuertoDeConexion, LocalDateTime umbralDeConexion,
                                 LocalDateTime instanteInicialMinimoDeEgreso, LocalDateTime instanteInicialMaximoDeEgreso, LocalDateTime instanteDeGeneracion,
                                 Aeropuerto destino, Set<Ruta> rutasAsignadas) {
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
                if(ruta.getVuelos().size() > vuelosFijos.size()) {
                    boolean mismoPrefijo = true;
                    for(int i = 0; i < vuelosFijos.size(); i++) {
                        if(!vuelosFijos.get(i).equals(ruta.getVuelos().get(i))) {
                            mismoPrefijo = false;
                            break;
                        }
                    }
                    esValida = mismoPrefijo && !ruta.getVuelos().get(vuelosFijos.size()).equals(vueloReplanificado);
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
        return null;
    }

    private Ruta construirRutaVoraz(Problematica problematica, Vuelo vueloReplanificado, List<Vuelo> vuelosFijos, Aeropuerto aeropuertoDeConexion, LocalDateTime umbralDeConexion,
                                    LocalDateTime instanteInicialDeIngreso, LocalDateTime instanteInicialMinimoDeEgreso, LocalDateTime instanteInicialMaximoDeEgreso, LocalDateTime instanteDeGeneracion,
                                    Aeropuerto destino, List<Plan> planes, Set<Vuelo> vuelosActivados) {
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
            aeropuertosVisitados.add(actual);
            // Búsqueda de plan de vuelo más próximo
            Vuelo mejorVuelo = obtenerVueloMasProximo(problematica, vueloReplanificado, actual, destino, instanteActual, origInstanteDeIngreso, origInstanteMinimoDeEgreso, origInstanteMaximoDeEgreso, instanteLimite, planes, aeropuertosVisitados, vuelosActivados);
            G4DUtility.Logger.delete_current_line();
            if(mejorVuelo == null) {
                return null;
            }
            // Asignación de vuelo
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

    // ================================================== VND ==================================================

    private void VND(Problematica problematica, Solucion solucion) {
        G4DUtility.Logger.logln("[VND]");
        // Búsqueda local de soluciones por nivel de intensidad
        for(int ele = eleMin; ele <= eleMax; ele++) {
            G4DUtility.Logger.logf(">> Intensidad de busqueda: %d de '%d'%n", ele, eleMax);
            int i = 1, mejoras = 0;
            // Iteración de vecindarios a visitar
            while (i <= 3) {
                Object[] contextoPropuesto = replicarContexto(problematica, solucion);
                Problematica pAux =  (Problematica) contextoPropuesto[0];
                Solucion sAux = (Solucion) contextoPropuesto[1];
                boolean huboMejora = switch (i) {
                    case 1 -> LSCompactar(pAux, sAux, ele);
                    case 2 -> LSFusionar(pAux, sAux, ele);
                    default -> LSRealocar(pAux, sAux, ele);
                };
                sincronizar(pAux, sAux);
                // Validación por mejora obtenida
                if (huboMejora && mejoras < MAX_MEJORAS_VND) {
                    problematica.reasignar(pAux);
                    solucion.reasignar(sAux);
                    mejoras++;
                    i = 1;
                } else {
                    i++;
                }
                G4DUtility.Logger.delete_upper_line();
            }
            G4DUtility.Logger.delete_upper_line();
        }
        G4DUtility.Logger.delete_upper_line();
    }

    // Las búsquedas locales/agitaciones activan vuelos y crean rutas sobre los conjuntos de la solución; la
    // problemática debe conocerlos también (si no, en la siguiente planificación esas rutas no se revisan).
    private static void sincronizar(Problematica problematica, Solucion solucion) {
        problematica.rutas.addAll(solucion.getRutasEnOperacion());
        problematica.vuelos.addAll(solucion.getVuelosEnTransito());
        solucion.getRutasEnOperacion().addAll(problematica.rutas);
        solucion.getVuelosEnTransito().addAll(problematica.vuelos);
    }

    // Conexión efectiva de una ruta para sus lotes (null = desde el origen / sede)
    private static Aeropuerto conexionDe(Ruta ruta, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        PuntoDeReplanificacion pdr = puntosDeReplanificacion.get(ruta);
        Aeropuerto conexion = (pdr != null) ? pdr.getAeropuertoDeConexion() : null;
        return (conexion == null || conexion.getEsSede()) ? null : conexion;
    }

    // Lista de rutas destino válidas para compactar 'rutasOrig' (misma conexión, con capacidad y respetando lo ya volado)
    private List<Ruta> obtenerDestinosDeCompactacion(Problematica problematica, List<Ruta> rutasOrig, Map<Ruta, Lote> segmentacionModificable, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        Aeropuerto conexion = conexionDe(rutasOrig.getFirst(), puntosDeReplanificacion);
        if (rutasOrig.stream().anyMatch(r -> !Objects.equals(conexionDe(r, puntosDeReplanificacion), conexion))) return new ArrayList<>();
        return segmentacionModificable.keySet().stream().filter(r -> !rutasOrig.contains(r))
                .filter(r -> Objects.equals(conexionDe(r, puntosDeReplanificacion), conexion))
                .filter(r -> r.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, conexion) > 0)
                .filter(r -> r.respetaPuntosDeReplanificacion(rutasOrig, puntosDeReplanificacion))
                .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                .collect(Collectors.toList());
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
            if (segmentacionModificable.size() < ele + 1) continue;
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(new ArrayList<>(segmentacionModificable.keySet()), ele);
            int posMejorComb = -1;
            // Iteración de combinaciones
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                int totalCompactar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
                List<Ruta> rutasDest = obtenerDestinosDeCompactacion(problematica, rutasOrig, segmentacionModificable, puntosDeReplanificacion);
                Aeropuerto conexion = conexionDe(rutasOrig.getFirst(), puntosDeReplanificacion);
                int capDispTotal = rutasDest.stream().mapToInt(r -> r.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, conexion)).sum();
                if (rutasDest.isEmpty() || capDispTotal < totalCompactar) continue;
                // Compactación
                boolean completa = compactarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
                if (completa) {
                    solucion.setFitness(problematica);
                    double fitnessObtenido = solucion.getFitness();
                    if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, dMin)) {
                        mejorFitness = solucion.getFitness();
                        posMejorComb = posComb;
                        huboMejora = true;
                    }
                }
                // Reversión de cambios
                revertirCambios(problematica, segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
            }
            // Aplicar mejor combinación
            if (posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                List<Ruta> rutasDest = obtenerDestinosDeCompactacion(problematica, rutasOrig, segmentacionModificable, puntosDeReplanificacion);
                if (!compactarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion)) {
                    revertirCambios(problematica, segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
                }
            }
        }
        solucion.setFitness(problematica);
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Compactación' : %.3f -> %.3f%s%n", fitnessInicial, solucion.getFitness(), huboMejora ? " | FITNESS OPTIMIZADO!" : "");
        return huboMejora;
    }

    // Mueve los lotes de 'rutasOrig' a 'rutasDest' (misma conexión). Devuelve false si no cupo todo (el llamador revierte).
    private boolean compactarSegmentacion(Problematica problematica, List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> segmentacion, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        boolean completa = true;
        // Iteración por orígenes para compactar
        for(Ruta rOrig : rutasOrig) {
            Lote lOrig = segmentacion.get(rOrig);
            int restante = lOrig.getTamanio();
            Aeropuerto aConexion = conexionDe(rOrig, puntosDeReplanificacion);
            PuntoDeReplanificacion pdrOrig = puntosDeReplanificacion.get(rOrig);
            if(pdrOrig != null) pdrOrig.getLotes().remove(lOrig);
            rOrig.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lOrig, aConexion, true);
            segmentacion.remove(rOrig);
            // Compactación en destinos seleccionados
            for(Ruta rDest : rutasDest) {
                if(restante == 0) break;
                int rDestCapDisp = rDest.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aConexion);
                if(rDestCapDisp <= 0) continue;
                int asignable = Math.min(restante, rDestCapDisp);
                Lote lOld = segmentacion.get(rDest);
                int consolidado = lOld.getTamanio() + asignable;
                rDest.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lOld, aConexion, true);
                PuntoDeReplanificacion pdrDest = puntosDeReplanificacion.get(rDest);
                if(pdrDest != null) pdrDest.getLotes().remove(lOld);
                segmentacion.remove(rDest);
                Lote lNew = new Lote();
                lNew.setTamanio(consolidado);
                rDest.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lNew, aConexion, vuelosEnTransito, rutasEnOperacion);
                if(pdrDest != null) pdrDest.getLotes().add(lNew);
                else if(pdrOrig != null) pdrOrig.getLotes().add(lNew);
                segmentacion.put(rDest, lNew);
                restante -= asignable;
            }
            if(restante > 0) completa = false;
        }
        return completa;
    }

    // Deja la segmentación como estaba ('segmentacionAux'). Solo se tocan las entradas que cambiaron: los lotes
    // intactos (p. ej. entregados, o que solo tienen registros desde su conexión) no se re-registran.
    private void revertirCambios(Problematica problematica, Map<Ruta, Lote> segmentacion, Map<Ruta, Lote> segmentacionAux, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        // Eliminación de registros nuevos
        for (Map.Entry<Ruta, Lote> e : new ArrayList<>(segmentacion.entrySet())) {
            Ruta r = e.getKey();
            Lote l = e.getValue();
            if (l.equals(segmentacionAux.get(r))) continue;
            Aeropuerto aConexion = null;
            for(PuntoDeReplanificacion pdr : puntosDeReplanificacion.values()) {
                if(pdr != null && pdr.getLotes().remove(l)) {
                    aConexion = pdr.getAeropuertoDeConexion();
                    break;
                }
            }
            r.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(l, aConexion);
            segmentacion.remove(r);
        }
        // Agregación de registros antiguos
        for (Map.Entry<Ruta, Lote> e : segmentacionAux.entrySet()) {
            Ruta r = e.getKey();
            Lote l = e.getValue();
            if (l.equals(segmentacion.get(r))) continue;
            Aeropuerto aConexion = null;
            PuntoDeReplanificacion pdr = puntosDeReplanificacion.get(r);
            if(pdr != null) {
                if(!pdr.getLotes().contains(l)) pdr.getLotes().add(l);
                aConexion = pdr.getAeropuertoDeConexion();
            }
            r.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, l, aConexion, vuelosEnTransito, rutasEnOperacion, true);
            segmentacion.put(r, l);
        }
    }

    // Une el tramo inicial de 'rIni' hasta una conexión con el tramo final de 'rFin' desde esa conexión.
    // Devuelve la ruta fusionada o null si la escala no es válida (estancia mínima y máxima).
    private Ruta construirFusion(Problematica problematica, Ruta rIni, Ruta rFin, int posConexionIni, int posConexionFin) {
        if(posConexionIni <= 0) return null;
        List<Vuelo> svIni = new ArrayList<>(rIni.getVuelos().subList(0, posConexionIni));
        List<Vuelo> svFin = new ArrayList<>(rFin.getVuelos().subList(posConexionFin, rFin.getVuelos().size()));
        if(svIni.isEmpty() || svFin.isEmpty()) return null;
        LocalDateTime llegadaAConexion = svIni.getLast().getFechaHoraLlegada();
        LocalDateTime salidaDesdeConexion = svFin.getFirst().getFechaHoraSalida();
        LocalDateTime salidaMinima = llegadaAConexion.plusMinutes((long)(60*problematica.minHorasDeEstancia));
        LocalDateTime salidaMaxima = llegadaAConexion.plusMinutes((long)(60*problematica.maxHorasDeEstancia));
        if(salidaDesdeConexion.isBefore(salidaMinima) || salidaDesdeConexion.isAfter(salidaMaxima)) return null;
        List<Vuelo> svNew = new ArrayList<>();
        svNew.addAll(svIni);
        svNew.addAll(svFin);
        Ruta rNew = new Ruta(rIni);
        rNew.setVuelos(svNew);
        rNew.instanciarAtributos();
        rNew.setEstado(EstadoRuta.OPERATIVA);
        return rNew;
    }

    // Rutas candidatas a ser el tramo final de una fusión para el pedido
    private List<Ruta> obtenerRutasFinDeFusion(Pedido pedido, Set<Ruta> rutasEnOperacion) {
        return rutasEnOperacion.stream().filter(r -> r.getDestino().equals(pedido.getDestino()))
                .filter(r -> r.getEstado() == EstadoRuta.OPERATIVA)
                .filter(r -> !r.getFechaHoraSalida().isBefore(pedido.getFechaHoraProcesamiento()) && !r.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion()))
                .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                .collect(Collectors.toList());
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
        for (Pedido pedido : pedidos) {
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            List<Ruta> rutasIni = new ArrayList<>(segmentacionModificable.keySet());
            List<Ruta> rutasFin = obtenerRutasFinDeFusion(pedido, rutasEnOperacion);
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(rutasIni, 1, rutasFin, 1);
            int cantCombinaciones = Math.min(ele, combinaciones.size());
            int posMejorComb = -1, posMejorConexionIni = -1, posMejorConexionFin = -1;
            // Combinaciones
            for(int posComb = 0; posComb < cantCombinaciones; posComb++) {
                List<Ruta> combinacion = combinaciones.get(posComb);
                Ruta rIni = combinacion.get(0);
                Ruta rFin = combinacion.get(1);
                if(rIni.equals(rFin)) continue;
                List<Aeropuerto> saFin = rFin.obtenerSecuenciaDeAeropuertos();
                if(saFin.size() < 3) continue;
                for(int posConexionFin = saFin.size() - 2; posConexionFin > 0; posConexionFin--) {
                    Aeropuerto aConexion = saFin.get(posConexionFin);
                    int posConexionIni = rIni.obtenerSecuenciaDeAeropuertos().indexOf(aConexion);
                    Ruta rNew = construirFusion(problematica, rIni, rFin, posConexionIni, posConexionFin);
                    if(rNew == null || rNew.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion())) continue;
                    if(!rNew.respetaPuntosDeReplanificacion(List.of(rIni), puntosDeReplanificacion)) continue;
                    // La fusión no puede ser antes de donde el lote está físicamente (su conexión)
                    PuntoDeReplanificacion pdrIni = puntosDeReplanificacion.get(rIni);
                    if(pdrIni != null && posConexionIni < pdrIni.getVuelosFijos().size()) continue;
                    Lote lote = segmentacion.get(rIni);
                    rIni.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aConexion, true);
                    if(rNew.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aConexion) < lote.getTamanio()) {
                        rIni.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion, true);
                        continue;
                    }
                    // Fusión
                    segmentacion.remove(rIni);
                    rNew.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion);
                    segmentacion.put(rNew, lote);
                    solucion.setFitness(problematica);
                    double fitnessObtenido = solucion.getFitness();
                    // Validación por mejora de fitness
                    if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, dMin)) {
                        mejorFitness = fitnessObtenido;
                        huboMejora = true;
                        posMejorComb = posComb;
                        posMejorConexionIni = posConexionIni;
                        posMejorConexionFin = posConexionFin;
                    }
                    // Reversión de cambios [Nuevos]
                    rNew.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aConexion);
                    segmentacion.remove(rNew);
                    rutasEnOperacion.remove(rNew);
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
                Ruta rNew = construirFusion(problematica, rIni, rFin, posMejorConexionIni, posMejorConexionFin);
                if(rNew != null) {
                    Lote lote = segmentacion.get(rIni);
                    rIni.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aConexion, true);
                    segmentacion.remove(rIni);
                    rNew.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion);
                    segmentacion.put(rNew, lote);
                }
            }
        }
        // Actualización de solución
        solucion.setFitness(problematica);
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Fusión'       : %.3f -> %.3f%s%n", fitnessInicial, solucion.getFitness(), huboMejora ? " | FITNESS OPTIMIZADO!" : "");
        return huboMejora;
    }

    // Rutas de otros lotes/pedidos donde se pueden realocar los lotes de 'rutasOrig'
    private List<Ruta> obtenerDestinosDeRealocacion(Problematica problematica, Pedido pedido, List<Ruta> rutasOrig, Map<Ruta, Lote> segmentacion, Set<Ruta> rutasEnOperacion, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        Aeropuerto conexion = conexionDe(rutasOrig.getFirst(), puntosDeReplanificacion);
        if (rutasOrig.stream().anyMatch(r -> !Objects.equals(conexionDe(r, puntosDeReplanificacion), conexion))) return new ArrayList<>();
        return rutasEnOperacion.stream().filter(r -> !segmentacion.containsKey(r))
                .filter(r -> r.getEstado() == EstadoRuta.OPERATIVA)
                .filter(r -> r.getDestino().equals(pedido.getDestino()))
                .filter(r -> conexion != null || r.getOrigen().getEsSede())
                .filter(r -> !r.getFechaHoraSalida().isBefore(pedido.getFechaHoraProcesamiento()) || conexion != null)
                .filter(r -> !r.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion()))
                .filter(r -> r.respetaPuntosDeReplanificacion(rutasOrig, puntosDeReplanificacion))
                .filter(r -> r.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, conexion) > 0)
                .sorted(Comparator.comparing(Ruta::getFechaHoraLlegada))
                .collect(Collectors.toList());
    }

    private Boolean LSRealocar(Problematica problematica, Solucion solucion, int ele) {
        G4DUtility.Logger.logln("> Realizando búsqueda local por 'Realocación'..");
        boolean huboMejora = false;
        double fitnessInicial = solucion.getFitness();
        double mejorFitness = fitnessInicial;
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        for (Pedido pedido : pedidos) {
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionAux = new HashMap<>(segmentacion);
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            if (segmentacionModificable.size() < ele) continue;
            List<Ruta> rutas = new ArrayList<>(segmentacionModificable.keySet());
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(rutas, ele);
            int posMejorComb = -1;
            // Iteración de combinaciones
            for (int posComb = 0; posComb < combinaciones.size(); posComb++) {
                List<Ruta> rutasOrig = combinaciones.get(posComb);
                List<Ruta> rutasDest = obtenerDestinosDeRealocacion(problematica, pedido, rutasOrig, segmentacion, rutasEnOperacion, puntosDeReplanificacion);
                Aeropuerto conexion = conexionDe(rutasOrig.getFirst(), puntosDeReplanificacion);
                int capDispTotal = rutasDest.stream().mapToInt(r -> r.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, conexion)).sum();
                int totalRealocar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
                if (rutasDest.isEmpty() || capDispTotal < totalRealocar) continue;
                // Realocación
                boolean completa = realocarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
                if (completa) {
                    solucion.setFitness(problematica);
                    double fitnessObtenido = solucion.getFitness();
                    if (G4DUtility.Calculator.isProximatelyFewer(fitnessObtenido, mejorFitness, dMin)) {
                        mejorFitness = fitnessObtenido;
                        posMejorComb = posComb;
                        huboMejora = true;
                    }
                }
                // Reversión de cambios
                revertirCambios(problematica, segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
            }
            // Aplicar mejor combinación
            if(posMejorComb != -1) {
                List<Ruta> rutasOrig = combinaciones.get(posMejorComb);
                List<Ruta> rutasDest = obtenerDestinosDeRealocacion(problematica, pedido, rutasOrig, segmentacion, rutasEnOperacion, puntosDeReplanificacion);
                if (!realocarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion)) {
                    revertirCambios(problematica, segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
                }
            }
        }
        solucion.setFitness(problematica);
        G4DUtility.Logger.delete_upper_line();
        G4DUtility.Logger.logf("> 'Realocación'  : %.3f -> %.3f%s%n", fitnessInicial, solucion.getFitness(), huboMejora ? " | FITNESS OPTIMIZADO!" : "");
        return huboMejora;
    }

    // Mueve los lotes de 'rutasOrig' a otras rutas. Devuelve false si no cupo todo (el llamador revierte).
    private boolean realocarSegmentacion(Problematica problematica, List<Ruta> rutasOrig, List<Ruta> rutasDest, Map<Ruta, Lote> segmentacion, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasEnOperacion, Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion) {
        boolean completa = true;
        // Iteración por orígenes para realocar
        for(Ruta rOrig : rutasOrig) {
            Lote lOrig = segmentacion.get(rOrig);
            int restante = lOrig.getTamanio();
            Aeropuerto aConexion = conexionDe(rOrig, puntosDeReplanificacion);
            PuntoDeReplanificacion pdrOrig = puntosDeReplanificacion.get(rOrig);
            if(pdrOrig != null) pdrOrig.getLotes().remove(lOrig);
            rOrig.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lOrig, aConexion, true);
            segmentacion.remove(rOrig);
            // Realocación en destinos seleccionados
            for(Ruta rDest : rutasDest) {
                if(restante == 0) break;
                if(segmentacion.containsKey(rDest)) continue;
                int rDestCapDisp = rDest.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aConexion);
                if(rDestCapDisp <= 0) continue;
                int asignable = Math.min(restante, rDestCapDisp);
                Lote lNew = new Lote();
                lNew.setTamanio(asignable);
                rDest.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lNew, aConexion, vuelosEnTransito, rutasEnOperacion);
                if(pdrOrig != null) pdrOrig.getLotes().add(lNew);
                segmentacion.put(rDest, lNew);
                restante -= asignable;
            }
            if(restante > 0) completa = false;
        }
        return completa;
    }

    // ================================================== VNS ==================================================

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
                    Shaking(p_prima, x_prima, k);
                    boolean huboAlteracion = !G4DUtility.Calculator.areProximatelyEqual(solucion.getFitness(), x_prima.getFitness(), dMin);
                    // Validación de solución por umbral de aberración
                    if (huboAlteracion && x_prima.getFitness() < solucion.obtenerUmbralDeAberracion()) {
                        G4DUtility.Logger.logln(" | >> POSIBLE MEJOR SOLUCIÓN");
                        solucionValida = true;
                        break;
                    } else {
                        G4DUtility.Logger.logln(huboAlteracion ? " | >> ABERRACIÓN" : " | >> SIN ALTERACIÓN");
                        G4DUtility.Logger.delete_upper_line();
                        if (intentos >= nMax) break;
                        intentos++;
                    }
                }
                // Validación de existencia de solución
                if (!solucionValida) {
                    k.value++;
                    t.value = (int) Duration.between(start, Instant.now()).getSeconds();
                    continue;
                }
                // Reoptimización de posible mejor solución
                Object[] ctx_prima_doble = replicarContexto(p_prima, x_prima);
                Problematica p_prima_doble = (Problematica)  ctx_prima_doble[0];
                Solucion x_prima_doble = (Solucion) ctx_prima_doble[1];
                VND(p_prima_doble, x_prima_doble);
                G4DUtility.Logger.logf("> 'Reoptimización' : %.3f -> %.3f%n", x_prima.getFitness(), x_prima_doble.getFitness());
                // Actualización de tiempo transcurrido
                t.value = (int) Duration.between(start, Instant.now()).getSeconds();
                // Actualización de vecindario
                NeighborhoodChange(problematica, solucion, p_prima_doble, x_prima_doble, p_best, x_best, k, t, t_best);
                G4DUtility.Logger.delete_lines(2);
            }
            t.value = (int) Duration.between(start, Instant.now()).getSeconds();
        } while (t.value < tMax);
        // La problemática debe quedar en el mismo contexto que la mejor solución
        Object[] ctx_final = replicarContexto(p_best, x_best);
        problematica.reasignar((Problematica) ctx_final[0]);
        solucion.reasignar((Solucion) ctx_final[1]);
    }

    private void Shaking(Problematica problematica, Solucion solucion, G4DUtility.IntegerWrapper k) {
        // Declaración & inicialización de variables
        double fitnessInicial = solucion.getFitness();
        // Perturbación aleatoria de solución por nivel de intesidad
        for (int i = 0; i < k.value; ++i) {
            int neighborhood = random.nextInt(3);
            int ele = eleMin + random.nextInt(eleMax - eleMin + 1);
            switch (neighborhood) {
                case 0 -> TCompactar(problematica, solucion, ele);
                case 1 -> TFusionar(problematica, solucion, ele);
                default -> TRealocar(problematica, solucion, ele);
            }
        }
        sincronizar(problematica, solucion);
        solucion.setFitness(problematica);
        G4DUtility.Logger.logf("> 'Agitación' (k = %d)   : %.3f -> %.3f", k.value, fitnessInicial, solucion.getFitness());
    }

    private void TCompactar(Problematica problematica, Solucion solucion, int ele) {
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Compactación de pedidos
        for (Pedido pedido : pedidos) {
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionAux = new HashMap<>(segmentacion);
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            if (segmentacionModificable.size() < ele + 1) continue;
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(new ArrayList<>(segmentacionModificable.keySet()), ele);
            if (combinaciones.isEmpty()) continue;
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            int totalCompactar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
            List<Ruta> rutasDest = obtenerDestinosDeCompactacion(problematica, rutasOrig, segmentacionModificable, puntosDeReplanificacion);
            Aeropuerto conexion = conexionDe(rutasOrig.getFirst(), puntosDeReplanificacion);
            int capDispTotal = rutasDest.stream().mapToInt(r -> r.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, conexion)).sum();
            if (rutasDest.isEmpty() || capDispTotal < totalCompactar) continue;
            // Compactación
            Collections.shuffle(rutasDest);
            if (!compactarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion)) {
                revertirCambios(problematica, segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
            }
        }
        // Actualización de solución
        solucion.setFitness(problematica);
    }

    private void TFusionar(Problematica problematica, Solucion solucion, int ele) {
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Fusión de rutas
        for (Pedido pedido : pedidos) {
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            List<Ruta> rutasIni = new ArrayList<>(segmentacionModificable.keySet());
            List<Ruta> rutasFin = obtenerRutasFinDeFusion(pedido, rutasEnOperacion);
            Collections.shuffle(rutasFin);
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(rutasIni, 1, rutasFin, 1);
            if(combinaciones.isEmpty()) continue;
            int cantCombinaciones = Math.max(1, Math.min(ele, combinaciones.size()));
            List<Ruta> combinacion = combinaciones.get(random.nextInt(cantCombinaciones));
            Ruta rIni = combinacion.get(0);
            Ruta rFin = combinacion.get(1);
            if(rIni.equals(rFin)) continue;
            List<Aeropuerto> saFin = rFin.obtenerSecuenciaDeAeropuertos();
            if(saFin.size() < 3) continue;
            int posConexionFin = random.nextInt(1, saFin.size() - 1);
            Aeropuerto aConexion = saFin.get(posConexionFin);
            int posConexionIni = rIni.obtenerSecuenciaDeAeropuertos().indexOf(aConexion);
            Ruta rNew = construirFusion(problematica, rIni, rFin, posConexionIni, posConexionFin);
            if(rNew == null || rNew.getFechaHoraLlegada().isAfter(pedido.getFechaHoraExpiracion())) continue;
            if(!rNew.respetaPuntosDeReplanificacion(List.of(rIni), puntosDeReplanificacion)) continue;
            // La fusión no puede ser antes de donde el lote está físicamente (su conexión)
            PuntoDeReplanificacion pdrIni = puntosDeReplanificacion.get(rIni);
            if(pdrIni != null && posConexionIni < pdrIni.getVuelosFijos().size()) continue;
            Lote lote = segmentacion.get(rIni);
            rIni.eliminarRegistroDeLoteDeProductosDesdeAeropuerto(lote, aConexion, true);
            if(rNew.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, aConexion) < lote.getTamanio()) {
                rIni.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion, true);
                continue;
            }
            // Fusión
            segmentacion.remove(rIni);
            rNew.agregarRegistroDeLoteDeProductosDesdeAeropuerto(problematica, lote, aConexion, vuelosEnTransito, rutasEnOperacion);
            segmentacion.put(rNew, lote);
        }
        // Actualización de solución
        solucion.setFitness(problematica);
    }

    private void TRealocar(Problematica problematica, Solucion solucion, int ele) {
        List<Pedido> pedidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosEnTransito = solucion.getVuelosEnTransito();
        Set<Ruta> rutasEnOperacion = solucion.getRutasEnOperacion();
        // Realocación de lotes
        for (Pedido pedido : pedidos) {
            Map<Ruta, Lote> segmentacion = pedido.obtenerSegementacionVigente().getLotesPorRuta();
            Map<Ruta, Lote> segmentacionAux = new HashMap<>(segmentacion);
            Map<Ruta, Lote> segmentacionModificable = pedido.obtenerSegmentacionModificable();
            Map<Ruta, PuntoDeReplanificacion> puntosDeReplanificacion = pedido.obtenerPuntosDeReplanificacion(problematica, segmentacionModificable);
            if (segmentacionModificable.size() < ele) continue;
            List<Ruta> rutas = new ArrayList<>(segmentacionModificable.keySet());
            List<List<Ruta>> combinaciones = G4DUtility.Calculator.getPossibleCombinations(rutas, ele);
            if (combinaciones.isEmpty()) continue;
            List<Ruta> rutasOrig = combinaciones.get(random.nextInt(combinaciones.size()));
            List<Ruta> rutasDest = obtenerDestinosDeRealocacion(problematica, pedido, rutasOrig, segmentacion, rutasEnOperacion, puntosDeReplanificacion);
            Aeropuerto conexion = conexionDe(rutasOrig.getFirst(), puntosDeReplanificacion);
            int capDispTotal = rutasDest.stream().mapToInt(r -> r.obtenerCapacidadDisponibleDesdeAeropuerto(problematica, conexion)).sum();
            int totalRealocar = rutasOrig.stream().mapToInt(r -> segmentacionModificable.get(r).getTamanio()).sum();
            if (rutasDest.isEmpty() || capDispTotal < totalRealocar) continue;
            // Realocación
            Collections.shuffle(rutasDest);
            if (!realocarSegmentacion(problematica, rutasOrig, rutasDest, segmentacion, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion)) {
                revertirCambios(problematica, segmentacion, segmentacionAux, vuelosEnTransito, rutasEnOperacion, puntosDeReplanificacion);
            }
        }
        // Actualización de solución
        solucion.setFitness(problematica);
    }

    private void NeighborhoodChange(Problematica problematica, Solucion solucion,
                                    Problematica p_prima_doble, Solucion x_prima_doble,
                                    Problematica p_best, Solucion x_best,
                                    G4DUtility.IntegerWrapper k, G4DUtility.IntegerWrapper t,
                                    G4DUtility.IntegerWrapper t_best) {
        // Validación por mejor vecindario
        if (x_prima_doble.getFitness() < x_best.getFitness()) {
            G4DUtility.Logger.logf("[NeighborhoodChange] | > NUEVO MEJOR [%.3f]%n", x_prima_doble.getFitness());
            Object[] ctx_best = replicarContexto(p_prima_doble, x_prima_doble);
            p_best.reasignar((Problematica) ctx_best[0]);
            x_best.reasignar((Solucion) ctx_best[1]);
            problematica.reasignar(p_prima_doble);
            solucion.reasignar(x_prima_doble);
            k.value = kMin;
            t_best.value = t.value;
        } else {
            G4DUtility.Logger.logln("[NeighborhoodChange] | No es mejor.");
            k.value++;
        }
    }

    // Quita de la RAM las rutas que ya no usa ningún pedido ni punto de replanificación, y los vuelos sin carga
    // que ninguna ruta usa. (El desalojo de pedidos terminados se hace por ventana en Problematica.desalojar)
    public void limpiezaFinal(Problematica problematica, Solucion solucion) {
        Set<Ruta> rutasEnUso = new HashSet<>();
        problematica.pedidos.forEach(p -> p.getSegmentaciones().forEach(sg -> rutasEnUso.addAll(sg.getLotesPorRuta().keySet())));
        problematica.puntosDeReplanificacion.forEach(pdr -> rutasEnUso.add(pdr.getRutaInicial()));
        int rutasAntes = problematica.rutas.size(), vuelosAntes = problematica.vuelos.size();
        problematica.rutas.removeIf(r -> !rutasEnUso.contains(r));
        solucion.getRutasEnOperacion().removeIf(r -> !rutasEnUso.contains(r));
        Set<Vuelo> vuelosEnUso = new HashSet<>();
        problematica.rutas.forEach(r -> vuelosEnUso.addAll(r.getVuelos()));
        problematica.vuelos.removeIf(v -> !vuelosEnUso.contains(v) && Objects.equals(v.getCapacidadDisponible(), v.getCapacidad()));
        solucion.getVuelosEnTransito().removeIf(v -> !vuelosEnUso.contains(v) && Objects.equals(v.getCapacidadDisponible(), v.getCapacidad()));
        G4DUtility.Logger.logf("[GC] '%d' rutas y '%d' vuelos sin uso liberados%n", rutasAntes - problematica.rutas.size(), vuelosAntes - problematica.vuelos.size());
        solucion.setFitness(problematica);
    }

    private Object[] replicarContexto(Problematica problematica, Solucion solucion) {
        Object[] contexto = new Object[2];
        Map<String, Cliente> poolClientes = new HashMap<>();
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Map<String, Lote> poolLotes = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        Map<String, Plan> poolPlanes = new HashMap<>();
        Map<String, Pedido> poolPedidos = new HashMap<>();
        contexto[0] = problematica.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes, poolPedidos);
        contexto[1] = solucion.replicar(poolClientes, poolAeropuertos, poolRutas, poolLotes, poolVuelos, poolPlanes, poolPedidos);
        return contexto;
    }
}
