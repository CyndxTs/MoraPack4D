/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ContinenteEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "CONTINENTE")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ContinenteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @OneToMany(mappedBy = "continente")
    private List<PaisEntity> paises = new ArrayList<>();

    public ContinenteEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContinenteEntity that = (ContinenteEntity) o;
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
    public List<PaisEntity> getPaises() { return paises; }
    public void setPaises(List<PaisEntity> paises) { this.paises = paises; }
}
