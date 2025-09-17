package pucp.grupo4d.modelo;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import pucp.grupo4d.util.G4D_Formatter;

public class Problematica {
    private Map<String, Aeropuerto> sedes;
    private List<Aeropuerto> aeropuertos;
    private List<Vuelo> vuelos;
    private List<Pedido> pedidos;

    public Problematica() {
        this.sedes = new HashMap<>();
        this.aeropuertos = new ArrayList<>();
        this.vuelos = new ArrayList<>();
        this.pedidos = new ArrayList<>();
    }
    //
    public void cargarDatos(String rutaArchivoAeropuertos, String rutaArchivoVuelos, String rutaArchivoPedidos) {
        cargarSedes(null);
        cargarAeropuertos(rutaArchivoAeropuertos);
        cargarVuelos(rutaArchivoVuelos);
        cargarPedidos(rutaArchivoPedidos);
    }
    //
    private void cargarSedes(String rutaArchivo) {
        this.sedes.put("SPIM", null);
        this.sedes.put("EBCI", null);
        this.sedes.put("UBBB", null);
    }
    //
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
                    aeropuerto.setId(lineaSC.nextInt());
                    aeropuerto.setCodigo(lineaSC.next());
                    aeropuerto.setCiudad(lineaSC.next());
                    aeropuerto.setPais(lineaSC.next());
                    aeropuerto.setContinente(continente);
                    aeropuerto.setAlias(lineaSC.next());
                    aeropuerto.setHusoHorario(lineaSC.nextInt());
                    aeropuerto.setCapacidadTotal(lineaSC.nextInt());
                    aeropuerto.setCapacidadDisponible(aeropuerto.getCapacidadTotal());
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
    //
    private void cargarVuelos(String rutaArchivo) {
        // Declaracion de variables
        int id = 1;
        String linea,codigoOrigen,codigoDestino;
        File archivo;
        Scanner archivoSC = null,lineaSC;
        Vuelo vuelo;
        // Carga de datos
        try {
            System.out.println("Leyendo archivo de 'Vuelos' desde '" + rutaArchivo + "'..");
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
                vuelo = new Vuelo();
                vuelo.setId(id++);
                codigoOrigen = lineaSC.next();
                vuelo.setOrigen(buscarAeropuertoPorCodigo(codigoOrigen));
                codigoDestino = lineaSC.next();
                vuelo.setDestino(buscarAeropuertoPorCodigo(codigoDestino));
                vuelo.setHoraSalida(lineaSC.next());
                vuelo.setHoraLlegada(lineaSC.next());
                vuelo.setCapacidadTotal(lineaSC.nextInt());
                vuelo.setCapacidadDisponible(vuelo.getCapacidadTotal());
                vuelo.setDuracion();
                vuelos.add(vuelo);
                lineaSC.close();
            }
            System.out.println("Se cargaron " + vuelos.size() + " planes de vuelo.");
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
        for (int i = 0; i < 3; i++) {
            Cliente cliente = new Cliente();
            cliente.setId(i+1);
            cliente.setNombre("Cli_" + (i + 1));
            int numPedidos = 1 + random.nextInt(3);
            for(int j = 0;j < numPedidos;j++) {
                Pedido pedido = new Pedido();
                pedido.setId(j + 1);
                pedido.setCliente(cliente);
                pedido.setDestino(aeropuertos.get(random.nextInt(aeropuertos.size())));
                LocalDateTime ldt_instanteCreacion = LocalDateTime.of(
                    LocalDate.now().plusDays(random.nextInt(3)).minusDays(random.nextInt(3)),
                    LocalTime.of(random.nextInt(24),random.nextInt(60),random.nextInt(60))
                );
                pedido.setInstanteCreacion(G4D_Formatter.toDateTimeString(ldt_instanteCreacion));
                int numProductos = 1 + random.nextInt(3);
                pedido.setCantidad(numProductos);
                for(int k = 0;k < numProductos;k++) {
                    Producto producto = new Producto();
                    producto.setId(k+1);
                    producto.setPedido(pedido);
                    producto.setDestino(pedido.getDestino());
                    pedido.getProductos().add(producto);
                }
                pedidos.add(pedido);
            }
        }
        System.out.println("Se cargaron " + pedidos.size() + " pedidos.");
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

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void setVuelos(List<Vuelo> vuelos) {
        this.vuelos = vuelos;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
}
