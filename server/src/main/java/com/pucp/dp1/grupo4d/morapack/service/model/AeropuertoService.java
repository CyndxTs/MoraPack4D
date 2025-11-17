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
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.repository.AeropuertoRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class AeropuertoService {

    @Autowired
    private AeropuertoMapper aeropuertoMapper;

    private final AeropuertoRepository aeropuertoRepository;

    public AeropuertoService(AeropuertoRepository aeropuertoRepository) {
        this.aeropuertoRepository = aeropuertoRepository;
    }

    public List<AeropuertoEntity> findAll() {
        return aeropuertoRepository.findAll();
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

    public ListResponse listar() {
        try {
            List<DTO> aeropuertosDTO = new ArrayList<>();
            List<AeropuertoEntity> aeropuertosEntity = this.findAll();
            aeropuertosEntity.forEach(a -> aeropuertosDTO.add(aeropuertoMapper.toDTO(a)));
            return new ListResponse(true, "Aeropuertos listados correctamente!", aeropuertosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }

    public ListResponse filtrar(FilterRequest request) {
        try {
            AeropuertoDTO dto = (AeropuertoDTO) request.getDto();
            String codigo = G4D.toAdmissibleValue(dto.getCodigo());
            String alias = G4D.toAdmissibleValue(dto.getAlias());
            String continente = G4D.toAdmissibleValue(dto.getContinente());
            String pais = G4D.toAdmissibleValue(dto.getPais());
            String ciudad = G4D.toAdmissibleValue(dto.getCiudad());
            Boolean esSede = dto.getEsSede();
            List<DTO> aeropuertosDTO = new ArrayList<>();
            List<AeropuertoEntity> aeropuertosEntity = aeropuertoRepository.filterBy(codigo, alias, continente, pais, ciudad, esSede);
            aeropuertosEntity.forEach(a -> aeropuertosDTO.add(aeropuertoMapper.toDTO(a)));
            return new ListResponse(true, "Aeropuertos filtrados correctamente!", aeropuertosDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - FILTRADO: " + e.getMessage());
        }
    }

    public void importar(MultipartFile archivo) {
        List<AeropuertoEntity> aeropuertos = new ArrayList<>();
        String continente = "";
        try {
            G4D.Logger.logf("Cargando aeropuertos desde '%s'..%n", archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
            if (archivoSC.hasNextLine()) archivoSC.nextLine();
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
                    aeropuertos.add(aeropuerto);
                } else continente = lineaSC.next();
                lineaSC.close();
            }
            archivoSC.close();
            aeropuertos.forEach(this::save);
            G4D.Logger.logf("[<] AEROPUERTOS CARGADOS! ('%d')%n", aeropuertos.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! ('%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
