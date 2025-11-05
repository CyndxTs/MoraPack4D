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
    public Map<String, Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<Plan> planes;
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
        cargarClientes(rutaArchivoClientes);
        cargarPedidos(rutaArchivoPedidos);
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
                    aeropuerto.setId(lineaSC.nextInt());
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
        Plan plan;
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
                plan = new Plan();
                Aeropuerto aOrig = obtenerAeropuertoPorCodigo(lineaSC.next());
                if(aOrig != null) {
                    Aeropuerto aDest = obtenerAeropuertoPorCodigo(lineaSC.next());
                    if(aDest != null) {
                        plan.setOrigen(aOrig);
                        plan.setDestino(aDest);
                        plan.setDistancia();
                        plan.setHoraSalidaLocal(G4D.toTime(lineaSC.next()));
                        plan.setHoraSalidaUTC();
                        plan.setHoraLlegadaLocal(G4D.toTime(lineaSC.next()));
                        plan.setHoraLlegadaUTC();
                        plan.setDuracion();
                        plan.setCapacidad(lineaSC.nextInt());
                        this.planes.add(plan);
                    }
                }
                lineaSC.close();
            }
            G4D.Logger.logf("[<] PLANES DE VUELO CARGADOS! ('%d' planes)%n", this.planes.size());
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
    private void cargarPedidos(String rutaArchivo) {
        G4D.Logger.logf("Cargando pedidos desde '%s'..%n",rutaArchivo);
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
                    pedido.setCodigo(aDest.getCodigo() + numPed);
                    pedido.setDestino(aDest);
                    pedido.setCantidadSolicitada(lineaSC.nextInt());
                    String codCli = lineaSC.next();
                    Cliente cliente = obtenerClientePorCodigo(codCli);
                    if(cliente == null) {
                        cliente = new Cliente();
                        cliente.setCodigo(codCli);
                        cliente.setNombre(G4D.Generator.getUniqueName());
                        cliente.setCorreo(G4D.Generator.getUniqueEmail());
                        cliente.setContrasenia("12345678");
                        this.clientes.add(cliente);
                    }
                    pedido.setCliente(cliente);
                    pedido.setFechaHoraGeneracionLocal(fechaHoraGeneracionLocal);
                    pedido.setFechaHoraGeneracionUTC(fechaHoraGeneracionUTC);
                    this.pedidos.add(pedido);
                }
                lineaSC.close();
            }
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
        for (Pedido p : this.pedidos) G4D.Logger.Stats.totalProd += p.getCantidadSolicitada();
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
