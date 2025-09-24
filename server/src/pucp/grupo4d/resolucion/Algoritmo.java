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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
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
import pucp.grupo4d.util.G4D_Util.IntegerWrapper;

public class Algoritmo {
    private static G4D_Util.Logger log;
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

    public Algoritmo() {
        this.solucionGVNS = null;
        this.solucionPSO = null;
    }
    
    public void GVNS(Problematica problematica) {
        // Declaracion de Variables (siguiendo tu estructura de C++)
        Solucion solucionAux = new Solucion();
        Solucion x_best = new Solucion();
        G4D_Util.IntegerWrapper t = new G4D_Util.IntegerWrapper(), t_best = new G4D_Util.IntegerWrapper();
        
        // Solución inicial (Nearest Neighbor)
        System.out.println("Generando solucion inicial..");
        solucionInicial(problematica, solucionAux);
        imprimirSolucion(solucionAux, "SolucionInicial.txt");

        // Optimización inicial (Variable Neighborhood Descent)
        System.out.println("Realizando optimización inicial por VND..");
        VND(problematica, solucionAux);
        imprimirSolucion(solucionAux, "SolucionVND.txt");

        // Optimización final (Variable Neighborhood Search)
        x_best = solucionAux.replicar();
        Instant start = Instant.now();
        do {
            G4D_Util.IntegerWrapper k = new G4D_Util.IntegerWrapper(K_MIN);
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
                    System.out.println("Validando..");
                    
                    if (x_prima.getFitness() != PEOR_FITNESS) {
                        solucionValida = true;
                        break;
                    } else {
                        System.out.printf("%18s%n", "[ABERRACION]");
                        intentos++;
                        if (intentos >= MAX_INTENTOS) break;
                    }
                }
                
                if (!solucionValida) {
                    k.value++;
                    continue;
                }
                
                System.out.printf("%20s%n", "[POSIBLE MEJORA]");
                Solucion x_prima_doble = x_prima.replicar();
                VND(problematica, x_prima_doble);
                
                // Actualizar tiempo
                Instant end = Instant.now();
                Duration duracion = Duration.between(start, end);
                t.value = (int) duracion.getSeconds();
                
                // Neighborhood Change
                NeighborhoodChange(problematica, solucionAux, x_prima_doble, x_best, k, t, t_best);
            }
            
            // Actualizar tiempo para condición del bucle externo
            Instant end = Instant.now();
            Duration duracion = Duration.between(start, end);
            t.value = (int) duracion.getSeconds();
            
        } while (t.value < T_MAX);
        
        // Solución final
        this.solucionGVNS = x_best.replicar();
        imprimirSolucion(x_best, "SolucionGVNS.txt");
        System.out.println();
        System.out.printf("MEJOR SOLUCION OBTENIDA: %.2f%n", x_best.getFitness());
    }

    // Solución Inicial: Nearest Neighbor
    private void solucionInicial(Problematica problematica, Solucion solucion) {
        //
        System.out.println("Generando solución inicial..");
        //
        List<Pedido> pedidos = problematica.getPedidos();
        List<PlanDeVuelo> planes = problematica.getPlanes();
        List<Aeropuerto> sedes = problematica.getSedes();
        Set<Vuelo> vuelosActivos = new HashSet<>();
        int numPed = 1,cantPed = pedidos.size(),numProd,cantProd;
        // Actualizar rutas de productos de pedidos
        for (Pedido pedido : pedidos) {
            System.out.printf("[#] ATENDIENDO PEDIDO #%d de %d%n",numPed,cantPed);
            LocalDateTime fechaHoraCreacion = pedido.getFechaHoraCreacion();
            List<Producto> productos = pedido.getProductos();
            cantProd = productos.size();
            numProd = 1;
            for (Producto producto : productos) {
                System.err.printf(">> ATENDIENDO PRODUCTO #%d de %d%n",numProd,cantProd);
                Ruta ruta = obtenerMejorRuta(fechaHoraCreacion,producto.getDestino(),sedes,planes,vuelosActivos);
                if (ruta == null){
                    producto.setOrigen(null);
                    System.out.printf("[ERROR] Ningún origen pudo atender al producto #%s del pedido %s.%n",numProd,numPed);
                    System.exit(1);
                }
                producto.setOrigen(ruta.getVuelos().getFirst().getPlan().getOrigen());
                producto.setDestino(ruta.getVuelos().getLast().getPlan().getDestino());
                producto.setFechaHoraLlegadaLocal(ruta.getVuelos().getLast().getFechaHoraLlegadaLocal());
                producto.setFechaHoraLlegadaUTC(ruta.getVuelos().getLast().getFechaHoraLlegadaUTC());
                LocalDateTime fechaHoraLimiteUTC = fechaHoraCreacion.plusMinutes((long)(double)(60*ruta.getTipo().getMaxHorasParaEntrega()));
                producto.setFechaHoraLimiteLocal(G4D_Util.toLocal(fechaHoraLimiteUTC,producto.getDestino().getHusoHorario()));
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
        int cantOrig = origenes.size(),numOrig = 1;
        Ruta ruta,mejorRuta = null;
        Set<Aeropuerto> aeropuertosVisitados;
        //
        for(Aeropuerto origen : origenes) {
            System.out.printf("[ORIGEN #%d de %d]%n",numOrig,cantOrig);
            aeropuertosVisitados = new HashSet<>();
            ruta = construirRutaVoraz(fechaHoraCreacion,origen,destino,planes,vuelosActivos,aeropuertosVisitados);
            if(ruta == null) {
                System.out.println("No es posible generar una ruta a partir de este origen.");
                continue;
            }
            if(mejorRuta == null || ruta.getDuracion() < mejorRuta.getDuracion()) {
                System.out.printf("Nueva mejor ruta asignada: %s%n",ruta.getId());
                mejorRuta = ruta;
            }
            numOrig++;
        }
        return mejorRuta;
    }
    //
    private Ruta construirRutaVoraz(LocalDateTime fechaHoraCreacion,Aeropuerto origen,Aeropuerto destino,
                                    List<PlanDeVuelo>planes,Set<Vuelo> vuelosActivos,Set<Aeropuerto> aeropuertosVisitados) {
        //
        System.err.println("Construyendo ruta..");
        //
        int numVuelo = 1;
        TipoRuta tipoRuta = (origen.getContinente().compareTo(destino.getContinente()) == 0) ? TipoRuta.INTRACONTINENTAL : TipoRuta.INTERCONTINENTAL;
        Double duracionActual = 0.0, maxDuracionParaEntrega = tipoRuta.getMaxHorasParaEntrega();
        Ruta ruta = new Ruta();
        List<Vuelo> secuenciaDeVuelos = new ArrayList<>();
        Aeropuerto actual = origen;
        LocalDateTime fechaHoraActual = fechaHoraCreacion;
        //
        if(actual.equals(destino)) return null;
        //
        while (!actual.equals(destino)) {
            aeropuertosVisitados.add(actual);
            if(numVuelo != 1) System.out.println("DESTINO NO ALCANZADO. Redirigiendo..");
            System.out.printf("> VUELO #%d%n",numVuelo);
            System.out.print("Buscando mejor plan de vuelo..");
            PlanDeVuelo mejorPlan = obtenerPlanMasProximo(actual,fechaHoraActual,planes,duracionActual,maxDuracionParaEntrega,aeropuertosVisitados);
            if(mejorPlan == null) {
                System.out.println(" [NO ENCONTRADO]");
                for(Vuelo vuelo : secuenciaDeVuelos) {
                    vuelo.setCapacidadDisponible(vuelo.getCapacidadDisponible() + 1);
                    if(vuelo.getCapacidadDisponible() == vuelo.getPlan().getCapacidadMaxima()) vuelosActivos.remove(vuelo);
                }
                return null;
            }
            System.out.println(" [ENCONTRADO]");
            System.out.print("Bucando vuelo activo..");
            Vuelo vuelo = obtenerVueloActivo(mejorPlan,fechaHoraActual,vuelosActivos);
            if(vuelo == null) {
                System.out.println(" [NO_ENCONTRADO]");
                System.out.println("Activando nuevo vuelo..");
                vuelo = new Vuelo();
                vuelo.setPlan(mejorPlan);
                vuelo.setCapacidadDisponible(vuelo.getPlan().getCapacidadMaxima());
                vuelo.instanciarHorarios(fechaHoraActual);
                vuelo.setDuracion();
                vuelosActivos.add(vuelo);
            } else System.out.println(" [ENCONTRADO]");
            System.out.printf("VUELO ASIGNADO: %s%n",vuelo.getId());
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
    private PlanDeVuelo obtenerPlanMasProximo(Aeropuerto origen, LocalDateTime fechaHoraActual, List<PlanDeVuelo> planes,
                                              Double duracionActual,Double maxDuracionParaEntrega,Set<Aeropuerto> visitados) {
        Double mejorProximidad = Double.MAX_VALUE;
        PlanDeVuelo planMaxProximo = null;
        List<PlanDeVuelo> planesPosibles = planes.stream().filter(p -> p.getOrigen().equals(origen))
                                                          .filter(p -> !visitados.contains(p.getDestino()))
                                                          .toList();
        for(PlanDeVuelo plan : planesPosibles) {
            LocalDateTime fechaHoraSalidaUTC =  G4D_Util.toUTC(
                G4D_Util.toDateTime(plan.getHoraSalida(),fechaHoraActual),
                plan.getOrigen().getHusoHorario()
            );
            LocalDateTime fechaHoraLlegadaUTC =  G4D_Util.toUTC(
                G4D_Util.toDateTime(plan.getHoraLlegada(),fechaHoraActual),
                plan.getDestino().getHusoHorario()
            );
            if(fechaHoraLlegadaUTC.isBefore(fechaHoraSalidaUTC)) fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
            if(fechaHoraSalidaUTC.isBefore(fechaHoraActual)) {
                fechaHoraSalidaUTC = fechaHoraSalidaUTC.plusDays(1);
                fechaHoraLlegadaUTC = fechaHoraLlegadaUTC.plusDays(1);
            }
            Double transcurrido = G4D_Util.calculateElapsedHours(fechaHoraActual,fechaHoraLlegadaUTC);
            if(duracionActual + transcurrido > maxDuracionParaEntrega) continue;
            Integer capacidadDisponible = plan.getDestino().obtenerCapacidadDisponible(G4D_Util.toUTC(G4D_Util.toDateTime(plan.getHoraLlegada(),fechaHoraActual),plan.getDestino().getHusoHorario()));
            if(capacidadDisponible < 1) continue;
            Double distancia = origen.obtenerDistanciaHasta(plan.getDestino());
            Double proximidad = transcurrido + 0.003 * distancia + 0.01 * capacidadDisponible;
            if(proximidad < mejorProximidad) {
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
                G4D_Util.toDateTime(plan.getHoraSalida(),fechaHoraActual),
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

    // Búsqueda local: Variable Neighborhood Descent
    private void VND(Problematica problematica, Solucion solucion) {
        int l = 1;
        boolean huboMejora;
        //
        while (l <= 3) {
            Solucion solucionPropuesta = solucion.replicar();
            huboMejora = false;
            switch (l) {
                case 1:
                    //huboMejora = LSInsertar(problematica, solucion, solucionPropuesta, l);
                    break; 
                case 2:
                    //huboMejora = LSIntercambiar(problematica, solucion, solucionPropuesta, l);
                    break;
                case 3:
                    //huboMejora = LSRealocar(problematica, solucion, solucionPropuesta, l);
                    break;
            }
            
            if (huboMejora && solucionPropuesta.getFitness() < solucion.getFitness()) {
                solucion.reasignar(solucionPropuesta);
                l = 1; // Reiniciar con primer vecindario
            } else {
                l++; // Probar siguiente vecindario
            }
        }
    }
    //
    private void Shaking(Solucion solucion, G4D_Util.IntegerWrapper k, Problematica problematica) {
        System.out.println("Shaking..");
        // Perturbar la solución actual para diversificación
        for (int i = 0; i < k.value; ++i) {
            int neighborhood = random.nextInt(3); // 0, 1 o 2
            int ele = L_MIN + random.nextInt(L_MAX - L_MIN + 1);
            
            switch (neighborhood) {
                case 0: // Insertar l elementos
                    // TInsertar(problematica, solucion, ele);
                    break;
                case 1: // Realocar l elementos  
                    // TRealocar(problematica, solucion, ele);
                    break;
                case 2: // Intercambiar l elementos
                    // TIntercambiar(problematica, solucion, ele);
                    break;
            }
            solucion.setFitness();
            System.out.println(solucion.getFitness()); // Historial de agitaciones
        }
        System.out.printf("%9s[%.2f]%n", "", solucion.getFitness());
    }

    private void NeighborhoodChange(Problematica problematica, Solucion solucionAux,
                                    Solucion x_prima_doble, Solucion x_best,
                                    G4D_Util.IntegerWrapper k, G4D_Util.IntegerWrapper t, 
                                    G4D_Util.IntegerWrapper mejorT) {
        
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


    //
    public void imprimirSolucionGVNS(String rutaArchivo) { imprimirSolucion(solucionGVNS, rutaArchivo); }
    //
    public void imprimirSolucionPSO(String rutaArchivo) { imprimirSolucion(solucionPSO, rutaArchivo); }
    //
    private void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        // Declaracion de variables
        int dimLinea = 135,posPedido = 0,posProducto,numProductos;
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
            G4D_Util.printCentered(archivoWriter,dimLinea,String.format("%s %32s %24s","DURACION PROM.","DISTANCIA RECORRIDA PROM.","CAP. DISPO. PROM."));
            G4D_Util.printCentered(archivoWriter,dimLinea,String.format(
                                "%15s %28s %11s%s | %s",
                                String.format("%.2f hrs.",solucion.getDuracionPromedio()),
                                String.format("%.2f Km.",solucion.getDistanciaRecorridaPromedio()),
                                " ",
                                String.format("V: %.2f",solucion.getCapacidadDiponiblePromedioPorVuelo()),
                                String.format("A: %.2f",solucion.getCapacidadDisponiblePromedioPorAeropuerto())
                            ));
            G4D_Util.printFullLine(archivoWriter, '=', dimLinea);
            for(Pedido pedido : solucion.getPedidos()) {
                G4D_Util.printCentered(archivoWriter, dimLinea, String.format("PEDIDO #%d", posPedido + 1));
                G4D_Util.printFullLine(archivoWriter, '-', dimLinea,4);
                archivoWriter.printf("%5s %-30s %15s %25s %25s%n","","CLIENTE","DESTINO","NUM. PRODUCTOS MPE","INSTANTE DE REGISTRO");
                archivoWriter.printf("%5s %-30s %13s %20s %31s%n","",pedido.getCliente().getNombre(),pedido.getDestino().getCodigo(),String.format("%03d",pedido.getCantidad()),G4D_Util.toDisplayString(pedido.getFechaHoraCreacion()));
                archivoWriter.println();
                G4D_Util.printCentered(archivoWriter, dimLinea, "> SECUENCIA DE VUELOS PLANIFICADOS <");
                G4D_Util.printFullLine(archivoWriter, '*', dimLinea,8);
                posProducto = 0;
                tiempoAhorrado = 0.0;
                numProductos = pedido.getProductos().size();
                for(Producto producto : pedido.getProductos()) {
                    tiempoAhorrado += G4D_Util.calculateElapsedHours(producto.getFechaHoraLlegadaUTC(),producto.getFechaHoraLimiteUTC());
                    archivoWriter.printf("%10s PRODUCTO #%s  |  ORIGEN: %s  |  TIPO DE ENVIO:  %s  |  ENTREGA PLANIFICADA: %s%n",">>",String.format("%03d",posProducto+1),producto.getOrigen().getCodigo(),producto.getRuta().getTipo(),G4D_Util.toDisplayString(producto.getFechaHoraLlegadaUTC()));
                    archivoWriter.println();
                    archivoWriter.printf("%46s %29s %22s%n","ORIGEN","DESTINO","TRANSCURRIDO");
                    if(producto.getRuta() != null) {
                        for(Vuelo vuelo: producto.getRuta().getVuelos()) {
                            archivoWriter.printf(
                                "%36s  %s  -->  %s  %s  ==  %.2f hrs.%n",
                                vuelo.getPlan().getOrigen().getCodigo(),
                                G4D_Util.toDisplayString(vuelo.getFechaHoraSalidaUTC()),
                                vuelo.getPlan().getDestino().getCodigo(),
                                G4D_Util.toDisplayString(vuelo.getFechaHoraLlegadaUTC()),
                                vuelo.getDuracion()
                            );
                        }
                    }
                    G4D_Util.printFullLine(archivoWriter, '.', dimLinea, 8);
                    archivoWriter.printf("%27s%n","Resumen de la ruta:");
                    archivoWriter.printf("%31s %.2f hrs.%n",">> Duración de la ruta:",producto.getRuta().getDuracion());
                    if(posProducto != numProductos - 1) G4D_Util.printFullLine(archivoWriter, '*', dimLinea, 8);
                    posProducto++;
                }
                G4D_Util.printFullLine(archivoWriter, '-', dimLinea,4);
                archivoWriter.printf("%23s%n","Resumen del pedido:");
                archivoWriter.printf("%25s %.2f hrs.%n",">> Tiempo optimizado:",tiempoAhorrado);
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
}
