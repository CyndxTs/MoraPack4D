/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ParametrosService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.ParametrosMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.ParametrosDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ParametrosEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.repository.ParametrosRepository;
import com.pucp.dp1.grupo4d.morapack.service.CommunicationService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class ParametrosService {
    private final ParametrosRepository parametrosRepository;
    private final ParametrosMapper parametrosMapper;
    private final AeropuertoService aeropuertoService;
    private final CommunicationService communicationService;

    public ParametrosService(ParametrosRepository parametrosRepository, ParametrosMapper parametrosMapper, AeropuertoService aeropuertoService, CommunicationService communicationService) {
        this.parametrosRepository = parametrosRepository;
        this.parametrosMapper = parametrosMapper;
        this.aeropuertoService = aeropuertoService;
        this.communicationService = communicationService;
    }

    public ParametrosEntity save(ParametrosEntity parametros) {
        aeropuertoService.unsetAllOriginFlags();
        if(!parametros.getCodOrigenes().isEmpty()) {
            aeropuertoService.setOriginFlagsByList(parametros.getCodOrigenes());
        }
        parametros.setId(1);
        return parametrosRepository.save(parametros);
    }

    public List<ParametrosEntity> findAll() {
        List<ParametrosEntity> result = new ArrayList<>();
        ParametrosEntity parametros = this.findById(1);
        if (parametros != null) {
            result.add(parametros);
        }
        return result;
    }

    public ParametrosEntity findById(Integer id) {
        ParametrosEntity result = parametrosRepository.findById(id).orElse(null);
        if (result != null) {
            List<String> codOrigenes =  new ArrayList<>();
            List<AeropuertoEntity> origenes = aeropuertoService.findAllByEsSede(true);
            origenes.forEach(a -> codOrigenes.add(a.getCodigo()));
            result.setCodOrigenes(codOrigenes);
        }
        return result;
    }

    public boolean existsById(Integer id) {
        return parametrosRepository.existsById(id);
    }

    public void deleteById(Integer id) {
        parametrosRepository.deleteById(id);
    }

    public ListResponse listar() {
        List<DTO> dtos = new ArrayList<>();
        List<ParametrosEntity> entities = this.findAll();
        entities.forEach(entity -> dtos.add(parametrosMapper.toDTO(entity)));
        return new ListResponse(true, "Parametros listados correctamente!", dtos);
    }

    public void importar(String idTransaccion, ParametrosDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando parametros..");
            ParametrosEntity parametros = new ParametrosEntity();
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando parametros", 0, 1));
            parametros.setMaxDiasEntregaIntracontinental(G4DUtility.Convertor.toAdmissible(dto.getMaxDiasEntregaIntracontinental(), 2));
            parametros.setMaxDiasEntregaIntercontinental(G4DUtility.Convertor.toAdmissible(dto.getMaxDiasEntregaIntercontinental(), 3));
            parametros.setMaxHorasRecojo(G4DUtility.Convertor.toAdmissible(dto.getMaxHorasRecojo(), 2.0));
            parametros.setMaxHorasEstancia(G4DUtility.Convertor.toAdmissible(dto.getMaxHorasEstancia(), 12.0));
            parametros.setMinHorasEstancia(G4DUtility.Convertor.toAdmissible(dto.getMinHorasEstancia(), 1.0));
            parametros.setProbabilidadReplanificacion(G4DUtility.Convertor.toAdmissible(dto.getProbabilidadReplanificacion(), 0.350));
            parametros.setCodOrigenes(G4DUtility.Convertor.toAdmissible(dto.getCodOrigenes(), () -> List.of("SPIM", "EBCI", "UBBB")));
            parametros.setDMin(G4DUtility.Convertor.toAdmissible(dto.getDMin(), 0.005));
            parametros.setIMax(G4DUtility.Convertor.toAdmissible(dto.getIMax(), 3));
            parametros.setEleMin(G4DUtility.Convertor.toAdmissible(dto.getEleMin(), 1));
            parametros.setEleMax(G4DUtility.Convertor.toAdmissible(dto.getEleMax(), 2));
            parametros.setKMin(G4DUtility.Convertor.toAdmissible(dto.getKMin(), 3));
            parametros.setKMax(G4DUtility.Convertor.toAdmissible(dto.getKMax(), 5));
            parametros.setNMax(G4DUtility.Convertor.toAdmissible(dto.getNMax(), 6));
            parametros.setTMax(G4DUtility.Convertor.toAdmissible(dto.getTMax(), 7));
            parametros.setFactorDeUmbralDeAberracion(G4DUtility.Convertor.toAdmissible(dto.getFactorDeUmbralDeAberracion(), 1.015));
            parametros.setFactorDeUtilizacionTemporal(G4DUtility.Convertor.toAdmissible(dto.getFactorDeUtilizacionTemporal(), 5000.0));
            parametros.setFactorDeDesviacionEspacial(G4DUtility.Convertor.toAdmissible(dto.getFactorDeDesviacionEspacial(), 2000.0));
            parametros.setFactorDeDisposicionOperacional(G4DUtility.Convertor.toAdmissible(dto.getFactorDeDisposicionOperacional(), 3000.0));
            this.save(parametros);
            System.out.println("[<] PARAMETROS IMPORTADOS!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando parametros", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new RuntimeException(e);
        }
    }
}
