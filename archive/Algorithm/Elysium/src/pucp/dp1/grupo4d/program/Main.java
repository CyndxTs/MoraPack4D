/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Main.java 
[**/

package pucp.dp1.grupo4d.program;

import java.time.LocalDateTime;

import pucp.dp1.grupo4d.algorithm.GVNS;
import pucp.dp1.grupo4d.algorithm.Problematica;

public class Main {
    public static void main(String[] args) {
        // Declaracion de problematica
        Problematica.FECHA_HORA_INICIO = LocalDateTime.of(2025, 11, 1, 0, 0, 0);
        Problematica.FECHA_HORA_FIN = LocalDateTime.of(2025, 12, 1, 0, 0, 0);
        Problematica problematica = new Problematica();
        problematica.cargarDatos(
                "../../in/c.1inf54.25.2.Aeropuerto.husos.v1.20250818__estudiantes.txt",
                "../../in/c.1inf54.25.2.planes_vuelo.v4.20250818.txt",
                "../../in/Clientes.txt",
                "../../in/c.1inf54.25.2.pedidos.v01/_pedidos_CANDY_.txt");
        // Declaracion de algoritmo
        GVNS gvns = new GVNS();
        // planificacion
        gvns.planificar(problematica);
    }
}
