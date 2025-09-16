package pucp.grupo4d.resolucion;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;
import pucp.grupo4d.modelo.Problematica;
import pucp.grupo4d.modelo.Solucion;
import pucp.grupo4d.util.G4D_Formatter;

public class Algoritmo {
    private final int L_MIN;        
    private final int L_MAX;        
    private final int K_MIN;        
    private final int K_MAX;        
    private final int T_MAX;        
    private final int MAX_INTENTOS; 
    private final int NO_ENCONTRADO;
    private Solucion solucionInicial;
    private Solucion solucionVND;
    private Solucion solucionGVNS;

    public Algoritmo() {
        this.L_MIN = 1;
        this.L_MAX = 2;
        this.K_MIN = 3;
        this.K_MAX = 5;
        this.T_MAX = 10;
        this.MAX_INTENTOS = 10;
        this.NO_ENCONTRADO = -1;
    }

    public void GVNS(Problematica problematica) {
        // Declaracion de Variables
        Solucion solucionAux = new Solucion();
        Solucion xBest = new Solucion();
        int t = 0;
        int tBest = 0;
        Random rand = new Random(System.currentTimeMillis());
        // Solución inicial (Nearest Neighbor)
        // To do: // solucionInicial(problematica, solucionAux);
        solucionAux.setPaquetes(problematica.getAllPaquetes());
        imprimirSolucion(solucionAux, "SolucionInicial.txt");
        /*
        // Optimización por VND
        // To do: // VND(problematica, solucionAux);
        imprimirSolucion(solucionAux, "SolucionVND.txt");
        System.out.printf("%18s%n", "Nuevo mejor!");
        System.out.printf("[%9.2f]%n", solucionAux.getFitness());
        // Guardar como mejor solución
        xBest = solucionAux.clonar();
        // Inicializar cronómetro
        Instant start = Instant.now();
        do {
            int k = K_MIN;
            solucionAux = xBest.clonar();

            while (k <= K_MAX && t < T_MAX) {
                Solucion xPrima = solucionAux.clonar();

                // Intentar hasta encontrar una solución posible
                while (true) {
                    xPrima = solucionAux.clonar();
                    shaking(xPrima, k, problematica);
                    System.out.println("Validando...");
                    if (posibleSolucion(xPrima)) {
                        break;
                    } else {
                        System.out.printf("%18s%n", "[ABERRACION]");
                    }
                }

                System.out.printf("%20s%n", "[POSIBLE MEJORA]");

                Solucion xPrimaDoble = xPrima.clonar();
                VND(problematica, xPrimaDoble);

                // Actualizar tiempo
                Instant end = Instant.now();
                Duration duracion = Duration.between(start, end);
                t = (int) duracion.getSeconds();

                // Cambio de vecindario si hay mejora
                cambiarVecindario(problematica, solucionAux, xPrimaDoble, xBest, k, t, tBest);
            }
        } while (t < T_MAX);
        // Guardar solución final
        problematica.setSolucion(xBest);
        imprimirSolucion(xBest, "SolucionGVNS.txt");
        */
    }

    // Búsqueda local: Variable Neighborhood Descent
    private void VND(Solucion solucion) {

    }

    // Perturbación: Shaking
    private void Shaking(Solucion solucion, int k) {

    }

    //
    public void imprimirSolucionInicial(String rutaArchivo) { imprimirSolucion(solucionInicial, rutaArchivo); }
    public void imprimirSolucionVND(String rutaArchivo) { imprimirSolucion(solucionVND, rutaArchivo); }
    public void imprimirSolucionGVNS(String rutaArchivo) { imprimirSolucion(solucionGVNS, rutaArchivo); }

    //
    private void imprimirSolucion(Solucion solucion, String rutaArchivo) {
        // Declaracion de variables
        int dimLinea = 120;
        FileWriter archivo;
        PrintWriter archivoWriter;
        // Carga de datos
        try {
            System.out.println("Cargando archivo 'Solucion' a la ruta '" + rutaArchivo + "'..");
            // Inicializaion del archivo y scanner
            archivo = new FileWriter(rutaArchivo);
            archivoWriter = new PrintWriter(archivo);
            // Impresion de reporte
            G4D_Formatter.imprimirLinea(archivoWriter, '=', dimLinea, true);
            G4D_Formatter.imprimirCentrado(archivoWriter, dimLinea, "FITNESS DE LA SOLUCIÓN");
            G4D_Formatter.imprimirCentrado(archivoWriter, dimLinea, String.format("%.2f", solucion.getFitness()));
            G4D_Formatter.imprimirLinea(archivoWriter, '=', dimLinea, true);
            System.out.println("Archivo 'Solucion' generado en la ruta '" + rutaArchivo + "'.");
            archivoWriter.flush();
            archivoWriter.close();
            archivo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
