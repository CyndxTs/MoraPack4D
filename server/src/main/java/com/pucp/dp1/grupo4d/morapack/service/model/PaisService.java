/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       PaisService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.PaisMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.PaisDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.ContinenteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PaisEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.ContinenteRepository;
import com.pucp.dp1.grupo4d.morapack.repository.PaisRepository;
import com.pucp.dp1.grupo4d.morapack.service.CommunicationService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaisService {
    // País por defecto para usuarios importados por archivo sin columna de país
    public static final String PAIS_POR_DEFECTO = "Perú";

    private final PaisRepository paisRepository;
    private final ContinenteRepository continenteRepository;
    private final PaisMapper paisMapper;
    private final CommunicationService communicationService;

    public PaisService(PaisRepository paisRepository, ContinenteRepository continenteRepository, PaisMapper paisMapper, CommunicationService communicationService) {
        this.paisRepository = paisRepository;
        this.continenteRepository = continenteRepository;
        this.paisMapper = paisMapper;
        this.communicationService = communicationService;
    }

    public PaisEntity save(PaisEntity pais) {
        return paisRepository.save(pais);
    }

    public List<PaisEntity> findAll() {
        return paisRepository.findAll();
    }

    public List<PaisEntity> findAll(Pageable pageable) {
        return paisRepository.findAll(pageable).getContent();
    }

    public List<PaisEntity> findAllByAttributes(String nombre, Integer husoHorario, String continente, Pageable pageable) {
        return paisRepository.findAllByAttributes(nombre, husoHorario, continente, pageable).getContent();
    }

    public Optional<PaisEntity> findById(Integer id) {
        return paisRepository.findById(id);
    }

    public Optional<PaisEntity> findByNombre(String nombre) {
        return paisRepository.findByNombre(nombre);
    }

    public boolean existsByNombre(String nombre) {
        return paisRepository.existsByNombre(nombre);
    }

    public List<ContinenteEntity> findAllContinentes() {
        return continenteRepository.findAll();
    }

    // Obtener país por nombre; si no existe lo crea (y a su continente) con el huso indicado.
    // El pool (opcional) evita consultas repetidas durante una importación.
    public PaisEntity obtenerOCrear(String nombrePais, Integer husoHorario, String nombreContinente, Map<String, PaisEntity> poolPaises) {
        String nombre = G4DUtility.Convertor.toAdmissible(nombrePais);
        if (nombre == null) {
            throw new G4DException("El nombre del país es obligatorio.");
        }
        nombre = nombre.trim();
        if (poolPaises != null && poolPaises.containsKey(nombre)) {
            return poolPaises.get(nombre);
        }
        PaisEntity pais = paisRepository.findByNombre(nombre).orElse(null);
        if (pais == null) {
            if (husoHorario == null) {
                throw new G4DException(String.format("El país ('%s') no existe y no se indicó su huso horario.", nombre));
            }
            String nombreCont = G4DUtility.Convertor.toAdmissible(nombreContinente);
            if (nombreCont == null) {
                throw new G4DException(String.format("El país ('%s') no existe y no se indicó su continente.", nombre));
            }
            final String nombreContFinal = nombreCont.trim();
            ContinenteEntity continente = continenteRepository.findByNombre(nombreContFinal).orElseGet(() -> {
                ContinenteEntity nuevo = new ContinenteEntity();
                nuevo.setNombre(nombreContFinal);
                return continenteRepository.save(nuevo);
            });
            pais = new PaisEntity();
            pais.setNombre(nombre);
            pais.setHusoHorario(husoHorario);
            pais.setContinente(continente);
            pais = paisRepository.save(pais);
        }
        if (poolPaises != null) {
            poolPaises.put(nombre, pais);
        }
        return pais;
    }

    // País de un usuario: el indicado o, si no se indicó, el país por defecto (o el primero registrado)
    // País que debe existir (cargado por el seed o por la importación de PAIS). No crea nada.
    public PaisEntity obtenerExistente(String nombrePais, Map<String, PaisEntity> poolPaises) {
        String nombre = G4DUtility.Convertor.toAdmissible(nombrePais);
        if (nombre == null) {
            throw new G4DException("El nombre del país es obligatorio.");
        }
        final String nombreFinal = nombre.trim();
        if (poolPaises != null && poolPaises.containsKey(nombreFinal)) {
            return poolPaises.get(nombreFinal);
        }
        PaisEntity pais = paisRepository.findByNombre(nombreFinal).orElseThrow(() -> new G4DException(String.format("El país ('%s') no está registrado en el sistema.", nombreFinal)));
        if (poolPaises != null) poolPaises.put(nombreFinal, pais);
        return pais;
    }

    public PaisEntity obtenerParaUsuario(String nombrePais) {
        String nombre = G4DUtility.Convertor.toAdmissible(nombrePais);
        if (nombre != null) {
            return paisRepository.findByNombre(nombre.trim()).orElseThrow(() -> new G4DException(String.format("El país ('%s') no está soportado por el sistema.", nombrePais)));
        }
        return paisRepository.findByNombre(PAIS_POR_DEFECTO)
                .or(() -> paisRepository.findAll().stream().findFirst())
                .orElseThrow(() -> new G4DException("No hay países registrados. Ejecute el seed o importe un país (PAIS)."));
    }

    public ListResponse listar(ListRequest request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("nombre"));
        List<DTO> dtos = new ArrayList<>();
        List<PaisEntity> entities = this.findAll(pageable);
        entities.forEach(entity -> dtos.add(paisMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Paises listados correctamente! ('%d')", dtos.size()), dtos);
    }

    public ListResponse filtrar(FilterRequest<PaisDTO> request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("nombre"));
        PaisDTO modelo = request.getModelo();
        String nombre = G4DUtility.Convertor.toAdmissible(modelo.getNombre());
        Integer husoHorario = modelo.getHusoHorario();
        String continente = G4DUtility.Convertor.toAdmissible(modelo.getContinente());
        List<DTO> dtos = new ArrayList<>();
        List<PaisEntity> entities = this.findAllByAttributes(nombre, husoHorario, continente, pageable);
        entities.forEach(entity -> dtos.add(paisMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Paises filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    public void importar(String idTransaccion, PaisDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando país..");
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando país", 0, 1));
            if (this.existsByNombre(dto.getNombre())) {
                throw new G4DException(String.format("El país ('%s') ya existe.", dto.getNombre()));
            }
            this.obtenerOCrear(dto.getNombre(), dto.getHusoHorario(), dto.getContinente(), null);
            System.out.println("[<] PAIS IMPORTADO!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando país", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            throw (e instanceof G4DException g4dEx) ? g4dEx : new RuntimeException(e);
        }
    }
}
