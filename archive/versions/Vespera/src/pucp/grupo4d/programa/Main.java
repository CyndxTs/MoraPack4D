/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Main.java 
[**/

package pucp.grupo4d.programa;

import pucp.grupo4d.modelo.Problematica;
import pucp.grupo4d.resolucion.Algoritmo;
import pucp.grupo4d.resolucion.GVNS;
import pucp.grupo4d.resolucion.PSO;

public class Main {
    public static void main(String[] args) {
        // Declaracion de problematica 1
        Problematica problematica1 = new Problematica();
        problematica1.cargarDatos(
                "../../in/c.1inf54.25.2.Aeropuerto.husos.v1.20250818__estudiantes.txt",
                "../../in/c.1inf54.25.2.planes_vuelo.v4.20250818.txt",
                "../../in/c.1inf54.25.2.pedidos.v01/_pedidos_CANDY_.txt");
        // Declaracion de algoritmo 1
        Algoritmo algoritmo1 = new GVNS();
        // Ejecucion de algoritmo 1
        GVNS.fastSearch = true;
        algoritmo1.planificar(problematica1);
        // Declaracion de problematica 2
        Problematica problematica2 = new Problematica();
        problematica2.cargarDatos(
                "../../in/c.1inf54.25.2.Aeropuerto.husos.v1.20250818__estudiantes.txt",
                "../../in/c.1inf54.25.2.planes_vuelo.v4.20250818.txt",
                "../../in/c.1inf54.25.2.pedidos.v01/_pedidos_CANDY_.txt");
        // Declaracion de algoritmo 2
        Algoritmo algoritmo2 = new PSO();
        // Ejecucion de algoritmo 2
        algoritmo2.planificar(problematica2);
    }
}
