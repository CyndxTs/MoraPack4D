/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Aeropuerto.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pucp.dp1.grupo4d.util.G4D;

public class Aeropuerto {
    private String id;
    private String codigo;
    private String ciudad;
    private String pais;
    private String continente;
    private String alias;
    private Integer husoHorario;
    private Integer capacidadMaxima;
    private String latitudDMS;
    private Double latitudDEC;
    private String longitudDMS;
    private Double longitudDEC;
    private List<RegistroDeProducto> historialDeProductos;

    public Aeropuerto() {
        this.id = G4D.getUniqueString("AER");
        this.historialDeProductos = new ArrayList<>();
    }

    public Double obtenerDistanciaHasta(Aeropuerto destino) {
        return G4D.getGeodesicDistance(this.latitudDEC,this.longitudDEC,destino.latitudDEC,destino.longitudDEC);
    }

    public Integer obtenerCapacidadDisponible(LocalDateTime fechaHoraIngresoUTC, LocalDateTime fechaHoraEgresoUTC) {
        int capacidadDisponible = this.capacidadMaxima;
        for(RegistroDeProducto registro : this.historialDeProductos) {
            LocalDateTime auxFechaHoraIngresoUTC = registro.getFechaHoraIngresoUTC();
            LocalDateTime auxfechaHoraEgresoUTC = registro.getFechaHoraEgresoUTC();
            if(auxfechaHoraEgresoUTC == null) {
                auxfechaHoraEgresoUTC = auxFechaHoraIngresoUTC.plusHours(Problematica.MAX_HORAS_RECOJO.longValue());
            }
            if(auxFechaHoraIngresoUTC.isAfter(fechaHoraEgresoUTC) || auxfechaHoraEgresoUTC.isBefore(fechaHoraIngresoUTC)) {
                capacidadDisponible--;
            }
        }
        return capacidadDisponible;
    }

    public void registrarProducto(String idProducto, LocalDateTime fechaHoraIngresoUTC, LocalDateTime fechaHoraEgresoUTC) {
        RegistroDeProducto registro = new RegistroDeProducto();
        registro.setIdProducto(idProducto);
        registro.setFechaHoraIngresoUTC(fechaHoraIngresoUTC);
        registro.setFechaHoraIngresoLocal(G4D.toLocal(fechaHoraIngresoUTC,this.husoHorario));
        registro.setFechaHoraEgresoUTC(fechaHoraEgresoUTC);
        registro.setFechaHoraEgresoLocal(G4D.toLocal(fechaHoraEgresoUTC,this.husoHorario));
        this.historialDeProductos.add(registro);
    }

    public void limpiarHistorial(LocalDateTime fechaHoraLimiteUTC) {
        this.historialDeProductos.removeIf(r -> r.getFechaHoraEgresoUTC() != null  && r.getFechaHoraEgresoUTC().isBefore(fechaHoraLimiteUTC));
    }

    public Aeropuerto replicar() {
        Aeropuerto aeropuerto = new Aeropuerto();
        aeropuerto.id = this.id;
        aeropuerto.codigo = this.codigo;
        aeropuerto.ciudad = this.ciudad;
        aeropuerto.pais = this.pais;
        aeropuerto.continente = this.continente;
        aeropuerto.alias = this.alias;
        aeropuerto.husoHorario = this.husoHorario;
        aeropuerto.capacidadMaxima = this.capacidadMaxima;
        aeropuerto.latitudDMS = this.latitudDMS;
        aeropuerto.latitudDEC = this.latitudDEC;
        aeropuerto.longitudDMS = this.longitudDMS;
        aeropuerto.longitudDEC = this.longitudDEC;
        for(RegistroDeProducto registro : this.historialDeProductos) aeropuerto.historialDeProductos.add(registro.replicar());
        return aeropuerto;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setHusoHorario(Integer husoHorario) {
        this.husoHorario = husoHorario;
    }

    public Integer getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(Integer capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public String getLatitudDMS() {
        return latitudDMS;
    }

    public void setLatitudDMS(String latitudDMS) {
        this.latitudDMS = latitudDMS;
    }

    public void setLatitudDMS() {
        this.latitudDMS = G4D.toLatDMS(this.latitudDEC);
    }

    public Double getLatitudDEC() {
        return latitudDEC;
    }

    public void setLatitudDEC(Double latitudDEC) {
        this.latitudDEC = latitudDEC;
    }

    public void setLatitudDEC() {
        this.latitudDEC = G4D.toLatDEC(this.latitudDMS);
    }

    public String getLongitudDMS() {
        return longitudDMS;
    }

    public void setLongitudDMS(String longitudDMS) {
        this.longitudDMS = longitudDMS;
    }

    public void setLongitudDMS() {
        this.longitudDMS = G4D.toLonDMS(this.longitudDEC);
    }

    public Double getLongitudDEC() {
        return longitudDEC;
    }

    public void setLongitudDEC(Double longitudDEC) {
        this.longitudDEC = longitudDEC;
    }

    public void setLongitudDEC() {
        this.longitudDEC = G4D.toLonDEC(this.longitudDMS);
    }

    public List<RegistroDeProducto> getHistorialDeProductos() {
        return historialDeProductos;
    }

    public void setHistorialDeProductos(List<RegistroDeProducto> historialDeProductos) {
        this.historialDeProductos = historialDeProductos;
    }
}
