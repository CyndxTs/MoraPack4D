/**]
 >> Project:    MoraPack
 >> Version:    Vespera
 >> Author:     Grupo 4D
 >> File:       Algoritmo.java 
[**/

package pucp.dp1.grupo4d.algorithm.version.vespera;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pucp.dp1.grupo4d.algorithm.version.vespera.enums.TipoRuta;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Aeropuerto;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Pedido;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.PlanDeVuelo;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Producto;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Ruta;
import pucp.dp1.grupo4d.algorithm.version.vespera.model.Vuelo;
import pucp.dp1.grupo4d.util.G4D;
import java.util.Random;

public class GVNS extends Algoritmo {
    public static boolean fastSearch = true;
    private static final Integer L_MIN = 1;
    private static final Integer L_MAX = 3;
    private static final Integer K_MIN = 3;
    private static final Integer K_MAX = 5;
    private static final Integer T_MAX = 10;
    private static final Integer MAX_INTENTOS = 10;
    private static final Double PEOR_FITNESS = 9999.99;
    private static final Random random = new Random();
    private Solucion solucion;

    public GVNS() {
        this.solucion = null;
    }
    //
    public void imprimirSolucion(String rutaArchivo) { super.imprimirSolucion(solucion, rutaArchivo); }
    // General Variable Neighborhood Search
    @Override
    public void planificar(Problematica problematica) {
        // Declaracion de variables
        Solucion solucionAux = new Solucion();
        Solucion x_best = new Solucion();
        G4D.IntegerWrapper t = new G4D.IntegerWrapper(), t_best = new G4D.IntegerWrapper();
        Instant inicioLocal,finLocal;
        Long duracionLocal;
        // Solución inicial (Nearest Neighbor)
        inicioLocal = Instant.now();
        G4D.Logger.log("Generando solución inicial.. ");
        if(fastSearch) solucionInicial_FastGreedy(problematica, solucionAux);
        else solucionInicial_RobustGreedy(problematica, solucionAux);
        finLocal = Instant.now();
        duracionLocal = Duration.between(inicioLocal,finLocal).toMillis();
        G4D.Logger.logf("[+] SOLUCION INICIAL GENERADA! (FITNESS: %.2f)%n",solucionAux.getFitness());
        G4D.Logger.logf("[#] TIEMPO DE CONVERGENCIA: %.2f seg.%n",duracionLocal/1000.0);
        imprimirSolucion(solucionAux, "SolucionInicial.txt");
        // Optimización inicial (Variable Neighborhood Descent)
        inicioLocal = Instant.now();
        G4D.Logger.log("Realizando optimización inicial.. ");
        VND(problematica, solucionAux);
        finLocal = Instant.now();
        duracionLocal = Duration.between(inicioLocal, finLocal).toMillis();
        G4D.Logger.logf("[+] OPTIMIZACION INICIAL REALIZADA! (FITNESS: %.2f)%n",solucionAux.getFitness());
        G4D.Logger.logf("[#] TIEMPO DE CONVERGENCIA: %.2f seg.%n",duracionLocal/1000.0);
        imprimirSolucion(solucionAux, "SolucionVND.txt");
        // Optimización final (Variable Neighborhood Search)
        inicioLocal = Instant.now();
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
                    Shaking(x_prima, k, problematica);
                    G4D.Logger.log("Validando..");
                    if (x_prima.getFitness() < PEOR_FITNESS) {
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
                
                G4D.Logger.logf("%s%n", " [POSIBLE SOLUCION]");
                Solucion x_prima_doble = x_prima.replicar();
                G4D.Logger.log("Reoptimizando.. ");
                VND(problematica, x_prima_doble);
                // Actualizar tiempo
                Instant end = Instant.now();
                t.value = (int) Duration.between(start, end).getSeconds();
                // Neighborhood Change
                NeighborhoodChange(problematica, solucionAux, x_prima_doble, x_best, k, t, t_best);
            }
            // Actualizar tiempo para condición del bucle externo
            Instant end = Instant.now();
            Duration duracion = Duration.between(start, end);
            t.value = (int) duracion.getSeconds();
        } while (t.value < T_MAX);
        this.solucion = x_best;
        finLocal = Instant.now();
        duracionLocal = Duration.between(inicioLocal,finLocal).toMillis();
        G4D.Logger.logf("[+] OPTIMIZACION FINAL REALIZADA! (FITNESS: %.2f)%n",x_best.getFitness());
        G4D.Logger.logf("[#] TIEMPO DE CONVERGENCIA: %.2f seg.%n",duracionLocal/1000.0);
        imprimirSolucion(x_best, "SolucionGVNS.txt");
    }
    // Solución Inicial: Nearest Neighbor [FAST GREEDY]
    private void solucionInicial_FastGreedy(Problematica problematica, Solucion solucion) {
        G4D.Logger.logln("[FAST GREEDY]");
        // Declaracion de variables
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Pedido> pedidos = problematica.pedidos;
        Set<Aeropuerto> aeropuertosOcupados = new HashSet<>();
        Set<Vuelo> vuelosActivos = new HashSet<>();
        Set<Ruta> rutasAsignadas = new HashSet<>();
        int numPed = 1,cantPed = pedidos.size(),posProd = 0,totalProd = 0;
        Double tProm_ped = 0.0,tProm_prod = 0.0;
        Long duracion = Long.valueOf(0);
        for(Pedido p : pedidos) totalProd += p.getCantidad();
        // Actualizar rutas de productos de pedidos
        for (Pedido pedido : pedidos) {
            LocalDateTime fechaHoraCreacionUTC = pedido.getFechaHoraCreacionUTC();
            for (int numProd = 1, cantProd = pedido.getCantidad();numProd <= cantProd;numProd++) {
                Instant start = Instant.now();
                G4D.Logger.logf("[#] PRODUCTOS ATENDIDOS: %d de %d%n",posProd,totalProd);
                G4D.Logger.logf("[#] TIEMPO PROMEDIO DE ATENCION POR PRODUCTO: %.2f seg.%n",tProm_prod);
                G4D.Logger.logf("[#] TIEMPO PROMEDIO DE ATENCION POR PEDIDO: %.2f seg.%n",tProm_ped);
                G4D.Logger.logf(">> ATENDIENDO PRODUCTO #%d DE '%d' DEL PEDIDO #%d DE '%d'%n",numProd,cantProd,numPed,cantPed);
                Producto producto = new Producto();
                Ruta ruta = obtenerMejorRuta(fechaHoraCreacionUTC,pedido.getDestino(),origenes,planes,aeropuertosOcupados,vuelosActivos,rutasAsignadas);
                if (ruta == null){
                    producto.setOrigen(null);
                    G4D.Logger.logf_err("[ERROR] Ningún origen pudo enrutar al producto #%s del pedido #%s.%n",numProd,numPed);
                    G4D.Logger.logf_err("[ERROR] Solo se atendieron %d de %d pedidos. (%d de %d productos)%n",numPed,cantPed,posProd,totalProd);
                    System.exit(1);
                }
                producto.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                producto.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                producto.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                producto.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                LocalDateTime fechaHoraLimiteUTC = fechaHoraCreacionUTC.plusMinutes((long)(double)(60*ruta.getTipo().getMaxHorasParaEntrega()));
                producto.setFechaHoraLimiteLocal(G4D.toLocal(fechaHoraLimiteUTC,producto.getDestino().getHusoHorario()));
                producto.setFechaHoraLimiteUTC(fechaHoraLimiteUTC);
                producto.setRuta(ruta);
                producto.registrarRuta(fechaHoraCreacionUTC);
                pedido.getProductos().add(producto);
                G4D.Logger.delete_lines(8);
                G4D.Logger.logf(">> PRODUCTO ENRUTADO. (Ruta asignada: %s)%n",ruta.getId());
                Instant end = Instant.now();
                duracion += Duration.between(start, end).toMillis();
                posProd++;
                tProm_prod = duracion/(1000.0*posProd);
                G4D.Logger.delete_lines(6);
            }
            tProm_ped = duracion/(1000.0*numPed);
            numPed++;
        }
        // Guardar en solución
        solucion.setPedidosAtendidos(pedidos);
        solucion.setVuelosActivos(vuelosActivos);
        solucion.setRutasAsignadas(rutasAsignadas);
        solucion.setFitness();
    }
    //
    private Ruta obtenerMejorRuta(LocalDateTime fechaHoraCreacion, Aeropuerto destino, List<Aeropuerto> origenes,
                                  List<PlanDeVuelo> planes, Set<Aeropuerto> aeropuertosTransitados, Set<Vuelo> vuelosEnTransito, Set<Ruta> rutasAsignadas) {
        G4D.Logger.logln("Enrutando..");
        // Declaracion de Variables
        int cantOrig = origenes.size(),numOrig = 1;
        Ruta ruta,mejorRuta = null;
        Set<Aeropuerto> aeropuertosVisitadosEnRuta,aeropuertosVisitadosEnMejorRuta = new HashSet<>();
        Set<Vuelo> vuelosActivosDeRuta,vuelosActivosDeMejorRuta = new HashSet<>();
        //
        for(Aeropuerto origen : origenes) {
            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            LocalDateTime fechaHoraLimite = fechaHoraCreacion.plusMinutes(60*tipoRuta.getMaxHorasParaEntrega().longValue());
            G4D.Logger.logf("[ORIGEN #%d de %d]%n",numOrig++,cantOrig);
            aeropuertosVisitadosEnRuta = new HashSet<>();
            vuelosActivosDeRuta = new HashSet<>(vuelosEnTransito);
            ruta = buscarRutaVoraz(rutasAsignadas,fechaHoraCreacion,fechaHoraLimite,origen,destino,vuelosActivosDeRuta);
            if(ruta == null) {
                G4D.Logger.log(" [NO ENCONTRADA]");
                G4D.Logger.delete_current_line();
                ruta = construirRutaVoraz(fechaHoraCreacion,fechaHoraLimite,origen,destino,planes,aeropuertosVisitadosEnRuta,vuelosActivosDeRuta);
                if(ruta == null) {
                    G4D.Logger.logln("No es posible generar una ruta a partir de este origen.");
                    continue;
                }else ruta.setTipo(tipoRuta);
            } else {
                G4D.Logger.log(" [ENCONTRADA]");
                G4D.Logger.delete_current_line();
            }
            if(mejorRuta == null || ruta.getDuracion() < mejorRuta.getDuracion()) {
                aeropuertosVisitadosEnMejorRuta = aeropuertosVisitadosEnRuta;
                vuelosActivosDeMejorRuta = vuelosActivosDeRuta;
                if(mejorRuta != null) rutasAsignadas.remove(mejorRuta);
                mejorRuta = ruta;
                rutasAsignadas.add(mejorRuta);
                G4D.Logger.logf("Nueva mejor ruta asignada! (%s)%n",ruta.getId());
            } else G4D.Logger.logln("La nueva ruta no supera a la mejor.");
        }
        aeropuertosTransitados.addAll(aeropuertosVisitadosEnMejorRuta);
        vuelosEnTransito.addAll(vuelosActivosDeMejorRuta);
        return mejorRuta;
    }
    //
    private Ruta buscarRutaVoraz(Set<Ruta> rutasAsignadas,LocalDateTime fechaHoraCreacion,LocalDateTime fechaHoraLimite,Aeropuerto origen,Aeropuerto destino,Set<Vuelo> vuelosActivos) {
        //
        G4D.Logger.log("Buscando ruta preasignada..");
        //
        boolean rutaValida;
        //
        if(origen.equals(destino)) {
            G4D.Logger.log(" [NO ENCONTRADA]");
            G4D.Logger.delete_current_line();
            return null;
        }
        for(Ruta ruta : rutasAsignadas) {
            List<Vuelo> secuenciaDeVuelos = ruta.getVuelos();
            if(secuenciaDeVuelos.getFirst().getPlan().getOrigen() != origen) continue;
            LocalDateTime fechaHoraSalida = secuenciaDeVuelos.getFirst().getFechaHoraSalidaUTC();
            if(fechaHoraSalida.isBefore(fechaHoraCreacion)) continue;
            LocalDateTime fechaHoraLlegada = secuenciaDeVuelos.getLast().getFechaHoraLlegadaUTC();
            if(fechaHoraLlegada.isAfter(fechaHoraLimite)) continue;
            rutaValida = true;
            for(Vuelo vuelo : secuenciaDeVuelos) {
                if(vuelo.getCapacidadDisponible() < 1) {
                    rutaValida = false;
                    break;
                }
            }
            if(rutaValida) {
                G4D.Logger.log(" [ENCONTRADA]");
                return ruta;
            }
        }
        G4D.Logger.log(" [NO ENCONTRADA]");
        return null;
    }
    //
    private Ruta construirRutaVoraz(LocalDateTime fechaHoraCreacion,LocalDateTime fechaHoraLimite,Aeropuerto origen,Aeropuerto destino,
                                    List<PlanDeVuelo>planes,Set<Aeropuerto> aeropuertosVisitados,Set<Vuelo> vuelosActivos) {
        //
        G4D.Logger.logln("Construyendo ruta..");
        //
        boolean seActivoVuelo;
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = fechaHoraCreacion;
        //
        if(actual.equals(destino)) return null;
        //
        while (!actual.equals(destino)) {
            seActivoVuelo = false;
            aeropuertosVisitados.add(actual);
            G4D.Logger.logf("> Vuelos asignados: %d%n",secuenciaDeVuelos.size());
            G4D.Logger.log("Buscando mejor plan de vuelo..");
            PlanDeVuelo mejorPlan = obtenerPlanMasProximo(actual,destino,fechaHoraActual,fechaHoraLimite,planes,aeropuertosVisitados);
            if(mejorPlan == null) {
                G4D.Logger.logln(" [NO ENCONTRADO]");
                G4D.Logger.log("Deshaciendo cambios..");
                for(Vuelo vuelo : secuenciaDeVuelos) {
                    if(vuelo.getCapacidadDisponible() == vuelo.getPlan().getCapacidad()) vuelosActivos.remove(vuelo);
                }
                G4D.Logger.delete_lines(4);
                return null;
            }
            G4D.Logger.logln(" [ENCONTRADO]");
            G4D.Logger.log("Bucando vuelo activo..");
            Vuelo vuelo = obtenerVueloActivo(mejorPlan,fechaHoraActual,fechaHoraLimite,vuelosActivos);
            if(vuelo == null) {
                G4D.Logger.logln(" [NO_ENCONTRADO]");
                G4D.Logger.logln("Activando nuevo vuelo..");
                seActivoVuelo = true;
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
                vuelo.instanciarHorarios(fechaHoraActual);
                vuelo.setDuracion();
                vuelo.setDistancia();
                vuelosActivos.add(vuelo);
            } else G4D.Logger.logln(" [ENCONTRADO]");
            G4D.Logger.logf("> VUELO ASIGNADO: %s",vuelo.getId());
            secuenciaDeVuelos.add(vuelo);
            fechaHoraActual = vuelo.getFechaHoraLlegadaUTC();
            actual = vuelo.getPlan().getDestino();
            G4D.Logger.delete_lines(((seActivoVuelo)?5:4));
        }
        G4D.Logger.delete_upper_line();
        G4D.Logger.log("DESTINO ALCANZADO. Guardando ruta..");
        ruta.setVuelos(secuenciaDeVuelos);
        ruta.setDuracion();
        ruta.setDistancia();
        G4D.Logger.delete_current_line();
        return ruta;
    }
    //
    private PlanDeVuelo obtenerPlanMasProximo(Aeropuerto origen, Aeropuerto destino, LocalDateTime fechaHoraActual,LocalDateTime fechaHoraLimite, List<PlanDeVuelo> planes,
                                              Set<Aeropuerto> visitados) {
        Double mejorProximidad = Double.MAX_VALUE;
        PlanDeVuelo planMaxProximo = null;
        List<PlanDeVuelo> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                                                          .filter(p -> !visitados.contains(p.getDestino()))
                                                          .toList();
        for(PlanDeVuelo plan : planesPosibles) {
            if(!plan.esAlcanzable(fechaHoraActual, fechaHoraLimite)) continue;
            Double proximidad = plan.calcularProximidad(fechaHoraActual, destino);
            if(proximidad < mejorProximidad) {
                mejorProximidad = proximidad;
                planMaxProximo = plan;
            }
        }
        return planMaxProximo;
    }
    //
    private Vuelo obtenerVueloActivo(PlanDeVuelo plan, LocalDateTime fechaHoraActual,LocalDateTime fechaHoraLimite, Set<Vuelo> vuelosActivos) {
        List<Vuelo> vuelosPosibles = vuelosActivos.stream().filter(v -> v.getPlan().getOrigen() == plan.getOrigen())
                                                           .filter(v -> v.getPlan().getDestino() == plan.getDestino())
                                                           .filter(v -> v.getPlan().getHoraSalida().equals(plan.getHoraSalida()))
                                                           .filter(v -> v.getPlan().getHoraLlegada().equals(plan.getHoraLlegada()))
                                                           .filter(v -> v.getFechaHoraLlegadaUTC().isBefore(fechaHoraLimite))
                                                           .filter(v -> v.getCapacidadDisponible() > 1)
                                                           .toList();
        LocalDateTime fechaHoraSalida = G4D.toUTC(
                G4D.toDateTime(plan.getHoraSalida(),fechaHoraActual),
                plan.getOrigen().getHusoHorario()
        );
        if(fechaHoraSalida.isBefore(fechaHoraActual)) {
            fechaHoraSalida = fechaHoraSalida.plusDays(1);
        }
        for(Vuelo vuelo : vuelosPosibles) {
            if(fechaHoraSalida.equals(vuelo.getFechaHoraSalidaUTC())) {
                return vuelo;
            }
        }
        return null;
    }
    // Solución Inicial: Adaptative Nearest Neighbor [ROBUST GREEDY]
    private void solucionInicial_RobustGreedy(Problematica problematica, Solucion solucion) {
        G4D.Logger.logln("[ROBUST GREEDY]");
        // Declaracion de variables
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Pedido> pedidos = problematica.pedidos;
        Set<Vuelo> vuelosEnTransito = new HashSet<>();
        Set<Ruta> rutasAsignadas = new HashSet<>();
        int numPed = 1,cantPed = pedidos.size(),posProd = 0,totalProd = 0;
        Double tProm_prod = 0.0,tProm_ped = 0.0;
        Long duracion = Long.valueOf(0);
        for(Pedido p : pedidos) totalProd += p.getCantidad();
        // Actualizar rutas de productos de pedidos
        for (Pedido pedido : pedidos) {
            LocalDateTime fechaHoraCreacion = pedido.getFechaHoraCreacionUTC();
            int cantProd = pedido.getCantidad();
            for(int numProd = 1;numProd <= cantProd;numProd++) {
                Instant start = Instant.now();
                G4D.Logger.logf("[#] PRODUCTOS ATENDIDOS: %d de %d%n",posProd,totalProd);
                G4D.Logger.logf("[#] TIEMPO PROMEDIO DE ATENCION POR PRODUCTO: %.2f seg.%n",tProm_prod);
                G4D.Logger.logf("[#] TIEMPO PROMEDIO DE ATENCION POR PEDIDO: %.2f seg.%n",tProm_ped);
                G4D.Logger.logf(">> ATENDIENDO PRODUCTO #%d DE '%d' DEL PEDIDO #%d DE '%d'%n",numProd,cantProd,numPed,cantPed);
                Producto producto = new Producto();
                Ruta ruta = obtenerMejorRuta(fechaHoraCreacion,origenes,pedido.getDestino(),planes,vuelosEnTransito,rutasAsignadas);
                if (ruta == null){
                    producto.setOrigen(null);
                    G4D.Logger.logf_err("[ERROR] Ningún origen pudo enrutar al producto #%s del pedido #%s.%n",numProd,numPed);
                    G4D.Logger.logf_err("[ERROR] Solo se atendieron %d de %d pedidos. (%d de %d productos)%n",numPed,cantPed,posProd,totalProd);
                    System.exit(1);
                } else G4D.Logger.delete_lines(8);
                producto.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                producto.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                producto.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                producto.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                LocalDateTime fechaHoraLimiteUTC = fechaHoraCreacion.plusMinutes((long)(double)(60*ruta.getTipo().getMaxHorasParaEntrega()));
                producto.setFechaHoraLimiteLocal(G4D.toLocal(fechaHoraLimiteUTC,producto.getDestino().getHusoHorario()));
                producto.setFechaHoraLimiteUTC(fechaHoraLimiteUTC);
                producto.setRuta(ruta);
                producto.registrarRuta(fechaHoraCreacion);
                pedido.getProductos().add(producto);
                G4D.Logger.logf(">> PRODUCTO ENRUTADO. (Ruta asignada: %s)%n",ruta.getId());
                G4D.Logger.delete_lines(6);
                Instant end = Instant.now();
                duracion += Duration.between(start, end).toMillis();
                posProd++;
                tProm_prod = duracion/(1000.0*posProd);
            }
            tProm_ped = duracion/(1000.0*numPed);
            numPed++;
        }
        // Guardar en solución
        solucion.setPedidosAtendidos(pedidos);
        solucion.setVuelosActivos(vuelosEnTransito);
        solucion.setRutasAsignadas(rutasAsignadas);
        solucion.setFitness();
        G4D.Logger.logf("[+] SOLUCION INICIAL GENERADA! (FITNESS: %.2f)%n",solucion.getFitness());
        G4D.Logger.logf("[#] SE ATENDIERON %d PEDIDOS CON UN TOTAL DE %d PRODUCTOS!%n",cantPed,totalProd);
        G4D.Logger.logf("[#] TIEMPO DE CONVERGENCIA: %.2f seg.%n",duracion/1000.0);
    }
    //
    private Ruta obtenerMejorRuta(LocalDateTime fechaHoraCreacion,List<Aeropuerto> origenes,Aeropuerto destino,
                                  List<PlanDeVuelo> planes,Set<Vuelo> vuelosEnTransito,Set<Ruta>rutasAsignadas) {
        G4D.Logger.logln("Enrutando..");
        // Declaracion de Variables
        G4D.IntegerWrapper numLines = new G4D.IntegerWrapper(1);
        int numOrig = 1;
        Ruta mejorRuta = null;
        Set<Aeropuerto> aeropuertosVisitados;
        List<Vuelo> vuelosUtilizados;
        //
        for(Aeropuerto origen : origenes) {
            G4D.Logger.logf("[ORIGEN #%d de %d]%n",numOrig++,origenes.size());
            TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
            LocalDateTime fechaHoraLimite = fechaHoraCreacion.plusMinutes(60*tipoRuta.getMaxHorasParaEntrega().longValue());
            aeropuertosVisitados = new HashSet<>();
            vuelosUtilizados = new ArrayList<>();
            Ruta rutaMasProxima = buscarRutaVoraz(rutasAsignadas,fechaHoraCreacion,fechaHoraLimite,origen,destino,vuelosEnTransito);
            G4D.Logger.delete_current_line();
            if(rutaMasProxima == null) {
                aeropuertosVisitados = new HashSet<>();
                aeropuertosVisitados.add(origen);
                vuelosUtilizados = new ArrayList<>();
                rutaMasProxima = new Ruta();
                rutaMasProxima.setId(null);
                asignarRutas(origen,destino,fechaHoraCreacion,fechaHoraLimite,origenes,planes,vuelosEnTransito,aeropuertosVisitados,vuelosUtilizados,rutaMasProxima,numLines);
            }
            if(rutaMasProxima.getId() == null) {
                G4D.Logger.delete_current_line();
                G4D.Logger.logln("No es posible generar una ruta a partir de este origen.");
                continue;
            } else {
                G4D.Logger.delete_current_line();
                G4D.Logger.logf("Mejor ruta para este origen: %s (%d vuelos) |",rutaMasProxima.getId(),rutaMasProxima.getVuelos().size());
                if(mejorRuta == null || rutaMasProxima.getDuracion() < mejorRuta.getDuracion()) {
                    mejorRuta = rutaMasProxima;
                    mejorRuta.setTipo(tipoRuta);
                    G4D.Logger.logln(" { ¡NUEVA MEJOR RUTA! }");
                } else G4D.Logger.logln(" { RUTA DESCARTADA }");
            }
        }
        if(mejorRuta != null) {
            vuelosEnTransito.addAll(mejorRuta.getVuelos());
            rutasAsignadas.add(mejorRuta);
        }
        return mejorRuta;
    }
    //
    private boolean asignarRutas(Aeropuerto actual,Aeropuerto destino,LocalDateTime fechaHoraActual,
                                 LocalDateTime fechaHoraLimite,List<Aeropuerto> origenes,
                                 List<PlanDeVuelo> planes,Set<Vuelo> vuelosEnTransito,Set<Aeropuerto> aeropuertosVisitados,
                                 List<Vuelo> vuelosUtilizados,Ruta mejorRuta,G4D.IntegerWrapper numLines) {
        G4D.Logger.logf("(Vuelos utilizados: %d) ACTUAL: %s (%s) | DESTINO: %s (%s)%n",vuelosUtilizados.size(),actual.getCodigo(),G4D.toDisplayString(fechaHoraActual),destino.getCodigo(),(mejorRuta.getId() == null) ? G4D.toDisplayString(fechaHoraLimite) : G4D.toDisplayString(mejorRuta.getVuelos().getLast().getFechaHoraLlegadaUTC()));
        numLines.increment();
        if(actual == destino) {
            G4D.Logger.delete_upper_line();
            numLines.decrement();
            G4D.Logger.log("¡DESTINO ALCANZADO!");
            return true;
        }
        G4D.Logger.logf("Buscando los mejores planes..");
        List<PlanDeVuelo> planesProximos = obtenerPlanesProximos(actual,destino, fechaHoraActual, fechaHoraLimite, planes, origenes, aeropuertosVisitados);
        if(planesProximos == null || planesProximos.isEmpty()) {
            G4D.Logger.logln(" [NO ENCONTRADOS]");
            numLines.increment();
            return false;
        } else {
            G4D.Logger.logln(" [ENCONTRADOS]");
            G4D.Logger.logln("Programando vuelos..");
            numLines.increment(2);
        }
        List<Vuelo> vuelosProximos = new ArrayList<>();
        int numVueloProx = 1;
        for(PlanDeVuelo plan : planesProximos) {
            G4D.Logger.delete_current_line();
            G4D.Logger.logf("(Vuelo #%d)",numVueloProx);
            Vuelo vuelo = obtenerVueloActivo(plan, fechaHoraActual,fechaHoraLimite,vuelosEnTransito);
            if(vuelo == null) {
                G4D.Logger.logf(" > Activando vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(plan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
                vuelo.instanciarHorarios(fechaHoraActual);
                vuelo.setDuracion();
                vuelo.setDistancia();
            }
            vuelosProximos.add(vuelo);
            G4D.Logger.delete_current_line();
            G4D.Logger.logf("(Vuelo #%d) > VUELO PROGRAMADO: %s",numVueloProx,vuelo.getId());
            numVueloProx++;
        }
        vuelosProximos.sort(Comparator.comparing(Vuelo::getFechaHoraLlegadaUTC));
        G4D.Logger.delete_lines(2);
        G4D.Logger.logln("Programando vuelos.. [PROGRAMADOS]");
        for(int i = 0;i < vuelosProximos.size();i++) {
            Vuelo vuelo = vuelosProximos.get(i);
            fechaHoraActual = vuelo.getFechaHoraLlegadaUTC();
            if(fechaHoraActual.isAfter(fechaHoraLimite) || (mejorRuta.getId() != null && fechaHoraActual.isAfter(mejorRuta.getVuelos().getLast().getFechaHoraLlegadaUTC()))) return false;
            actual = vuelo.getPlan().getDestino();
            vuelosUtilizados.add(vuelo);
            aeropuertosVisitados.add(actual);
            if(i == 0) {
                G4D.Logger.delete_lines(numLines.value);
                numLines.value = 1;
            }
            if(asignarRutas(actual,destino,fechaHoraActual,fechaHoraLimite,origenes,planes,vuelosEnTransito,aeropuertosVisitados,vuelosUtilizados,mejorRuta,numLines)) {
                Ruta ruta = new Ruta();
                ruta.setVuelos(new ArrayList<>(vuelosUtilizados));
                ruta.setDistancia();
                ruta.setDuracion();
                if(mejorRuta.getId() == null || ruta.getDuracion() < mejorRuta.getDuracion()) {
                    G4D.Logger.logf(" NUEVA MEJOR RUTA ALMACENADA: %s (%d Vuelos)",ruta.getId(),ruta.getVuelos().size());
                    mejorRuta.reasignar(ruta);
                } else G4D.Logger.logf(" RUTA DESCARTADA.",ruta.getId(),ruta.getVuelos().size());
                return false;
            } else {
                G4D.Logger.delete_lines(numLines.value);
                numLines.decrement(numLines.value - 1);
                vuelosUtilizados.remove(vuelo);
                // aeropuertosVisitados.remove(actual);
            }
        }
        G4D.Logger.delete_lines(numLines.value);
        return false;
    }
    //
    private List<PlanDeVuelo> obtenerPlanesProximos(Aeropuerto aOrig,Aeropuerto aDest, LocalDateTime fechaHoraActual,
                                                    LocalDateTime fechaHoraLimite, List<PlanDeVuelo> planes,
                                                    List<Aeropuerto> origenes,Set<Aeropuerto> aeropuertosVisitados) {
        return planes.stream()
                     .filter(p -> p.getOrigen().equals(aOrig))
                     .filter(p -> !aeropuertosVisitados.contains(p.getDestino()))
                     .filter(p -> !origenes.contains(p.getDestino()))
                     .filter(p -> p.getDestino().obtenerCapacidadDisponible(G4D.toUTC(G4D.toDateTime(p.getHoraLlegada(),fechaHoraActual),p.getDestino().getHusoHorario())) >= 1)
                     .sorted((p1,p2) -> {
                         Double proximidadP1 = p1.calcularProximidad(fechaHoraActual,aDest);
                         Double proximidadP2 = p2.calcularProximidad(fechaHoraActual,aDest);
                         return proximidadP1.compareTo(proximidadP2); 
                     })
                     .toList();
    }
    // Búsqueda local: Variable Neighborhood Descent
    private void VND(Problematica problematica,Solucion solucion) {
        G4D.Logger.logln("[VND]");
        //
        int i = 1;
        boolean huboMejora;
        //
        while (i <= 3) {
            Solucion solucionPropuesta = solucion.replicar();
            huboMejora = false;
            int ele = random.nextInt(L_MIN,L_MAX + 1);
            switch (i) {
                case 1:
                    huboMejora = LSInsertar(problematica,solucionPropuesta,ele);
                    break; 
                case 2:
                    huboMejora = LSIntercambiar(problematica,solucionPropuesta,ele);
                    break;
                case 3:
                    huboMejora = LSRealocar(problematica,solucionPropuesta,ele);
                    break;
            }
            if (huboMejora) {
                solucion.reasignar(solucionPropuesta);
                G4D.Logger.delete_lines(i + 1);
                i = 1;
            } else {
                i++;
            }
        }
    }
    private Boolean LSInsertar(Problematica problematica,Solucion solucionPropuesta,int ele) {
        G4D.Logger.logln("Realizando búsqueda local por 'Insercion'..");
        //
        boolean huboMejora = false;
        Solucion mejorSolucion = solucionPropuesta.replicar();
        List<Ruta> rutasAsignadas = new ArrayList<>(solucionPropuesta.getRutasAsignadas());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<Pedido> pedidosAtendidos = solucionPropuesta.getPedidosAtendidos();
        Set<Vuelo> vuelosActivos = solucionPropuesta.getVuelosActivos();
        for(Ruta ruta : rutasAsignadas) {
            List<Aeropuerto> secuenciaOriginal = ruta.obtenerSecuenciaDeAeropuertos();
            if(secuenciaOriginal.size() < 3 + ele) continue;
            for(int posExtraccion = 1; posExtraccion <= secuenciaOriginal.size() - ele - 1; posExtraccion++) {
                for(int posInsercion = 1; posInsercion <= secuenciaOriginal.size() - ele - 1; posInsercion++) {
                    if(posExtraccion == posInsercion) continue;
                    List<Aeropuerto> secuenciaCopia = new ArrayList<>(secuenciaOriginal);
                    List<Aeropuerto> extraidos = new ArrayList<>();
                    for(int i = 0; i < ele; i++) extraidos.add(secuenciaCopia.remove(posExtraccion));
                    secuenciaCopia.addAll(posInsercion,extraidos);
                    LocalDateTime instanteActual = obtenerInstanteInicialDeRuta(ruta,pedidosAtendidos);
                    LocalDateTime instanteLimite = instanteActual.plusMinutes(60*ruta.getTipo().getMaxHorasParaEntrega().longValue());
                    List<Vuelo> svAux = obtenerSecuenciaDeVuelos(secuenciaCopia, planes, vuelosActivos,origenes, instanteActual, instanteLimite);
                    if(svAux == null) continue;
                    List<Vuelo> svOriginal = ruta.getVuelos();
                    int cantProd = obtenerCantidadDeProductosDeRuta(ruta, pedidosAtendidos);
                    eliminarActividadDeVuelos(cantProd,svOriginal, vuelosActivos);
                    agregarActividadDeVuelos(cantProd,svAux, vuelosActivos);
                    ruta.setVuelos(svAux);
                    solucionPropuesta.setFitness();
                    G4D.Logger.logf("Mejor fitness: %.3f | fitness Actual: %.3f |",mejorSolucion.getFitness(),solucionPropuesta.getFitness());
                    if(solucionPropuesta.getFitness() < mejorSolucion.getFitness()) {
                        G4D.Logger.log(" > NUEVO MEJOR!");
                        mejorSolucion = solucionPropuesta.replicar();
                        huboMejora = true;
                    }
                    eliminarActividadDeVuelos(cantProd,svAux,vuelosActivos);
                    agregarActividadDeVuelos(cantProd,svOriginal,vuelosActivos);
                    ruta.setVuelos(svOriginal);
                    G4D.Logger.delete_current_line();
                }
            }
        }
        solucionPropuesta.reasignar(mejorSolucion);
        G4D.Logger.delete_upper_line();
        G4D.Logger.log("Realizando búsqueda local por 'Insercion'.. ");
        if(huboMejora) G4D.Logger.logf("[FITNESS OPTIMIZADO > %.3f]%n",solucionPropuesta.getFitness());
        else G4D.Logger.logln("[FITNESS MANTENIDO]");
        return huboMejora;
    }
    private Boolean LSIntercambiar(Problematica problematica,Solucion solucionPropuesta,int ele) {
        G4D.Logger.logln("Realizando búsqueda local por 'Intercambio'..");
        boolean huboMejora = false;
        Solucion mejorSolucion = solucionPropuesta.replicar();
        List<Ruta> rutasAsignadas = new ArrayList<>(solucionPropuesta.getRutasAsignadas());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<Pedido> pedidosAtendidos = solucionPropuesta.getPedidosAtendidos();
        Set<Vuelo> vuelosActivos = solucionPropuesta.getVuelosActivos();
        for(Ruta ruta : rutasAsignadas) {
            List<Aeropuerto> secuenciaOriginal = ruta.obtenerSecuenciaDeAeropuertos();
            if(secuenciaOriginal.size() < 2 + 2*ele) continue;
            for(int posA = 1; posA <= secuenciaOriginal.size() - ele - 1; posA++) {
                for(int posB = 1; posB <= secuenciaOriginal.size() - ele - 1; posB++) {
                    if(posA == posB || Math.abs(posA - posB) < ele) continue;
                    List<Aeropuerto> secuenciaCopia = new ArrayList<>(secuenciaOriginal);
                    List<Aeropuerto> grupoA = new ArrayList<>(), grupoB = new ArrayList<>();
                    for(int i = 0; i < ele; i++) grupoA.add(secuenciaCopia.get(posA + i));
                    for(int i = 0; i < ele; i++) grupoB.add(secuenciaCopia.get(posB + i));
                    for(int i = 0; i < ele; i++) secuenciaCopia.set(posA + i, grupoB.get(i));
                    for(int i = 0; i < ele; i++) secuenciaCopia.set(posB + i, grupoA.get(i));
                    LocalDateTime instanteActual = obtenerInstanteInicialDeRuta(ruta,pedidosAtendidos);
                    LocalDateTime instanteLimite = instanteActual.plusMinutes(60*ruta.getTipo().getMaxHorasParaEntrega().longValue());
                    List<Vuelo> svAux = obtenerSecuenciaDeVuelos(secuenciaCopia, planes, vuelosActivos,origenes, instanteActual, instanteLimite);
                    if(svAux == null) continue;
                    List<Vuelo> svOriginal = ruta.getVuelos();
                    int cantProd = obtenerCantidadDeProductosDeRuta(ruta, pedidosAtendidos);
                    eliminarActividadDeVuelos(cantProd,svOriginal, vuelosActivos);
                    agregarActividadDeVuelos(cantProd,svAux, vuelosActivos);
                    ruta.setVuelos(svAux);
                    solucionPropuesta.setFitness();
                    G4D.Logger.logf("Mejor fitness: %.3f | fitness Actual: %.3f |",mejorSolucion.getFitness(),solucionPropuesta.getFitness());
                    if(solucionPropuesta.getFitness() < mejorSolucion.getFitness()) {
                        G4D.Logger.log(" > NUEVO MEJOR!");
                        mejorSolucion = solucionPropuesta.replicar();
                        huboMejora = true;
                    }
                    eliminarActividadDeVuelos(cantProd,svAux,vuelosActivos);
                    agregarActividadDeVuelos(cantProd,svOriginal,vuelosActivos);
                    ruta.setVuelos(svOriginal);
                    G4D.Logger.delete_current_line();
                }
            }
        }
        solucionPropuesta.reasignar(mejorSolucion);
        G4D.Logger.delete_upper_line();
        G4D.Logger.log("Realizando búsqueda local por 'Intercambio'.. ");
        if(huboMejora) G4D.Logger.logf("[FITNESS OPTIMIZADO > %.3f]%n",solucionPropuesta.getFitness());
        else G4D.Logger.logln("[FITNESS MANTENIDO]");
        return huboMejora;
    }
    //
    private Boolean LSRealocar(Problematica problematica,Solucion solucionPropuesta,int ele) {
        G4D.Logger.logln("Realizando búsqueda local por 'Realocacion'..");
        boolean huboMejora = false;
        Solucion mejorSolucion = solucionPropuesta.replicar();
        List<Ruta> rutasAsignadas = new ArrayList<>(solucionPropuesta.getRutasAsignadas());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<Pedido> pedidosAtendidos = solucionPropuesta.getPedidosAtendidos();
        Set<Vuelo> vuelosActivos = solucionPropuesta.getVuelosActivos();

        for(int i = 0; i < rutasAsignadas.size(); i++) {
            Ruta r1 = rutasAsignadas.get(i);
            List<Aeropuerto> s1Original = r1.obtenerSecuenciaDeAeropuertos();
            if(s1Original.size() < 2 + ele) continue;
            for(int posExtraccion = 1; posExtraccion <= s1Original.size() - ele; posExtraccion++) {
                for(int j = 0; j < rutasAsignadas.size(); j++) {
                    if(i == j) continue;
                    Ruta r2 = rutasAsignadas.get(j);
                    List<Aeropuerto> s2Original = r2.obtenerSecuenciaDeAeropuertos();
                    for(int posInsercion = 1; posInsercion <= s2Original.size(); posInsercion++) {
                        List<Aeropuerto> s1Copia = new ArrayList<>(s1Original);
                        List<Aeropuerto> s2Copia = new ArrayList<>(s2Original);
                        List<Aeropuerto> extraidos = new ArrayList<>();
                        for(int k = 0; k < ele; k++) extraidos.add(s1Copia.remove(posExtraccion));
                        s2Copia.addAll(posInsercion,extraidos);
                        LocalDateTime instanteActual_1 = obtenerInstanteInicialDeRuta(r1,pedidosAtendidos);
                        LocalDateTime instanteLimite_1 = instanteActual_1.plusMinutes(60*r1.getTipo().getMaxHorasParaEntrega().longValue());
                        List<Vuelo> sv1Aux = obtenerSecuenciaDeVuelos(s1Copia, planes, vuelosActivos,origenes, instanteActual_1, instanteLimite_1);
                        if(sv1Aux == null) continue;
                        List<Vuelo> sv1Original = r1.getVuelos();
                        int cantProd_1 = obtenerCantidadDeProductosDeRuta(r1, pedidosAtendidos);
                        eliminarActividadDeVuelos(cantProd_1,sv1Original, vuelosActivos);
                        agregarActividadDeVuelos(cantProd_1,sv1Aux, vuelosActivos);
                        r1.setVuelos(sv1Aux);
                        LocalDateTime instanteActual_2 = obtenerInstanteInicialDeRuta(r2,pedidosAtendidos);
                        LocalDateTime instanteLimite_2= instanteActual_2.plusMinutes(60*r2.getTipo().getMaxHorasParaEntrega().longValue());
                        List<Vuelo> sv2Aux = obtenerSecuenciaDeVuelos(s2Copia, planes, vuelosActivos,origenes, instanteActual_2, instanteLimite_2);
                        if(sv2Aux == null) {
                            eliminarActividadDeVuelos(cantProd_1,sv1Aux,vuelosActivos);
                            agregarActividadDeVuelos(cantProd_1,sv1Original,vuelosActivos);
                            r1.setVuelos(sv1Original);
                            continue;
                        }
                        List<Vuelo> sv2Original = r2.getVuelos();
                        int cantProd_2 = obtenerCantidadDeProductosDeRuta(r2, pedidosAtendidos);
                        eliminarActividadDeVuelos(cantProd_2,sv2Original, vuelosActivos);
                        agregarActividadDeVuelos(cantProd_2,sv2Aux, vuelosActivos);
                        r2.setVuelos(sv2Aux);
                        solucionPropuesta.setFitness();
                        G4D.Logger.logf("%nMejor fitness: %.3f | fitness Actual: %.3f |",mejorSolucion.getFitness(),solucionPropuesta.getFitness());
                        if(solucionPropuesta.getFitness() < mejorSolucion.getFitness()) {
                            G4D.Logger.log(" > NUEVO MEJOR!");
                            mejorSolucion = solucionPropuesta.replicar();
                            huboMejora = true;
                        }
                        eliminarActividadDeVuelos(cantProd_1,sv1Aux,vuelosActivos);
                        agregarActividadDeVuelos(cantProd_1,sv1Original,vuelosActivos);
                        r1.setVuelos(sv1Original);
                        eliminarActividadDeVuelos(cantProd_2,sv2Aux,vuelosActivos);
                        agregarActividadDeVuelos(cantProd_2,sv2Original,vuelosActivos);
                        r2.setVuelos(sv2Original);
                        G4D.Logger.delete_current_line();
                    }
                }
            }
        }
        solucionPropuesta.reasignar(mejorSolucion);
        G4D.Logger.delete_upper_line();
        G4D.Logger.log("Realizando búsqueda local por 'Realocacion'.. ");
        if(huboMejora) G4D.Logger.logf("[FITNESS OPTIMIZADO > %.3f]%n",solucionPropuesta.getFitness());
        else G4D.Logger.logln("[FITNESS MANTENIDO]");
        return huboMejora;
    }
    //
    private void Shaking(Solucion solucion, G4D.IntegerWrapper k, Problematica problematica) {
        G4D.Logger.logln("Shaking..");
        // Perturbar la solución actual para diversificación
        for (int i = 0; i < k.value; ++i) {
            int neighborhood = random.nextInt(3);
            int ele = L_MIN + random.nextInt(L_MAX - L_MIN + 1);
            switch (neighborhood) {
                case 0:
                    TInsertar(problematica, solucion, ele);
                    break;
                case 1:
                    TRealocar(problematica, solucion, ele);
                    break;
                case 2:
                    TIntercambiar(problematica, solucion, ele);
                    break;
            }
            G4D.Logger.logf(" > %.3f%n",solucion.getFitness());
        }
        G4D.Logger.delete_lines(1 + k.value);
        G4D.Logger.logf("Shaking.. > [%.3f]%n",solucion.getFitness());
    }
    //
    private void TInsertar(Problematica problematica,Solucion solucion,int ele) {
        G4D.Logger.log("Realizando perturbacion por 'Insercion'..");
        List<Ruta> rutas = new ArrayList<>(solucion.getRutasAsignadas());
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Pedido> pedidosAtendidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosActivos = solucion.getVuelosActivos();
        for(Ruta ruta : rutas) {
            List<Aeropuerto> secuenciaDeAeropuertos = ruta.obtenerSecuenciaDeAeropuertos();
            if(secuenciaDeAeropuertos.size() < 3 + ele) continue;
            int posExtraccion = random.nextInt(1,secuenciaDeAeropuertos.size() - ele);
            int posInsercion = posExtraccion;
            while(posExtraccion == posInsercion) posInsercion = random.nextInt(1,secuenciaDeAeropuertos.size() - ele);
            List<Aeropuerto> extraidos = new ArrayList<>();
            for(int i = 0; i < ele; i++) extraidos.add(secuenciaDeAeropuertos.remove(posExtraccion));
            secuenciaDeAeropuertos.addAll(posInsercion,extraidos);
            LocalDateTime instanteActual = obtenerInstanteInicialDeRuta(ruta,pedidosAtendidos);
            LocalDateTime instanteLimite = instanteActual.plusMinutes(60*ruta.getTipo().getMaxHorasParaEntrega().longValue());
            List<Vuelo> secuenciaDeVuelos = obtenerSecuenciaDeVuelos(secuenciaDeAeropuertos, planes, vuelosActivos,origenes, instanteActual, instanteLimite);
            if(secuenciaDeVuelos == null) continue;
            int cantProd = obtenerCantidadDeProductosDeRuta(ruta, pedidosAtendidos);
            eliminarActividadDeVuelos(cantProd, ruta.getVuelos(), vuelosActivos);
            agregarActividadDeVuelos(cantProd,secuenciaDeVuelos, vuelosActivos);
            ruta.setVuelos(secuenciaDeVuelos);
        }
        solucion.setFitness();
    }
    //
    private void TIntercambiar(Problematica problematica,Solucion solucion,int ele) {
        G4D.Logger.log("Realizando perturbacion por 'Intercambio'..");
        List<Ruta> rutasAsignadas = new ArrayList<>(solucion.getRutasAsignadas());
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Pedido> pedidosAtendidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosActivos = solucion.getVuelosActivos();
        for(Ruta ruta : rutasAsignadas) {
            List<Aeropuerto> secuenciaDeAeropuertos = ruta.obtenerSecuenciaDeAeropuertos();
            if(secuenciaDeAeropuertos.size() < 2 + 2*ele) continue;
            int posA = random.nextInt(1, secuenciaDeAeropuertos.size() - ele);
            int posB = posA;
            while(Math.abs(posA - posB) < ele) posB = random.nextInt(1, secuenciaDeAeropuertos.size() - ele);
            List<Aeropuerto> grupoA = new ArrayList<>(), grupoB = new ArrayList<>();
            if(posA > posB) {
                for(int i = 0; i < ele; i++) grupoA.add(secuenciaDeAeropuertos.remove(posA));
                for(int i = 0; i < ele; i++) grupoB.add(secuenciaDeAeropuertos.remove(posB));
                secuenciaDeAeropuertos.addAll(posB, grupoA);
                secuenciaDeAeropuertos.addAll(posA, grupoB);
            } else {
                for(int i = 0; i < ele; i++) grupoB.add(secuenciaDeAeropuertos.remove(posB));
                for(int i = 0; i < ele; i++) grupoA.add(secuenciaDeAeropuertos.remove(posA));
                secuenciaDeAeropuertos.addAll(posA, grupoB);
                secuenciaDeAeropuertos.addAll(posB, grupoA);
            }
            LocalDateTime instanteActual = obtenerInstanteInicialDeRuta(ruta,pedidosAtendidos);
            LocalDateTime instanteLimite = instanteActual.plusMinutes(60*ruta.getTipo().getMaxHorasParaEntrega().longValue());
            List<Vuelo> secuenciaDeVuelos = obtenerSecuenciaDeVuelos(secuenciaDeAeropuertos, planes, vuelosActivos,origenes, instanteActual, instanteLimite);
            if(secuenciaDeVuelos == null) continue;
            int cantProd = obtenerCantidadDeProductosDeRuta(ruta, pedidosAtendidos);
            eliminarActividadDeVuelos(cantProd, ruta.getVuelos(), vuelosActivos);
            agregarActividadDeVuelos(cantProd,secuenciaDeVuelos, vuelosActivos);
            ruta.setVuelos(secuenciaDeVuelos);
        }
        solucion.setFitness();
    }
    private void TRealocar(Problematica problematica,Solucion solucion,int ele) {
        G4D.Logger.log("Realizando perturbacion por 'Realocacion'..");
        List<Ruta> rutas = new ArrayList<>(solucion.getRutasAsignadas());
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Pedido> pedidosAtendidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosActivos = solucion.getVuelosActivos();
        while(rutas.size() > 1) {
            Ruta r1 = rutas.remove(random.nextInt(rutas.size()));
            List<Aeropuerto> sa1 = r1.obtenerSecuenciaDeAeropuertos();
            if(sa1.size() >= 2 + ele) {
                int posExtraccion = (sa1.size() == 2 + ele) ? 1 : random.nextInt(1,sa1.size()-ele);
                Ruta r2 = rutas.remove(random.nextInt(rutas.size()));
                List<Aeropuerto> sa2 = r2.obtenerSecuenciaDeAeropuertos();
                int posInsercion = random.nextInt(1,sa2.size());
                List<Aeropuerto> extraidos = new ArrayList<>();
                for(int i = 0; i < ele; i++) extraidos.add(sa1.remove(posExtraccion));
                sa2.addAll(posInsercion,extraidos);
                LocalDateTime instanteActual_1 = obtenerInstanteInicialDeRuta(r1,pedidosAtendidos);
                LocalDateTime instanteLimite_1 = instanteActual_1.plusMinutes(60*r1.getTipo().getMaxHorasParaEntrega().longValue());
                List<Vuelo> sv1 = obtenerSecuenciaDeVuelos(sa1, planes, vuelosActivos,origenes, instanteActual_1, instanteLimite_1);
                if(sv1 == null) continue;
                List<Vuelo> sv1Orig = r1.getVuelos();
                int cantProd_1 = obtenerCantidadDeProductosDeRuta(r1, pedidosAtendidos);
                eliminarActividadDeVuelos(cantProd_1, sv1Orig, vuelosActivos);
                agregarActividadDeVuelos(cantProd_1, sv1, vuelosActivos);
                r1.setVuelos(sv1);
                LocalDateTime instanteActual_2 = obtenerInstanteInicialDeRuta(r2,pedidosAtendidos);
                LocalDateTime instanteLimite_2 = instanteActual_2.plusMinutes(60*r2.getTipo().getMaxHorasParaEntrega().longValue());
                List<Vuelo> sv2 = obtenerSecuenciaDeVuelos(sa2, planes, vuelosActivos,origenes, instanteActual_2, instanteLimite_2);
                if(sv2 == null) {
                    eliminarActividadDeVuelos(cantProd_1,sv1, vuelosActivos);
                    agregarActividadDeVuelos(cantProd_1,sv1Orig, vuelosActivos);
                    r1.setVuelos(sv1Orig);
                    continue;
                }
                int cantProd_2 = obtenerCantidadDeProductosDeRuta(r2, pedidosAtendidos);
                eliminarActividadDeVuelos(cantProd_2, r2.getVuelos(), vuelosActivos);
                agregarActividadDeVuelos(cantProd_2, sv2, vuelosActivos);
                r2.setVuelos(sv2);
            }
        }
        solucion.setFitness();
    }
    //
    private List<Vuelo> obtenerSecuenciaDeVuelos(List<Aeropuerto> aeropuertos,List<PlanDeVuelo> planesDisponibles,Set<Vuelo> vuelosActivos,List<Aeropuerto> origenes, LocalDateTime instanteActual, LocalDateTime instanteLimite) {
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Set<Aeropuerto> visitados = new HashSet<>();
        Aeropuerto aDest = aeropuertos.getLast();
        for(int i = 0;i < aeropuertos.size() - 1;i++) {
            Aeropuerto aOrig = aeropuertos.get(i);
            PlanDeVuelo planMasProximo = obtenerPlanMasProximo(aOrig, aDest, instanteActual, instanteLimite, planesDisponibles, visitados);
            if(planMasProximo == null) return null;
            Vuelo vuelo = obtenerVueloActivo(planMasProximo, instanteActual, instanteLimite, vuelosActivos);
            if(vuelo == null) {
                vuelo = new Vuelo();
                vuelo.setPlan(planMasProximo);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidad());
                vuelo.instanciarHorarios(instanteActual);
                vuelo.setDuracion();
                vuelo.setDistancia();
            }
            instanteActual = vuelo.getFechaHoraLlegadaUTC();
            secuenciaDeVuelos.add(vuelo);
        }
        return secuenciaDeVuelos;
    }
    //
    private LocalDateTime obtenerInstanteInicialDeRuta(Ruta ruta, List<Pedido> pedidos) {
        Pedido pedido = pedidos.stream().filter(ped -> ped.getProductos().stream().anyMatch(prod -> prod.getRuta().equals(ruta))).findFirst().orElse(null);
        return (pedido != null) ? pedido.getFechaHoraCreacionUTC() : null;
    }
    //
    private Integer obtenerCantidadDeProductosDeRuta(Ruta ruta,List<Pedido> pedidos) {
        int cantProd = 0;
        for(Pedido ped : pedidos) cantProd += ped.getProductos().stream().filter(prod -> prod.getRuta().equals(ruta)).count();
        return cantProd;
    }
    //
    private void agregarActividadDeVuelos(int cantProd,List<Vuelo> vuelos,Set<Vuelo> vuelosActivos) {
        for(Vuelo vuelo : vuelos) vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() - cantProd);
        vuelosActivos.addAll(vuelos);
    }
    //
    private void eliminarActividadDeVuelos(int cantProd,List<Vuelo> vuelos,Set<Vuelo> vuelosActivos) {
        for(Vuelo vuelo : vuelos) vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + cantProd);
        vuelosActivos.removeIf(v -> v.getCapacidadDisponible() == v.getPlan().getCapacidad());
    }
    //
    private void NeighborhoodChange(Problematica problematica, Solucion solucionAux,
                                    Solucion x_prima_doble, Solucion x_best,
                                    G4D.IntegerWrapper k, G4D.IntegerWrapper t, 
                                    G4D.IntegerWrapper mejorT) {
        
        System.out.println("Validando.. ");
        
        if (x_prima_doble.getFitness() < x_best.getFitness()) {
            x_best = x_prima_doble.replicar();
            solucionAux.reasignar(x_prima_doble);
            k.value = K_MIN;
            mejorT.value = t.value;
            System.out.printf("%18s%n", "Nuevo mejor!");
            System.out.printf("%9s[%.2f]%n", "", x_best.getFitness());
        } else {
            System.out.printf("%19s%n", "No es mejor..");
            k.value++;
        }
    }
}
