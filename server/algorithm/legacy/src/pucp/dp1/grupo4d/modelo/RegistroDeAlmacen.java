/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroDeProducto.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import pucp.dp1.grupo4d.util.G4D;

public class RegistroDeAlmacen {
    private String id;
    private String idRuta;
    private Integer cantidad;
    private LocalDateTime fechaHoraIngresoLocal;
    private LocalDateTime fechaHoraIngresoUTC;
    private LocalDateTime fechaHoraEgresoLocal;
    private LocalDateTime fechaHoraEgresoUTC;
    private List<String> productos;

    public RegistroDeAlmacen() {
        this.id = G4D.getUniqueString("REG");
    }

    public RegistroDeAlmacen replicar() {
        RegistroDeAlmacen registro = new RegistroDeAlmacen();
        registro.id = this.id;
        registro.idRuta = this.idRuta;
        registro.cantidad = this.cantidad;
        registro.fechaHoraIngresoLocal = this.fechaHoraIngresoLocal;
        registro.fechaHoraIngresoUTC = this.fechaHoraIngresoUTC;
        registro.fechaHoraEgresoLocal = this.fechaHoraEgresoLocal;
        registro.fechaHoraEgresoUTC = this.fechaHoraEgresoUTC;
        registro.productos = new ArrayList<>(this.productos);
        return registro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistroDeAlmacen that = (RegistroDeAlmacen) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(String idRuta) {
        this.idRuta = idRuta;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaHoraIngresoLocal() {
        return fechaHoraIngresoLocal;
    }

    public void setFechaHoraIngresoLocal(LocalDateTime fechaHoraIngresoLocal) {
        this.fechaHoraIngresoLocal = fechaHoraIngresoLocal;
    }

    public LocalDateTime getFechaHoraIngresoUTC() {
        return fechaHoraIngresoUTC;
    }

    public void setFechaHoraIngresoUTC(LocalDateTime fechaHoraIngresoUTC) {
        this.fechaHoraIngresoUTC = fechaHoraIngresoUTC;
    }

    public LocalDateTime getFechaHoraEgresoLocal() {
        return fechaHoraEgresoLocal;
    }

    public void setFechaHoraEgresoLocal(LocalDateTime fechaHoraEgresoLocal) {
        this.fechaHoraEgresoLocal = fechaHoraEgresoLocal;
    }

    public LocalDateTime getFechaHoraEgresoUTC() {
        return fechaHoraEgresoUTC;
    }

    public void setFechaHoraEgresoUTC(LocalDateTime fechaHoraEgresoUTC) {
        this.fechaHoraEgresoUTC = fechaHoraEgresoUTC;
    }

    public List<String> getProductos() {
        return productos;
    }

    public void setProductos(List<String> productos) {
        this.productos = productos;
    }
}
