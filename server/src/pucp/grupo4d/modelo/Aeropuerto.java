package pucp.grupo4d.modelo;

import pucp.grupo4d.util.G4D_Formatter;

public class Aeropuerto {
    private int id;
    private String codigo;
    private String ciudad;
    private String pais;
    private String continente;
    private String alias;
    private int husoHorario;
    private int capacidadTotal;
    private int capacidadDisponible;
    private String latitudDMS;
    private double latitudDEC;
    private String longitudDMS;
    private double longitudDEC;

    public Aeropuerto() {}

    public double calcularDistancia(Aeropuerto aDest) {
        return G4D_Formatter.calcularDistancia(latitudDEC,longitudDEC,aDest.latitudDEC,aDest.longitudDEC);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getHusoHorario() {
        return husoHorario;
    }

    public void setHusoHorario(int husoHorario) {
        this.husoHorario = husoHorario;
    }

    public int getCapacidadTotal() {
        return capacidadTotal;
    }

    public void setCapacidadTotal(int capacidadTotal) {
        this.capacidadTotal = capacidadTotal;
    }

    public int getCapacidadDisponible() {
        return capacidadDisponible;
    }

    public void setCapacidadDisponible(int capacidadDisponible) {
        this.capacidadDisponible = capacidadDisponible;
    }

    public String getLatitud_DMS() {
        return latitudDMS;
    }

    public double getLatitud_DEC() {
        return latitudDEC;
    }

    public void setLatitud(String latitudDMS) {
        this.latitudDMS = latitudDMS;
        this.latitudDEC = G4D_Formatter.toLatDEC(latitudDMS);
    }

    public void setLatitud(double latitudDEC) {
        this.latitudDEC = latitudDEC;
        this.latitudDMS = G4D_Formatter.toLatDMS(latitudDEC);
    }

    public String getLongitud_DMS() {
        return longitudDMS;
    }

    public double getLongitud_DEC() {
        return longitudDEC;
    }

    public void setLongitud(String longitudDMS) {
        this.longitudDMS = longitudDMS;
        this.longitudDEC = G4D_Formatter.toLonDEC(longitudDMS);
    }

    public void setLongitud(double longitudDEC) {
        this.longitudDEC = longitudDEC;
        this.longitudDMS = G4D_Formatter.toLonDMS(longitudDEC);
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
}
