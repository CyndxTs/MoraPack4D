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
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.AeropuertoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
import java.util.*;

@Service
public class AeropuertoService {

    private final AeropuertoRepository aeropuertoRepository;
    private final AeropuertoMapper aeropuertoMapper;
    private final List<AeropuertoEntity> aeropuertos = new ArrayList<>();

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
        AeropuertoEntity aeropuerto = aeropuertos.stream().filter(entity -> entity.getCodigo().equals(codigo)).findFirst().orElse(null);
        if (aeropuerto == null) {
            aeropuerto = this.findByCodigo(codigo).orElse(null);
            if (aeropuerto != null) {
                aeropuertos.add(aeropuerto);
            }
        }
        return aeropuerto;
    }

    public ListResponse listar(ListRequest request) throws Exception {
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
            List<DTO> dtos = new ArrayList<>();
            List<AeropuertoEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(aeropuertoMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Aeropuertos listados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public ListResponse filtrar(FilterRequest<AeropuertoDTO> request) throws Exception {
        try {
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
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(ImportRequest<AeropuertoDTO> request) throws Exception{
        try {
            AeropuertoDTO dto = request.getDto();
            AeropuertoEntity aeropuerto = aeropuertoMapper.toEntity(dto);
            this.save(aeropuerto);
            G4DUtility.Logger.logln("[<] AEROPUERTO CARGADO!");
            return new GenericResponse(true, "Aeropuerto importado correctamente!");
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo) throws Exception {
        try {
            System.out.printf("Importando aeropuertos desde '%s'..%n", archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4DUtility.Reader.getFileCharset(archivo));
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
                    aeropuerto.setLatitudDEC(G4DUtility.Calculator.getLatDEC(aeropuerto.getLatitudDMS()));
                    lineaSC.next();
                    aeropuerto.setLongitudDMS(lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next() + " " + lineaSC.next());
                    aeropuerto.setLongitudDEC(G4DUtility.Calculator.getLonDEC(aeropuerto.getLongitudDMS()));
                    aeropuerto.setEsSede(false);
                    aeropuertos.add(aeropuerto);
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            archivoSC.close();
            aeropuertos.forEach(this::save);
            System.out.printf("[<] AEROPUERTOS IMPORTADOS! ('%d')%n", aeropuertos.size());
            return new GenericResponse(true, String.format("Aeropuertos importados correctamente! ('%d')",  aeropuertos.size()));
        } catch (NoSuchElementException | FileNotFoundException e) {
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado o está vacío.", archivo.getName()));
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        aeropuertos.clear();
        aeropuertoMapper.clearPools();
    }
}
