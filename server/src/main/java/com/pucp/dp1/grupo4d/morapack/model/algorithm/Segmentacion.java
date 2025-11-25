/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Segmentacion.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Segmentacion {
    private String codigo;
    private LocalDateTime fechaHoraAplicacion;
    private LocalDateTime fechaHoraSustitucion;
    private Map<Ruta, Lote> lotesPorRuta;

    public Segmentacion() {
        this.lotesPorRuta = new HashMap<>();
    }

    public Segmentacion replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Ruta> poolRutas, Map<String,Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        Segmentacion segmentacion = new Segmentacion();
        segmentacion.codigo = this.codigo;
        segmentacion.fechaHoraAplicacion = this.fechaHoraAplicacion;
        segmentacion.fechaHoraSustitucion = this.fechaHoraSustitucion;
        this.lotesPorRuta.entrySet().forEach(e -> {
            Ruta ruta = poolRutas.computeIfAbsent(e.getKey().getCodigo(), codigo -> e.getKey().replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes));
            Lote lote = poolLotes.computeIfAbsent(e.getValue().getCodigo(), codigo -> e.getValue().replicar());
            segmentacion.lotesPorRuta.put(ruta, lote);
        });
        return segmentacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segmentacion that = (Segmentacion) o;
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

    public LocalDateTime getFechaHoraAplicacion() {
        return fechaHoraAplicacion;
    }

    public void setFechaHoraAplicacion(LocalDateTime fechaHoraAplicacion) {
        this.fechaHoraAplicacion = fechaHoraAplicacion;
    }

    public LocalDateTime getFechaHoraSustitucion() {
        return fechaHoraSustitucion;
    }

    public void setFechaHoraSustitucion(LocalDateTime fechaHoraSustitucion) {
        this.fechaHoraSustitucion = fechaHoraSustitucion;
    }


    public Map<Ruta, Lote> getLotesPorRuta() {
        return lotesPorRuta;
    }

    public void setLotesPorRuta(Map<Ruta, Lote> lotesPorRuta) {
        this.lotesPorRuta = lotesPorRuta;
    }
}
