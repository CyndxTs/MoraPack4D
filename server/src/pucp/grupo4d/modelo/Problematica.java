/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Problematica.java 
[**/

package pucp.grupo4d.modelo;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import pucp.grupo4d.util.G4D_Formatter;

public class Problematica {
    public static final Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL = 2;
    public static final Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL = 3;
    public static final Double MAX_HORAS_RECOJO = 2.0;
    private Map<String, Aeropuerto> sedes;
    private List<Aeropuerto> aeropuertos;
    private List<PlanDeVuelo> planes;
    private List<Pedido> pedidos;

    public Problematica() {
        this.sedes = new HashMap<>();
        this.aeropuertos = new ArrayList<>();
        this.planes = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }
    // Carga de elementos desde archivos
    public void cargarDatos(String rutaArchivoAeropuertos, String rutaArchivoVuelos, String rutaArchivoPedidos) {
        cargarSedes(null);
        cargarAeropuertos(rutaArchivoAeropuertos);
        cargarPlanesDeVuelos(rutaArchivoVuelos);
        cargarPedidos(rutaArchivoPedidos);
    }
    // Carga de datos de sedes
    private void cargarSedes(String rutaArchivo) {
        this.sedes.put("SPIM", null);
        this.sedes.put("EBCI", null);
        this.sedes.put("UBBB", null);
    }
    // Carga de datos de Aeropuertos
    private void cargarAeropuertos(String rutaArchivo) {
        // Declaracion de variables
        String continente = "",linea;
        File archivo;
        Scanner archivoSC = null,lineaSC;
        Aeropuerto aeropuerto;
        // Carga de datos
        try {
            System.out.println("Leyendo archivo de 'Aeropuertos' desde '" + rutaArchivo + "'..");
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo,G4D_Formatter.getFileCharset(archivo));
            // Descarte de cabezera
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            // Iterativa de lectura y carga
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                if(linea.isEmpty()) continue; // Validacion por linea vacia
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
                    if (sedes.containsKey(aeropuerto.getCodigo())) sedes.put(aeropuerto.getCodigo(), aeropuerto);
                    aeropuertos.add(aeropuerto);
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            System.out.print("Se cargaron " + aeropuertos.size() + " aeropuertos.");
            System.out.println(" (" + sedes.values().stream().filter(Objects::nonNull).count() + " de " + sedes.size() + " sedes cargadas)");
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: No se encontró el archivo en la ruta '" + rutaArchivo + "'");
            System.exit(1);
        } catch (NullPointerException e) {
            System.err.println("ERROR: Se proporciono una ruta vacia." + e.getMessage());
            System.exit(1);
        } catch (NoSuchElementException e) {
            System.err.println("ERROR: Formato del archivo inválido. " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
    }
    // Cargas de datos de vuelos
    private void cargarPlanesDeVuelos(String rutaArchivo) {
        // Declaracion de variables
        String linea,codigoOrigen,codigoDestino;
        File archivo;
        Scanner archivoSC = null,lineaSC;
        PlanDeVuelo plan;
        // Carga de datos
        try {
            System.out.println("Leyendo archivo de 'Planes' desde '" + rutaArchivo + "'..");
            // Inicializaion del archivo y scanner
            archivo = new File(rutaArchivo);
            archivoSC = new Scanner(archivo,G4D_Formatter.getFileCharset(archivo));
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
                plan.setHoraSalida(G4D_Formatter.toTime(lineaSC.next()));
                plan.setHoraLlegada(G4D_Formatter.toTime(lineaSC.next()));
                plan.setCapacidadMaxima(lineaSC.nextInt());
                planes.add(plan);
                lineaSC.close();
            }
            System.out.println("Se cargaron " + planes.size() + " planes de vuelo.");
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: No se encontró el archivo de vuelos en la ruta '" + rutaArchivo + "'");
            System.exit(1);
        } catch (NullPointerException e) {
            System.err.println("ERROR: Se proporciono una ruta vacia." + e.getMessage());
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
        System.out.println("Generando pedidos..");
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            Cliente cliente = new Cliente();
            cliente.setNombre("Cli_" + (i + 1));
            int numPedidos = 1 + random.nextInt(5);
            for(int j = 0;j < numPedidos;j++) {
                Pedido pedido = new Pedido();
                pedido.setCliente(cliente);
                pedido.setDestino(aeropuertos.get(random.nextInt(aeropuertos.size())));
                LocalDateTime instanteCreacion = LocalDateTime.of(
                    LocalDate.now().plusDays(random.nextInt(5)).minusDays(random.nextInt(5)),
                    LocalTime.of(random.nextInt(24),random.nextInt(60),0)
                );
                pedido.setFechaHoraCreacion(instanteCreacion);
                int numProductos = 20 + random.nextInt(50);
                pedido.setCantidad(numProductos);
                for(int k = 0;k < numProductos;k++) {
                    Producto producto = new Producto();
                    producto.setDestino(pedido.getDestino());
                    pedido.getProductos().add(producto);
                }
                pedidos.add(pedido);
            }
        }
        this.pedidos.sort(Comparator.comparing(Pedido::getFechaHoraCreacion));
        Integer numProd = 0;
        for(Pedido p : pedidos) numProd += p.getProductos().size();
        System.out.println("Se cargaron " + pedidos.size() + " pedidos. (" + numProd + " productos)");
    }
    //
    private Aeropuerto buscarAeropuertoPorCodigo(String codigo) {
        for (Aeropuerto a : aeropuertos) {
            if (a.getCodigo().equalsIgnoreCase(codigo)) {
                return a;
            }
        }
        return null;
    }

    public List<Aeropuerto> getSedes() {
        return new ArrayList<>(sedes.values());
    }

    public List<Aeropuerto> getAeropuertos() {
        return aeropuertos;
    }

    public void setAeropuertos(List<Aeropuerto> aeropuertos) {
        this.aeropuertos = aeropuertos;
    }

    public List<PlanDeVuelo> getPlanes() {
        return planes;
    }

    public void setPlanes(List<PlanDeVuelo> planes) {
        this.planes = planes;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
}
