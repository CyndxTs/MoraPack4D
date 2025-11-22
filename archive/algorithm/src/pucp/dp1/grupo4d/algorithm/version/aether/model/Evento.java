/**]
 >> Project:    MoraPack
 >> Version:    Aether
 >> Author:     Grupo 4D
 >> File:       Evento.java 
[**/

package pucp.dp1.grupo4d.algorithm.version.aether.model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import pucp.dp1.grupo4d.algorithm.version.aether.enums.TipoEvento;
import pucp.dp1.grupo4d.util.G4D;

public class Evento {
    private String codigo;
    private TipoEvento tipo;
    private LocalDateTime fechaHoraInicioLocal;
    private LocalDateTime fechaHoraInicioUTC;
    private LocalDateTime fechaHoraFinLocal;
    private LocalDateTime fechaHoraFinUTC;
    private LocalTime horaSalidaReprogramadaLocal;
    private LocalTime horaSalidaReprogramadaUTC;
    private LocalTime horaLlegadaReprogramadaLocal;
    private LocalTime horaLlegadaReprogramadaUTC;

    public Evento() {
        this.codigo = G4D.Generator.getUniqueString("EVE");
        this.tipo = TipoEvento.REPROGRAMACION;
    }

    public Evento replicar() {
        Evento evento = new Evento();
        evento.codigo = this.codigo;
        evento.tipo = this.tipo;
        evento.fechaHoraInicioLocal = this.fechaHoraInicioLocal;
        evento.fechaHoraInicioUTC = this.fechaHoraInicioUTC;
        evento.fechaHoraFinLocal = this.fechaHoraFinLocal;
        evento.fechaHoraFinUTC = this.fechaHoraFinUTC;
        evento.horaSalidaReprogramadaLocal = this.horaSalidaReprogramadaLocal;
        evento.horaSalidaReprogramadaUTC = this.horaSalidaReprogramadaUTC;
        evento.horaLlegadaReprogramadaLocal = this.horaLlegadaReprogramadaLocal;
        evento.horaLlegadaReprogramadaUTC = this.horaLlegadaReprogramadaUTC;
        return evento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Evento that = (Evento) o;
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

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFechaHoraInicioLocal() {
        return fechaHoraInicioLocal;
    }

    public void setFechaHoraInicioLocal(LocalDateTime fechaHInicioLocal) {
        this.fechaHoraInicioLocal = fechaHInicioLocal;
    }

    public LocalDateTime getFechaHoraInicioUTC() {
        return fechaHoraInicioUTC;
    }

    public void setFechaHoraInicioUTC(LocalDateTime fechaHoraInicioUTC) {
        this.fechaHoraInicioUTC = fechaHoraInicioUTC;
    }

    public LocalDateTime getFechaHoraFinLocal() {
        return fechaHoraFinLocal;
    }

    public void setFechaHoraFinLocal(LocalDateTime fechaHoraFinLocal) {
        this.fechaHoraFinLocal = fechaHoraFinLocal;
    }

    public LocalDateTime getFechaHoraFinUTC() {
        return fechaHoraFinUTC;
    }

    public void setFechaHoraFinUTC(LocalDateTime fechaHoraFinUTC) {
        this.fechaHoraFinUTC = fechaHoraFinUTC;
    }

    public LocalTime getHoraSalidaReprogramadaLocal() {
        return horaSalidaReprogramadaLocal;
    }

    public void setHoraSalidaReprogramadaLocal(LocalTime horaSalidaReprogramadaLocal) {
        this.horaSalidaReprogramadaLocal = horaSalidaReprogramadaLocal;
    }

    public LocalTime getHoraSalidaReprogramadaUTC() {
        return horaSalidaReprogramadaUTC;
    }

    public void setHoraSalidaReprogramadaUTC(LocalTime horaSalidaReprogramadaUTC) {
        this.horaSalidaReprogramadaUTC = horaSalidaReprogramadaUTC;
    }

    public LocalTime getHoraLlegadaReprogramadaLocal() {
        return horaLlegadaReprogramadaLocal;
    }

    public void setHoraLlegadaReprogramadaLocal(LocalTime horaLlegadaReprogramadaLocal) {
        this.horaLlegadaReprogramadaLocal = horaLlegadaReprogramadaLocal;
    }

    public LocalTime getHoraLlegadaReprogramadaUTC() {
        return horaLlegadaReprogramadaUTC;
    }

    public void setHoraLlegadaReprogramadaUTC(LocalTime horaLlegadaReprogramadaUTC) {
        this.horaLlegadaReprogramadaUTC = horaLlegadaReprogramadaUTC;
    }
}
