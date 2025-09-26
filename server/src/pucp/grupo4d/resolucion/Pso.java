/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Algoritmo.java 
[**/

package pucp.grupo4d.resolucion;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import pucp.grupo4d.modelo.PlanDeVuelo;
import pucp.grupo4d.modelo.Ruta;
import pucp.grupo4d.modelo.Aeropuerto;
import pucp.grupo4d.modelo.Pedido;
import pucp.grupo4d.modelo.Producto;
import pucp.grupo4d.modelo.Problematica;
import pucp.grupo4d.modelo.Solucion;
import pucp.grupo4d.modelo.TipoRuta;
import pucp.grupo4d.modelo.Vuelo;
import pucp.grupo4d.util.G4D_Util;

public class Pso {
    private static final Integer L_MIN = 1;
    private static final Integer L_MAX = 2;
    private static final Integer K_MIN = 3;
    private static final Integer K_MAX = 5;
    private static final Integer T_MAX = 10;
    private static final Integer MAX_INTENTOS = 10;
    private static final Integer NO_ENCONTRADO = -1;
    private static final Double PEOR_FITNESS = 9999.99;
    private static final Random random = new Random();
    private Solucion solucionGVNS;
    private Solucion solucionPSO;
    private double PSO_W_TIME = 1.0; // peso del tiempo transcurrido
    private double PSO_W_DIST = 0.003; // peso de la distancia (km)
    private double PSO_W_CAP = 0.010; // peso (beneficio) de capacidad disponible (resta en proximidad)

    public Pso() {
        this.solucionGVNS = null;
        this.solucionPSO = null;
    }

    //

    // Solución Inicial: Nearest Neighbor
    private void solucionInicial(Problematica problematica, Solucion solucion) {
        //
        System.out.println("Generando solución inicial..");
        //
        List<Pedido> pedidos = problematica.getPedidos();
        List<PlanDeVuelo> planes = problematica.getPlanes();
        List<Aeropuerto> sedes = problematica.getSedes();
        Set<Vuelo> vuelosActivos = new HashSet<>();
        int numPed = 1, cantPed = pedidos.size(), numProd, cantProd;
        // Actualizar rutas de productos de pedidos
        for (Pedido pedido : pedidos) {
            System.out.printf("[#] ATENDIENDO PEDIDO #%d de %d%n", numPed, cantPed);
            LocalDateTime fechaHoraCreacion = pedido.getFechaHoraCreacion();
            List<Producto> productos = pedido.getProductos();
            cantProd = productos.size();
            numProd = 1;
            for (Producto producto : productos) {
                System.err.printf(">> ATENDIENDO PRODUCTO #%d de %d%n", numProd, cantProd);
                Ruta ruta = obtenerMejorRuta(fechaHoraCreacion, producto.getDestino(), sedes, planes, vuelosActivos);
                if (ruta == null) {
                    producto.setOrigen(null);
                    System.out.printf("[ERROR] Ningún origen pudo atender al producto #%s del pedido %s.%n", numProd,
                            numPed);
                    System.exit(1);
                }
                producto.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                producto.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                producto.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                producto.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                LocalDateTime fechaHoraLimiteUTC = fechaHoraCreacion
                        .plusMinutes((long) (double) (60 * ruta.getTipo().getMaxHorasParaEntrega()));
                producto.setFechaHoraLimiteLocal(
                        G4D_Util.toLocal(fechaHoraLimiteUTC, producto.getDestino().getHusoHorario()));
                producto.setFechaHoraLimiteUTC(fechaHoraLimiteUTC);
                producto.setRuta(ruta);
                producto.registrarRuta(fechaHoraCreacion);
                System.out.println("PRODUCTO ATENDIDO.");
                numProd++;
            }
            System.out.println("PEDIDO ATENDIDO.");
            numPed++;
        }
        // Guardar en solución
        solucion.setPedidos(pedidos);
        solucion.setFitness();
    }

    //
    private Ruta obtenerMejorRuta(LocalDateTime fechaHoraCreacion, Aeropuerto destino, List<Aeropuerto> origenes,
            List<PlanDeVuelo> planes, Set<Vuelo> vuelosActivos) {
        System.out.println("Enrutando..");
        //
        int cantOrig = origenes.size(), numOrig = 1;
        Ruta ruta, mejorRuta = null;
        Set<Aeropuerto> aeropuertosVisitados;
        //
        for (Aeropuerto origen : origenes) {
            System.out.printf("[ORIGEN #%d de %d]%n", numOrig, cantOrig);
            aeropuertosVisitados = new HashSet<>();
            ruta = construirRutaVoraz(fechaHoraCreacion, origen, destino, planes, vuelosActivos, aeropuertosVisitados);
            if (ruta == null) {
                System.out.println("No es posible generar una ruta a partir de este origen.");
                continue;
            }
            if (mejorRuta == null || ruta.getDuracion() < mejorRuta.getDuracion()) {
                System.out.printf("Nueva mejor ruta asignada: %s%n", ruta.getId());
                mejorRuta = ruta;
            }
            numOrig++;
        }
        return mejorRuta;
    }

    //
    private Ruta construirRutaVoraz(LocalDateTime fechaHoraCreacion, Aeropuerto origen, Aeropuerto destino,
            List<PlanDeVuelo> planes, Set<Vuelo> vuelosActivos, Set<Aeropuerto> aeropuertosVisitados) {
        //
        System.err.println("Construyendo ruta..");
        //
        int numVuelo = 1;
        TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL
                : TipoRuta.INTERCONTINENTAL;
        Double duracionActual = 0.0, maxDuracionParaEntrega = tipoRuta.getMaxHorasParaEntrega();
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = fechaHoraCreacion;
        //
        if (actual.equals(destino))
            return null;
        //
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            if (numVuelo != 1)
                System.out.println("DESTINO NO ALCANZADO. Redirigiendo..");
            System.out.printf("> VUELO #%d%n", numVuelo);
            System.out.print("Buscando mejor plan de vuelo..");
            PlanDeVuelo mejorPlan = obtenerPlanMasProximo(actual, fechaHoraActual, planes, duracionActual,
                    maxDuracionParaEntrega, aeropuertosVisitados);
            if (mejorPlan == null) {
                System.out.println(" [NO ENCONTRADO]");
                for (Vuelo vuelo : secuenciaDeVuelos) {
                    vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + 1);
                    if (vuelo.getCapacidadDisponible() == vuelo.getPlan().getCapacidadMaxima())
                        vuelosActivos.remove(vuelo);
                }
                return null;
            }
            System.out.println(" [ENCONTRADO]");
            System.out.print("Bucando vuelo activo..");
            Vuelo vuelo = obtenerVueloActivo(mejorPlan, fechaHoraActual, vuelosActivos);
            if (vuelo == null) {
                System.out.println(" [NO_ENCONTRADO]");
                System.out.println("Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidadMaxima());
                vuelo.instanciarHorarios(fechaHoraActual);
                vuelo.setDuracion();
                vuelosActivos.add(vuelo);
            } else
                System.out.println(" [ENCONTRADO]");
            System.out.printf("VUELO ASIGNADO: %s%n", vuelo.getId());
            vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - 1);
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegadaUTC();
            duracionActual += vuelo.getDuracion();
            actual = vuelo.getPlan().getDestino();
            numVuelo++;
        }
        System.out.println("DESTINO ALCANZADO. Guardando ruta..");
        //
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.setTipo(tipoRuta);
        ruta.setDuracion();
        return ruta;
    }

    //
    private PlanDeVuelo obtenerPlanMasProximo(Aeropuerto origen, LocalDateTime fechaHoraActual,
            List<PlanDeVuelo> planes,
            Double duracionActual, Double maxDuracionParaEntrega, Set<Aeropuerto> visitados) {
        Double mejorProximidad = Double.MAX_VALUE;
        PlanDeVuelo planMaxProximo = null;
        List<PlanDeVuelo> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                .filter(p -> !visitados.contains(p.getDestino()))
                .toList();
        for (PlanDeVuelo plan : planesPosibles) {
            LocalDateTime fechaHoraSalidaUTC = G4D_Util.toUTC(
                    G4D_Util.toDateTime(plan.getHoraSalida(), fechaHoraActual),
                    plan.getOrigen().getHusoHorario());
            LocalDateTime fechaHoraLlegadaUTC = G4D_Util.toUTC(
                    G4D_Util.toDateTime(plan.getHoraLlegada(), fechaHoraActual),
                    plan.getDestino().getHusoHorario());
            if (fechaHoraLlegadaUTC.isBefore(fechaHoraSalidaUTC))
                fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
            if (fechaHoraSalidaUTC.isBefore(fechaHoraActual)) {
                fechaHoraSalidaUTC = fechaHoraSalidaUTC.plusDays(1);
                fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
            }
            Double transcurrido = G4D_Util.calculateElapsedHours(fechaHoraActual, fechaHoraLlegadaUTC);
            if (duracionActual + transcurrido > maxDuracionParaEntrega)
                continue;
            Integer capacidadDisponible = plan.getDestino().obtenerCapacidadDisponible(
                    G4D_Util.toUTC(G4D_Util.toDateTime(plan.getHoraLlegada(), fechaHoraActual),
                            plan.getDestino().getHusoHorario()));
            if (capacidadDisponible < 1)
                continue;
            Double distancia = origen.obtenerDistanciaHasta(plan.getDestino());
            double proximidad = PSO_W_TIME * transcurrido
                    + PSO_W_DIST * distancia
                    - PSO_W_CAP * capacidadDisponible;
            if (proximidad < mejorProximidad) {
                mejorProximidad = proximidad;
                planMaxProximo = plan;
            }
        }
        return planMaxProximo;
    }

    private Vuelo obtenerVueloActivo(PlanDeVuelo plan, LocalDateTime fechaHoraActual, Set<Vuelo> vuelosActivos) {
        List<Vuelo> vuelosPosibles = vuelosActivos.stream().filter(v -> v.getPlan().getOrigen() == plan.getOrigen())
                .filter(v -> v.getPlan().getDestino() == plan.getDestino())
                .filter(v -> v.getPlan().getHoraSalida().equals(plan.getHoraSalida()))
                .filter(v -> v.getPlan().getHoraLlegada().equals(plan.getHoraLlegada()))
                .toList();
        LocalDateTime fechaHoraSalida = G4D_Util.toUTC(
                G4D_Util.toDateTime(plan.getHoraSalida(), fechaHoraActual),
                plan.getOrigen().getHusoHorario());
        if (fechaHoraSalida.isBefore(fechaHoraActual)) {
            fechaHoraSalida = fechaHoraSalida.plusDays(1);
        }
        for (Vuelo vuelo : vuelosPosibles) {
            if (fechaHoraSalida.equals(vuelo.getFechaHoraSalidaUTC())) {
                return vuelo;
            }
        }
        return null;
    }

    public void imprimirSolucionGVNS(String rutaArchivo) {
        imprimirSolucion(solucionGVNS, rutaArchivo);
    }

    //
    public void imprimirSolucionPSO(String rutaArchivo) {
        imprimirSolucion(solucionPSO, rutaArchivo);
    }

    //
    private void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        // Declaracion de variables
        int dimLinea = 135, posPedido = 0, posProducto, numProductos;
        Double tiempoAhorrado = 0.0;
        FileWriter archivo;
        PrintWriter archivoWriter;
        // Carga de datos
        try {
            System.out.println("Cargando archivo 'Solucion' a la ruta '" + rutaArchivo + "'..");
            // Inicializaion del archivo y scanner
            archivo = new FileWriter(rutaArchivo);
            archivoWriter = new PrintWriter(archivo);
            // Impresion de reporte
            G4D_Util.printFullLine(archivoWriter, '=', dimLinea);
            G4D_Util.printCentered(archivoWriter, dimLinea, "FITNESS DE LA SOLUCIÓN");
            G4D_Util.printCentered(archivoWriter, dimLinea, String.format("%.2f", solucion.getFitness()));
            archivoWriter.println();
            G4D_Util.printCentered(archivoWriter, dimLinea,
                    String.format("%s %32s %24s", "DURACION PROM.", "DISTANCIA RECORRIDA PROM.", "CAP. DISPO. PROM."));
            G4D_Util.printCentered(archivoWriter, dimLinea, String.format(
                    "%15s %28s %11s%s | %s",
                    String.format("%.2f hrs.", solucion.getDuracionPromedio()),
                    String.format("%.2f Km.", solucion.getDistanciaRecorridaPromedio()),
                    " ",
                    String.format("V: %.2f", solucion.getCapacidadDiponiblePromedioPorVuelo()),
                    String.format("A: %.2f", solucion.getCapacidadDisponiblePromedioPorAeropuerto())));
            G4D_Util.printFullLine(archivoWriter, '=', dimLinea);
            for (Pedido pedido : solucion.getPedidos()) {
                G4D_Util.printCentered(archivoWriter, dimLinea, String.format("PEDIDO #%d", posPedido + 1));
                G4D_Util.printFullLine(archivoWriter, '-', dimLinea, 4);
                archivoWriter.printf("%5s %-30s %15s %25s %25s%n", "", "CLIENTE", "DESTINO", "NUM. PRODUCTOS MPE",
                        "INSTANTE DE REGISTRO");
                archivoWriter.printf("%5s %-30s %13s %20s %31s%n", "", pedido.getCliente().getNombre(),
                        pedido.getDestino().getCodigo(), String.format("%03d", pedido.getCantidad()),
                        G4D_Util.toDisplayString(pedido.getFechaHoraCreacion()));
                archivoWriter.println();
                G4D_Util.printCentered(archivoWriter, dimLinea, "> SECUENCIA DE VUELOS PLANIFICADOS <");
                G4D_Util.printFullLine(archivoWriter, '*', dimLinea, 8);
                posProducto = 0;
                tiempoAhorrado = 0.0;
                numProductos = pedido.getProductos().size();
                for (Producto producto : pedido.getProductos()) {
                    tiempoAhorrado += G4D_Util.calculateElapsedHours(producto.getFechaHoraLlegadaUTC(),
                            producto.getFechaHoraLimiteUTC());
                    archivoWriter.printf(
                            "%10s PRODUCTO #%s  |  ORIGEN: %s  |  TIPO DE ENVIO:  %s  |  ENTREGA PLANIFICADA: %s%n",
                            ">>", String.format("%03d", posProducto + 1), producto.getOrigen().getCodigo(),
                            producto.getRuta().getTipo(), G4D_Util.toDisplayString(producto.getFechaHoraLlegadaUTC()));
                    archivoWriter.println();
                    archivoWriter.printf("%46s %29s %22s%n", "ORIGEN", "DESTINO", "TRANSCURRIDO");
                    if (producto.getRuta() != null) {
                        for (Vuelo vuelo : producto.getRuta().getVuelos()) {
                            archivoWriter.printf(
                                    "%36s  %s  -->  %s  %s  ==  %.2f hrs.%n",
                                    vuelo.getPlan().getOrigen().getCodigo(),
                                    G4D_Util.toDisplayString(vuelo.getFechaHoraSalidaUTC()),
                                    vuelo.getPlan().getDestino().getCodigo(),
                                    G4D_Util.toDisplayString(vuelo.getFechaHoraLlegadaUTC()),
                                    vuelo.getDuracion());
                        }
                    }
                    G4D_Util.printFullLine(archivoWriter, '.', dimLinea, 8);
                    archivoWriter.printf("%27s%n", "Resumen de la ruta:");
                    archivoWriter.printf("%31s %.2f hrs.%n", ">> Duración de la ruta:",
                            producto.getRuta().getDuracion());
                    if (posProducto != numProductos - 1)
                        G4D_Util.printFullLine(archivoWriter, '*', dimLinea, 8);
                    posProducto++;
                }
                G4D_Util.printFullLine(archivoWriter, '-', dimLinea, 4);
                archivoWriter.printf("%23s%n", "Resumen del pedido:");
                archivoWriter.printf("%25s %.2f hrs.%n", ">> Tiempo optimizado:", tiempoAhorrado);
                G4D_Util.printFullLine(archivoWriter, '=', dimLinea);
                posPedido++;
            }
            System.out.println("Archivo 'Solucion' generado en la ruta '" + rutaArchivo + "'.");
            archivoWriter.flush();
            archivoWriter.close();
            archivo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Solucion construirSolucionPSO(Problematica problematica, double wTime, double wDist, double wCap) {

        this.PSO_W_TIME = wTime;
        this.PSO_W_DIST = wDist;
        this.PSO_W_CAP = wCap;

        // Clonar pedidos para no modificar los de 'problematica'
        Map<String, Aeropuerto> poolA = new HashMap<>();
        Map<String, Vuelo> poolV = new HashMap<>();
        List<Pedido> pedidosClon = new ArrayList<>();
        for (Pedido p : problematica.getPedidos())
            pedidosClon.add(p.replicar(poolA, poolV));

        List<PlanDeVuelo> planes = problematica.getPlanes();
        List<Aeropuerto> sedes = problematica.getSedes();

        // Armar solución (igual a solucionInicial, pero sin registrar en aeropuertos)
        Set<Vuelo> vuelosActivos = new HashSet<>();
        for (Pedido pedido : pedidosClon) {
            LocalDateTime t0 = pedido.getFechaHoraCreacion();
            List<Producto> productos = pedido.getProductos();
            int cant = productos.size(), i = 1;
            for (Producto prod : productos) {
                Ruta ruta = obtenerMejorRuta(t0, prod.getDestino(), sedes, planes, vuelosActivos);
                if (ruta == null) {
                    // producto no atendido: deja origen/destino en null y ruta null
                    prod.setOrigen(null);
                    prod.setDestino(null);
                    prod.setRuta(null);
                    continue;
                }
                prod.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                prod.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                prod.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                prod.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                LocalDateTime limiteUTC = t0.plusMinutes((long) (60 * ruta.getTipo().getMaxHorasParaEntrega()));
                prod.setFechaHoraLimiteLocal(G4D_Util.toLocal(limiteUTC, prod.getDestino().getHusoHorario()));
                prod.setFechaHoraLimiteUTC(limiteUTC);
                prod.setRuta(ruta);

                i++;
            }
        }
        Solucion sol = new Solucion();
        sol.setPedidos(pedidosClon);
        sol.setFitness(); // usa tu función objetivo tal cual
        return sol;
    }

    public void PSO(Problematica problematica) {
        final int SWARM = 12; // número de partículas
        final int ITER = 20; // iteraciones
        final double INERTIA = 0.7; // inercia
        final double C1 = 1.4; // componente cognitiva
        final double C2 = 1.4; // componente social

        // Rangos de búsqueda (acotados y simples)
        final double[] T_RANGE = { 0.8, 1.6 }; // peso tiempo
        final double[] D_RANGE = { 0.001, 0.010 }; // peso distancia
        final double[] C_RANGE = { 0.000, 0.050 }; // peso capacidad

        class Particle {
            double t, d, c; // posición (pesos)
            double vt, vd, vc;// velocidad
            double bestT, bestD, bestC;
            double bestFitness = Double.MAX_VALUE;
            double fitness = Double.MAX_VALUE;
            Solucion bestSol = null;
        }

        List<Particle> swarm = new ArrayList<>();
        Random rnd = new Random();

        // Inicialización aleatoria
        for (int i = 0; i < SWARM; i++) {
            Particle p = new Particle();
            p.t = T_RANGE[0] + rnd.nextDouble() * (T_RANGE[1] - T_RANGE[0]);
            p.d = D_RANGE[0] + rnd.nextDouble() * (D_RANGE[1] - D_RANGE[0]);
            p.c = C_RANGE[0] + rnd.nextDouble() * (C_RANGE[1] - C_RANGE[0]);
            p.vt = (rnd.nextDouble() - 0.5) * 0.1;// se establece 0.5 para centrarlo en el rango establecido y luego lo
                                                  // escalamos
            p.vd = (rnd.nextDouble() - 0.5) * 0.001;
            p.vc = (rnd.nextDouble() - 0.5) * 0.005;

            // Evalúa
            Solucion s = construirSolucionPSO(problematica, p.t, p.d, p.c);
            p.fitness = s.getFitness();
            p.bestFitness = p.fitness;
            p.bestT = p.t;
            p.bestD = p.d;
            p.bestC = p.c;
            p.bestSol = s;
            swarm.add(p);
        }

        // Mejor global -> tomamos la particula con menor fitness ya que buscamos
        // minimizar
        Particle gBest = swarm.stream().min((a, b) -> Double.compare(a.fitness, b.fitness)).get();

        // Iteraciones
        for (int it = 0; it < ITER; it++) {
            for (Particle p : swarm) {
                double r1 = rnd.nextDouble(), r2 = rnd.nextDouble();

                // Actualizar velocidades
                p.vt = INERTIA * p.vt + C1 * r1 * (p.bestT - p.t) + C2 * r2 * (gBest.bestT - p.t);
                p.vd = INERTIA * p.vd + C1 * r1 * (p.bestD - p.d) + C2 * r2 * (gBest.bestD - p.d);
                p.vc = INERTIA * p.vc + C1 * r1 * (p.bestC - p.c) + C2 * r2 * (gBest.bestC - p.c);

                // Actualizar posiciones
                p.t += p.vt;
                p.d += p.vd;
                p.c += p.vc;

                // limitar dentro de los rangos
                p.t = Math.max(T_RANGE[0], Math.min(T_RANGE[1], p.t));
                p.d = Math.max(D_RANGE[0], Math.min(D_RANGE[1], p.d));
                p.c = Math.max(C_RANGE[0], Math.min(C_RANGE[1], p.c));

                // Evaluar con los nuevos pesos (siempre mismas funciones de ruta)
                Solucion s = construirSolucionPSO(problematica, p.t, p.d, p.c);
                p.fitness = s.getFitness();

                // Actualizar mejor personal
                if (p.fitness < p.bestFitness) {
                    p.bestFitness = p.fitness;
                    p.bestT = p.t;
                    p.bestD = p.d;
                    p.bestC = p.c;
                    p.bestSol = s;
                }
            }
            // Actualizar mejor global
            Particle cand = swarm.stream().min((a, b) -> Double.compare(a.bestFitness, b.bestFitness)).get();
            if (cand.bestFitness < gBest.bestFitness)
                gBest = cand;
        }

        // Reconstruye solución final con los mejores pesos y esta vez REGISTRA la ruta
        this.PSO_W_TIME = gBest.bestT;
        this.PSO_W_DIST = gBest.bestD;
        this.PSO_W_CAP = gBest.bestC;

        // Ahora sí: construir solución “oficial” (registrando en aeropuertos)
        Solucion finalSol = new Solucion();
        {
            List<PlanDeVuelo> planes = problematica.getPlanes();
            List<Aeropuerto> sedes = problematica.getSedes();
            Set<Vuelo> vuelosActivos = new HashSet<>();

            // Clona pedidos para construir y registrar sin tocar originales
            Map<String, Aeropuerto> poolA = new HashMap<>();
            Map<String, Vuelo> poolV = new HashMap<>();
            List<Pedido> pedidosClon = new ArrayList<>();
            for (Pedido p : problematica.getPedidos())
                pedidosClon.add(p.replicar(poolA, poolV));

            for (Pedido pedido : pedidosClon) {
                LocalDateTime t0 = pedido.getFechaHoraCreacion();
                for (Producto prod : pedido.getProductos()) {
                    Ruta ruta = obtenerMejorRuta(t0, prod.getDestino(), sedes, planes, vuelosActivos);
                    if (ruta == null)
                        continue;
                    prod.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                    prod.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                    prod.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                    prod.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                    LocalDateTime limiteUTC = t0.plusMinutes((long) (60 * ruta.getTipo().getMaxHorasParaEntrega()));
                    prod.setFechaHoraLimiteLocal(G4D_Util.toLocal(limiteUTC, prod.getDestino().getHusoHorario()));
                    prod.setFechaHoraLimiteUTC(limiteUTC);
                    prod.setRuta(ruta);
                    prod.registrarRuta(t0); // ahora sí registramos
                }
            }
            finalSol.setPedidos(pedidosClon);
            finalSol.setFitness();
        }
        this.solucionPSO = finalSol;
    }

}
