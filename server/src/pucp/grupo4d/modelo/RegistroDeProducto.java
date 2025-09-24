/**
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroDeProducto.java 
**/

package pucp.grupo4d.modelo;

import java.time.LocalDateTime;

import pucp.grupo4d.util.G4D_Util;

public class RegistroDeProducto {
    private String id;
    private String idProducto;
    private LocalDateTime fechaHoraIngresoLocal;
    private LocalDateTime fechaHoraIngresoUTC;
    private LocalDateTime fechaHoraEgresoLocal;
    private LocalDateTime fechaHoraEgresoUTC;

    public RegistroDeProducto() {
        this.id = G4D_Util.generateIdentifier("REG");
    }

    public RegistroDeProducto replicar() {
        System.out.println(">>>>>>> R-REGISTRO");
        RegistroDeProducto registro = new RegistroDeProducto();
        registro.id = this.id;
        registro.idProducto = this.idProducto;
        registro.fechaHoraIngresoLocal = this.fechaHoraIngresoLocal;
        registro.fechaHoraIngresoUTC = this.fechaHoraIngresoUTC;
        registro.fechaHoraEgresoLocal = this.fechaHoraEgresoLocal;
        registro.fechaHoraEgresoUTC = this.fechaHoraEgresoUTC;
        return registro;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public LocalDateTime getFechaHoraIngresoLocal() {
        return fechaHoraIngresoLocal;
    }

    public void setFechaHoraIngresoLocal(LocalDateTime fechaHoraIngresoLocal) {
        this.fechaHoraIngresoLocal = fechaHoraIngresoLocal;
    }

    public LocalDateTime getFechaHoraIngresoUTC() {
        return fechaHoraIngresoUTC;
    }

    public void setFechaHoraIngresoUTC(LocalDateTime fechaHoraIngresoUTC) {
        this.fechaHoraIngresoUTC = fechaHoraIngresoUTC;
    }

    public LocalDateTime getFechaHoraEgresoLocal() {
        return fechaHoraEgresoLocal;
    }

    public void setFechaHoraEgresoLocal(LocalDateTime fechaHoraEgresoLocal) {
        this.fechaHoraEgresoLocal = fechaHoraEgresoLocal;
    }

    public LocalDateTime getFechaHoraEgresoUTC() {
        return fechaHoraEgresoUTC;
    }

    public void setFechaHoraEgresoUTC(LocalDateTime fechaHoraEgresoUTC) {
        this.fechaHoraEgresoUTC = fechaHoraEgresoUTC;
    }
}
