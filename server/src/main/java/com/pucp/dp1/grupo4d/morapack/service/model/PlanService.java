/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.repository.PlanRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class PlanService {

    @Autowired
    private AeropuertoService aeropuertoService;

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<PlanEntity> findAll() {
        return planRepository.findAll();
    }

    public Optional<PlanEntity> findById(Integer id) {
        return planRepository.findById(id);
    }

    public Optional<PlanEntity> findByCodigo(String codigo) {
        return planRepository.findByCodigo(codigo);
    }

    public PlanEntity save(PlanEntity plan) {
        return planRepository.save(plan);
    }

    public void deleteById(Integer id) {
        planRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return planRepository.existsById(id);
    }

    public boolean existsByCodigo(String codigo) {
        return planRepository.findByCodigo(codigo).isPresent();
    }

    public void importarDesdeArchivo(MultipartFile archivo) {
        List<PlanEntity> planes = new ArrayList<>();
        String linea;
        Scanner archivoSC = null, lineaSC;
        try {
            G4D.Logger.logf("Cargando planes de vuelo desde '%s'..%n",archivo.getName());
            archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue;
                lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                PlanEntity plan = new PlanEntity();
                AeropuertoEntity aOrig = aeropuertoService.findByCodigo(lineaSC.next()).orElse(null);
                if(aOrig != null) {
                    AeropuertoEntity aDest = aeropuertoService.findByCodigo(lineaSC.next()).orElse(null);
                    if(aDest != null) {
                        plan.setOrigen(aOrig);
                        plan.setDestino(aDest);
                        plan.setDistancia(G4D.getGeodesicDistance(aOrig.getLatitudDEC(), aOrig.getLongitudDEC(), aDest.getLatitudDEC(), aDest.getLongitudDEC()));
                        plan.setHoraSalidaLocal(G4D.toTime(lineaSC.next()));
                        plan.setHoraSalidaUTC(G4D.toUTC(plan.getHoraSalidaLocal(), plan.getOrigen().getHusoHorario()));
                        plan.setHoraLlegadaLocal(G4D.toTime(lineaSC.next()));
                        plan.setHoraLlegadaUTC(G4D.toUTC(plan.getHoraLlegadaLocal(), plan.getDestino().getHusoHorario()));
                        plan.setDuracion(G4D.getElapsedHours(plan.getHoraSalidaUTC(), plan.getHoraLlegadaUTC()));
                        plan.setCapacidad(lineaSC.nextInt());
                        plan.setCodigo(G4D.Generator.getUniqueString("PLA"));
                        planes.add(plan);
                    }
                }
                lineaSC.close();
            }
            planes.forEach(this::save);
            G4D.Logger.logf("[<] PLANES DE VUELO CARGADOS! ('%d')%n", planes.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (archivoSC != null) archivoSC.close();
        }
    }
}
