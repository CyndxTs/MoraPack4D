/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PuntoDeReplanificacion.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PuntoDeReplanificacion {
    private Ruta ruta;
    private LocalDateTime umbralDeConexion;
    private List<Vuelo> vuelosFijos;
    private List<Lote> lotes;

    public PuntoDeReplanificacion() {
        this.vuelosFijos = new ArrayList<>();
        this.lotes = new ArrayList<>();
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public LocalDateTime getUmbralDeConexion() {
        return umbralDeConexion;
    }

    public void setUmbralDeConexion(LocalDateTime umbralDeConexion) {
        this.umbralDeConexion = umbralDeConexion;
    }

    public List<Vuelo> getVuelosFijos() {
        return vuelosFijos;
    }

    public void setVuelosFijos(List<Vuelo> vuelosFijos) {
        this.vuelosFijos = vuelosFijos;
    }

    public List<Lote> getLotes() {
        return lotes;
    }

    public void setLote(List<Lote> lotes) {
        this.lotes = lotes;
    }
}
