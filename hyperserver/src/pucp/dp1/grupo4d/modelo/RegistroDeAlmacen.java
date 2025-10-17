/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       RegistroDeProducto.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pucp.dp1.grupo4d.util.G4D;

public class RegistroDeAlmacen {
    private String id;
    private String idRuta;
    private Integer tamanio;
    private LocalDateTime fechaHoraIngresoLocal;
    private LocalDateTime fechaHoraIngresoUTC;
    private LocalDateTime fechaHoraEgresoLocal;
    private LocalDateTime fechaHoraEgresoUTC;
    private List<LoteDeProductos> lotes;

    public RegistroDeAlmacen() {
        this.id = G4D.Generator.getUniqueString("REG");
        this.tamanio = 0;
        this.lotes = new ArrayList<>();
    }

    public RegistroDeAlmacen replicar(Map<String, LoteDeProductos> poolLotes) {
        RegistroDeAlmacen registro = new RegistroDeAlmacen();
        registro.id = this.id;
        registro.idRuta = this.idRuta;
        registro.tamanio = this.tamanio;
        registro.fechaHoraIngresoLocal = this.fechaHoraIngresoLocal;
        registro.fechaHoraIngresoUTC = this.fechaHoraIngresoUTC;
        registro.fechaHoraEgresoLocal = this.fechaHoraEgresoLocal;
        registro.fechaHoraEgresoUTC = this.fechaHoraEgresoUTC;
        for(LoteDeProductos l : this.lotes) registro.lotes.add(poolLotes.computeIfAbsent(l.getId(), id -> l.replicar()));
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

    public Integer getTamanio() {
        return tamanio;
    }

    public void setTamanio() {
        int tamanio = 0;
        for(LoteDeProductos l : this.lotes) tamanio += l.getTamanio();
        this.tamanio = tamanio;
    }

    public void setTamanio(Integer tamanio) {
        this.tamanio = tamanio;
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

    public List<LoteDeProductos> getLotes() {
        return lotes;
    }

    public void setLotes(List<LoteDeProductos> lotes) {
        this.lotes = lotes;
    }
}
