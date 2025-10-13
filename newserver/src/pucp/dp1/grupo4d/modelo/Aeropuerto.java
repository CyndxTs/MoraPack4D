/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       Aeropuerto.java 
[**/

package pucp.dp1.grupo4d.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pucp.dp1.grupo4d.util.G4D;

public class Aeropuerto {
    private String id;
    private String codigo;
    private String ciudad;
    private String pais;
    private String continente;
    private String alias;
    private Integer husoHorario;
    private Integer capacidad;
    private String latitudDMS;
    private Double latitudDEC;
    private String longitudDMS;
    private Double longitudDEC;
    private List<RegistroDeAlmacen> historialDeProductos;

    public Aeropuerto() {
        this.id = G4D.getUniqueString("AER");
        this.historialDeProductos = new ArrayList<>();
    }

    public Double obtenerDistanciaHasta(Aeropuerto destino) {
        return G4D.getGeodesicDistance(this.latitudDEC,this.longitudDEC,destino.latitudDEC,destino.longitudDEC);
    }

    public Integer obtenerCapacidadDisponible(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        Map<LocalDateTime, Integer> eventos = new TreeMap<>();
        for(RegistroDeAlmacen registro : this.historialDeProductos) {
            if(registro.getFechaHoraIngresoUTC().isBefore(fechaHoraFin) && 
            registro.getFechaHoraEgresoUTC().isAfter(fechaHoraInicio)) {
                eventos.merge(registro.getFechaHoraIngresoUTC(), -registro.getCantidad(), Integer::sum);
                eventos.merge(registro.getFechaHoraEgresoUTC(), +registro.getCantidad(), Integer::sum);
            }
        }
        int disponibilidadActual = this.capacidad, minimaDisponibilidad = this.capacidad;
        for(int canProd : eventos.values()) {
            disponibilidadActual += canProd;
            minimaDisponibilidad = Math.min(minimaDisponibilidad, disponibilidadActual);
        }
        return minimaDisponibilidad;
    }

    public List<Producto> generarLoteDeProductos(int numProd, Ruta ruta) {
        List<Producto> productos = new ArrayList<>();
        for(int i = 0; i < numProd; i++) {
            Producto producto = new Producto();
            producto.setRuta(ruta);
            productos.add(producto);
        }
        return productos;
    }

    public void registrarLote(List<String> productos, String idRuta, LocalDateTime fechaHoraIngresoUTC, LocalDateTime fechaHoraEgresoUTC) {
        if(agregarProductosEnLote(productos, idRuta)) return;
        RegistroDeAlmacen registro = new RegistroDeAlmacen();
        registro.setIdRuta(idRuta);
        registro.setCantidad(productos.size());
        registro.setFechaHoraIngresoUTC(fechaHoraIngresoUTC);
        registro.setFechaHoraIngresoLocal(G4D.toLocal(fechaHoraIngresoUTC,this.husoHorario));
        registro.setFechaHoraEgresoUTC(fechaHoraEgresoUTC);
        registro.setFechaHoraEgresoLocal(G4D.toLocal(fechaHoraEgresoUTC,this.husoHorario));
        registro.setProductos(productos);
        this.historialDeProductos.add(registro);
    }

    public Boolean agregarProductosEnLote(List<String> productos, String idRuta) {
        for(RegistroDeAlmacen registro : this.historialDeProductos) {
            if(registro.getIdRuta().compareTo(idRuta) == 0) {
                registro.setCantidad(registro.getCantidad() + productos.size());
                registro.getProductos().addAll(productos);
                return true;
            }
        }
        return false;
    }

    public Boolean eliminarProductosDeLote(List<String> productos, String idRuta) {
        for(RegistroDeAlmacen registro : this.historialDeProductos) {
            if(registro.getIdRuta().compareTo(idRuta) == 0) {
                registro.setCantidad(registro.getCantidad() - productos.size());
                if(registro.getCantidad() > 0) {
                    registro.getProductos().removeAll(productos);
                } else {
                    this.historialDeProductos.remove(registro);
                }
                return true;
            }
        }
        return false;
    }

    public Aeropuerto replicar() {
        Aeropuerto aeropuerto = new Aeropuerto();
        aeropuerto.id = this.id;
        aeropuerto.codigo = this.codigo;
        aeropuerto.ciudad = this.ciudad;
        aeropuerto.pais = this.pais;
        aeropuerto.continente = this.continente;
        aeropuerto.alias = this.alias;
        aeropuerto.husoHorario = this.husoHorario;
        aeropuerto.capacidad = this.capacidad;
        aeropuerto.latitudDMS = this.latitudDMS;
        aeropuerto.latitudDEC = this.latitudDEC;
        aeropuerto.longitudDMS = this.longitudDMS;
        aeropuerto.longitudDEC = this.longitudDEC;
        for(RegistroDeAlmacen registro : this.historialDeProductos) aeropuerto.historialDeProductos.add(registro.replicar());
        return aeropuerto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aeropuerto that = (Aeropuerto) o;
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

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getContinente() {
        return continente;
    }

    public void setContinente(String continente) {
        this.continente = continente;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getHusoHorario() {
        return husoHorario;
    }

    public void setHusoHorario(Integer husoHorario) {
        this.husoHorario = husoHorario;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getLatitudDMS() {
        return latitudDMS;
    }

    public void setLatitudDMS() {
        this.latitudDMS = G4D.toLatDMS(this.latitudDEC);
    }

    public void setLatitudDMS(String latitudDMS) {
        this.latitudDMS = latitudDMS;
    }

    public Double getLatitudDEC() {
        return latitudDEC;
    }

    public void setLatitudDEC() {
        this.latitudDEC = G4D.toLatDEC(this.latitudDMS);
    }

    public void setLatitudDEC(Double latitudDEC) {
        this.latitudDEC = latitudDEC;
    }

    public String getLongitudDMS() {
        return longitudDMS;
    }

    public void setLongitudDMS() {
        this.longitudDMS = G4D.toLonDMS(this.longitudDEC);
    }

    public void setLongitudDMS(String longitudDMS) {
        this.longitudDMS = longitudDMS;
    }

    public Double getLongitudDEC() {
        return longitudDEC;
    }

    public void setLongitudDEC() {
        this.longitudDEC = G4D.toLonDEC(this.longitudDMS);
    }

    public void setLongitudDEC(Double longitudDEC) {
        this.longitudDEC = longitudDEC;
    }

    public List<RegistroDeAlmacen> getHistorialDeProductos() {
        return historialDeProductos;
    }

    public void setHistorialDeProductos(List<RegistroDeAlmacen> historialDeProductos) {
        this.historialDeProductos = historialDeProductos;
    }
}
