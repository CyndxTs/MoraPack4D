/**]
 >> Project:    MoraPack
 >> Version:    Aether
 >> Author:     Grupo 4D
 >> File:       Problematica.java 
[**/

package pucp.dp1.grupo4d.algorithm.version.aether;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import pucp.dp1.grupo4d.algorithm.version.aether.model.Aeropuerto;
import pucp.dp1.grupo4d.algorithm.version.aether.model.Cliente;
import pucp.dp1.grupo4d.algorithm.version.aether.model.Lote;
import pucp.dp1.grupo4d.algorithm.version.aether.model.Pedido;
import pucp.dp1.grupo4d.algorithm.version.aether.model.Plan;
import pucp.dp1.grupo4d.algorithm.version.aether.model.Ruta;
import pucp.dp1.grupo4d.algorithm.version.aether.model.Vuelo;
import pucp.dp1.grupo4d.util.G4D;

public class Problematica {
    public static Integer MAX_DIAS_ENTREGA_INTRACONTINENTAL = 2;
    public static Integer MAX_DIAS_ENTREGA_INTERCONTINENTAL = 3;
    public static Double MAX_HORAS_RECOJO = 2.0;
    public static Double MIN_HORAS_ESTANCIA = 1.0;
    public static Double MAX_HORAS_ESTANCIA = 12.0;
    public static List<String> CODIGOS_DE_ORIGENES = List.of("SPIM", "EBCI", "UBBB");
    public static LocalDateTime FECHA_HORA_INICIO = LocalDateTime.now().withYear(1999);
    public static LocalDateTime FECHA_HORA_FIN = LocalDateTime.now();
    public static LocalDateTime FECHA_HORA_LIMITE_REPLANIFICACION = LocalDateTime.now().withYear(1999);
    public List<Aeropuerto> origenes;
    public List<Aeropuerto> destinos;
    public List<Plan> planes;
    public List<Cliente> clientes;
    public List<Pedido> pedidos;
    public Set<Vuelo> vuelosEnTransito;
    public Set<Ruta> rutasEnOperacion;

    public Problematica() {
        this.origenes = new ArrayList<>();
        this.destinos = new ArrayList<>();
        this.planes = new ArrayList<>();
        this.clientes = new ArrayList<>();
        this.pedidos = new ArrayList<>();
        this.vuelosEnTransito = new HashSet<>();
        this.rutasEnOperacion = new HashSet<>();
    }

    public Problematica(Problematica problematica) {
        this.reasignar(problematica);
    }
    
    public Problematica replicar() {
        Problematica problematica = new Problematica();
        Map<String, Aeropuerto> poolAeropuertos = new HashMap<>();
        Map<String, Lote> poolLotes = new HashMap<>();
        Map<String, Cliente> poolClientes = new HashMap<>();
        Map<String, Plan> poolPlanes = new HashMap<>();
        Map<String, Ruta> poolRutas = new HashMap<>();
        Map<String, Vuelo> poolVuelos = new HashMap<>();
        for(Aeropuerto orig : this.origenes) problematica.origenes.add(poolAeropuertos.computeIfAbsent(orig.getCodigo(), codigo -> orig.replicar(poolLotes)));
        for(Aeropuerto dest : this.destinos) problematica.destinos.add(poolAeropuertos.computeIfAbsent(dest.getCodigo(), codigo -> dest.replicar(poolLotes)));
        for(Plan plan : this.planes) problematica.planes.add(poolPlanes.computeIfAbsent(plan.getCodigo(), codigo -> plan.replicar(poolAeropuertos, poolLotes)));
        for(Cliente cli : this.clientes) problematica.clientes.add(poolClientes.computeIfAbsent(cli.getCodigo(), codigo -> cli.replicar()));
        for(Pedido ped : this.pedidos) problematica.pedidos.add(ped.replicar(poolClientes, poolAeropuertos, poolLotes, poolRutas, poolVuelos, poolPlanes));
        for(Vuelo vue : this.vuelosEnTransito) problematica.vuelosEnTransito.add(poolVuelos.computeIfAbsent(vue.getCodigo(), codigo -> vue.replicar(poolAeropuertos, poolLotes, poolPlanes)));
        for(Ruta rut : this.rutasEnOperacion) problematica.rutasEnOperacion.add(poolRutas.computeIfAbsent(rut.getCodigo(), codigo -> rut.replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes)));
        return problematica;
    }

    public void reasignar(Problematica problematica) {
        this.clientes = new ArrayList<>(problematica.clientes);
        this.destinos = new ArrayList<>(problematica.destinos);
        this.origenes = new ArrayList<>(problematica.origenes);
        this.planes = new ArrayList<>(problematica.planes);
        this.pedidos = new ArrayList<>(problematica.pedidos);
        this.rutasEnOperacion = new HashSet<>(problematica.rutasEnOperacion);
        this.vuelosEnTransito = new HashSet<>(problematica.vuelosEnTransito);
    }

    public void cargarDatos(String rutaArchivoAeropuertos, String rutaArchivoVuelos, String rutaArchivoClientes, String rutaArchivoPedidos) {
        cargarAeropuertos(rutaArchivoAeropuertos);
        cargarPlanesDeVuelo(rutaArchivoVuelos);
        cargarClientes(rutaArchivoClientes);
        cargarPedidos(rutaArchivoPedidos);
    }

    private void cargarAeropuertos(String rutaArchivo) {
        G4D.Logger.logf("Cargando aeropuertos desde '%s'..%n",rutaArchivo);
        try {
            String continente = "";
            File archivo = new File(rutaArchivo);
            Scanner archivoSC = new Scanner(archivo, G4D.getFileCharset(archivo));
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue;
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                if (Character.isDigit(linea.charAt(0))) {
                    Aeropuerto aeropuerto = new Aeropuerto();
                    lineaSC.nextInt();
                    aeropuerto.setCodigo(lineaSC.next());
                    aeropuerto.setCiudad(lineaSC.next());
                    aeropuerto.setPais(lineaSC.next());
                    aeropuerto.setContinente(continente);
                    lineaSC.next();
                    aeropuerto.setHusoHorario(lineaSC.nextInt());
                    aeropuerto.setCapacidad(lineaSC.nextInt());
                    lineaSC.useDelimiter("\\s+");
                    lineaSC.next();
                    aeropuerto.setLatitud(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    lineaSC.next();
                    aeropuerto.setLongitud(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    if (CODIGOS_DE_ORIGENES.contains(aeropuerto.getCodigo())) {
                        this.origenes.add(aeropuerto);
                    } else this.destinos.add(aeropuerto);
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            archivoSC.close();
            G4D.Logger.logf("[<] AEROPUERTOS CARGADOS! ('%d' origenes | '%d' destinos)%n", this.origenes.size(), this.destinos.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void cargarPlanesDeVuelo(String rutaArchivo) {
        G4D.Logger.logf("Cargando planes de vuelo desde '%s'..%n",rutaArchivo);
        try {
            File archivo = new File(rutaArchivo);
            Scanner archivoSC = new Scanner(archivo, G4D.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue;
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                Plan plan = new Plan();
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
            archivoSC.close();
            G4D.Logger.logf("[<] PLANES DE VUELO CARGADOS! ('%d' planes)%n", this.planes.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void cargarClientes(String rutaArchivo) {
        G4D.Logger.logf("Cargando clientes desde '%s'..%n",rutaArchivo);
        try {
            File archivo = new File(rutaArchivo);
            Scanner archivoSC = new Scanner(archivo, G4D.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}"); 
                Cliente cliente = new Cliente();
                cliente.setCodigo(lineaSC.next());
                cliente.setNombre(lineaSC.next());
                this.clientes.add(cliente);
                lineaSC.close();
            }
            archivoSC.close();
            G4D.Logger.logf("[<] CLIENTES CARGADOS! ('%d' clientes)%n", this.clientes.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void cargarPedidos(String rutaArchivo) {
        G4D.Logger.logf("Cargando pedidos desde '%s'..%n",rutaArchivo);
        try {
            File archivo = new File(rutaArchivo);
            Scanner archivoSC = new Scanner(archivo, G4D.getFileCharset(archivo));
            LocalDateTime fechaHoraGeneracionMinima = FECHA_HORA_INICIO.minusDays(MAX_DIAS_ENTREGA_INTERCONTINENTAL);
            LocalDateTime fechaHoraGeneracionMaxima = FECHA_HORA_FIN.plusDays(MAX_DIAS_ENTREGA_INTERCONTINENTAL);
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
                    if(!fechaHoraGeneracionUTC.isBefore(fechaHoraGeneracionMinima) && !fechaHoraGeneracionUTC.isAfter(fechaHoraGeneracionMaxima)) {
                        pedido.setCodigo(aDest.getCodigo() + numPed);
                        pedido.setDestino(aDest);
                        pedido.setCantidadSolicitada(lineaSC.nextInt());
                        String codCli = lineaSC.next();
                        Cliente cliente = obtenerClientePorCodigo(codCli);
                        if(cliente == null) {
                            cliente = new Cliente();
                            cliente.setCodigo(codCli);
                            cliente.setNombre(G4D.Generator.getUniqueName());
                            this.clientes.add(cliente);
                        }
                        pedido.setCliente(cliente);
                        pedido.setFechaHoraGeneracionLocal(fechaHoraGeneracionLocal);
                        pedido.setFechaHoraGeneracionUTC(fechaHoraGeneracionUTC);
                        this.pedidos.add(pedido);
                    }
                }
                lineaSC.close();
            }
            archivoSC.close();
            G4D.Logger.Stats.totalPed = this.pedidos.size();
            for (Pedido p : this.pedidos) G4D.Logger.Stats.totalProd += p.getCantidadSolicitada();
            G4D.Logger.logf("[<] PEDIDOS CARGADOS! ('%d' pedidos -> '%d' productos)%n", G4D.Logger.Stats.totalPed, G4D.Logger.Stats.totalProd);
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", rutaArchivo);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private Aeropuerto obtenerAeropuertoPorCodigo(String codigo) {
        return this.destinos.stream().filter(aDest -> aDest.getCodigo().equals(codigo)).findFirst().orElse(this.origenes.stream().filter(aOrig -> aOrig.getCodigo().equals(codigo)).findFirst().orElse(null));
    }

    private Cliente obtenerClientePorCodigo(String codigo) {
        return this.clientes.stream().filter(c -> c.getCodigo().equals(codigo)).findFirst().orElse(null);
    }
}
