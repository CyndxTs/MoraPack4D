/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VersionRunner.java 
[**/

package pucp.dp1.grupo4d.program;

import java.time.LocalDateTime;

public class VersionRunner {
    private String rutaArchivoAeropuertos;
    private String rutaArchivoPlanesDeVuelo;
    private String rutaArchivoPedidos;
    private String rutaArchivoClientes;

    public VersionRunner(String rutaArchivoAeropuertos, String rutaArchivoPlanesDeVuelo, String rutaArchivoPedidos,
                         String rutaArchivoClientes) {
        this.rutaArchivoAeropuertos = rutaArchivoAeropuertos;
        this.rutaArchivoPlanesDeVuelo = rutaArchivoPlanesDeVuelo;
        this.rutaArchivoPedidos = rutaArchivoPedidos;
        this.rutaArchivoClientes = rutaArchivoClientes;
    }

    public void runAether() {
        pucp.dp1.grupo4d.algorithm.version.aether.Problematica.INICIO_PLANIFICACION = LocalDateTime.of(2025, 11, 1, 0, 0);
        pucp.dp1.grupo4d.algorithm.version.aether.Problematica.FIN_PLANIFICACION = LocalDateTime.of(2025, 11, 7, 0, 0);
        var p = new pucp.dp1.grupo4d.algorithm.version.aether.Problematica();
        p.cargarDatos(this.rutaArchivoAeropuertos, this.rutaArchivoPlanesDeVuelo, this.rutaArchivoClientes, this.rutaArchivoPedidos);
        pucp.dp1.grupo4d.algorithm.version.aether.GVNS.I_MAX = 1;
        var gvns = new pucp.dp1.grupo4d.algorithm.version.aether.GVNS();
        gvns.planificar(p);
    }

    public void runCaelum() {
        pucp.dp1.grupo4d.algorithm.version.caelum.Problematica.FECHA_HORA_INICIO = LocalDateTime.of(1925, 11, 1, 0, 0);
        pucp.dp1.grupo4d.algorithm.version.caelum.Problematica.FECHA_HORA_FIN = LocalDateTime.of(2026, 12, 1, 0, 0);
        var p = new pucp.dp1.grupo4d.algorithm.version.caelum.Problematica();
        p.cargarDatos(this.rutaArchivoAeropuertos, this.rutaArchivoPlanesDeVuelo, this.rutaArchivoClientes, this.rutaArchivoPedidos);
        pucp.dp1.grupo4d.algorithm.version.caelum.GVNS.I_MAX = 2;
        var gvns = new pucp.dp1.grupo4d.algorithm.version.caelum.GVNS();
        gvns.planificar(p);
    }

    public void runLumen() {
        var p = new pucp.dp1.grupo4d.algorithm.version.lumen.Problematica();
        p.cargarDatos(this.rutaArchivoAeropuertos, this.rutaArchivoPlanesDeVuelo, this.rutaArchivoClientes, this.rutaArchivoPedidos);
        var gvns = new pucp.dp1.grupo4d.algorithm.version.lumen.GVNS();
        gvns.planificar(p);
    }

    public void runSolstice() {
        var p = new pucp.dp1.grupo4d.algorithm.version.solstice.Problematica();
        p.cargarDatos(this.rutaArchivoAeropuertos, this.rutaArchivoPlanesDeVuelo, this.rutaArchivoPedidos);
        var gvns = new pucp.dp1.grupo4d.algorithm.version.solstice.GVNS();
        gvns.planificar(p);
    }

    public void runVespera(String algorithm) {
        switch (algorithm.toUpperCase()) {
            case "GVNS":
                var p1 = new pucp.dp1.grupo4d.algorithm.version.vespera.Problematica();
                p1.cargarDatos(this.rutaArchivoAeropuertos, this.rutaArchivoPlanesDeVuelo, this.rutaArchivoPedidos);
                var a1 = new pucp.dp1.grupo4d.algorithm.version.vespera.GVNS();
                pucp.dp1.grupo4d.algorithm.version.vespera.GVNS.fastSearch = true;
                a1.planificar(p1);
                break;
            case "PSO":
                var p2 = new pucp.dp1.grupo4d.algorithm.version.vespera.Problematica();
                p2.cargarDatos(this.rutaArchivoAeropuertos, this.rutaArchivoPlanesDeVuelo, this.rutaArchivoPedidos);
                var a2 = new pucp.dp1.grupo4d.algorithm.version.vespera.PSO();
                a2.planificar(p2);
                break;
            default:
                System.out.println("Vespera: Los algoritmos manejados son 'GVNS' y 'PSO'");
                break;
        }
    }    
}
