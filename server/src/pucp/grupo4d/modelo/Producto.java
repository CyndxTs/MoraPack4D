/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Producto.java 
[**/

package pucp.grupo4d.modelo;

import pucp.grupo4d.util.G4D_Formatter;
import pucp.grupo4d.util.G4D_Formatter.Replicable;

public class Producto implements Replicable<Producto> {
    private String id;
    private String instanteLlegada;
    private String instanteLimite;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private Ruta ruta;

    public Producto() {
        this.id = G4D_Formatter.generateIdentifier("PRO");
    }

    @Override
    public Producto replicar() {
        Producto producto = new Producto();
        producto.id = this.id;
        producto.instanteLlegada = this.instanteLlegada;
        producto.instanteLimite = this.instanteLimite;
        producto.origen = (this.origen != null) ? this.origen.replicar() : null;
        producto.destino = (this.destino != null) ? this.destino.replicar() : null;
        producto.ruta = (this.ruta != null) ? this.ruta.replicar() : null;
        return producto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInstanteLlegada() {
        return instanteLlegada;
    }

    public void setInstanteLlegada(String instanteLlegada) {
        this.instanteLlegada = instanteLlegada;
    }

    public String getInstanteLimite() {
        return instanteLimite;
    }

    public void setInstanteLimite(String instanteLimite) {
        this.instanteLimite = instanteLimite;
    }

    public Aeropuerto getOrigen() {
        return origen;
    }

    public void setOrigen(Aeropuerto origen) {
        this.origen = origen;
    }

    public Aeropuerto getDestino() {
        return destino;
    }

    public void setDestino(Aeropuerto destino) {
        this.destino = destino;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }
}
