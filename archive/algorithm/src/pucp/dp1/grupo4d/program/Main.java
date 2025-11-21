/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Main.java 
[**/

package pucp.dp1.grupo4d.program;

public class Main {
    public static void main(String[] args) {
        String runnableVersion = "ELYSIUM";
        VersionRunner runner = new VersionRunner(
            "../files/c.1inf54.25.2.Aeropuerto.husos.v1.20250818__estudiantes.txt",
            "../files/c.1inf54.25.2.planes_vuelo.v4.20250818.txt",
            "../files/c.1inf54.25.2.pedidos.v01/_pedidos_CANDY_.txt",
            "../files/Clientes.txt"
        );

        switch (runnableVersion.toUpperCase()) {
            case "AETHER" -> runner.runAether();
            case "CAELUM" -> runner.runCaelum();
            case "LUMEN" -> runner.runLumen();
            case "SOLSTICE" -> runner.runSolstice();
            case "VESPERA" -> runner.runVespera("GVNS");
            default -> System.out.println("Las versiones activas son: 'AETHER', 'CAELUM', 'LUMEN', 'SOLSTICE' y 'VESPERA'.");
        }
    }
}
