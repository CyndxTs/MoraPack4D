/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Cliente.java 
[**/

package pucp.dp1.grupo4d.modelo;

import pucp.dp1.grupo4d.util.G4D;

public class Cliente {
    private String id;
    private String nombre;
    private String correo;

    public Cliente() {
        this.id = G4D.getUniqueString("CLI");
    }

    public Cliente replicar() {
        Cliente cliente = new Cliente();
        cliente.id = this.id;
        cliente.nombre = this.nombre;
        cliente.correo = this.correo;
        return cliente;
    }

    public Cliente(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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