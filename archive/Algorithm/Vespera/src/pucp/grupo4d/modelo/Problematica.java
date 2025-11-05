/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Problematica.java 
[**/

package pucp.grupo4d.modelo;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import pucp.grupo4d.util.G4D_Util;

public class Problematica {
    public static final Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL = 2;
    public static final Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL = 3;
    public static final Double MAX_HORAS_RECOJO = 2.0;
    public static final Double MIN_HORAS_ESTANCIA = 0.5;
    public Map<String, Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<PlanDeVuelo> planes;
    public List<Pedido> pedidos;

    public Problematica() {
        this.origenes = new HashMap<>();
        this.origenes.put("SPIM", null);
        this.origenes.put("EBCI", null);
        this.origenes.put("UBBB", null);
        this.destinos = new ArrayList<>();
        this.planes = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }
    // Carga de datos para problematica
    public void cargarDatos(String rutaArchivoAeropuertos, String rutaArchivoVuelos, String rutaArchivoPedidos) {
        cargarAeropuertos(rutaArchivoAeropuertos);
        cargarPlanesDeVuelo(rutaArchivoVuelos);
        cargarPedidos(rutaArchivoPedidos);
    }
    // Carga de datos de Aeropuertos
    private void cargarAeropuertos(String rutaArchivo) {
        G4D_Util.Logger.logf("Leyendo archivo de 'Aeropuertos' desde '%s'..%n",rutaArchivo);
        // Declaracion de variables
        String continente = "", linea;
        File archivo;
        Scanner archivoSC = null, lineaSC;
        Aeropuerto aeropuerto;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo, G4D_Util.getFileCharset(archivo));
            // Descarte de cabezera
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue; // Validacion por linea vacia
                // Inicializacion de scanner de linea
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}"); // Configuracion de separador a "2 espacios"
                // Validacion por tipo de linea a partir de primer caracter
                if (Character.isDigit(linea.charAt(0))) {
                    aeropuerto = new Aeropuerto();
                    aeropuerto.setId(lineaSC.next());
                    aeropuerto.setCodigo(lineaSC.next());
                    aeropuerto.setCiudad(lineaSC.next());
                    aeropuerto.setPais(lineaSC.next());
                    aeropuerto.setContinente(continente);
                    aeropuerto.setAlias(lineaSC.next());
                    aeropuerto.setHusoHorario(lineaSC.nextInt());
                    aeropuerto.setCapacidadMaxima(lineaSC.nextInt());
                    lineaSC.useDelimiter("\\s+");
                    lineaSC.next();
                    aeropuerto.setLatitud(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    lineaSC.next();
                    aeropuerto.setLongitud(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    if (origenes.containsKey(aeropuerto.getCodigo())) origenes.put(aeropuerto.getCodigo(), aeropuerto);
                    else destinos.add(aeropuerto);
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            G4D_Util.Logger.logf("Se cargaron correctamente '%d' origenes y '%d' destinos.%n",origenes.size(),destinos.size());
        } catch (NullPointerException e) {
            G4D_Util.Logger.logln_err("ERROR: Se proporciono una ruta vacia." + e.getMessage());
            System.exit(1);
        } catch (NoSuchElementException e) {
            G4D_Util.Logger.logln_err("ERROR: Formato del archivo inválido. " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
    }
    // Cargas de datos de planes de vuelo
    private void cargarPlanesDeVuelo(String rutaArchivo) {
        G4D_Util.Logger.logf("Leyendo archivo de 'PlanesDeVuelo' desde '%s'..%n",rutaArchivo);
        // Declaracion de variables
        String linea, codigoOrigen, codigoDestino;
        File archivo;
        Scanner archivoSC = null, lineaSC;
        PlanDeVuelo plan;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo, G4D_Util.getFileCharset(archivo));
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue;
                // Inicializacion de scanner de linea
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                plan = new PlanDeVuelo();
                codigoOrigen = lineaSC.next();
                plan.setOrigen(buscarAeropuertoPorCodigo(codigoOrigen));
                codigoDestino = lineaSC.next();
                plan.setDestino(buscarAeropuertoPorCodigo(codigoDestino));
                plan.setHoraSalida(G4D_Util.toTime(lineaSC.next()));
                plan.setHoraLlegada(G4D_Util.toTime(lineaSC.next()));
                plan.setCapacidad(lineaSC.nextInt());
                plan.setDuracion();
                plan.setDistancia();
                planes.add(plan);
                lineaSC.close();
            }
            G4D_Util.Logger.logf("Se cargaron %d planes de vuelo.%n",planes.size());
        } catch (NullPointerException e) {
            G4D_Util.Logger.logln_err("ERROR: Se proporciono una ruta vacia." + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
    }
    // // PROVISIONAL HASTA TENER FORMATO DEL ARCHIVO DE PEDIDOS
    private void cargarPedidos(String rutaArchivo) {
        G4D_Util.Logger.logf("Leyendo archivo de 'Pedidos' desde '%s'..%n",rutaArchivo);
        // Declaracion de variables
        File archivo;
        Scanner archivoSC = null;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo, G4D_Util.getFileCharset(archivo));
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                Pedido pedido = new Pedido();
                String numPed = lineaSC.next();
                LocalDateTime fechaHoraGeneracionLocal = LocalDateTime.of(
                    G4D_Util.toDate(lineaSC.nextInt()),
                    LocalTime.of(
                        lineaSC.nextInt(), lineaSC.nextInt(), 0
                    )
                );
                Aeropuerto aDest = buscarAeropuertoPorCodigo(lineaSC.next());
                if(aDest != null) {
                    LocalDateTime fechaHoraGeneracionUTC = G4D_Util.toUTC(fechaHoraGeneracionLocal, aDest.getHusoHorario());
                    pedido.setId(aDest.getCodigo() + numPed);
                    pedido.setDestino(aDest);
                    pedido.setCantidad(lineaSC.nextInt());
                    pedido.setClienteId(lineaSC.nextInt());
                    pedido.setFechaHoraCreacionLocal(fechaHoraGeneracionLocal);
                    pedido.setFechaHoraCreacionUTC(fechaHoraGeneracionUTC);
                    this.pedidos.add(pedido);
                }
                lineaSC.close();
            }
        } catch (NullPointerException e) {
            G4D_Util.Logger.logln_err("ERROR: Se proporciono una ruta vacia." + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
        this.pedidos.sort(Comparator.comparing(Pedido::getFechaHoraCreacionUTC));
        Integer numProd = 0;
        for (Pedido p : pedidos) numProd += p.getCantidad();
        G4D_Util.Logger.logf("Se cargaron %d pedidos. (%d productos)%n",pedidos.size(),numProd);
    }
    //
    private Aeropuerto buscarAeropuertoPorCodigo(String codigo) {
        for (Aeropuerto orig : this.origenes.values()) {
            if (orig.getCodigo().compareTo(codigo) == 0) {
                return orig;
            }
        }
        for (Aeropuerto dest : this.destinos) {
            if (dest.getCodigo().compareTo(codigo) == 0) {
                return dest;
            }
        }
        return null;
    }
}
