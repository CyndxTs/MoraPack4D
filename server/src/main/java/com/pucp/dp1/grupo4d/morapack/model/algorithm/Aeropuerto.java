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
import com.pucp.dp1.grupo4d.morapack.algorithm.Problematica;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;

public class Aeropuerto {
    private String codigo;
    private String continente;
    private String pais;
    private String ciudad;
    private String alias;
    private Integer husoHorario;
    private Integer capacidad;
    private Double latitud;
    private Double longitud;
    private Boolean esSede;
    private List<Registro> registros;

    public Aeropuerto() {
        this.codigo = G4DUtility.Generator.getUniqueString("AER");
        this.capacidad = 0;
        this.registros = new ArrayList<>();
    }

    public Aeropuerto replicar(Map<String, Lote> poolLotes) {
        Aeropuerto aeropuerto = new Aeropuerto();
        aeropuerto.codigo = this.codigo;
        aeropuerto.ciudad = this.ciudad;
        aeropuerto.pais = this.pais;
        aeropuerto.continente = this.continente;
        aeropuerto.husoHorario = this.husoHorario;
        aeropuerto.capacidad = this.capacidad;
        aeropuerto.latitud = this.latitud;
        aeropuerto.longitud = this.longitud;
        this.registros.forEach(r -> aeropuerto.registros.add(r.replicar(poolLotes)));
        return aeropuerto;
    }

    public Lote generarLoteDeProductos(int cantProd) {
        Lote lote = new Lote();
        lote.setTamanio(cantProd);
        return lote;
    }

    public void registrarLoteDeProductos(Lote lote, LocalDateTime fechaHoraIngreso, LocalDateTime fechaHoraEgreso) {
        Registro registro = new Registro();
        registro.setFechaHoraIngreso(fechaHoraIngreso);
        registro.setFechaHoraEgreso(fechaHoraEgreso);
        registro.setLote(lote);
        this.registros.add(registro);
    }

    public Boolean eliminarRegistroDeLoteDeProductos(Lote lote, boolean softDelete) {
        for(Registro registro : this.registros) {
            if(registro.getLote().equals(lote) && registro.getSigueVigente()) {
                if(softDelete) {
                    registro.setSigueVigente(false);
                } else this.registros.remove(registro);
                return true;
            }
        }
        return false;
    }

    public void actualizarEstanciaHaciaTiempoMaximoHabitable(Lote lote) {
        int disp = this.capacidad;
        Map<LocalDateTime, Integer> eventos = new TreeMap<>();
        Registro registroDeLote = null;
        for(Registro registro : this.registros) {
            if(!registro.getLote().equals(lote)) {
                LocalDateTime rFechaHoraIngreso = registro.getFechaHoraIngreso(), rFechaHoraEgreso = registro.getFechaHoraEgreso();
                int tamanio = registro.getLote().getTamanio();
                eventos.merge(rFechaHoraIngreso, -tamanio, Integer::sum);
                eventos.merge(rFechaHoraEgreso, +tamanio, Integer::sum);
            } else registroDeLote = registro;
        }
        if(registroDeLote != null) {
            LocalDateTime fechaHoraIngresoDeLote = registroDeLote.getFechaHoraIngreso();
            if(!eventos.isEmpty()) {
                for(Map.Entry<LocalDateTime, Integer> entry : eventos.entrySet()) {
                    if(!fechaHoraIngresoDeLote.isAfter(entry.getKey()) && disp + entry.getValue() < lote.getTamanio()) {
                        long maxMinutos = (long)(60*Math.min(G4DUtility.Calculator.getElapsedHours(fechaHoraIngresoDeLote, entry.getKey()), Problematica.MAX_HORAS_ESTANCIA));
                        registroDeLote.setFechaHoraEgreso(fechaHoraIngresoDeLote.plusMinutes(maxMinutos));
                        return;
                    }
                    disp += entry.getValue();
                }
            } else {
                long maxMinutos = (long)(60* Problematica.MAX_HORAS_ESTANCIA);
                registroDeLote.setFechaHoraEgreso(fechaHoraIngresoDeLote.plusMinutes(maxMinutos));
            }
        }
    }

    public Integer obtenerCapacidadDisponible(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        int disp = this.capacidad, minDisp = this.capacidad;
        Map<LocalDateTime, Integer> eventos = new TreeMap<>();
        for(Registro registro : this.registros) {
            LocalDateTime rFechaHoraIngreso = registro.getFechaHoraIngreso(), rFechaHoraEgreso = registro.getFechaHoraEgreso();
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
        return G4DUtility.Calculator.getGeodesicDistance(this.latitud, this.longitud, aDest.latitud, aDest.longitud);
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

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(String latDMS) {
        this.latitud = G4DUtility.Calculator.getLatDEC(latDMS);
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(String lonDMS) {
        this.longitud = G4DUtility.Calculator.getLonDEC(lonDMS);
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public Boolean getEsSede() {
        return esSede;
    }

    public void setEsSede(boolean esSede) {
        this.esSede = esSede;
    }

    public List<Registro> getRegistros() {
        return registros;
    }

    public void setRegistros(List<Registro> registros) {
        this.registros = registros;
    }
}
