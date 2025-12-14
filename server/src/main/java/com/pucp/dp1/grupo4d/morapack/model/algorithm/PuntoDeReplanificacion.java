/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PuntoDeReplanificacion.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PuntoDeReplanificacion {
    private LocalDateTime umbralDeConexion;
    private Aeropuerto aeropuertoDeConexion;
    private Ruta rutaInicial;
    private Vuelo vueloReplanificado;
    private List<Vuelo> vuelosFijos;
    private Set<Lote> lotes;

    public PuntoDeReplanificacion() {
        this.vuelosFijos = new ArrayList<>();
        this.lotes = new HashSet<>();
    }

    public PuntoDeReplanificacion(PuntoDeReplanificacion pdr) {
        this.reasignar(pdr);
    }

    public void reasignar(PuntoDeReplanificacion pdr) {
        this.umbralDeConexion = pdr.umbralDeConexion;
        this.aeropuertoDeConexion = pdr.aeropuertoDeConexion;
        this.rutaInicial = pdr.rutaInicial;
        this.vueloReplanificado = pdr.vueloReplanificado;
        this.vuelosFijos = new ArrayList<>(pdr.vuelosFijos);
        this.lotes = new HashSet<>(pdr.lotes);
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

    public Vuelo getVueloReplanificado() {
        return vueloReplanificado;
    }

    public void setVueloReplanificado(Vuelo vueloReplanificado) {
        this.vueloReplanificado = vueloReplanificado;
    }

    public List<Vuelo> getVuelosFijos() {
        return vuelosFijos;
    }

    public void setVuelosFijos(List<Vuelo> vuelosFijos) {
        this.vuelosFijos = vuelosFijos;
    }

    public Set<Lote> getLotes() {
        return lotes;
    }

    public void setLotes(HashSet<Lote> lotes) {
        this.lotes = lotes;
    }
}
