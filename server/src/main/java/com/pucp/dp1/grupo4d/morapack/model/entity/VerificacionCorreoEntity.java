/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       VerificacionCorreoEntity.java
 [**/

package com.pucp.dp1.grupo4d.morapack.model.entity;

import com.pucp.dp1.grupo4d.morapack.model.enumeration.PropositoVerificacion;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Código de 6 dígitos enviado por correo. En REGISTRO guarda los datos del registro pendiente
 * (contraseña ya hasheada): el USUARIO recién se crea cuando el código es correcto.
 */
@Entity
@Table(name = "VERIFICACION_CORREO")
public class VerificacionCorreoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropositoVerificacion proposito;

    @Column(name = "hash_codigo", nullable = false, length = 255)
    private String hashCodigo;

    @Column(length = 60)
    private String nombres;

    @Column(length = 60)
    private String apellidos;

    @Column(length = 255)
    private String contrasenia;

    @ManyToOne
    @JoinColumn(name = "id_pais")
    private PaisEntity pais;

    @Column(nullable = false)
    private Integer intentos = 0;

    @Column(name = "fh_envio", nullable = false)
    private LocalDateTime fechaHoraEnvio;

    @Column(name = "fh_expiracion", nullable = false)
    private LocalDateTime fechaHoraExpiracion;

    public VerificacionCorreoEntity() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VerificacionCorreoEntity that = (VerificacionCorreoEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public PropositoVerificacion getProposito() { return proposito; }
    public void setProposito(PropositoVerificacion proposito) { this.proposito = proposito; }
    public String getHashCodigo() { return hashCodigo; }
    public void setHashCodigo(String hashCodigo) { this.hashCodigo = hashCodigo; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    public PaisEntity getPais() { return pais; }
    public void setPais(PaisEntity pais) { this.pais = pais; }
    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }
    public LocalDateTime getFechaHoraEnvio() { return fechaHoraEnvio; }
    public void setFechaHoraEnvio(LocalDateTime fechaHoraEnvio) { this.fechaHoraEnvio = fechaHoraEnvio; }
    public LocalDateTime getFechaHoraExpiracion() { return fechaHoraExpiracion; }
    public void setFechaHoraExpiracion(LocalDateTime fechaHoraExpiracion) { this.fechaHoraExpiracion = fechaHoraExpiracion; }
}
