/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoDTO.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.dto;

import java.util.ArrayList;
import java.util.List;

public class AeropuertoDTO implements DTO {
    private String codigo;
    private String ciudad;
    private String pais;
    private String continente;
    private String alias;
    private Integer husoHorario;
    private Integer capacidad;
    private Double latitudDEC;
    private Double longitudDEC;
    private Boolean esSede;
    private List<RegistroDTO> registros;

    public AeropuertoDTO() {
        this.registros = new ArrayList<>();
    }

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
    public Double getLatitudDEC() { return latitudDEC; }
    public void setLatitudDEC(Double latitudDEC) { this.latitudDEC = latitudDEC; }
    public Double getLongitudDEC() { return longitudDEC; }
    public void setLongitudDEC(Double longitudDEC) { this.longitudDEC = longitudDEC; }
    public Boolean getEsSede() { return esSede; }
    public void setEsSede(Boolean esSede) { this.esSede = esSede; }
    public List<RegistroDTO> getRegistros() { return registros; }
    public void setRegistros(List<RegistroDTO> registros) { this.registros = registros; }
}
