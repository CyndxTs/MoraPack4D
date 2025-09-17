package pucp.grupo4d.modelo;

import pucp.grupo4d.util.G4D_Formatter;

public class Vuelo {
    private int id;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private String horaSalida;
    private String horaLlegada;
    private int capacidadTotal;
    private int capacidadDisponible;
    private EstadoVuelo estado;
    private double duracion;

    public Vuelo() {
        this.estado = EstadoVuelo.PROGRAMADO;
        this.duracion = 0.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Aeropuerto getOrigen() {
        return origen;
    }

    public void setOrigen(Aeropuerto origen) {
        this.origen = origen;
    }

    public Aeropuerto getDestino() {
        return destino;
    }

    public void setDestino(Aeropuerto destino) {
        this.destino = destino;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(String horaLlegada) {
        this.horaLlegada = horaLlegada;
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

    public EstadoVuelo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVuelo estado) {
        this.estado = estado;
    }

    public double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        double duracion = G4D_Formatter.toDEC_Hour(horaLlegada) - G4D_Formatter.toDEC_Hour(horaSalida);
        if(duracion < 0) duracion += 24;
        this.duracion = duracion;
    }
}
