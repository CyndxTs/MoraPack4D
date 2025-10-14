/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Vuelo.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.Map;
import pucp.dp1.grupo4d.util.G4D;

public class Vuelo {
    private String id;
    private Integer capacidadDisponible;
    private Double duracion;
    private Double distancia;
    private LocalDateTime fechaHoraSalidaLocal;
    private LocalDateTime fechaHoraSalidaUTC;
    private LocalDateTime fechaHoraLlegadaLocal;
    private LocalDateTime fechaHoraLlegadaUTC;
    private PlanDeVuelo plan;

    public Vuelo() {
        this.id = G4D.getUniqueString("VUE");
        this.capacidadDisponible = 0;
        this.duracion = 0.0;
        this.distancia = 0.0;
    }

    public void instanciarHorarios(LocalDateTime fechaHoraReferencia) {
        LocalDateTime[] rangoUTC = G4D.getDateTimeRange(this.plan.getHoraSalidaUTC(), this.plan.getHoraLlegadaUTC(), fechaHoraReferencia);
        this.fechaHoraSalidaUTC = rangoUTC[0];
        setFechaHoraSalidaLocal();
        this.fechaHoraLlegadaUTC = rangoUTC[1];
        setFechaHoraLlegadaLocal();
    }

    public Vuelo replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, LoteDeProductos> poolLotes) {
        Vuelo vuelo = new Vuelo();
        vuelo.id = this.id;
        vuelo.capacidadDisponible = this.capacidadDisponible;
        vuelo.duracion = this.duracion;
        vuelo.distancia = this.distancia;
        vuelo.fechaHoraSalidaLocal = this.fechaHoraSalidaLocal;
        vuelo.fechaHoraSalidaUTC = this.fechaHoraSalidaUTC;
        vuelo.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal;
        vuelo.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC;
        vuelo.plan = (this.plan != null) ? this.plan.replicar(poolAeropuertos, poolLotes) : null;
        return vuelo;
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
        this.duracion = G4D.getElapsedHours(this.fechaHoraSalidaUTC, this.fechaHoraLlegadaUTC);
    }

    public void setDuracion(Double duracion) {
        this.duracion = duracion;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia() {
        this.distancia = this.plan.getDistancia();
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    public LocalDateTime getFechaHoraSalidaLocal() {
        return fechaHoraSalidaLocal;
    }

    public void setFechaHoraSalidaLocal() {
        this.fechaHoraSalidaLocal = G4D.toLocal(this.fechaHoraSalidaUTC, this.plan.getOrigen().getHusoHorario());
    }

    public void setFechaHoraSalidaLocal(LocalDateTime fechaHoraSalidaLocal) {
        this.fechaHoraSalidaLocal = fechaHoraSalidaLocal;
    }

    public LocalDateTime getFechaHoraSalidaUTC() {
        return fechaHoraSalidaUTC;
    }

    public void setFechaHoraSalidaUTC() {
        this.fechaHoraSalidaUTC = G4D.toUTC(this.fechaHoraSalidaLocal, this.plan.getOrigen().getHusoHorario());
    }

    public void setFechaHoraSalidaUTC(LocalDateTime fechaHoraSalidaUTC) {
        this.fechaHoraSalidaUTC = fechaHoraSalidaUTC;
    }

    public LocalDateTime getFechaHoraLlegadaLocal() {
        return fechaHoraLlegadaLocal;
    }

    public void setFechaHoraLlegadaLocal() {
        this.fechaHoraLlegadaLocal = G4D.toLocal(this.fechaHoraLlegadaUTC, this.plan.getDestino().getHusoHorario());
    }

    public void setFechaHoraLlegadaLocal(LocalDateTime fechaHoraLlegadaLocal) {
        this.fechaHoraLlegadaLocal = fechaHoraLlegadaLocal;
    }

    public LocalDateTime getFechaHoraLlegadaUTC() {
        return fechaHoraLlegadaUTC;
    }

    public void setFechaHoraLlegadaUTC() {
        this.fechaHoraLlegadaUTC = G4D.toUTC(this.fechaHoraLlegadaLocal, this.plan.getDestino().getHusoHorario());
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
