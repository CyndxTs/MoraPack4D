/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       EventoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.EventoMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.EventoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportFileRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.EventoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PlanEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEscenario;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoEvento;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.EventoRepository;
import com.pucp.dp1.grupo4d.morapack.service.CommunicationService;
import com.pucp.dp1.grupo4d.morapack.service.ImportationService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class EventoService {
    private final EventoRepository eventoRepository;
    private final EventoMapper eventoMapper;
    private final PlanService planService;
    private final CommunicationService communicationService;
    private final ImportationService importationService;

    public EventoService(EventoRepository eventoRepository, EventoMapper eventoMapper, PlanService planService, CommunicationService communicationService, ImportationService importationService) {
        this.eventoRepository = eventoRepository;
        this.eventoMapper = eventoMapper;
        this.planService = planService;
        this.communicationService = communicationService;
        this.importationService = importationService;
    }

    public EventoEntity save(EventoEntity evento) {
        return eventoRepository.save(evento);
    }

    public List<EventoEntity> findAll() {
        return eventoRepository.findAll();
    }

    public List<EventoEntity> findAll(Pageable pageable) {
        return eventoRepository.findAll(pageable).getContent();
    }

    public Optional<EventoEntity> findById(Integer id) {
        return eventoRepository.findById(id);
    }

    public boolean existsById(Integer id) {
        return eventoRepository.existsById(id);
    }

    public void deleteById(Integer id) {
        eventoRepository.deleteById(id);
    }

    public Optional<EventoEntity> findByCodigo(String codigo) {
        return eventoRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return eventoRepository.findByCodigo(codigo).isPresent();
    }

    public ListResponse listar(ListRequest request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.desc("fechaHoraInicio"), Sort.Order.desc("fechaHoraFin"));
        List<DTO> dtos = new ArrayList<>();
        List<EventoEntity> entities = this.findAll(pageable);
        entities.forEach(entity -> dtos.add(eventoMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Eventos listados correctamente! ('%d')", dtos.size()), dtos);
    }

    // Clave de un plan por origen, destino y horas UTC
    private static String clavePlan(String codOrigen, String codDestino, LocalTime horaSalidaUTC, LocalTime horaLlegadaUTC) {
        return String.format("%s|%s|%s|%s", codOrigen, codDestino, horaSalidaUTC, horaLlegadaUTC);
    }

    // Completa los datos de un evento según su tipo a partir de las nuevas horas locales / nueva capacidad
    private void completarSegunTipo(EventoEntity evento, PlanEntity plan, LocalDate fechaLocal, String[] extra) {
        switch (evento.getTipo()) {
            case CANCELACION -> { }
            case REPROGRAMACION -> {
                LocalTime nuevaSalidaLocal = G4DUtility.Convertor.toTime(extra[0]);
                LocalTime nuevaLlegadaLocal = G4DUtility.Convertor.toTime(extra[1]);
                LocalDateTime nuevaSalida = G4DUtility.Convertor.toUTC(LocalDateTime.of(fechaLocal, nuevaSalidaLocal), plan.getOrigen().getHusoHorario());
                LocalTime nuevaLlegadaUTC = G4DUtility.Convertor.toUTC(nuevaLlegadaLocal, plan.getDestino().getHusoHorario());
                LocalDateTime[] rango = G4DUtility.Convertor.toDateTimeRange(nuevaSalida.toLocalTime(), nuevaLlegadaUTC, nuevaSalida);
                evento.setFechaHoraNuevaSalida(rango[0]);
                evento.setFechaHoraNuevaLlegada(rango[1]);
            }
            case REAJUSTE_CAPACIDAD -> {
                int nuevaCapacidad = Integer.parseInt(extra[0].trim());
                if (nuevaCapacidad < 0) {
                    throw new G4DException(String.format("La nueva capacidad ('%d') no puede ser negativa.", nuevaCapacidad));
                }
                evento.setNuevaCapacidad(nuevaCapacidad);
            }
        }
    }

    // Carga unitaria (fechas en UTC)
    @Transactional
    public void importar(String idTransaccion, EventoDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando evento..");
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando evento", 0, 1));
            PlanEntity plan = planService.findByCodigo(dto.getCodPlan()).orElseThrow(() -> new G4DException(String.format("El plan ('%s') del evento es inválido.", dto.getCodPlan())));
            TipoEvento tipo = G4DUtility.Convertor.toAdmissible(dto.getTipo(), TipoEvento.class);
            if (tipo == null) throw new G4DException(String.format("El tipo de evento ('%s') es inválido.", dto.getTipo()));
            TipoEscenario tipoEscenario = G4DUtility.Convertor.toAdmissible(dto.getTipoEscenario(), TipoEscenario.class);
            if (tipoEscenario == null) throw new G4DException(String.format("El tipo de escenario ('%s') del evento es inválido.", dto.getTipoEscenario()));
            EventoEntity evento = new EventoEntity();
            evento.setCodigo(G4DUtility.Convertor.toAdmissible(dto.getCodigo(), G4DUtility.Generator.getUniqueString("EVE")));
            evento.setTipo(tipo);
            evento.setTipoEscenario(tipoEscenario);
            evento.setPlan(plan);
            evento.setFechaHoraInicio(G4DUtility.Convertor.toDateTime(dto.getFechaHoraInicio()));
            evento.setFechaHoraFin(G4DUtility.Convertor.toDateTime(dto.getFechaHoraFin()));
            if (tipo == TipoEvento.REPROGRAMACION) {
                evento.setFechaHoraNuevaSalida(G4DUtility.Convertor.toDateTime(dto.getFechaHoraNuevaSalida()));
                evento.setFechaHoraNuevaLlegada(G4DUtility.Convertor.toDateTime(dto.getFechaHoraNuevaLlegada()));
                if (evento.getFechaHoraNuevaSalida() == null || evento.getFechaHoraNuevaLlegada() == null) {
                    throw new G4DException("Una reprogramación necesita la nueva salida y la nueva llegada.");
                }
            } else if (tipo == TipoEvento.REAJUSTE_CAPACIDAD) {
                if (dto.getNuevaCapacidad() == null || dto.getNuevaCapacidad() < 0) {
                    throw new G4DException("Un reajuste de capacidad necesita una nueva capacidad válida.");
                }
                evento.setNuevaCapacidad(dto.getNuevaCapacidad());
            }
            this.save(evento);
            System.out.println("[<] EVENTO IMPORTADO!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando evento", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw (e instanceof G4DException g4dEx) ? g4dEx : new RuntimeException(e);
        }
    }

    // Carga por archivo (horas locales; el mes, el año y el escenario vienen en la request)
    //   DD-ORIG-DEST-HH:MM-HH:MM-CANCELACION
    //   DD-ORIG-DEST-HH:MM-HH:MM-REPROGRAMACION-HH:MM-HH:MM
    //   DD-ORIG-DEST-HH:MM-HH:MM-REAJUSTE_CAPACIDAD-CAP
    // Las dos primeras horas identifican el vuelo afectado (salida local del origen, llegada local del destino).
    // Si cualquier línea es inválida (incluido un día inexistente en el mes), se cancela toda la importación.
    @Transactional
    public void importar(String idTransaccion, Path archivo, ImportFileRequest request) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        int lProcesadas = 0;
        try {
            System.out.printf(">> Importando eventos desde '%s'..%n", archivo.getFileName());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 0, 2));
            TipoEscenario tipoEscenario = G4DUtility.Convertor.toAdmissible(request.getTipoEscenario(), TipoEscenario.class);
            if (tipoEscenario == null) throw new G4DException(String.format("El tipo de escenario ('%s') es inválido.", request.getTipoEscenario()));
            Integer mes = request.getMes();
            Integer anio = (request.getAnio() != null) ? request.getAnio() : LocalDate.now().getYear();
            if (mes == null || mes < 1 || mes > 12) throw new G4DException(String.format("El mes ('%s') de los eventos es inválido.", mes));
            BufferedReader br = Files.newBufferedReader(archivo, G4DUtility.Reader.getFileCharset(archivo));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 1, 2));
            Map<String, PlanEntity> poolPlanes = new HashMap<>();
            planService.findAll().forEach(p -> poolPlanes.put(clavePlan(p.getOrigen().getCodigo(), p.getDestino().getCodigo(), p.getHoraSalida(), p.getHoraLlegada()), p));
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 2, 2));
            List<EventoEntity> eventos = new ArrayList<>();
            String linea;
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            while ((linea = br.readLine()) != null) {
                linea = linea.replace("﻿", "").trim();
                if (!linea.isBlank()) {
                    String[] partes = linea.split("-");
                    final int numLinea = lProcesadas + 1;
                    int dia = Integer.parseInt(partes[0].trim());
                    String codOrigen = partes[1].trim(), codDestino = partes[2].trim();
                    LocalTime salidaLocal = G4DUtility.Convertor.toTime(partes[3].trim());
                    LocalTime llegadaLocal = G4DUtility.Convertor.toTime(partes[4].trim());
                    TipoEvento tipo = G4DUtility.Convertor.toAdmissible(partes[5], TipoEvento.class);
                    if (tipo == null) throw new G4DException(String.format("Tipo de evento '%s' inválido en línea #%d", partes[5], lProcesadas + 1));
                    PlanEntity muestra = poolPlanes.values().stream().filter(p -> p.getOrigen().getCodigo().equals(codOrigen) && p.getDestino().getCodigo().equals(codDestino)).findFirst()
                            .orElseThrow(() -> new G4DException(String.format("No existe ningún plan '%s' -> '%s' (línea #%d)", codOrigen, codDestino, numLinea)));
                    AeropuertoEntity origen = muestra.getOrigen(), destino = muestra.getDestino();
                    LocalTime salidaUTC = G4DUtility.Convertor.toUTC(salidaLocal, origen.getHusoHorario());
                    LocalTime llegadaUTC = G4DUtility.Convertor.toUTC(llegadaLocal, destino.getHusoHorario());
                    PlanEntity plan = poolPlanes.get(clavePlan(codOrigen, codDestino, salidaUTC, llegadaUTC));
                    if (plan == null) throw new G4DException(String.format("No existe el plan '%s' -> '%s' %s-%s en línea #%d", codOrigen, codDestino, partes[3], partes[4], lProcesadas + 1));
                    LocalDate fechaLocal = LocalDate.of(anio, mes, dia);
                    LocalDateTime inicio = G4DUtility.Convertor.toUTC(LocalDateTime.of(fechaLocal, salidaLocal), origen.getHusoHorario());
                    LocalDateTime[] rango = G4DUtility.Convertor.toDateTimeRange(plan.getHoraSalida(), plan.getHoraLlegada(), inicio);
                    EventoEntity evento = new EventoEntity();
                    evento.setCodigo(G4DUtility.Generator.getUniqueString("EVE"));
                    evento.setTipo(tipo);
                    evento.setTipoEscenario(tipoEscenario);
                    evento.setPlan(plan);
                    evento.setFechaHoraInicio(rango[0]);
                    evento.setFechaHoraFin(rango[1]);
                    this.completarSegunTipo(evento, plan, fechaLocal, Arrays.copyOfRange(partes, 6, partes.length));
                    eventos.add(evento);
                }
                lProcesadas++;
                communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            }
            br.close();
            importationService.batchSave(eventos, progressDestination, "eventos");
            System.out.printf("[<] EVENTOS IMPORTADOS! ('%d')%n", eventos.size());
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (G4DException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw e;
        } catch (IOException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getFileName()));
        } catch (RuntimeException e) {
            // Índices, números, horas o fechas inválidas (p. ej. día 31 en un mes de 30)
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado (línea #%d).", archivo.getFileName(), lProcesadas + 1));
        }
    }
}
