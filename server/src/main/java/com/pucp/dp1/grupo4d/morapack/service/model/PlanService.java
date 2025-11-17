/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.PlanMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PedidoEntity;
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
    @Autowired
    private PlanMapper planMapper;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<PlanEntity> findAll() {
        return planRepository.findAll();
    }

    public Optional<PlanEntity> findById(Integer id) {
        return planRepository.findById(id);
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

    public Optional<PlanEntity> findByCodigo(String codigo) {
        return planRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return planRepository.findByCodigo(codigo).isPresent();
    }

    public ListResponse listar() {
        try {
            List<DTO> planesDTO = new ArrayList<>();
            List<PlanEntity> planesEntity = this.findAll();
            planesEntity.forEach(p -> planesDTO.add(planMapper.toDTO(p)));
            return new ListResponse(true, "Planes listados correctamente!", planesDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }

    public void importar(MultipartFile archivo) {
        List<PlanEntity> planes = new ArrayList<>();
        try {
            G4D.Logger.logf("Cargando planes de vuelo desde '%s'..%n",archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue;
                Scanner lineaSC = new Scanner(linea);
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
            archivoSC.close();
            planes.forEach(this::save);
            G4D.Logger.logf("[<] PLANES DE VUELO CARGADOS! ('%d')%n", planes.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
