/**]
 >> Project:    MoraPack
 >> Version:    Vespera
 >> Author:     Grupo 4D
 >> File:       PSO.java
[**/

package pucp.dp1.grupo4d.algorithm.version.vespera;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import pucp.dp1.grupo4d.algorithm.version.vespera.enums.TipoRuta;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Aeropuerto;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Pedido;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.PlanDeVuelo;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Producto;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Ruta;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Vuelo;
import pucp.dp1.grupo4d.util.G4D;

public class PSO extends Algoritmo {
    // private static final Random random = new Random();
    // private double PSO_W_TIME = 1.0;
    // private double PSO_W_DIST = 0.003;
    // private double PSO_W_CAP = 0.010;

    private Solucion solucion;

    public PSO() {
        this.solucion = null;
    }

    public void imprimirSolucion(String rutaArchivo) {
        super.imprimirSolucion(solucion, rutaArchivo);
    }

    @Override
    public void planificar(Problematica problematica) {
        final int SWARM = 8; // simple
        final int ITER = 5; // base

        final double[] T_RANGE = { 0.8, 1.6 };
        final double[] D_RANGE = { 0.001, 0.01 };
        final double[] C_RANGE = { 0.000, 0.05 };

        class Particle {
            double t, d, c; // posición (pesos)
            double vt, vd, vc; // velocidad
            double bestT, bestD, bestC;
            double bestFitness = Double.MAX_VALUE;
            double fitness = Double.MAX_VALUE;
            // Solucion bestSol = null;
        }

        List<Particle> swarm = new ArrayList<>();
        Random rnd = new Random();

        // 1) Inicialización aleatoria + construcción con planificadores
        for (int i = 0; i < SWARM; i++) {
            Particle p = new Particle();
            p.t = T_RANGE[0] + rnd.nextDouble() * (T_RANGE[1] - T_RANGE[0]);
            p.d = D_RANGE[0] + rnd.nextDouble() * (D_RANGE[1] - D_RANGE[0]);
            p.c = C_RANGE[0] + rnd.nextDouble() * (C_RANGE[1] - C_RANGE[0]);
            p.vt = (rnd.nextDouble() - 0.5) * 0.10;
            p.vd = (rnd.nextDouble() - 0.5) * 0.001;
            p.vc = (rnd.nextDouble() - 0.5) * 0.005;

            Solucion s = construirSolucionPSO(problematica, p.t, p.d, p.c);
            p.fitness = s.getFitness();
            p.bestFitness = p.fitness;
            p.bestT = p.t;
            p.bestD = p.d;
            p.bestC = p.c;
            // p.bestSol = s;

            swarm.add(p);
        }

        Particle gBest = swarm.stream().min((a, b) -> Double.compare(a.fitness, b.fitness)).get();

        // 2) Iteraciones (base)
        for (int it = 0; it < ITER; it++) {
            for (Particle p : swarm) {
                double r1 = rnd.nextDouble(), r2 = rnd.nextDouble();
                // Velocidades
                p.vt = 0.6 * p.vt + 1.2 * r1 * (p.bestT - p.t) + 1.2 * r2 * (gBest.bestT - p.t);
                p.vd = 0.6 * p.vd + 1.2 * r1 * (p.bestD - p.d) + 1.2 * r2 * (gBest.bestD - p.d);
                p.vc = 0.6 * p.vc + 1.2 * r1 * (p.bestC - p.c) + 1.2 * r2 * (gBest.bestC - p.c);
                // Posiciones con límites
                p.t = Math.max(T_RANGE[0], Math.min(T_RANGE[1], p.t + p.vt));
                p.d = Math.max(D_RANGE[0], Math.min(D_RANGE[1], p.d + p.vd));
                p.c = Math.max(C_RANGE[0], Math.min(C_RANGE[1], p.c + p.vc));

                // Evalúa con los planificadores
                Solucion s = construirSolucionPSO(problematica, p.t, p.d, p.c);
                p.fitness = s.getFitness();

                if (p.fitness < p.bestFitness) {
                    p.bestFitness = p.fitness;
                    p.bestT = p.t;
                    p.bestD = p.d;
                    p.bestC = p.c;
                    // p.bestSol = s;
                }
            }
            Particle cand = swarm.stream().min((a, b) -> Double.compare(a.bestFitness, b.bestFitness)).get();
            if (cand.bestFitness < gBest.bestFitness)
                gBest = cand;
        }

        // this.PSO_W_TIME = gBest.bestT;
        // this.PSO_W_DIST = gBest.bestD;
        // this.PSO_W_CAP = gBest.bestC;

        Solucion finalSol = new Solucion();
        {
            List<PlanDeVuelo> planes = problematica.planes;
            List<Aeropuerto> sedes = new ArrayList<>(problematica.origenes.values());
            Set<Vuelo> vuelosActivos = new HashSet<>();
            Set<Ruta> rutasAsignadas = new HashSet<>();

            // Clonar pedidos y pools (A,V,R) para registrar sin tocar problematica
            Map<String, Aeropuerto> poolA = new HashMap<>();
            Map<String, Vuelo> poolV = new HashMap<>();
            Map<String, Ruta> poolR = new HashMap<>();
            List<Pedido> pedidosClon = new ArrayList<>();
            for (Pedido p : problematica.pedidos)
                pedidosClon.add(p.replicar(poolA, poolV, poolR));

            for (Pedido pedido : pedidosClon) {
                LocalDateTime t0 = pedido.getFechaHoraCreacionUTC();
                int cant = pedido.getCantidad();
                for (int numProd = 1; numProd <= cant; numProd++) {
                    G4D.Logger.logf(">> ATENDIENDO PRODUCTO #%d DE '%d'%n", numProd, cant);

                    Producto prod = new Producto();
                    Set<Aeropuerto> aeropuertosTransitados = new HashSet<>();

                    Ruta ruta = obtenerMejorRuta(
                            t0, pedido.getDestino(), sedes, planes,
                            aeropuertosTransitados, vuelosActivos, rutasAsignadas);
                    if (ruta == null) {
                        G4D.Logger.logf_err("[ERROR] Ningún origen pudo enrutar el producto #%d%n", numProd);
                        continue;
                    }

                    prod.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                    prod.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                    prod.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                    prod.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                    LocalDateTime limiteUTC = t0.plusMinutes((long) (60 * ruta.getTipo().getMaxHorasParaEntrega()));
                    prod.setFechaHoraLimiteLocal(G4D.toLocal(limiteUTC, prod.getDestino().getHusoHorario()));
                    prod.setFechaHoraLimiteUTC(limiteUTC);
                    prod.setRuta(ruta);

                    // Registrar (consume capacidades y registra en aeropuertos)
                    prod.registrarRuta(t0);

                    // Agregar producto al pedido
                    pedido.getProductos().add(prod);

                    G4D.Logger.logf(">> PRODUCTO ENRUTADO. (Ruta asignada: %s)%n", ruta.getId());
                }
            }
            finalSol.setPedidosAtendidos(pedidosClon);
            finalSol.setVuelosActivos(vuelosActivos);
            finalSol.setRutasAsignadas(rutasAsignadas);
            finalSol.setFitness();
        }
        this.solucion = finalSol;

        // Impresión final
        super.imprimirSolucion(finalSol, "SolucionPSO.txt");
        G4D.Logger.logf("[+] MEJOR SOLUCION OBTENIDA (PSO): %.2f%n", finalSol.getFitness());
    }

    private Solucion construirSolucionPSO(Problematica problematica, double wTime, double wDist, double wCap) {
        // this.PSO_W_TIME = wTime;
        // this.PSO_W_DIST = wDist;
        // this.PSO_W_CAP = wCap;

        // Pools/clones para construir sin tocar el estado original
        Map<String, Aeropuerto> poolA = new HashMap<>();
        Map<String, Vuelo> poolV = new HashMap<>();
        Map<String, Ruta> poolR = new HashMap<>();
        List<Pedido> pedidosClon = new ArrayList<>();
        for (Pedido p : problematica.pedidos)
            pedidosClon.add(p.replicar(poolA, poolV, poolR));

        List<PlanDeVuelo> planes = problematica.planes;
        List<Aeropuerto> sedes = new ArrayList<>(problematica.origenes.values());

        Set<Vuelo> vuelosActivos = new HashSet<>();
        Set<Ruta> rutasAsignadas = new HashSet<>();

        for (Pedido pedido : pedidosClon) {
            LocalDateTime t0 = pedido.getFechaHoraCreacionUTC();
            int cant = pedido.getCantidad();
            for (int numProd = 1; numProd <= cant; numProd++) {
                G4D.Logger.logf(">> ATENDIENDO PRODUCTO #%d DE '%d'%n", numProd, cant);

                Producto prod = new Producto();
                Set<Aeropuerto> aeropuertosTransitados = new HashSet<>();

                Ruta ruta = obtenerMejorRuta(
                        t0, pedido.getDestino(), sedes, planes,
                        aeropuertosTransitados, vuelosActivos, rutasAsignadas);
                if (ruta == null) {
                    G4D.Logger.logf_err("[ERROR] Ningún origen pudo enrutar el producto #%d%n", numProd);
                    continue;
                }

                prod.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                prod.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                prod.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                prod.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                LocalDateTime limiteUTC = t0.plusMinutes((long) (60 * ruta.getTipo().getMaxHorasParaEntrega()));
                prod.setFechaHoraLimiteLocal(G4D.toLocal(limiteUTC, prod.getDestino().getHusoHorario()));
                prod.setFechaHoraLimiteUTC(limiteUTC);
                prod.setRuta(ruta);

                // Registrar también en evaluación base para respetar capacidades
                prod.registrarRuta(t0);

                // Agregar para que se imprima correctamente si fuese necesario
                pedido.getProductos().add(prod);

                G4D.Logger.logf(">> PRODUCTO ENRUTADO. (Ruta asignada: %s)%n", ruta.getId());
            }
        }

        Solucion sol = new Solucion();
        sol.setPedidosAtendidos(pedidosClon);
        sol.setVuelosActivos(vuelosActivos);
        sol.setRutasAsignadas(rutasAsignadas);
        sol.setFitness();
        return sol;
    }


    private Ruta obtenerMejorRuta(LocalDateTime fechaHoraCreacion, Aeropuerto destino, List<Aeropuerto> origenes,
            List<PlanDeVuelo> planes, Set<Aeropuerto> aeropuertosTransitados,
            Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasAsignadas) {
        G4D.Logger.logln("Enrutando..");

        Ruta ruta, mejorRuta = null;
        Set<Aeropuerto> aeropuertosVisitadosEnRuta, aeropuertosVisitadosEnMejorRuta = new HashSet<>();
        Set<Vuelo> vuelosActivosDeRuta, vuelosActivosDeMejorRuta = new HashSet<>();
        int cantOrig = origenes.size(), numOrig = 1;

        for (Aeropuerto origen : origenes) {
            G4D.Logger.logf("[ORIGEN #%d de %d]%n", numOrig++, cantOrig);

            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0)
                    ? TipoRuta.INTRACONTINENTAL
                    : TipoRuta.INTERCONTINENTAL;

            LocalDateTime fechaHoraLimite = fechaHoraCreacion
                    .plusMinutes(60 * tipoRuta.getMaxHorasParaEntrega().longValue());

            aeropuertosVisitadosEnRuta = new HashSet<>();
            vuelosActivosDeRuta = new HashSet<>(vuelosEnTransito);

            ruta = buscarRutaVoraz(rutasAsignadas, fechaHoraCreacion, fechaHoraLimite, origen, destino,
                    vuelosActivosDeRuta);
            if (ruta == null) {
                G4D.Logger.log(" [NO ENCONTRADA]");
                G4D.Logger.delete_current_line();

                ruta = construirRutaVoraz(fechaHoraCreacion, fechaHoraLimite, origen, destino, planes,
                        aeropuertosVisitadosEnRuta, vuelosActivosDeRuta);
                if (ruta == null) {
                    G4D.Logger.logln("No es posible generar una ruta a partir de este origen.");
                    continue;
                } else {
                    ruta.setTipo(tipoRuta);
                }
            } else {
                G4D.Logger.log(" [ENCONTRADA]");
                G4D.Logger.delete_current_line();
                ruta.setTipo(tipoRuta);
            }

            if (mejorRuta == null || ruta.getDuracion() < mejorRuta.getDuracion()) {
                aeropuertosVisitadosEnMejorRuta = aeropuertosVisitadosEnRuta;
                vuelosActivosDeMejorRuta = vuelosActivosDeRuta;
                if (mejorRuta != null)
                    rutasAsignadas.remove(mejorRuta);
                mejorRuta = ruta;
                rutasAsignadas.add(mejorRuta);
                G4D.Logger.logf("Nueva mejor ruta asignada! (%s)%n", ruta.getId());
            } else {
                G4D.Logger.logln("La nueva ruta no supera a la mejor.");
            }
        }
        aeropuertosTransitados.addAll(aeropuertosVisitadosEnMejorRuta);
        vuelosEnTransito.addAll(vuelosActivosDeMejorRuta);
        return mejorRuta;
    }

    private Ruta buscarRutaVoraz(Set<Ruta> rutasAsignadas, LocalDateTime fechaHoraCreacion,
            LocalDateTime fechaHoraLimite, Aeropuerto origen,
            Aeropuerto destino, Set<Vuelo> vuelosActivos) {
        G4D.Logger.log("Buscando ruta preasignada..");

        if (origen.equals(destino)) {
            G4D.Logger.log(" [NO ENCONTRADA]");
            G4D.Logger.delete_current_line();
            return null;
        }
        for (Ruta ruta : rutasAsignadas) {
            List<Vuelo> secuenciaDeVuelos = ruta.getVuelos();
            if (secuenciaDeVuelos == null || secuenciaDeVuelos.isEmpty())
                continue;

            if (!secuenciaDeVuelos.getFirst().getPlan().getOrigen().equals(origen))
                continue;
            if (!secuenciaDeVuelos.getLast().getPlan().getDestino().equals(destino))
                continue;

            LocalDateTime fechaHoraSalida = secuenciaDeVuelos.getFirst().getFechaHoraSalidaUTC();
            LocalDateTime fechaHoraLlegada = secuenciaDeVuelos.getLast().getFechaHoraLlegadaUTC();
            if (fechaHoraSalida.isBefore(fechaHoraCreacion))
                continue;
            if (fechaHoraLlegada.isAfter(fechaHoraLimite))
                continue;

            boolean rutaValida = true;
            for (Vuelo vuelo : secuenciaDeVuelos) {
                if (vuelo.getCapacidadDisponible() < 1) {
                    rutaValida = false;
                    break;
                }
            }
            if (rutaValida) {
                G4D.Logger.log(" [ENCONTRADA]");
                return ruta;
            }
        }
        G4D.Logger.log(" [NO ENCONTRADA]");
        return null;
    }

    private Ruta construirRutaVoraz(LocalDateTime fechaHoraCreacion, LocalDateTime fechaHoraLimite,
            Aeropuerto origen, Aeropuerto destino, List<PlanDeVuelo> planes,
            Set<Aeropuerto> aeropuertosVisitados, Set<Vuelo> vuelosActivos) {
        G4D.Logger.logln("Construyendo ruta..");

        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = fechaHoraCreacion;

        if (actual.equals(destino))
            return null;

        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            G4D.Logger.logf("> Vuelos asignados: %d%n", secuenciaDeVuelos.size());
            G4D.Logger.log("Buscando mejor plan de vuelo..");

            PlanDeVuelo mejorPlan = obtenerPlanMasProximo(actual, destino, fechaHoraActual, fechaHoraLimite, planes,
                    aeropuertosVisitados);
            if (mejorPlan == null) {
                G4D.Logger.logln(" [NO ENCONTRADO]");
                // revertir activaciones no utilizadas
                for (Vuelo vuelo : secuenciaDeVuelos) {
                    if (vuelo.getCapacidadDisponible() == vuelo.getPlan().getCapacidad()) {
                        vuelosActivos.remove(vuelo);
                    }
                }
                return null;
            }
            G4D.Logger.logln(" [ENCONTRADO]");

            Vuelo vuelo = obtenerVueloActivo(mejorPlan, fechaHoraActual, fechaHoraLimite, vuelosActivos);
            if (vuelo == null) {
                G4D.Logger.logln("Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
                vuelo.instanciarHorarios(fechaHoraActual);
                vuelo.setDuracion();
                vuelo.setDistancia();
                vuelosActivos.add(vuelo);
            } else {
                G4D.Logger.logln(" [VUELO ACTIVO ENCONTRADO]");
            }

            G4D.Logger.logf("> VUELO ASIGNADO: %s%n", vuelo.getId());
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegadaUTC();
            actual = vuelo.getPlan().getDestino();
        }

        G4D.Logger.log("DESTINO ALCANZADO. Guardando ruta..");
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.setDuracion();
        ruta.setDistancia();
        G4D.Logger.delete_current_line();
        return ruta;
    }

    private PlanDeVuelo obtenerPlanMasProximo(Aeropuerto origen, Aeropuerto destino,
            LocalDateTime fechaHoraActual, LocalDateTime fechaHoraLimite,
            List<PlanDeVuelo> planes, Set<Aeropuerto> visitados) {
        Double mejorProximidad = Double.MAX_VALUE;
        PlanDeVuelo planMaxProximo = null;

        List<PlanDeVuelo> planesPosibles = planes.stream()
                .filter(p -> p.getOrigen().equals(origen))
                .filter(p -> !visitados.contains(p.getDestino()))
                .toList();

        for (PlanDeVuelo plan : planesPosibles) {
            if (!plan.esAlcanzable(fechaHoraActual, fechaHoraLimite))
                continue;
            Double proximidad = plan.calcularProximidad(fechaHoraActual, destino);
            if (proximidad < mejorProximidad) {
                mejorProximidad = proximidad;
                planMaxProximo = plan;
            }
        }
        return planMaxProximo;
    }

    private Vuelo obtenerVueloActivo(PlanDeVuelo plan, LocalDateTime fechaHoraActual,
            LocalDateTime fechaHoraLimite, Set<Vuelo> vuelosActivos) {
        List<Vuelo> vuelosPosibles = vuelosActivos.stream()
                .filter(v -> v.getPlan().getOrigen().equals(plan.getOrigen()))
                .filter(v -> v.getPlan().getDestino().equals(plan.getDestino()))
                .filter(v -> v.getPlan().getHoraSalida().equals(plan.getHoraSalida()))
                .filter(v -> v.getPlan().getHoraLlegada().equals(plan.getHoraLlegada()))
                .filter(v -> v.getFechaHoraLlegadaUTC().isBefore(fechaHoraLimite))
                .filter(v -> v.getCapacidadDisponible() >= 1)
                .toList();

        LocalDateTime fechaHoraSalida = G4D.toUTC(
                G4D.toDateTime(plan.getHoraSalida(), fechaHoraActual),
                plan.getOrigen().getHusoHorario());
        if (fechaHoraSalida.isBefore(fechaHoraActual))
            fechaHoraSalida = fechaHoraSalida.plusDays(1);

        for (Vuelo vuelo : vuelosPosibles) {
            if (fechaHoraSalida.equals(vuelo.getFechaHoraSalidaUTC()))
                return vuelo;
        }
        return null;
    }
}
