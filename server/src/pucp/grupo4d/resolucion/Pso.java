
/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Algoritmo.java 
[**/
/*
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
    public void imprimirSolucionPSO(String rutaArchivo) {
        imprimirSolucion(solucionPSO, rutaArchivo);
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
*/