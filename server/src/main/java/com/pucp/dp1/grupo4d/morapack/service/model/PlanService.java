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
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.PlanRepository;
import com.pucp.dp1.grupo4d.morapack.service.CommunicationService;
import com.pucp.dp1.grupo4d.morapack.service.ImportationService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanMapper planMapper;
    private final AeropuertoService aeropuertoService;
    private final CommunicationService communicationService;
    private final ImportationService importationService;

    public PlanService(PlanRepository planRepository, PlanMapper planMapper, AeropuertoService aeropuertoService, CommunicationService communicationService, ImportationService importationService) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
        this.aeropuertoService = aeropuertoService;
        this.communicationService = communicationService;
        this.importationService = importationService;
    }

    public PlanEntity save(PlanEntity plan) {
        return planRepository.save(plan);
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

    public boolean existsById(Integer id) {
        return planRepository.existsById(id);
    }

    public void deleteById(Integer id) {
        planRepository.deleteById(id);
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

    public void importar(String idTransaccion, PlanDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando plan..");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 0, 1));
            Map<String, AeropuertoEntity> poolAeropuertos = aeropuertoService.findAll().stream().collect(Collectors.toMap(AeropuertoEntity::getCodigo, a -> a));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando plan", 0, 1));
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
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando plan", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new RuntimeException(e);
        }
    }

    public void importar(String idTransaccion, Path archivo) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.printf(">> Importando planes de vuelo desde '%s'..%n", archivo.getFileName());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 0, 3));
            BufferedReader br = Files.newBufferedReader(archivo, G4DUtility.Reader.getFileCharset(archivo));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 1, 3));
            Map<String, AeropuertoEntity> poolAeropuertos = aeropuertoService.findAll().stream().collect(Collectors.toMap(AeropuertoEntity::getCodigo, a -> a));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 2, 3));
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0;
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 3, 3));
            List<PlanEntity> planes = new ArrayList<>();
            String linea;
            communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
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
                communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if(planes.size() % 500 == 0 || lProcesadas == lTotales) {
                    importationService.batchSave(planes, "planes de vuelo");
                    System.out.printf("[<] PLANES IMPORTADOS! ('%d')%n", planes.size());
                    planes.clear();
                }
            }
            poolAeropuertos.clear();
            br.close();
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (ArrayIndexOutOfBoundsException | NoSuchElementException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getFileName()));
        } catch (IOException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getFileName()));
        }
    }
}
