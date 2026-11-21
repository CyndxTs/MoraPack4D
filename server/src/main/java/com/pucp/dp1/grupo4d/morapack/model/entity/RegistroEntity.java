/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "REGISTRO")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RegistroEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "sigue_vigente", nullable = false)
    private Boolean sigueVigente = true;

    @Column(name = "fh_ingreso", nullable = false)
    private LocalDateTime fechaHoraIngreso;

    @Column(name = "fh_egreso", nullable = false)
    private LocalDateTime fechaHoraEgreso;

    @ManyToOne
    @JoinColumn(name = "id_aeropuerto", nullable = false)
    private AeropuertoEntity aeropuerto;

    @ManyToOne
    @JoinColumn(name = "id_lote", nullable = false)
    private LoteEntity lote;

    public RegistroEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroEntity that = (RegistroEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Boolean getSigueVigente() { return sigueVigente; }
    public void setSigueVigente(Boolean sigueVigente) { this.sigueVigente = sigueVigente; }
    public LocalDateTime getFechaHoraIngreso() { return fechaHoraIngreso; }
    public void setFechaHoraIngreso(LocalDateTime fechaHoraIngreso) { this.fechaHoraIngreso = fechaHoraIngreso; }
    public LocalDateTime getFechaHoraEgreso() { return fechaHoraEgreso; }
    public void setFechaHoraEgreso(LocalDateTime fechaHoraEgreso) { this.fechaHoraEgreso = fechaHoraEgreso; }
    public AeropuertoEntity getAeropuerto() { return aeropuerto; }
    public void setAeropuerto(AeropuertoEntity aeropuerto) { this.aeropuerto = aeropuerto; }
    public LoteEntity getLote() { return lote; }
    public void setLote(LoteEntity lote) { this.lote = lote; }
}
