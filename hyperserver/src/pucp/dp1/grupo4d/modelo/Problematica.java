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
    public Map<String, Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<PlanDeVuelo> planes;
    public List<Cliente> clientes;
    public List<Pedido> pedidos;

    public Problematica() {
        this.origenes = new HashMap<>();
        this.origenes.put("SPIM", null);
        this.origenes.put("EBCI", null);
        this.origenes.put("UBBB", null);
        this.destinos = new ArrayList<>();
        this.planes = new ArrayList<>();
        this.clientes = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }

    // Carga de datos para problematica
    public void cargarDatos(String rutaArchivoAeropuertos, String rutaArchivoVuelos, String rutaArchivoClientes, String rutaArchivoPedidos) {
        cargarAeropuertos(rutaArchivoAeropuertos);
        cargarPlanesDeVuelo(rutaArchivoVuelos);
        cargarClientes((rutaArchivoClientes != null) ? rutaArchivoClientes : generarClientes());
        cargarPedidos((rutaArchivoPedidos != null) ?  rutaArchivoPedidos : generarPedidos());
    }
    // Carga de datos de Aeropuertos
    private void cargarAeropuertos(String rutaArchivo) {
        G4D.Logger.logf("Cargando aeropuertos desde '%s'..%n",rutaArchivo);
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
                    if (this.origenes.containsKey(aeropuerto.getCodigo())) {
                        this.origenes.put(aeropuerto.getCodigo(), aeropuerto);
                    } else this.destinos.add(aeropuerto);
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            G4D.Logger.logf("[<] AEROPUERTOS CARGADOS! ('%d' origenes | '%d' destinos)%n", this.origenes.size(), this.destinos.size());
        } catch (FileNotFoundException | NullPointerException e) {
            G4D.Logger.logf_err("[X] ARCHIVO DE 'AEROPUERTOS' NO ENCONTRADO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
    }
    // Cargas de datos de planes de vuelo
    private void cargarPlanesDeVuelo(String rutaArchivo) {
        G4D.Logger.logf("Cargando planes de vuelo desde '%s'..%n",rutaArchivo);
        // Declaracion de variables
        String linea;
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
                Aeropuerto aOrig = obtenerAeropuertoPorCodigo(lineaSC.next());
                if(aOrig != null) {
                    plan.setOrigen(aOrig);
                    Aeropuerto aDest = obtenerAeropuertoPorCodigo(lineaSC.next());
                    if(aDest != null) {
                        plan.setDestino(aDest);
                        plan.setDistancia();
                        plan.setHoraSalidaLocal(G4D.toTime(lineaSC.next()));
                        plan.setHoraSalidaUTC();
                        plan.setHoraLlegadaLocal(G4D.toTime(lineaSC.next()));
                        plan.setHoraLlegadaUTC();
                        plan.setCapacidad(lineaSC.nextInt());
                        this.planes.add(plan);
                    }
                }
                lineaSC.close();
            }
            G4D.Logger.logf("[<] PLANES DE VUELO CARGADOS! ('%d' planes)%n", this.planes.size());
        } catch (FileNotFoundException | NullPointerException e) {
            G4D.Logger.logf_err("[X] ARCHIVO DE 'PLANES DE VUELO' NO ENCONTRADO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
    }
    //
    private String generarClientes() {
        G4D.Logger.logln("Generando clientes en 'Clientes.txt'..");
        //
        String rutaArchivo = "Clientes.txt";
        int cantCli = 300;
        // Generando archivo
        try {
            // Inicializaion del archivo y scanner
            FileWriter archivo = new FileWriter(rutaArchivo);
            PrintWriter archivoWriter = new PrintWriter(archivo);
            //
            for(int i = 0; i < cantCli; i++) {
                int codigo = i + 1;
                String nombre = G4D.Generator.getUniqueName();
                String correo = G4D.Generator.getUniqueEmail();
                archivoWriter.printf("%07d    %-50s    %s%n",
                    codigo,
                    nombre,
                    correo 
                );
            }
            archivoWriter.flush();
            archivoWriter.close();
            archivo.close();
            G4D.Logger.logf("[>] ARCHIVO DE 'CLIENTES' GENERADO! (RUTA: '%s')%n", rutaArchivo);
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        } 
        return rutaArchivo;
    }
    //
    private void cargarClientes(String rutaArchivo) {
        G4D.Logger.logf("Cargando clientes desde '%s'..%n",rutaArchivo);
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
                lineaSC.useDelimiter("\\s{2,}"); 
                Cliente cliente = new Cliente();
                cliente.setCodigo(lineaSC.next());
                cliente.setNombre(lineaSC.next());
                cliente.setCorreo(lineaSC.next());
                this.clientes.add(cliente);
                lineaSC.close();
            }
        } catch (FileNotFoundException | NullPointerException e) {
            G4D.Logger.logf_err("[X] ARCHIVO DE 'CLIENTES' NO ENCONTRADO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
        G4D.Logger.logf("[<] CLIENTES CARGADOS! ('%d' clientes)%n", this.clientes.size());
    }
    //
    private String generarPedidos() {
        G4D.Logger.logln("Generando pedidos en 'Pedidos.txt'..");
        // Declaracion de variables
        String rutaArchivo = "Pedidos.txt";
        Random random = new Random();
        int minPed = 200,maxPed = 249, ped_minNumProd = 950,ped_maxNumProd = 999;
        // Generando archivo
        try {
            // Inicializaion del archivo y scanner
            FileWriter archivo = new FileWriter(rutaArchivo);
            PrintWriter archivoWriter = new PrintWriter(archivo);
            //
            for(int i = 0,cantPed = random.nextInt(minPed,maxPed);i < cantPed;i++) {
                int numProd = random.nextInt(ped_minNumProd,ped_maxNumProd);
                int numCli = 1 + random.nextInt(this.clientes.size());
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
            G4D.Logger.logf("[>] ARCHIVO DE 'PEDIDOS' GENERADO! (RUTA: '%s')%n", rutaArchivo);
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        } 
        return rutaArchivo;
    }
    //
    private void cargarPedidos(String rutaArchivo) {
        G4D.Logger.logf("Cargando pedidos desde '%s'..%n",rutaArchivo);
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
                Aeropuerto aDest = obtenerAeropuertoPorCodigo(lineaSC.next());
                pedido.setDestino(aDest);
                pedido.setCantidadDeProductosSolicitados(lineaSC.nextInt());
                Cliente cliente = obtenerClientePorCodigo(lineaSC.next());
                if(cliente != null) {
                    pedido.setCliente(cliente);
                    pedido.setFechaHoraGeneracionLocal(fechaHoraCreacionLocal);
                    pedido.setFechaHoraGeneracionUTC();
                    this.pedidos.add(pedido);
                }
                lineaSC.close();
            }
        } catch (FileNotFoundException | NullPointerException e) {
            G4D.Logger.logf_err("[X] ARCHIVO DE 'PEDIDOS' NO ENCONTRADO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
        this.pedidos.sort(Comparator.comparing(Pedido::getFechaHoraGeneracionUTC));
        G4D.Logger.Stats.totalPed = this.pedidos.size();
        for (Pedido p : this.pedidos) G4D.Logger.Stats.totalProd += p.getCantidadDeProductosSolicitados();
        G4D.Logger.logf("[<] PEDIDOS CARGADOS! ('%d' pedidos -> '%d' productos)%n", G4D.Logger.Stats.totalPed, G4D.Logger.Stats.totalProd);
    }
    //
    private Aeropuerto obtenerAeropuertoPorCodigo(String codigo) {
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
    //
    private Cliente obtenerClientePorCodigo(String codigo) {
        for(Cliente cli : this.clientes) {
            if(cli.getCodigo().equals(codigo)) {
                return cli;
            }
        }
        return null;
    }
}
