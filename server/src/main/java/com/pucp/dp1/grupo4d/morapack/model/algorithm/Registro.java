/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Registro.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.Map;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class Registro {
    private String codigo;
    private Boolean sigueVigente;
    private LocalDateTime fechaHoraIngreso;
    private LocalDateTime fechaHoraEgreso;
    private Lote lote;

    public Registro() {
        this.codigo = G4DUtility.Generator.getUniqueString("REG");
        this.sigueVigente = true;
    }

    public Registro replicar(Map<String, Lote> poolLotes) {
        Registro registro = new Registro();
        registro.codigo = this.codigo;
        registro.sigueVigente = this.sigueVigente;
        registro.fechaHoraIngreso = this.fechaHoraIngreso;
        registro.fechaHoraEgreso = this.fechaHoraEgreso;
        registro.lote = (this.lote != null) ? poolLotes.computeIfAbsent(this.lote.getCodigo(), codigo -> this.lote.replicar()) : null;
        return registro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registro  that = (Registro) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Boolean getSigueVigente() {
        return sigueVigente;
    }

    public void setSigueVigente(boolean sigueVigente) {
        this.sigueVigente = sigueVigente;
    }

    public LocalDateTime getFechaHoraIngreso() {
        return fechaHoraIngreso;
    }

    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) {
        this.fechaHoraIngreso = fechaHoraIngreso;
    }

    public LocalDateTime getFechaHoraEgreso() {
        return fechaHoraEgreso;
    }

    public void setFechaHoraEgreso(LocalDateTime fechaHoraEgreso) {
        this.fechaHoraEgreso = fechaHoraEgreso;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }
}
