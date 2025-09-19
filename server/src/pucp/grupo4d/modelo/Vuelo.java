/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Vuelo.java 
[**/

package pucp.grupo4d.modelo;

import pucp.grupo4d.util.G4D_Formatter;

public class Vuelo {
    private String id;
    private Integer capacidadDisponible;
    private PlanDeVuelo plan;
    private String instanteSalida;
    private String instanteLlegada;

    public Vuelo() {
        this.id = G4D_Formatter.generateIdentifier("VUE");
        this.capacidadDisponible = 0;
    }

    public Vuelo replicar() {
        Vuelo vuelo = new Vuelo();
        vuelo.id = this.id;
        vuelo.capacidadDisponible = this.capacidadDisponible;
        vuelo.plan = (this.plan != null) ? this.plan.replicar() : null;
        vuelo.instanteSalida = this.instanteSalida;
        vuelo.instanteLlegada = this.instanteLlegada;
        return vuelo;
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

    public String getInstanteSalida() {
        return instanteSalida;
    }

    public void setInstanteSalida(String instanteSalida) {
        this.instanteSalida = instanteSalida;
    }

    public String getInstanteLlegada() {
        return instanteLlegada;
    }

    public void setInstanteLlegada(String instanteLlegada) {
        this.instanteLlegada = instanteLlegada;
    }
}
