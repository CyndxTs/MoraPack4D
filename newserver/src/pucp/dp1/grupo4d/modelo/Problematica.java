/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Problematica.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import pucp.dp1.grupo4d.util.G4D;

public class Problematica {
    public static final Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL = 2;
    public static final Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL = 3;
    public static final Double MAX_HORAS_RECOJO = 2.0;
    public static final Double MIN_HORAS_ESTANCIA = 1.0;
    public static final Double MAX_HORAS_ESTANCIA = 12.0;
    public static final Map<String, Aeropuerto> origenes = new HashMap<>();
    public List<Aeropuerto> destinos;
    public List<PlanDeVuelo> planes;
    public List<Pedido> pedidos;

    static {
        origenes.put("SPIM", null);
        origenes.put("EBCI", null);
        origenes.put("UBBB", null);
    }

    public Problematica() {
        this.destinos = new ArrayList<>();
        this.planes = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }


    // Carga de datos para problematica
    public void cargarDatos(String rutaArchivoAeropuertos, String rutaArchivoVuelos, String rutaArchivoPedidos) {
        cargarAeropuertos(rutaArchivoAeropuertos);
        cargarPlanesDeVuelo(rutaArchivoVuelos);
        if(rutaArchivoPedidos == null) cargarPedidos(generarPedidos());
        else cargarPedidos(rutaArchivoPedidos);
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
        } catch (FileNotFoundException e) {
            G4D.Logger.logf_err("ERROR: No se encontró el archivo en la ruta '%s'%n",rutaArchivo);
            System.exit(1);
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
        } catch (FileNotFoundException e) {
            G4D.Logger.logln_err("ERROR: No se encontró el archivo de 'PlanesDeVuelo' en la ruta '" + rutaArchivo + "'");
            System.exit(1);
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
    //
    private String generarPedidos() {
        G4D.Logger.logln("Generando archivo de 'Pedidos'..");
        // Declaracion de variables
        String rutaArchivo = "Pedidos.txt";
        Random random = new Random();
        int minPed = 350,maxPed = 400, ped_minNumProd = 950,ped_maxNumProd = 999, ped_maxNumCli = 100;
        // Generando archivo
        try {
            // Inicializaion del archivo y scanner
            FileWriter archivo = new FileWriter(rutaArchivo);
            PrintWriter archivoWriter = new PrintWriter(archivo);
            //
            for(int i = 0,cantPed = random.nextInt(minPed,maxPed);i < cantPed;i++) {
                int numProd = random.nextInt(ped_minNumProd,ped_maxNumProd);
                int numCli = 1 + random.nextInt(ped_maxNumCli);
                String destino = this.destinos.get(random.nextInt(this.destinos.size())).getCodigo();
                LocalDateTime fechaHoraCreacion = LocalDateTime.of(
                    LocalDate.now().withDayOfMonth(1 + random.nextInt(LocalDate.now().lengthOfMonth())),
                    LocalTime.of(
                        random.nextInt(24),
                        random.nextInt(60),
                        0
                    )
                );
                archivoWriter.printf("%02d-%02d-%02d-%s-%03d-%07d%n",
                    fechaHoraCreacion.getDayOfMonth(),
                    fechaHoraCreacion.getHour(),
                    fechaHoraCreacion.getMinute(),
                    destino,
                    numProd,
                    numCli     
                );
            }
            archivoWriter.flush();
            archivoWriter.close();
            archivo.close();
            G4D.Logger.logf("Archivo de 'Pedidos' generado en la ruta '%s'.%n",rutaArchivo);
        } catch (Exception e){
            e.printStackTrace();
        }
        return rutaArchivo;
    }
    // // PROVISIONAL HASTA TENER FORMATO DEL ARCHIVO DE PEDIDOS
    private void cargarPedidos(String rutaArchivo) {
        G4D.Logger.logf("Leyendo archivo de 'Pedidos' desde '%s'..%n",rutaArchivo);
        // Declaracion de variables
        String linea;
        File archivo;
        Scanner archivoSC = null, lineaSC;
        // Carga de datos
        try {
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo, G4D.getFileCharset(archivo));
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                // Inicializacion de scanner de linea
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                Pedido pedido = new Pedido();
                LocalDateTime fechaHoraCreacionLocal = LocalDateTime.of(
                    LocalDate.now().withDayOfMonth(lineaSC.nextInt()),
                    LocalTime.of(
                        lineaSC.nextInt(),
                        lineaSC.nextInt(),
                        0
                    )
                );
                String codigoDestino = lineaSC.next();
                int numProd = lineaSC.nextInt();
                int codigoCli = lineaSC.nextInt();
                Cliente cliente = new Cliente(String.format("%07d",codigoCli));
                Aeropuerto destino = obtenerAeropuertoPorCodigo(codigoDestino);
                pedido.setDestino(destino);
                pedido.setFechaHoraCreacionLocal(fechaHoraCreacionLocal);
                pedido.setFechaHoraCreacionUTC(G4D.toUTC(fechaHoraCreacionLocal,destino.getHusoHorario()));
                pedido.setCantidad(numProd);
                pedido.setCliente(cliente);
                this.pedidos.add(pedido);
                lineaSC.close();
            }
        } catch (FileNotFoundException e) {
            G4D.Logger.logln_err("ERROR: No se encontró el archivo de 'Pedidos' en la ruta '" + rutaArchivo + "'");
            System.exit(1);
        } catch (NullPointerException e) {
            G4D.Logger.logln_err("ERROR: Se proporciono una ruta vacia." + e.getMessage());
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
