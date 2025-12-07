/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PlanService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.PlanMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PlanDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.PlanRepository;
import com.pucp.dp1.grupo4d.morapack.service.WebSocketService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Service
public class PlanService {
    private final PlanRepository planRepository;
    private final AeropuertoService aeropuertoService;
    private final PlanMapper planMapper;
    private final List<PlanEntity> planes = new ArrayList<>();

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
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("horaSalidaUTC"), Sort.Order.asc("horaLlegadaUTC"));
            List<DTO> dtos = new ArrayList<>();
            List<PlanEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(planMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Planes listados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportRequest<PlanDTO> request) {
        try {
            System.out.println("Importando plan..");
            PlanDTO dto = request.getDto();
            PlanEntity plan =  new PlanEntity();
            plan.setCodigo(dto.getCodigo());
            plan.setDistancia(dto.getDistancia());
            plan.setDuracion(dto.getDuracion());
            String codOrigen = dto.getCodOrigen();
            AeropuertoEntity origen = aeropuertoService.obtenerPorCodigo(codOrigen);
            if(origen != null) {
                String codDestino = dto.getCodDestino();
                AeropuertoEntity destino = aeropuertoService.obtenerPorCodigo(codDestino);
                if(destino != null) {
                    plan.setOrigen(origen);
                    plan.setDestino(destino);
                    plan.setHoraSalidaUTC(G4DUtility.Convertor.toTime(dto.getHoraSalida()));
                    plan.setHoraSalidaLocal(G4DUtility.Convertor.toLocal(plan.getHoraSalidaUTC(), origen.getHusoHorario()));
                    plan.setHoraLlegadaUTC(G4DUtility.Convertor.toTime(dto.getHoraLlegada()));
                    plan.setHoraLlegadaLocal(G4DUtility.Convertor.toLocal(plan.getHoraLlegadaUTC(), destino.getHusoHorario()));
                    this.save(plan);
                } else throw new G4DException(String.format("El destino ('%s') del plan es inválido.", codDestino));
            } else throw new G4DException(String.format("El origen ('%s') del plan es inválido.", codOrigen));
            System.out.println("[<] PLAN DE VUELO IMPORTADO!");
            return new GenericResponse(true, "Plan importado correctamente!");
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo) {
        try {
            System.out.printf("Importando planes de vuelo desde '%s'..%n", archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4DUtility.Reader.getFileCharset(archivo));
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0;
            WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.INICIADO));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                if (!linea.isEmpty()) {
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
                            plan.setDistancia(G4DUtility.Calculator.getGeodesicDistance(origen.getLatitudDEC(), origen.getLongitudDEC(), destino.getLatitudDEC(), destino.getLongitudDEC()));
                            plan.setHoraSalidaLocal(G4DUtility.Convertor.toTime(lineaSC.next()));
                            plan.setHoraSalidaUTC(G4DUtility.Convertor.toUTC(plan.getHoraSalidaLocal(), plan.getOrigen().getHusoHorario()));
                            plan.setHoraLlegadaLocal(G4DUtility.Convertor.toTime(lineaSC.next()));
                            plan.setHoraLlegadaUTC(G4DUtility.Convertor.toUTC(plan.getHoraLlegadaLocal(), plan.getDestino().getHusoHorario()));
                            plan.setDuracion(G4DUtility.Calculator.getElapsedHours(plan.getHoraSalidaUTC(), plan.getHoraLlegadaUTC()));
                            plan.setCapacidad(lineaSC.nextInt());
                            plan.setCodigo(G4DUtility.Generator.getUniqueString("PLA"));
                            planes.add(plan);
                        } else throw new G4DException(String.format("El destino ('%s') del plan de la linea #%d es inválido.", codDestino, lProcesadas + 1));
                    } else throw new G4DException(String.format("El origen ('%s') del plan de la linea #%d es inválido.", codOrigen, lProcesadas + 1));
                    lineaSC.close();
                }
                lProcesadas++;
                WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if(lProcesadas % 500 == 0 || !archivoSC.hasNextLine()) {
                    int eTotales = planes.size();
                    int eProcesadas = 0;
                    WebSocketService.enviar("/topic/loader", new ProgressPayload("Guardando planes de vuelo", eProcesadas, eTotales));
                    for(PlanEntity entity : planes) {
                        this.save(entity);
                        eProcesadas++;
                        WebSocketService.enviar("/topic/loader", new ProgressPayload("Guardando planes de vuelo", eProcesadas, eTotales));
                    }
                    System.out.printf("[<] PLANES IMPORTADOS! ('%d')%n", planes.size());
                    clearPools();
                }
            }
            archivoSC.close();
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            return new GenericResponse(true, "Planes importados correctamente!");
        } catch (NoSuchElementException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getName()));
        } catch (IOException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getName()));
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        planes.clear();
        aeropuertoService.clearPools();
    }
}
