/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PaisEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PAIS")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaisEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(name = "huso_horario", nullable = false)
    private Integer husoHorario;

    @ManyToOne
    @JoinColumn(name = "id_continente", nullable = false)
    private ContinenteEntity continente;

    @OneToMany(mappedBy = "pais")
    private List<AeropuertoEntity> aeropuertos = new ArrayList<>();

    public PaisEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaisEntity that = (PaisEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getHusoHorario() { return husoHorario; }
    public void setHusoHorario(Integer husoHorario) { this.husoHorario = husoHorario; }
    public ContinenteEntity getContinente() { return continente; }
    public void setContinente(ContinenteEntity continente) { this.continente = continente; }
    public List<AeropuertoEntity> getAeropuertos() { return aeropuertos; }
    public void setAeropuertos(List<AeropuertoEntity> aeropuertos) { this.aeropuertos = aeropuertos; }
}
