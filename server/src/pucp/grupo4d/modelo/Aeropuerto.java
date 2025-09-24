/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Aeropuerto.java 
[**/

package pucp.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import pucp.grupo4d.util.G4D_Formatter;

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
        this.id = G4D_Formatter.generateIdentifier("AER");
        this.historialDeProductos = new ArrayList<>();
    }

    public Double obtenerDistanciaHasta(Aeropuerto aDest) {
        return G4D_Formatter.calculateGeodesicDistance(latitudDEC,longitudDEC,aDest.latitudDEC,aDest.longitudDEC);
    }

    public Integer obtenerCapacidadDisponible(LocalDateTime fechaHoraRevision) {
        Integer capacidadDisponible = this.capacidadMaxima;
        for(RegistroDeProducto registro : this.historialDeProductos) {
            LocalDateTime fechaHoraEgresoUTC = registro.getFechaHoraEgresoUTC();
            if(fechaHoraEgresoUTC == null) {
                fechaHoraEgresoUTC = registro.getFechaHoraIngresoUTC().plusHours(Problematica.MAX_HORAS_RECOJO.longValue());
            }
            if(fechaHoraEgresoUTC.isAfter(fechaHoraRevision)) {
                capacidadDisponible--;
            }
        }
        return capacidadDisponible;
    }

    public void registrarProducto(String idProducto, LocalDateTime fechaHoraIngreso, LocalDateTime fechaHoraEgreso) {
        RegistroDeProducto registro = new RegistroDeProducto();
        registro.setId(idProducto);
        registro.setFechaHoraIngresoUTC(fechaHoraIngreso);
        registro.setFechaHoraIngresoLocal(G4D_Formatter.toLocal(fechaHoraIngreso,this.husoHorario));
        registro.setFechaHoraEgresoUTC(fechaHoraEgreso);
        registro.setFechaHoraEgresoLocal(G4D_Formatter.toLocal(fechaHoraEgreso,this.husoHorario));
        historialDeProductos.add(registro);
    }

    public Aeropuerto replicar() {
        System.out.println(">>>>>> R-AEROPUERTO");
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

    public double getLatitudDEC() {
        return latitudDEC;
    }

    public void setLatitud(String latitudDMS) {
        this.latitudDMS = latitudDMS;
        this.latitudDEC = G4D_Formatter.toLatDEC(latitudDMS);
    }

    public void setLatitud(Double latitudDEC) {
        this.latitudDEC = latitudDEC;
        this.latitudDMS = G4D_Formatter.toLatDMS(latitudDEC);
    }

    public String getLongitudDMS() {
        return longitudDMS;
    }

    public Double getLongitudDEC() {
        return longitudDEC;
    }

    public void setLongitud(String longitudDMS) {
        this.longitudDMS = longitudDMS;
        this.longitudDEC = G4D_Formatter.toLonDEC(longitudDMS);
    }

    public void setLongitud(Double longitudDEC) {
        this.longitudDEC = longitudDEC;
        this.longitudDMS = G4D_Formatter.toLonDMS(longitudDEC);
    }
}
