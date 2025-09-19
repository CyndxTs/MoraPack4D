/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroProducto.java 
[**/

package pucp.grupo4d.modelo;

public class RegistroProducto {
    private String idProducto;
    private String instanteLlegada;
    private String instanteSalida;

    public RegistroProducto() {}

    public RegistroProducto replicar() {
        RegistroProducto registroProducto = new RegistroProducto();
        registroProducto.idProducto = this.idProducto;
        registroProducto.instanteLlegada = this.instanteLlegada;
        registroProducto.instanteSalida = this.instanteSalida;
        return registroProducto;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getInstanteLlegada() {
        return instanteLlegada;
    }

    public void setInstanteLlegada(String instanteLlegada) {
        this.instanteLlegada = instanteLlegada;
    }

    public String getInstanteSalida() {
        return instanteSalida;
    }

    public void setInstanteSalida(String instanteSalida) {
        this.instanteSalida = instanteSalida;
    }
}
