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
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AeropuertoEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PaisEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.AeropuertoRepository;
import com.pucp.dp1.grupo4d.morapack.service.CommunicationService;
import com.pucp.dp1.grupo4d.morapack.service.ImportationService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Sort;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class AeropuertoService {
    private final AeropuertoRepository aeropuertoRepository;
    private final AeropuertoMapper aeropuertoMapper;
    private final CommunicationService communicationService;
    private final ImportationService importationService;
    private final PaisService paisService;

    public AeropuertoService(AeropuertoRepository aeropuertoRepository, AeropuertoMapper aeropuertoMapper, CommunicationService communicationService, ImportationService importationService, PaisService paisService) {
        this.aeropuertoRepository = aeropuertoRepository;
        this.aeropuertoMapper = aeropuertoMapper;
        this.communicationService = communicationService;
        this.importationService = importationService;
        this.paisService = paisService;
    }

    public AeropuertoEntity save(AeropuertoEntity aeropuerto) {
        return aeropuertoRepository.save(aeropuerto);
    }

    public int unsetAllOriginFlags() {
        return aeropuertoRepository.unsetAllOriginFlags();
    }

    public int setOriginFlagsByList(List<String> codOrigenes) {
        return aeropuertoRepository.setOriginFlagsByList(codOrigenes);
    }

    // Reemplaza el conjunto de sedes (origen de los envíos). Debe quedar al menos una.
    @Transactional
    public GenericResponse actualizarSedes(List<String> codigos) {
        List<String> limpios = (codigos == null) ? List.of() : codigos.stream().filter(c -> c != null && !c.isBlank()).map(c -> c.trim().toUpperCase()).distinct().toList();
        if (limpios.isEmpty()) throw new G4DException("Debe haber al menos una sede.");
        for (String codigo : limpios) {
            if (!this.existsByCodigo(codigo)) throw new G4DException(String.format("El aeropuerto ('%s') no existe.", codigo));
        }
        this.unsetAllOriginFlags();
        this.setOriginFlagsByList(limpios);
        return new GenericResponse(true, String.format("Sedes actualizadas: %s.", String.join(", ", limpios)));
    }

    public List<AeropuertoEntity> findAll() {
        return aeropuertoRepository.findAll();
    }

    public List<AeropuertoEntity> findAll(Pageable pageable) {
        return aeropuertoRepository.findAll(pageable).getContent();
    }

    public List<AeropuertoEntity> findAllByAttributes(String codigo, String alias, String continente, String pais, String ciudad, Boolean esSede, Pageable pageable) {
        return aeropuertoRepository.findAllByAttributes(codigo, alias, continente, pais, ciudad, esSede, pageable).getContent();
    }

    public List<AeropuertoEntity> findAllByEsSede(Boolean esSede) {
        return aeropuertoRepository.findAllByEsSede(esSede);
    }

    public Optional<AeropuertoEntity> findById(Integer id) {
        return aeropuertoRepository.findById(id);
    }

    public boolean existsById(Integer id) {
        return aeropuertoRepository.existsById(id);
    }

    public void deleteById(Integer id) {
        aeropuertoRepository.deleteById(id);
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
        List<AeropuertoEntity> entities = this.findAllByAttributes(codigo, alias, continente, pais, ciudad, esSede, pageable);
        entities.forEach(entity -> dtos.add(aeropuertoMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Aeropuertos filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    @Transactional
    public void importar(String idTransaccion, AeropuertoDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando aeropuerto..");
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando aeropuerto", 0, 1));
            if (this.existsByCodigo(dto.getCodigo())) {
                throw new G4DException(String.format("El aeropuerto ('%s') ya existe.", dto.getCodigo()));
            }
            AeropuertoEntity aeropuerto = new AeropuertoEntity();
            aeropuerto.setCodigo(dto.getCodigo());
            aeropuerto.setCiudad(dto.getCiudad());
            aeropuerto.setPais(paisService.obtenerExistente(dto.getPais(), null));
            aeropuerto.setAlias(dto.getAlias());
            aeropuerto.setCapacidad(dto.getCapacidad());
            aeropuerto.setEsSede(dto.getEsSede() != null ? dto.getEsSede() : false);
            aeropuerto.setLatitudDEC(dto.getLatitud());
            aeropuerto.setLatitudDMS(G4DUtility.Calculator.getLatDMS(aeropuerto.getLatitudDEC()));
            aeropuerto.setLongitudDEC(dto.getLongitud());
            aeropuerto.setLongitudDMS(G4DUtility.Calculator.getLonDMS(aeropuerto.getLongitudDEC()));
            this.save(aeropuerto);
            System.out.println("[<] AEROPUERTO CARGADO!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando aeropuerto", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw (e instanceof G4DException g4dEx) ? g4dEx : new RuntimeException(e);
        }
    }

    // Formato del profesor: encabezado de 2 líneas, luego bloques "<Continente>." seguidos de filas
    // "NN  CODIGO  CIUDAD  PAIS  ALIAS  GMT  CAPACIDAD  Latitude: ...  Longitude: ..."
    public void importar(String idTransaccion, Path archivo) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.printf(">> Importando aeropuertos desde '%s'..%n", archivo.getFileName());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 0, 2));
            BufferedReader br = Files.newBufferedReader(archivo, G4DUtility.Reader.getFileCharset(archivo));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 1, 2));
            Map<String, PaisEntity> poolPaises = new HashMap<>();
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 2, omitidos = 0;
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 2, 2));
            List<AeropuertoEntity> aeropuertos = new ArrayList<>();
            Set<String> codigosDelArchivo = new HashSet<>();
            String continente = "";
            String linea;
            br.readLine();
            br.readLine();
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            while ((linea = br.readLine()) != null) {
                linea = linea.replace("\uFEFF", "").trim();
                if(!linea.isBlank()) {
                    if(Character.isDigit(linea.charAt(0))) {
                        // Sede solo si la línea lo indica (palabra SEDE al final); si no, la marca el administrador
                        boolean esSede = linea.toUpperCase().matches(".*\\sSEDE$");
                        if(esSede) linea = linea.substring(0, linea.length() - 4).trim();
                        String[] partes = linea.split("\\s{2,}");
                        String codigo = partes[1].trim();
                        if(!codigosDelArchivo.add(codigo)) throw new G4DException(String.format("El aeropuerto '%s' está repetido en el archivo (línea #%d).", codigo, lProcesadas + 1));
                        if(!this.existsByCodigo(codigo)) {
                            AeropuertoEntity aeropuerto = new AeropuertoEntity();
                            aeropuerto.setCodigo(codigo);
                            aeropuerto.setCiudad(partes[2].trim());
                            String nombrePais = partes[3].trim();
                            final int numLinea = lProcesadas + 1;
                            PaisEntity pais;
                            try {
                                pais = paisService.obtenerExistente(nombrePais, poolPaises);
                            } catch (G4DException e) {
                                throw new G4DException(String.format("El país '%s' del aeropuerto '%s' no está registrado (línea #%d). Importación cancelada.", nombrePais, codigo, numLinea));
                            }
                            aeropuerto.setPais(pais);
                            aeropuerto.setAlias(partes[4].trim());
                            aeropuerto.setCapacidad(Integer.parseInt(partes[6].trim()));
                            String latDMS = partes[7].replace("Latitude:", "").trim();
                            String lonDMS = partes[9].trim();
                            aeropuerto.setLatitudDMS(latDMS);
                            aeropuerto.setLatitudDEC(G4DUtility.Calculator.getLatDEC(latDMS));
                            aeropuerto.setLongitudDMS(lonDMS);
                            aeropuerto.setLongitudDEC(G4DUtility.Calculator.getLonDEC(lonDMS));
                            aeropuerto.setEsSede(esSede);
                            aeropuertos.add(aeropuerto);
                        } else omitidos++;
                    } else continente = linea.split("\\.")[0].trim();
                }
                lProcesadas++;
                communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            }
            // Se guarda todo al final: si alguna línea falla, la importación se cancela sin guardar nada
            if (!aeropuertos.isEmpty()) {
                importationService.batchSave(aeropuertos, progressDestination, "aeropuertos");
                System.out.printf("[<] AEROPUERTOS IMPORTADOS! ('%d')%n", aeropuertos.size());
            }
            if (omitidos > 0) System.out.printf("[*] AEROPUERTOS OMITIDOS POR CODIGO REPETIDO: '%d'%n", omitidos);
            br.close();
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getFileName()));
        } catch (IOException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getFileName()));
        } catch (G4DException e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw e;
        }
    }
}
