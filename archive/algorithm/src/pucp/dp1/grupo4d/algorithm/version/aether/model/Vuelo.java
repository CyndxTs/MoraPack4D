/**]
 >> Project:    MoraPack
 >> Version:    Aether
 >> Author:     Grupo 4D
 >> File:       Vuelo.java
[**/

package pucp.dp1.grupo4d.algorithm.version.aether.model;

import java.time.LocalDateTime;
import java.util.Map;
import pucp.dp1.grupo4d.util.G4D;

public class Vuelo {
    private String codigo;
    private Integer capacidadDisponible;
    private LocalDateTime fechaHoraSalidaLocal;
    private LocalDateTime fechaHoraSalidaUTC;
    private LocalDateTime fechaHoraLlegadaLocal;
    private LocalDateTime fechaHoraLlegadaUTC;
    private Plan plan;

    public Vuelo() {
        this.codigo = G4D.Generator.getUniqueString("VUE");
        this.capacidadDisponible = 0;
    }

    public Vuelo replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Plan> poolPlanes) {
        Vuelo vuelo = new Vuelo();
        vuelo.codigo = this.codigo;
        vuelo.capacidadDisponible = this.capacidadDisponible;
        vuelo.fechaHoraSalidaLocal = this.fechaHoraSalidaLocal;
        vuelo.fechaHoraSalidaUTC = this.fechaHoraSalidaUTC;
        vuelo.fechaHoraLlegadaLocal = this.fechaHoraLlegadaLocal;
        vuelo.fechaHoraLlegadaUTC = this.fechaHoraLlegadaUTC;
        vuelo.plan = (this.plan != null) ? poolPlanes.computeIfAbsent(this.plan.getCodigo(), codigo -> this.plan.replicar(poolAeropuertos, poolLotes)) : null;
        return vuelo;
    }

    public void instanciarHorarios(LocalDateTime fechaHoraReferencia) {
        LocalDateTime[] rangoUTC = G4D.getDateTimeRange(this.plan.getHoraSalidaUTC(), this.plan.getHoraLlegadaUTC(), fechaHoraReferencia);
        this.fechaHoraSalidaUTC = rangoUTC[0];
        this.setFechaHoraSalidaLocal();
        this.fechaHoraLlegadaUTC = rangoUTC[1];
        this.setFechaHoraLlegadaLocal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vuelo that = (Vuelo) o;
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

    public Integer getCapacidadDisponible() {
        return capacidadDisponible;
    }

    public void setCapacidadDisponible(Integer capacidadDisponible) {
        this.capacidadDisponible = capacidadDisponible;
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

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }
}
