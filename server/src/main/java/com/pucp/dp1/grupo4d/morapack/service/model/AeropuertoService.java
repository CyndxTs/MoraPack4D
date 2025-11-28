/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AeropuertoService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.AeropuertoMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.AeropuertoDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ImportRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.repository.AeropuertoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class AeropuertoService {

    private final AeropuertoRepository aeropuertoRepository;
    private final AeropuertoMapper aeropuertoMapper;
    private final HashMap<String, AeropuertoEntity> aeropuertos = new HashMap<>();

    public AeropuertoService(AeropuertoRepository aeropuertoRepository, AeropuertoMapper aeropuertoMapper) {
        this.aeropuertoRepository = aeropuertoRepository;
        this.aeropuertoMapper = aeropuertoMapper;
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

    public AeropuertoEntity obtenerPorCodigo(String codigo) {
        AeropuertoEntity aeropuerto = aeropuertos.get(codigo);
        if (aeropuerto == null) {
            aeropuerto = this.findByCodigo(codigo).orElse(null);
            if (aeropuerto != null) {
                aeropuertos.put(codigo, aeropuerto);
            }
        }
        return aeropuerto;
    }

    public ListResponse listar(ListRequest request) {
        try {
            int page = G4D.toAdmissibleValue(request.getPage(), 0);
            int size = G4D.toAdmissibleValue(request.getSize(), 10);
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            List<DTO> dtos = new ArrayList<>();
            List<AeropuertoEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(aeropuertoMapper.toDTO(entity)));
            return new ListResponse(true, "Aeropuertos listados correctamente!", dtos);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public ListResponse filtrar(FilterRequest<AeropuertoDTO> request) {
        try {
            int page = G4D.toAdmissibleValue(request.getPage(), 0);
            int size = G4D.toAdmissibleValue(request.getSize(), 10);
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            AeropuertoDTO model = request.getFilterModel();
            String codigo = G4D.toAdmissibleValue(model.getCodigo());
            String alias = G4D.toAdmissibleValue(model.getAlias());
            String continente = G4D.toAdmissibleValue(model.getContinente());
            String pais = G4D.toAdmissibleValue(model.getPais());
            String ciudad = G4D.toAdmissibleValue(model.getCiudad());
            Boolean esSede = model.getEsSede();
            List<DTO> dtos = new ArrayList<>();
            List<AeropuertoEntity> entities = aeropuertoRepository.filterBy(codigo, alias, continente, pais, ciudad, esSede, pageable).getContent();
            entities.forEach(entity -> dtos.add(aeropuertoMapper.toDTO(entity)));
            return new ListResponse(true, "Aeropuertos filtrados correctamente!", dtos);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - FILTRADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportRequest<AeropuertoDTO> request) {
        try {
            AeropuertoDTO dto = request.getDto();
            AeropuertoEntity aeropuerto = aeropuertoMapper.toEntity(dto);
            this.save(aeropuerto);
            G4D.Logger.logln("[<] AEROPUERTO CARGADO!");
            return new GenericResponse(true, "Aeropuerto importado correctamente!");
        } catch (Exception e) {
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo) {
        try {
            G4D.Logger.logf("Cargando aeropuertos desde '%s'..%n", archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            String continente = "";
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                if (linea.isEmpty()) continue;
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                if (Character.isDigit(linea.charAt(0))) {
                    AeropuertoEntity aeropuerto = new AeropuertoEntity();
                    lineaSC.nextInt();
                    aeropuerto.setCodigo(lineaSC.next());
                    aeropuerto.setCiudad(lineaSC.next());
                    aeropuerto.setPais(lineaSC.next());
                    aeropuerto.setContinente(continente);
                    aeropuerto.setAlias(lineaSC.next());
                    aeropuerto.setHusoHorario(lineaSC.nextInt());
                    aeropuerto.setCapacidad(lineaSC.nextInt());
                    lineaSC.useDelimiter("\\s+");
                    lineaSC.next();
                    aeropuerto.setLatitudDMS(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    aeropuerto.setLatitudDEC(G4D.toLatDEC(aeropuerto.getLatitudDMS()));
                    lineaSC.next();
                    aeropuerto.setLongitudDMS(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    aeropuerto.setLongitudDEC(G4D.toLonDEC(aeropuerto.getLongitudDMS()));
                    aeropuerto.setEsSede(false);
                    aeropuertos.put(aeropuerto.getCodigo(), aeropuerto);
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            archivoSC.close();
            aeropuertos.values().forEach(this::save);
            G4D.Logger.logf("[<] AEROPUERTOS CARGADOS! ('%d')%n", aeropuertos.size());
            return new GenericResponse(true, "Aeropuertos importados correctamente!");
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! ('%s')%n", archivo.getName());
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } catch (Exception e) {
            return new GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        aeropuertos.clear();
        aeropuertoMapper.clearPools();
    }
}
