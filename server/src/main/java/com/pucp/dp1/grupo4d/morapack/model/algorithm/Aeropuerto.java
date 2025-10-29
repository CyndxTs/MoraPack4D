/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Aeropuerto.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.algorithm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.pucp.dp1.grupo4d.morapack.util.G4D;

public class Aeropuerto {
    private String codigo;
    private String ciudad;
    private String pais;
    private String continente;
    private String alias;
    private Integer husoHorario;
    private Integer capacidad;
    private String latitudDMS;
    private Double latitudDEC;
    private String longitudDMS;
    private Double longitudDEC;
    private List<Registro> registros;

    public Aeropuerto() {
        this.codigo = G4D.Generator.getUniqueString("AER");
        this.capacidad = 0;
        this.registros = new ArrayList<>();
    }

    public Lote generarLoteDeProductos(int cantProd) {
        Lote lote = new Lote();
        lote.setTamanio(cantProd);
        lote.setProductos();
        return lote;
    }

    public Integer obtenerCapacidadDisponible(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        int disp = this.capacidad, minDisp = this.capacidad;
        Map<LocalDateTime, Integer> eventos = new TreeMap<>();
        for(Registro registro : this.registros) {
            LocalDateTime rFechaHoraIngreso = registro.getFechaHoraIngresoUTC(), rFechaHoraEgreso = registro.getFechaHoraEgresoUTC();
            if(rFechaHoraIngreso.isBefore(fechaHoraFin) && rFechaHoraEgreso.isAfter(fechaHoraInicio)) {
                int tamanio = registro.getLote().getTamanio();
                eventos.merge(rFechaHoraIngreso, -tamanio, Integer::sum);
                eventos.merge(rFechaHoraEgreso, +tamanio, Integer::sum);
            }
        }
        for(int canProd : eventos.values()) {
            disp += canProd;
            minDisp = Math.min(minDisp, disp);
        }
        return minDisp;
    }

    public Double obtenerDistanciaHasta(Aeropuerto aDest) {
        return G4D.getGeodesicDistance(this.latitudDEC, this.longitudDEC, aDest.latitudDEC, aDest.longitudDEC);
    }

    public void registrarLoteDeProductos(Lote lote, LocalDateTime fechaHoraIngresoUTC, LocalDateTime fechaHoraEgresoUTC) {
        Registro registro = new Registro();
        registro.setFechaHoraIngresoUTC(fechaHoraIngresoUTC);
        registro.setFechaHoraIngresoLocal(G4D.toLocal(fechaHoraIngresoUTC, this.husoHorario));
        registro.setFechaHoraEgresoUTC(fechaHoraEgresoUTC);
        registro.setFechaHoraEgresoLocal(G4D.toLocal(fechaHoraEgresoUTC, this.husoHorario));
        registro.setLote(lote);
        this.registros.add(registro);
    }

    public Boolean eliminarLoteDeProductos(Lote lote) {
        for(Registro registro : this.registros) {
            if(registro.getLote().equals(lote)) {
                this.registros.remove(registro);
                return true;
            }
        }
        return false;
    }

    public Aeropuerto replicar(Map<String, Lote> poolLotes) {
        Aeropuerto aeropuerto = new Aeropuerto();
        aeropuerto.codigo = this.codigo;
        aeropuerto.ciudad = this.ciudad;
        aeropuerto.pais = this.pais;
        aeropuerto.continente = this.continente;
        aeropuerto.alias = this.alias;
        aeropuerto.husoHorario = this.husoHorario;
        aeropuerto.capacidad = this.capacidad;
        aeropuerto.latitudDMS = this.latitudDMS;
        aeropuerto.latitudDEC = this.latitudDEC;
        aeropuerto.longitudDMS = this.longitudDMS;
        aeropuerto.longitudDEC = this.longitudDEC;
        for(Registro r : this.registros) aeropuerto.registros.add(r.replicar(poolLotes));
        return aeropuerto;
    }

    @Override
    public String toString() {
        return String.format("%s - %s, %s", this.codigo, this.ciudad, this.pais);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aeropuerto that = (Aeropuerto) o;
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

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getContinente() {
        return continente;
    }

    public void setContinente(String continente) {
        this.continente = continente;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getHusoHorario() {
        return husoHorario;
    }

    public void setHusoHorario(int husoHorario) {
        this.husoHorario = husoHorario;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getLatitudDMS() {
        return latitudDMS;
    }

    public void setLatitudDMS() {
        this.latitudDMS = G4D.toLatDMS(this.latitudDEC);
    }

    public void setLatitudDMS(String latitudDMS) {
        this.latitudDMS = latitudDMS;
    }

    public Double getLatitudDEC() {
        return latitudDEC;
    }

    public void setLatitudDEC() {
        this.latitudDEC = G4D.toLatDEC(this.latitudDMS);
    }

    public void setLatitudDEC(double latitudDEC) {
        this.latitudDEC = latitudDEC;
    }

    public String getLongitudDMS() {
        return longitudDMS;
    }

    public void setLongitudDMS() {
        this.longitudDMS = G4D.toLonDMS(this.longitudDEC);
    }

    public void setLongitudDMS(String longitudDMS) {
        this.longitudDMS = longitudDMS;
    }

    public Double getLongitudDEC() {
        return longitudDEC;
    }

    public void setLongitudDEC() {
        this.longitudDEC = G4D.toLonDEC(this.longitudDMS);
    }

    public void setLongitudDEC(double longitudDEC) {
        this.longitudDEC = longitudDEC;
    }

    public List<Registro> getRegistros() {
        return registros;
    }

    public void setRegistros(List<Registro> registros) {
        this.registros = registros;
    }
}
