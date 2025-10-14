package pucp.dp1.grupo4d.modelo;

import java.util.ArrayList;
import java.util.List;
import pucp.dp1.grupo4d.util.G4D;

public class LoteDeProductos {
    private String id;
    private Integer tamanio;
    private List<Producto> productos;

    public LoteDeProductos() {
        this.id = G4D.getUniqueString("LOT");
        this.tamanio = 0;
        this.productos = new ArrayList<>();
    }

    public LoteDeProductos replicar() {
        LoteDeProductos lote = new LoteDeProductos();
        lote.id = this.id;
        lote.tamanio = this.tamanio;
        for(Producto p : this.productos) lote.productos.add(p.replicar());
        return lote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoteDeProductos that = (LoteDeProductos) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTamanio() {
        return tamanio;
    }

    public void setTamanio(Integer tamanio) {
        this.tamanio = tamanio;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos() {
        this.productos = new ArrayList<>();
        for(int i = 0; i < this.tamanio; i++) this.productos.add(new Producto());
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
