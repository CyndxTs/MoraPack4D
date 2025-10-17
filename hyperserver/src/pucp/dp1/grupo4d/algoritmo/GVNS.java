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
    private static final Integer T_MAX = 10;
    private static final Integer MAX_INTENTOS = 10;
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
        G4D.Logger.logf("[+] SOLUCION INICIAL GENERADA! (FITNESS: %.2f)%n", solucionAux.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(solucionAux, "SolucionInicial.txt");
        // Optimización inicial (Variable Neighborhood Descent)
        G4D.Logger.Stats.set_local_start();
        G4D.Logger.log("Realizando optimización inicial.. ");
        VND(problematica, solucionAux);
        G4D.Logger.Stats.set_local_duration();
        G4D.Logger.logf("[+] OPTIMIZACION INICIAL REALIZADA! (FITNESS: %.2f)%n", solucionAux.getFitness());
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
                    Shaking(x_prima, k, problematica);
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
                Solucion x_prima_doble = x_prima.replicar();
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
        G4D.Logger.logf("[+] OPTIMIZACION FINAL REALIZADA! (FITNESS: %.2f)%n", x_best.getFitness());
        G4D.Logger.Stats.log_stat_local_sol();
        imprimirSolucion(x_best, "SolucionGVNS.txt");
        G4D.Logger.Stats.set_global_duration();
        G4D.Logger.Stats.log_stat_global_sol();
        G4D.Printer.open("CapacidadesFinalesDeRuta.txt");
        List<Ruta> rutas = new ArrayList<>(solucionAux.getRutasEnOperacion());
        for(Ruta ruta : rutas) {
            G4D.Printer.printf("%s -> %s | Disp: %d de %d %n", ruta.getOrigen().getCodigo(), ruta.getDestino().getCodigo(),  ruta.obtenerCapacidadDisponible(), ruta.obtenerCapacidad());
        }
        G4D.Printer.close();
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
            }
            int rCapDisp = ruta.obtenerCapacidadDisponible();
            int cantEnrutables = Math.min(rCapDisp, cantPorEnrutar);
            G4D.Logger.logf("Enrutando %d productos.. {%s}", cantEnrutables, ruta.getId());
            LoteDeProductos lote = origen.generarLoteDeProductos(cantEnrutables);
            ruta.registraLoteDeProductos(lote);
            rutasAsignadas.add(ruta);
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
        vuelosActivados.addAll(secuenciaDeVuelos);
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
        int i = 1;
        boolean huboMejora;
        //
        while (i <= 3) {
            Solucion solucionPropuesta = solucion.replicar();
            huboMejora = false;
            int ele = random.nextInt(L_MIN, L_MAX + 1);
            switch (i) {
                case 1:
                    // huboMejora = LSInsertar(problematica,solucionPropuesta,ele);
                    break; 
                case 2:
                    // huboMejora = LSIntercambiar(problematica,solucionPropuesta,ele);
                    break;
                case 3:
                    // huboMejora = LSRealocar(problematica,solucionPropuesta,ele);
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
    /*
    private Boolean LSInsertar(Problematica problematica, Solucion solucionPropuesta, int ele) {
        G4D.Logger.logln("Realizando búsqueda local por 'Insercion'..");
        //
        boolean huboMejora = false;
        Solucion mejorSolucion = solucionPropuesta.replicar();
        List<Ruta> rutasAsignadas = new ArrayList<>(solucionPropuesta.getRutasAsignadas());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<Pedido> pedidosAtendidos = solucionPropuesta.getPedidosAtendidos();
        Set<Vuelo> vuelosActivos = solucionPropuesta.getVuelosEnTransito();
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
        Set<Vuelo> vuelosActivos = solucionPropuesta.getVuelosEnTransito();
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
        Set<Vuelo> vuelosActivos = solucionPropuesta.getVuelosEnTransito();

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
    */
    //
    private void Shaking(Solucion solucion, G4D.IntegerWrapper k, Problematica problematica) {
        G4D.Logger.logln("Agitando..");
        // Perturbar la solución actual para diversificación
        for (int i = 0; i < k.value; ++i) {
            int neighborhood = random.nextInt(3);
            int ele = L_MIN + random.nextInt(L_MAX - L_MIN + 1);
            switch (neighborhood) {
                case 0:
                    // TInsertar(problematica, solucion, ele);
                    break;
                case 1:
                    // TRealocar(problematica, solucion, ele);
                    break;
                case 2:
                    // TIntercambiar(problematica, solucion, ele);
                    break;
            }
            G4D.Logger.logf(" > %.3f%n",solucion.getFitness());
        }
        G4D.Logger.delete_lines(2 + k.value);
        G4D.Logger.logf("Agitando.. > [%.3f]%n",solucion.getFitness());
    }
    /*
    private void TInsertar(Problematica problematica,Solucion solucion,int ele) {
        G4D.Logger.log("Realizando perturbacion por 'Insercion'..");
        List<Ruta> rutas = new ArrayList<>(solucion.getRutasAsignadas());
        List<Aeropuerto> origenes = new ArrayList<>(problematica.origenes.values());
        List<PlanDeVuelo> planes = problematica.planes;
        List<Pedido> pedidosAtendidos = solucion.getPedidosAtendidos();
        Set<Vuelo> vuelosActivos = solucion.getVuelosEnTransito();
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
        Set<Vuelo> vuelosActivos = solucion.getVuelosEnTransito();
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
        Set<Vuelo> vuelosActivos = solucion.getVuelosEnTransito();
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
    */
    private void NeighborhoodChange(Problematica problematica, Solucion solucionAux,
                                    Solucion x_prima_doble, Solucion x_best,
                                    G4D.IntegerWrapper k, G4D.IntegerWrapper t, 
                                    G4D.IntegerWrapper mejorT) {
       G4D.Logger.log("Validando cambio de vencindario.. ");
        
        if (x_prima_doble.getFitness() < x_best.getFitness()) {
            x_best = x_prima_doble.replicar();
            solucionAux.reasignar(x_prima_doble);
            k.value = K_MIN;
            mejorT.value = t.value;
            // System.out.printf("%18s%n", "Nuevo mejor!");
            // System.out.printf("%9s[%.2f]%n", "", x_best.getFitness());
        } else {
            // System.out.printf("%19s%n", "No es mejor..");
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
