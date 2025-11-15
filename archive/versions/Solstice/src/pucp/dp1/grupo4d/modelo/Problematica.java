/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Problematica.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import pucp.dp1.grupo4d.util.G4D;

public class Problematica {
    public static Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL = 2;
    public static Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL = 3;
    public static Double MAX_HORAS_RECOJO = 2.0;
    public static Double MIN_HORAS_ESTANCIA = 1.0;
    public static Double MAX_HORAS_ESTANCIA = 12.0;
    public Map<String, Aeropuerto> origenes = new HashMap<>();
    public List<Aeropuerto> destinos;
    public List<PlanDeVuelo> planes;
    public List<Pedido> pedidos;
    
    public Problematica() {
        this.origenes = new HashMap<>();
        origenes.put("SPIM", null);
        origenes.put("EBCI", null);
        origenes.put("UBBB", null);
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
        G4D.Logger.logf("Leyendo archivo de 'Aeropuertos' desde '%s'..%n",rutaArchivo);
        // Declaracion de variables
        String continente = "", linea;
        File archivo;
        Scanner archivoSC = null, lineaSC;
        Aeropuerto aeropuerto;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo, G4D.getFileCharset(archivo));
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
                    aeropuerto.setCapacidad(lineaSC.nextInt());
                    lineaSC.useDelimiter("\\s+");
                    lineaSC.next();
                    aeropuerto.setLatitudDMS(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    aeropuerto.setLatitudDEC();
                    lineaSC.next();
                    aeropuerto.setLongitudDMS(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    aeropuerto.setLongitudDEC();
                    if (origenes.containsKey(aeropuerto.getCodigo())) {
                        origenes.put(aeropuerto.getCodigo(), aeropuerto);
                    } else destinos.add(aeropuerto);
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            G4D.Logger.logf("Se cargaron correctamente '%d' origenes y '%d' destinos.%n",origenes.size(),destinos.size());
        } catch (NullPointerException e) {
            G4D.Logger.logln_err("ERROR: Se proporciono una ruta vacia." + e.getMessage());
            System.exit(1);
        } catch (NoSuchElementException e) {
            G4D.Logger.logln_err("ERROR: Formato del archivo inválido. " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
    }
    // Cargas de datos de planes de vuelo
    private void cargarPlanesDeVuelo(String rutaArchivo) {
        G4D.Logger.logf("Leyendo archivo de 'PlanesDeVuelo' desde '%s'..%n",rutaArchivo);
        // Declaracion de variables
        String linea, codigoOrigen, codigoDestino;
        File archivo;
        Scanner archivoSC = null, lineaSC;
        PlanDeVuelo plan;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo, G4D.getFileCharset(archivo));
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue;
                // Inicializacion de scanner de linea
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                plan = new PlanDeVuelo();
                codigoOrigen = lineaSC.next();
                plan.setOrigen(obtenerAeropuertoPorCodigo(codigoOrigen));
                codigoDestino = lineaSC.next();
                plan.setDestino(obtenerAeropuertoPorCodigo(codigoDestino));
                plan.setHoraSalidaLocal(G4D.toTime(lineaSC.next()));
                plan.setHoraSalidaUTC();
                plan.setHoraLlegadaLocal(G4D.toTime(lineaSC.next()));
                plan.setHoraLlegadaUTC();
                plan.setCapacidad(lineaSC.nextInt());
                plan.setDistancia();
                planes.add(plan);
                lineaSC.close();
            }
            G4D.Logger.logf("Se cargaron %d planes de vuelo.%n",planes.size());
        } catch (NullPointerException e) {
            G4D.Logger.logln_err("ERROR: Se proporciono una ruta vacia." + e.getMessage());
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
        G4D.Logger.logf("Leyendo archivo de 'Pedidos' desde '%s'..%n",rutaArchivo);
        // Declaracion de variables
        File archivo;
        Scanner archivoSC = null;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo, G4D.getFileCharset(archivo));
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                Pedido pedido = new Pedido();
                String numPed = lineaSC.next();
                LocalDateTime fechaHoraGeneracionLocal = LocalDateTime.of(
                    G4D.toDate(lineaSC.nextInt()),
                    LocalTime.of(
                        lineaSC.nextInt(), lineaSC.nextInt(), 0
                    )
                );
                Aeropuerto aDest = obtenerAeropuertoPorCodigo(lineaSC.next());
                if(aDest != null) {
                    LocalDateTime fechaHoraGeneracionUTC = G4D.toUTC(fechaHoraGeneracionLocal, aDest.getHusoHorario());
                    pedido.setId(aDest.getCodigo() + numPed);
                    pedido.setDestino(aDest);
                    pedido.setCantidad(lineaSC.nextInt());
                    String codCli = lineaSC.next();
                    Cliente cliente = new Cliente();
                    cliente.setCodigo(codCli);
                    pedido.setCliente(cliente);
                    pedido.setFechaHoraGeneracionLocal(fechaHoraGeneracionLocal);
                    pedido.setFechaHoraGeneracionUTC(fechaHoraGeneracionUTC);
                    this.pedidos.add(pedido);
                }
                lineaSC.close();
            }
        } catch (NullPointerException e) {
            G4D.Logger.logln_err("ERROR: Se proporciono una ruta vacia." + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
        this.pedidos.sort(Comparator.comparing(Pedido::getFechaHoraGeneracionUTC));
        Integer numProd = 0;
        for (Pedido p : pedidos) numProd += p.getCantidad();
        G4D.Logger.logf("Se cargaron %d pedidos. (%d productos)%n",pedidos.size(),numProd);
    }
    //
    private Aeropuerto obtenerAeropuertoPorCodigo(String codigo) {
        for (Aeropuerto orig : origenes.values()) {
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
