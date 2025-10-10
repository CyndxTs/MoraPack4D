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
        // Declaracion de problematica
        Problematica problematica = new Problematica();
        // Inicializacion de problematica
        problematica.cargarDatos(
                "c.1inf54.25.2.Aeropuerto.husos.v1.20250818__estudiantes.txt",
                "c.1inf54.25.2.planes_vuelo.v4.20250818.txt",
                "Pedidos.txt");
        // Declaracion de algoritmo
        Algoritmo algoritmo1 = new GVNS(); // new PSO();
        // Optimizacion
        GVNS.fastSearch = true;
        algoritmo1.planificar(problematica);
    }
}
