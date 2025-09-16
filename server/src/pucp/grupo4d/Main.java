package pucp.grupo4d;

import pucp.grupo4d.modelo.Problematica;
import pucp.grupo4d.resolucion.Algoritmo;

public class Main {
    public static void main(String[] args) {
        // Declaracion de problematica
        Problematica problematica = new Problematica();
        // Inicializacion de problematica
        problematica.cargarDatos(
            "c.1inf54.25.2.Aeropuerto.husos.v1.20250818__estudiantes.txt",
            "c.1inf54.25.2.planes_vuelo.v4.20250818.txt",
            null
        );
        // Declaracion de algoritmo
        Algoritmo algoritmo = new Algoritmo();
        // Optimizacion
        algoritmo.GVNS(problematica);
    }
}
