/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.PlanMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PlanDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.repository.PlanRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final AeropuertoService aeropuertoService;
    private final PlanMapper planMapper;
    private final HashMap<String, PlanEntity> planes = new HashMap<>();

    public PlanService(PlanRepository planRepository, AeropuertoService aeropuertoService, PlanMapper planMapper) {
        this.planRepository = planRepository;
        this.aeropuertoService = aeropuertoService;
        this.planMapper = planMapper;
    }

    public List<PlanEntity> findAll() {
        return planRepository.findAll();
    }

    public List<PlanEntity> findAll(Pageable pageable) {
        return planRepository.findAll(pageable).getContent();
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

    public ListResponse listar(ListRequest request) {
        try {
            int page = G4D.toAdmissibleValue(request.getPage(), 0);
            int size = G4D.toAdmissibleValue(request.getSize(), 10);
            Pageable pageable = PageRequest.of(page, size,  Sort.by(Sort.Order.asc("horaSalidaUTC"), Sort.Order.asc("horaLlegadaUTC")));
            List<DTO> dtos = new ArrayList<>();
            List<PlanEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(planMapper.toDTO(entity)));
            return new ListResponse(true, "Planes listados correctamente!", dtos);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportRequest<PlanDTO> request) {
        try {
            PlanDTO dto = request.getDto();
            PlanEntity plan = planMapper.toEntity(dto);
            this.save(plan);
            G4D.Logger.logln("[<] PLAN DE VUELO CARGADO!");
            return new GenericResponse(true, "Plan importado correctamente!");
        } catch (Exception e) {
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo) {
        try {
            G4D.Logger.logf("Cargando planes de vuelo desde '%s'..%n",archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            int numLinea = 1;
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) {
                    numLinea++;
                    continue;
                }
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("-");
                PlanEntity plan = new PlanEntity();
                String codOrigen = lineaSC.next();
                AeropuertoEntity origen = aeropuertoService.obtenerPorCodigo(codOrigen);
                if(origen != null) {
                    String codDestino = lineaSC.next();
                    AeropuertoEntity destino = aeropuertoService.obtenerPorCodigo(codDestino);
                    if(destino != null) {
                        plan.setOrigen(origen);
                        plan.setDestino(destino);
                        plan.setDistancia(G4D.getGeodesicDistance(origen.getLatitudDEC(), origen.getLongitudDEC(), destino.getLatitudDEC(), destino.getLongitudDEC()));
                        plan.setHoraSalidaLocal(G4D.toTime(lineaSC.next()));
                        plan.setHoraSalidaUTC(G4D.toUTC(plan.getHoraSalidaLocal(), plan.getOrigen().getHusoHorario()));
                        plan.setHoraLlegadaLocal(G4D.toTime(lineaSC.next()));
                        plan.setHoraLlegadaUTC(G4D.toUTC(plan.getHoraLlegadaLocal(), plan.getDestino().getHusoHorario()));
                        plan.setDuracion(G4D.getElapsedHours(plan.getHoraSalidaUTC(), plan.getHoraLlegadaUTC()));
                        plan.setCapacidad(lineaSC.nextInt());
                        plan.setCodigo(G4D.Generator.getUniqueString("PLA"));
                        planes.put(plan.getCodigo(), plan);
                    } else throw new Exception(String.format("El destino del plan de la linea #%d es inválido. ('%s')", numLinea, codDestino));
                } else throw new Exception(String.format("El origen del plan de la linea #%d es inválido. ('%s')", numLinea, codOrigen));
                lineaSC.close();
                numLinea++;
            }
            archivoSC.close();
            planes.values().forEach(this::save);
            G4D.Logger.logf("[<] PLANES DE VUELO CARGADOS! ('%d')%n", planes.size());
            return new GenericResponse(true, "Planes importados correctamente!");
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! (RUTA: '%s')%n", archivo.getName());
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } catch (Exception e) {
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        planes.clear();
        aeropuertoService.clearPools();
        planMapper.clearPools();
    }
}
