/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Vuelo.java 
[**/

package pucp.grupo4d.modelo;

import pucp.grupo4d.util.G4D_Formatter;
import pucp.grupo4d.util.G4D_Formatter.Replicable;

public class Vuelo implements Replicable<Vuelo> {
    private String id;
    private Integer capacidadDisponible;
    private PlanDeVuelo plan;
    private String instanteSalidaLocal;
    private String instanteSalidaUniversal;
    private String instanteLlegadaLocal;
    private String instanteLlegadaUniversal;
    private Double duracion;

    public Vuelo() {
        this.id = G4D_Formatter.generateIdentifier("VUE");
        this.capacidadDisponible = 0;
        this.duracion = 0.0;
    }

    @Override
    public Vuelo replicar() {
        Vuelo vuelo = new Vuelo();
        vuelo.id = this.id;
        vuelo.capacidadDisponible = this.capacidadDisponible;
        vuelo.plan = (this.plan != null) ? this.plan.replicar() : null;
        vuelo.instanteSalidaLocal = this.instanteSalidaLocal;
        vuelo.instanteSalidaUniversal = this.instanteSalidaUniversal;
        vuelo.instanteLlegadaLocal = this.instanteLlegadaLocal;
        vuelo.instanteLlegadaUniversal = this.instanteLlegadaUniversal;
        vuelo.duracion = this.duracion;
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

    public PlanDeVuelo getPlan() {
        return plan;
    }

    public void setPlan(PlanDeVuelo plan) {
        this.plan = plan;
    }

    public String getInstanteSalidaLocal() {
        return instanteSalidaLocal;
    }

    public String getInstanteSalidaUniversal() {
        return instanteSalidaUniversal;
    }

    public void setInstantes(String instanteReferencia) {
        this.instanteSalidaLocal = G4D_Formatter.toDateTimeString(plan.getHoraSalida(),instanteReferencia);
        this.instanteSalidaUniversal = G4D_Formatter.toUTC_DateTimeString(this.instanteSalidaLocal,this.plan.getOrigen().getHusoHorario());
        this.instanteLlegadaLocal = G4D_Formatter.toDateTimeString(plan.getHoraLlegada(),instanteReferencia);
        this.instanteLlegadaUniversal = G4D_Formatter.toUTC_DateTimeString(this.instanteLlegadaLocal,this.plan.getDestino().getHusoHorario());
        if(G4D_Formatter.isOffset_DateTime(this.instanteLlegadaUniversal,this.instanteSalidaUniversal)) {
            this.instanteLlegadaLocal = G4D_Formatter.addDay(this.instanteLlegadaLocal);
            this.instanteLlegadaUniversal = G4D_Formatter.addDay(this.instanteLlegadaUniversal);
        }
        if(G4D_Formatter.isOffset_DateTime(this.instanteSalidaUniversal, instanteReferencia)) {
            this.instanteSalidaLocal = G4D_Formatter.addDay(this.instanteSalidaLocal);
            this.instanteSalidaUniversal = G4D_Formatter.addDay(this.instanteSalidaUniversal);
            this.instanteLlegadaLocal = G4D_Formatter.addDay(this.instanteLlegadaLocal);
            this.instanteLlegadaUniversal = G4D_Formatter.addDay(this.instanteLlegadaUniversal);
        }
    }

    public String getInstanteLlegadaLocal() {
        return instanteLlegadaLocal;
    }

    public String getInstanteLlegadaUniversal() {
        return instanteLlegadaUniversal;
    }

    public Double getDuracion() {
        return duracion;
    }

    public void setDuracion() {
        if(this.instanteSalidaUniversal == null || this.instanteLlegadaUniversal == null) this.duracion = 0.0;
        else this.duracion = G4D_Formatter.calculateElapsed_DateTime(instanteSalidaUniversal, instanteLlegadaUniversal);
    }
}
