/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Vuelo.java 
[**/

package pucp.grupo4d.modelo;

import java.time.LocalDateTime;

import pucp.grupo4d.util.G4D_Formatter;
import pucp.grupo4d.util.G4D_Formatter.Replicable;

public class Vuelo implements Replicable<Vuelo> {
    private String id;
    private Integer capacidadDisponible;
    private Double duracion;
    private LocalDateTime fechaHoraSalidaLocal;
    private LocalDateTime fechaHoraSalidaUTC;
    private LocalDateTime fechaHoraLlegadaLocal;
    private LocalDateTime fechaHoraLlegadaUTC;
    private PlanDeVuelo plan;

    public Vuelo() {
        this.id = G4D_Formatter.generateIdentifier("VUE");
        this.capacidadDisponible = 0;
        this.duracion = 0.0;
    }

    public void instanciarHorarios(LocalDateTime fechaHoraReferencia) {
        this.fechaHoraSalidaLocal = G4D_Formatter.toDateTime(this.plan.getHoraSalida(),fechaHoraReferencia);
        this.fechaHoraSalidaUTC = G4D_Formatter.toUTC(this.fechaHoraSalidaLocal,this.plan.getOrigen().getHusoHorario());
        this.fechaHoraLlegadaLocal = G4D_Formatter.toDateTime(this.plan.getHoraLlegada(),fechaHoraReferencia);
        this.fechaHoraLlegadaUTC = G4D_Formatter.toUTC(this.fechaHoraLlegadaLocal,this.plan.getDestino().getHusoHorario());
        if(this.fechaHoraLlegadaUTC.isBefore(fechaHoraSalidaUTC)) {
            this.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal.plusDays(1);
            this.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC.plusDays(1);
        }
        if(this.fechaHoraSalidaUTC.isBefore(fechaHoraReferencia)) {
            this.fechaHoraSalidaLocal = this.fechaHoraSalidaLocal.plusDays(1);
            this.fechaHoraSalidaUTC = this.fechaHoraSalidaUTC.plusDays(1);
            this.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal.plusDays(1);
            this.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC.plusDays(1);
        }
    }

    @Override
    public Vuelo replicar() {
        Vuelo copia = new Vuelo();
        copia.id = this.id;
        copia.capacidadDisponible = this.capacidadDisponible;
        copia.duracion = this.duracion;
        copia.fechaHoraSalidaLocal = this.fechaHoraSalidaLocal;
        copia.fechaHoraSalidaUTC = this.fechaHoraSalidaUTC;
        copia.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal;
        copia.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC;
        copia.plan = (this.plan != null) ? this.plan.replicar() : null;
        return copia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vuelo vuelo = (Vuelo) o;
        return id != null && id.equals(vuelo.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getCapacidadDisponible() {
        return capacidadDisponible;
    }

    public void setCapacidadDisponible(Integer capacidadDisponible) {
        this.capacidadDisponible = capacidadDisponible;
    }

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        this.duracion = G4D_Formatter.calculateElapsedHours(this.fechaHoraSalidaUTC,this.fechaHoraLlegadaUTC);
    }

    public LocalDateTime getFechaHoraSalidaLocal() {
        return fechaHoraSalidaLocal;
    }

    public void setFechaHoraSalidaLocal(LocalDateTime fechaHoraSalidaLocal) {
        this.fechaHoraSalidaLocal = fechaHoraSalidaLocal;
    }

    public LocalDateTime getFechaHoraSalidaUTC() {
        return fechaHoraSalidaUTC;
    }

    public void setFechaHoraSalidaUTC(LocalDateTime fechaHoraSalidaUTC) {
        this.fechaHoraSalidaUTC = fechaHoraSalidaUTC;
    }

    public LocalDateTime getFechaHoraLlegadaLocal() {
        return fechaHoraLlegadaLocal;
    }

    public void setFechaHoraLlegadaLocal(LocalDateTime fechaHoraLlegadaLocal) {
        this.fechaHoraLlegadaLocal = fechaHoraLlegadaLocal;
    }

    public LocalDateTime getFechaHoraLlegadaUTC() {
        return fechaHoraLlegadaUTC;
    }

    public void setFechaHoraLlegadaUTC(LocalDateTime fechaHoraLlegadaUTC) {
        this.fechaHoraLlegadaUTC = fechaHoraLlegadaUTC;
    }

    public PlanDeVuelo getPlan() {
        return plan;
    }

    public void setPlan(PlanDeVuelo plan) {
        this.plan = plan;
    }
}
