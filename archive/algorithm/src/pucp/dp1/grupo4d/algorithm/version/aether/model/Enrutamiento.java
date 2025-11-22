/**]
 >> Project:    MoraPack
 >> Version:    Aether
 >> Author:     Grupo 4D
 >> File:       Enrutamiento.java
[**/

package pucp.dp1.grupo4d.algorithm.version.aether.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Enrutamiento {
    private String codigo;
    private LocalDateTime fechaHoraInicioVigenciaLocal;
    private LocalDateTime fechaHoraInicioVigenciaUTC;
    private LocalDateTime fechaHoraFinVigenciaLocal;
    private LocalDateTime fechaHoraFinVigenciaUTC;
    private Map<Ruta, Lote> lotesPorRuta;

    public Enrutamiento() {
        this.lotesPorRuta = new HashMap<>();
    }
    
    public Enrutamiento replicar(Map<String,Aeropuerto> poolAeropuertos, Map<String, Lote> poolLotes, Map<String, Ruta> poolRutas, Map<String,Vuelo> poolVuelos, Map<String, Plan> poolPlanes) {
        Enrutamiento enrutamiento = new Enrutamiento();
        enrutamiento.codigo = this.codigo;
        enrutamiento.fechaHoraInicioVigenciaLocal = this.fechaHoraInicioVigenciaLocal;
        enrutamiento.fechaHoraInicioVigenciaUTC = this.fechaHoraInicioVigenciaUTC;
        enrutamiento.fechaHoraFinVigenciaLocal = this.fechaHoraFinVigenciaLocal;
        enrutamiento.fechaHoraFinVigenciaUTC = this.fechaHoraFinVigenciaUTC;
        for(Map.Entry<Ruta, Lote> entry : this.lotesPorRuta.entrySet()) {
            Ruta ruta = poolRutas.computeIfAbsent(entry.getKey().getCodigo(), codigo -> entry.getKey().replicar(poolAeropuertos, poolLotes, poolVuelos, poolPlanes));
            Lote lote = poolLotes.computeIfAbsent(entry.getValue().getCodigo(), codigo -> entry.getValue().replicar());
            enrutamiento.lotesPorRuta.put(ruta, lote);
        }
        return enrutamiento;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enrutamiento that = (Enrutamiento) o;
        return codigo != null && codigo.equals(that.codigo);
    }

    @Override
    public int hashCode() {
        return codigo != null ? codigo.hashCode() : 0;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDateTime getFechaHoraInicioVigenciaLocal() {
        return fechaHoraInicioVigenciaLocal;
    }

    public void setFechaHoraInicioVigenciaLocal(LocalDateTime fechaHoraInicioVigenciaLocal) {
        this.fechaHoraInicioVigenciaLocal = fechaHoraInicioVigenciaLocal;
    }

    public LocalDateTime getFechaHoraInicioVigenciaUTC() {
        return fechaHoraInicioVigenciaUTC;
    }

    public void setFechaHoraInicioVigenciaUTC(LocalDateTime fechaHoraInicioVigenciaUTC) {
        this.fechaHoraInicioVigenciaUTC = fechaHoraInicioVigenciaUTC;
    }

    public LocalDateTime getFechaHoraFinVigenciaLocal() {
        return fechaHoraFinVigenciaLocal;
    }

    public void setFechaHoraFinVigenciaLocal(LocalDateTime fechaHoraFinVigenciaLocal) {
        this.fechaHoraFinVigenciaLocal = fechaHoraFinVigenciaLocal;
    }

    public LocalDateTime getFechaHoraFinVigenciaUTC() {
        return fechaHoraFinVigenciaUTC;
    }

    public void setFechaHoraFinVigenciaUTC(LocalDateTime fechaHoraFinVigenciaUTC) {
        this.fechaHoraFinVigenciaUTC = fechaHoraFinVigenciaUTC;
    }

    public Map<Ruta, Lote> getLotesPorRuta() {
        return lotesPorRuta;
    }

    public void setLotesPorRuta(Map<Ruta, Lote> lotesPorRuta) {
        this.lotesPorRuta = lotesPorRuta;
    }
}
