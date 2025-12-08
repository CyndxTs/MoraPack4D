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
import com.pucp.dp1.grupo4d.morapack.service.ImportService;
import com.pucp.dp1.grupo4d.morapack.service.WebSocketService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanService {
    private final PlanRepository planRepository;
    private final AeropuertoService aeropuertoService;
    private final PlanMapper planMapper;
    private final ImportService importService;

    public PlanService(PlanRepository planRepository, AeropuertoService aeropuertoService, PlanMapper planMapper, ImportService importService) {
        this.planRepository = planRepository;
        this.aeropuertoService = aeropuertoService;
        this.planMapper = planMapper;
        this.importService = importService;
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
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("horaSalidaUTC"), Sort.Order.asc("horaLlegadaUTC"));
        List<DTO> dtos = new ArrayList<>();
        List<PlanEntity> entities = this.findAll(pageable);
        entities.forEach(entity -> dtos.add(planMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Planes listados correctamente! ('%d')", dtos.size()), dtos);
    }

    public GenericResponse importar(ImportRequest<PlanDTO> request) {
        System.out.println("Importando plan..");
        Map<String, AeropuertoEntity> poolAeropuertos = aeropuertoService.findAll().stream().collect(Collectors.toMap(AeropuertoEntity::getCodigo, a -> a));
        PlanDTO dto = request.getDto();
        PlanEntity plan =  new PlanEntity();
        plan.setCodigo(dto.getCodigo());
        plan.setDistancia(dto.getDistancia());
        plan.setDuracion(dto.getDuracion());
        String codOrigen = dto.getCodOrigen();
        AeropuertoEntity origen = poolAeropuertos.getOrDefault(codOrigen, null);
        if(origen != null) {
            String codDestino = dto.getCodDestino();
            AeropuertoEntity destino = poolAeropuertos.getOrDefault(codDestino, null);
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
    }

    public GenericResponse importar(MultipartFile archivo) {
        try {
            System.out.printf("Importando planes de vuelo desde '%s'..%n", archivo.getName());
            BufferedReader br = new BufferedReader(new InputStreamReader(archivo.getInputStream(), G4DUtility.Reader.getFileCharset(archivo)));
            List<PlanEntity> planes = new ArrayList<>();
            Map<String, AeropuertoEntity> poolAeropuertos = aeropuertoService.findAll().stream().collect(Collectors.toMap(AeropuertoEntity::getCodigo, a -> a));
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0;
            String linea;
            WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.INICIADO));
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if(!linea.isBlank()) {
                    String[] partes = linea.split("-");
                    String codOrigen = partes[0];
                    String codDestino = partes[1];
                    AeropuertoEntity origen = poolAeropuertos.getOrDefault(codOrigen, null);
                    if(origen != null) {
                        AeropuertoEntity destino = poolAeropuertos.getOrDefault(codDestino, null);
                        if(destino != null) {
                            LocalTime horaSalidaLocal = G4DUtility.Convertor.toTime(partes[2]);
                            LocalTime horaLlegadaLocal = G4DUtility.Convertor.toTime(partes[3]);
                            int capacidad = Integer.parseInt(partes[4]);
                            PlanEntity plan = new PlanEntity();
                            plan.setCodigo(G4DUtility.Generator.getUniqueString("PLA"));
                            plan.setOrigen(origen);
                            plan.setDestino(destino);
                            plan.setDistancia(G4DUtility.Calculator.getGeodesicDistance(origen.getLatitudDEC(), origen.getLongitudDEC(), destino.getLatitudDEC(), destino.getLongitudDEC()));
                            plan.setHoraSalidaLocal(horaSalidaLocal);
                            plan.setHoraSalidaUTC(G4DUtility.Convertor.toUTC(horaSalidaLocal, origen.getHusoHorario()));
                            plan.setHoraLlegadaLocal(horaLlegadaLocal);
                            plan.setHoraLlegadaUTC(G4DUtility.Convertor.toUTC(horaLlegadaLocal, destino.getHusoHorario()));
                            plan.setDuracion(G4DUtility.Calculator.getElapsedHours(plan.getHoraSalidaUTC(), plan.getHoraLlegadaUTC()));
                            plan.setCapacidad(capacidad);
                            planes.add(plan);
                        } else throw new G4DException(String.format("Destino '%s' inválido en línea #%d", codDestino, lProcesadas + 1));
                    } else throw new G4DException(String.format("Origen '%s' inválido en línea #%d", codOrigen, lProcesadas + 1));
                }
                lProcesadas++;
                WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if(planes.size() % 1000 == 0 || lProcesadas == lTotales) {
                    importService.batchSave(planes, "planes de vuelo");
                    System.out.printf("[<] PLANES IMPORTADOS! ('%d')%n", planes.size());
                    planes.clear();
                }
            }
            br.close();
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            return new GenericResponse(true, "Planes de vuelo importados correctamente!");
        } catch (ArrayIndexOutOfBoundsException | NoSuchElementException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getName()));
        } catch (IOException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getName()));
        }
    }
}
