package com.pucp.dp1.grupo4d.morapack.model.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AeropuertoResponse {
    private Integer id;
    private String codigo;
    private String ciudad;
    private String pais;
    private String continente;
    private String alias;
    private Integer husoHorario;
    private Integer capacidad;
    private String latitudDMS;
    private String longitudDMS;
    private Double latitudDEC;
    private Double longitudDEC;
    private Boolean esSede;
}