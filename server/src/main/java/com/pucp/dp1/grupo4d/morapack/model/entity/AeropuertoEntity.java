/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "AEROPUERTO", schema = "morapack4d")
public class AeropuertoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "codigo", length = 4, nullable = false, unique = true)
    private String codigo;

    @Column(name = "ciudad", length = 30, nullable = false)
    private String ciudad;

    @Column(name = "pais", length = 20, nullable = false)
    private String pais;

    @Column(name = "continente", length = 20, nullable = false)
    private String continente;

    @Column(name = "alias", length = 4, nullable = false, unique = true)
    private String alias;

    @Column(name = "huso_horario", nullable = false)
    private Integer husoHorario;

    @Column(name = "capacidad", nullable = false)
    private Integer capacidad;

    @Column(name = "latitud_dms", length = 20, nullable = false)
    private String latitudDMS;

    @Column(name = "longitud_dms", length = 20, nullable = false)
    private String longitudDMS;

    @Column(name = "latitud_dec", nullable = false)
    private Double latitudDEC;

    @Column(name = "longitud_dec", nullable = false)
    private Double longitudDEC;

    @Column(name = "es_sede")
    private Boolean esSede = false;

    @OneToMany(mappedBy = "destino", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PedidoEntity> pedidosComoDestino = new ArrayList<>();

    @OneToMany(mappedBy = "aeropuerto", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<RegistroEntity> registros = new ArrayList<>();

    @OneToMany(mappedBy = "origen", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PlanEntity> planesComoOrigen = new ArrayList<>();

    @OneToMany(mappedBy = "destino", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<PlanEntity> planesComoDestino = new ArrayList<>();

    @OneToMany(mappedBy = "origen", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<RutaEntity> rutasComoOrigen = new ArrayList<>();

    @OneToMany(mappedBy = "destino", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<RutaEntity> rutasComoDestino = new ArrayList<>();

    public AeropuertoEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AeropuertoEntity)) return false;
        AeropuertoEntity that = (AeropuertoEntity) o;
        return Objects.equals(codigo, that.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getContinente() { return continente; }
    public void setContinente(String continente) { this.continente = continente; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public Integer getHusoHorario() { return husoHorario; }
    public void setHusoHorario(Integer husoHorario) { this.husoHorario = husoHorario; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public String getLatitudDMS() { return latitudDMS; }
    public void setLatitudDMS(String latitudDMS) { this.latitudDMS = latitudDMS; }
    public String getLongitudDMS() { return longitudDMS; }
    public void setLongitudDMS(String longitudDMS) { this.longitudDMS = longitudDMS; }
    public Double getLatitudDEC() { return latitudDEC; }
    public void setLatitudDEC(Double latitudDEC) { this.latitudDEC = latitudDEC; }
    public Double getLongitudDEC() { return longitudDEC; }
    public void setLongitudDEC(Double longitudDEC) { this.longitudDEC = longitudDEC; }
    public Boolean getEsSede() { return esSede; }
    public void setEsSede(Boolean esSede) { this.esSede = esSede; }
    public List<PedidoEntity> getPedidosComoDestino() { return pedidosComoDestino; }
    public void setPedidosComoDestino(List<PedidoEntity> pedidosComoDestino) { this.pedidosComoDestino = pedidosComoDestino; }
    public List<RegistroEntity> getRegistros() { return registros; }
    public void setRegistros(List<RegistroEntity> registros) { this.registros = registros; }
    public List<PlanEntity> getPlanesComoOrigen() { return planesComoOrigen; }
    public void setPlanesComoOrigen(List<PlanEntity> planesComoOrigen) { this.planesComoOrigen = planesComoOrigen; }
    public List<PlanEntity> getPlanesComoDestino() { return planesComoDestino; }
    public void setPlanesComoDestino(List<PlanEntity> planesComoDestino) { this.planesComoDestino = planesComoDestino; }
    public List<RutaEntity> getRutasComoOrigen() { return rutasComoOrigen; }
    public void setRutasComoOrigen(List<RutaEntity> rutasComoOrigen) { this.rutasComoOrigen = rutasComoOrigen; }
    public List<RutaEntity> getRutasComoDestino() { return rutasComoDestino; }
    public void setRutasComoDestino(List<RutaEntity> rutasComoDestino) { this.rutasComoDestino = rutasComoDestino; }
}
