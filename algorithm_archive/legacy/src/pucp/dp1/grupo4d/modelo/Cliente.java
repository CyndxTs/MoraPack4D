/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioEntity.java
[**/

package pucp.dp1.grupo4d.modelo;

import pucp.dp1.grupo4d.util.G4D;

public class Cliente {
    private String id;
    private String codigo;
    private String nombre;
    private String correo;

    public Cliente() {
        this.id = G4D.getUniqueString("CLI");
        this.nombre = G4D.getUniqueName();
    }

    public Cliente replicar() {
        Cliente cliente = new Cliente();
        cliente.id = this.id;
        cliente.nombre = this.nombre;
        cliente.correo = this.correo;
        return cliente;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", this.codigo, this.nombre);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente that = (Cliente) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}