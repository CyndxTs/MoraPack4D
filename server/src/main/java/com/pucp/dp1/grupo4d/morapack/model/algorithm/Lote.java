/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Lote.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoLote;
import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class Lote {
    private String codigo;
    private Integer tamanio;
    private EstadoLote estado;

    public Lote() {
        this.codigo = G4D.Generator.getUniqueString("LOT");
        this.tamanio = 0;
        this.estado = EstadoLote.PLANIFICADO;
    }

    public Lote replicar() {
        Lote lote = new Lote();
        lote.codigo = this.codigo;
        lote.tamanio = this.tamanio;
        lote.estado = this.estado;
        return lote;
    }

    public Boolean esModificable(Ruta rutaAsignada) {
        boolean loteReplanificado = this.getEstado() == EstadoLote.REPLANIFICADO;
        boolean rutaPosterior = rutaAsignada.getFechaHoraSalida().isAfter(Problematica.INICIO_REPLANIFICACION);
        return loteReplanificado || rutaPosterior;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lote that = (Lote) o;
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

    public Integer getTamanio() {
        return tamanio;
    }

    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public void setEstado(EstadoLote estado) {
        this.estado = estado;
    }
}
