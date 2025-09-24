/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Cliente.java 
[**/

package pucp.grupo4d.modelo;

import pucp.grupo4d.util.G4D_Util;

public class Cliente {
    private String id;
    private String nombre;

    public Cliente() {
        this.id = G4D_Util.generateIdentifier("CLI");
    }

    public Cliente replicar() {
        System.out.println("R-CLIENTE");
        Cliente cliente = new Cliente();
        cliente.id = this.id;
        cliente.nombre = this.nombre;
        return cliente;
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
}
