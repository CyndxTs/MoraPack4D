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
    private LocalDateTime umbralDeConexion;
    private Aeropuerto aeropuertoDeConexion;
    private Ruta rutaInicial;
    private List<Vuelo> vuelosFijos;
    private List<Lote> lotes;

    public PuntoDeReplanificacion() {
        this.vuelosFijos = new ArrayList<>();
        this.lotes = new ArrayList<>();
    }

    public LocalDateTime getUmbralDeConexion() {
        return umbralDeConexion;
    }

    public void setUmbralDeConexion(LocalDateTime umbralDeConexion) {
        this.umbralDeConexion = umbralDeConexion;
    }

    public Aeropuerto getAeropuertoDeConexion() {
        return aeropuertoDeConexion;
    }

    public void setAeropuertoDeConexion(Aeropuerto aeropuertoDeConexion) {
        this.aeropuertoDeConexion = aeropuertoDeConexion;
    }

    public Ruta getRutaInicial() {
        return rutaInicial;
    }

    public void setRutaInicial(Ruta rutaInicial) {
        this.rutaInicial = rutaInicial;
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

    public void setLotes(List<Lote> lotes) {
        this.lotes = lotes;
    }
}
