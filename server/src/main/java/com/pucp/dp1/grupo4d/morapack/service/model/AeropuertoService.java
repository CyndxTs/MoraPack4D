/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.AeropuertoMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.AeropuertoRepository;
import com.pucp.dp1.grupo4d.morapack.service.ImportService;
import com.pucp.dp1.grupo4d.morapack.service.WebSocketService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class AeropuertoService {
    private final AeropuertoRepository aeropuertoRepository;
    private final AeropuertoMapper aeropuertoMapper;
    private final ImportService importService;

    public AeropuertoService(AeropuertoRepository aeropuertoRepository, AeropuertoMapper aeropuertoMapper, ImportService importService) {
        this.aeropuertoRepository = aeropuertoRepository;
        this.aeropuertoMapper = aeropuertoMapper;
        this.importService = importService;
    }

    public List<AeropuertoEntity> findAll() {
        return aeropuertoRepository.findAll();
    }

    public List<AeropuertoEntity> findAll(Pageable pageable) {
        return aeropuertoRepository.findAll(pageable).getContent();
    }

    public Optional<AeropuertoEntity> findById(Integer id) {
        return aeropuertoRepository.findById(id);
    }

    public AeropuertoEntity save(AeropuertoEntity aeropuerto) {
        return aeropuertoRepository.save(aeropuerto);
    }

    public void deleteById(Integer id) {
        aeropuertoRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return aeropuertoRepository.existsById(id);
    }

    public Optional<AeropuertoEntity> findByCodigo(String codigo) {
        return aeropuertoRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return aeropuertoRepository.findByCodigo(codigo).isPresent();
    }

    public Optional<AeropuertoEntity> findByAlias(String alias) {
        return aeropuertoRepository.findByAlias(alias);
    }

    public boolean existsByAlias(String alias) {
        return aeropuertoRepository.findByAlias(alias).isPresent();
    }

    public List<AeropuertoEntity> findByEsSede(Boolean esSede) {
        return aeropuertoRepository.findByEsSede(esSede);
    }

    public ListResponse listar(ListRequest request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        List<DTO> dtos = new ArrayList<>();
        List<AeropuertoEntity> entities = this.findAll(pageable);
        entities.forEach(entity -> dtos.add(aeropuertoMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Aeropuertos listados correctamente! ('%d')", dtos.size()), dtos);
    }

    public ListResponse filtrar(FilterRequest<AeropuertoDTO> request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        AeropuertoDTO modelo = request.getModelo();
        String codigo = G4DUtility.Convertor.toAdmissible(modelo.getCodigo());
        String alias = G4DUtility.Convertor.toAdmissible(modelo.getAlias());
        String continente = G4DUtility.Convertor.toAdmissible(modelo.getContinente());
        String pais = G4DUtility.Convertor.toAdmissible(modelo.getPais());
        String ciudad = G4DUtility.Convertor.toAdmissible(modelo.getCiudad());
        Boolean esSede = modelo.getEsSede();
        List<DTO> dtos = new ArrayList<>();
        List<AeropuertoEntity> entities = aeropuertoRepository.filterBy(codigo, alias, continente, pais, ciudad, esSede, pageable).getContent();
        entities.forEach(entity -> dtos.add(aeropuertoMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Aeropuertos filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    public GenericResponse importar(ImportRequest<AeropuertoDTO> request) {
        AeropuertoDTO dto = request.getDto();
        AeropuertoEntity aeropuerto = new AeropuertoEntity();
        aeropuerto.setCodigo(dto.getCodigo());
        aeropuerto.setCiudad(dto.getCiudad());
        aeropuerto.setPais(dto.getPais());
        aeropuerto.setContinente(dto.getContinente());
        aeropuerto.setAlias(dto.getAlias());
        aeropuerto.setHusoHorario(dto.getHusoHorario());
        aeropuerto.setCapacidad(dto.getCapacidad());
        aeropuerto.setEsSede(dto.getEsSede());
        aeropuerto.setLatitudDEC(dto.getLatitud());
        aeropuerto.setLatitudDMS(G4DUtility.Calculator.getLatDMS(aeropuerto.getLatitudDEC()));
        aeropuerto.setLongitudDEC(dto.getLongitud());
        aeropuerto.setLongitudDMS(G4DUtility.Calculator.getLonDMS(aeropuerto.getLongitudDEC()));
        this.save(aeropuerto);
        G4DUtility.Logger.logln("[<] AEROPUERTO CARGADO!");
        return new GenericResponse(true, "Aeropuerto importado correctamente!");
    }

    public GenericResponse importar(MultipartFile archivo) {
        try {
            System.out.printf("Importando aeropuertos desde '%s'..%n", archivo.getName());
            BufferedReader br = new BufferedReader(new InputStreamReader(archivo.getInputStream(), G4DUtility.Reader.getFileCharset(archivo)));
            List<AeropuertoEntity> aeropuertos = new ArrayList<>();
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 2;
            String continente = "";
            String linea;
            br.readLine();
            br.readLine();
            WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.INICIADO));
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if(!linea.isBlank()) {
                    if(Character.isDigit(linea.charAt(0))) {
                        String[] partes = linea.split("\\s{2,}");
                        AeropuertoEntity aeropuerto = new AeropuertoEntity();
                        aeropuerto.setCodigo(partes[1].trim());
                        aeropuerto.setCiudad(partes[2].trim());
                        aeropuerto.setPais(partes[3].trim());
                        aeropuerto.setContinente(continente);
                        aeropuerto.setAlias(partes[4].trim());
                        aeropuerto.setHusoHorario(Integer.parseInt(partes[5].trim()));
                        aeropuerto.setCapacidad(Integer.parseInt(partes[6].trim()));
                        String latDMS = partes[7].replace("Latitude:", "").trim();
                        String lonDMS = partes[9].trim();
                        aeropuerto.setLatitudDMS(latDMS);
                        aeropuerto.setLatitudDEC(G4DUtility.Calculator.getLatDEC(latDMS));
                        aeropuerto.setLongitudDMS(lonDMS);
                        aeropuerto.setLongitudDEC(G4DUtility.Calculator.getLonDEC(lonDMS));
                        aeropuerto.setEsSede(false);
                        aeropuertos.add(aeropuerto);
                    } else continente = linea.split("\\.")[0].trim();
                }
                lProcesadas++;
                WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (lProcesadas % 1000 == 0 || lProcesadas == lTotales) {
                    importService.batchSave(aeropuertos, "aeropuertos");
                    System.out.printf("[<] AEROPUERTOS IMPORTADOS! ('%d')%n", aeropuertos.size());
                    aeropuertos.clear();
                }
            }
            br.close();
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            return new GenericResponse(true, "Aeropuertos importados correctamente!");
        } catch (ArrayIndexOutOfBoundsException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getName()));
        } catch (IOException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getName()));
        }
    }
}
