/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       SesionEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Sesión de un dispositivo: dura 12 h desde el inicio (no se extiende). Solo se guarda el hash del token de refresco.
 */
@Entity
@Table(name = "SESION")
public class SesionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @Column(name = "hash_token", nullable = false, unique = true, length = 64)
    private String hashToken;

    @Column(length = 255)
    private String dispositivo;

    @Column(name = "fh_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    @Column(name = "fh_expiracion", nullable = false)
    private LocalDateTime fechaHoraExpiracion;

    @Column(name = "fh_revocacion")
    private LocalDateTime fechaHoraRevocacion;

    public SesionEntity() {}

    public boolean estaVigente(LocalDateTime ahora) {
        return fechaHoraRevocacion == null && fechaHoraExpiracion.isAfter(ahora);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SesionEntity that = (SesionEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UsuarioEntity getUsuario() { return usuario; }
    public void setUsuario(UsuarioEntity usuario) { this.usuario = usuario; }
    public String getHashToken() { return hashToken; }
    public void setHashToken(String hashToken) { this.hashToken = hashToken; }
    public String getDispositivo() { return dispositivo; }
    public void setDispositivo(String dispositivo) { this.dispositivo = dispositivo; }
    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }
    public LocalDateTime getFechaHoraExpiracion() { return fechaHoraExpiracion; }
    public void setFechaHoraExpiracion(LocalDateTime fechaHoraExpiracion) { this.fechaHoraExpiracion = fechaHoraExpiracion; }
    public LocalDateTime getFechaHoraRevocacion() { return fechaHoraRevocacion; }
    public void setFechaHoraRevocacion(LocalDateTime fechaHoraRevocacion) { this.fechaHoraRevocacion = fechaHoraRevocacion; }
}
