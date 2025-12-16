/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.AdministradorRepository;
import com.pucp.dp1.grupo4d.morapack.service.ImportService;
import com.pucp.dp1.grupo4d.morapack.service.WebSocketService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class AdministradorService {
    private final ImportService importService;
    private final AdministradorRepository administradorRepository;
    private final UsuarioMapper usuarioMapper;

    public AdministradorService(AdministradorRepository administradorRepository, UsuarioMapper usuarioMapper, ImportService importService) {
        this.administradorRepository = administradorRepository;
        this.usuarioMapper = usuarioMapper;
        this.importService = importService;
    }

    public List<AdministradorEntity> findAll() {
        return administradorRepository.findAll();
    }

    public List<AdministradorEntity> findAll(Pageable pageable) {
        return administradorRepository.findAll(pageable).getContent();
    }

    public Optional<AdministradorEntity> findById(Integer id) {
        return administradorRepository.findById(id);
    }

    public AdministradorEntity save(AdministradorEntity administrador) {
        return administradorRepository.save(administrador);
    }

    public void deleteById(Integer id) {
        administradorRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return administradorRepository.existsById(id);
    }

    public Optional<AdministradorEntity> findByCodigo(String codigo) {
        return administradorRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return administradorRepository.findByCodigo(codigo).isPresent();
    }

    public Optional<AdministradorEntity> findByCorreo(String correo) {
        return administradorRepository.findByCorreo(correo);
    }

    public boolean existsByCorreo(String correo) {
        return administradorRepository.findByCorreo(correo).isPresent();
    }

    public Integer findMaxCode() {
        return G4DUtility.Convertor.toAdmissible(administradorRepository.findMaxCode(), 0);
    }

    public ListResponse listar(ListRequest request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        List<DTO> dtos = new ArrayList<>();
        List<AdministradorEntity> entities = this.findAll(pageable);
        entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Administradores listados correctamente! ('%d')", dtos.size()), dtos);
    }

    public ListResponse filtrar(FilterRequest<UsuarioDTO> request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        UsuarioDTO modelo = request.getModelo();
        String nombre = G4DUtility.Convertor.toAdmissible(modelo.getNombre());
        String correo = G4DUtility.Convertor.toAdmissible(modelo.getCorreo());
        String estado = G4DUtility.Convertor.toAdmissibleEnumString(modelo.getEstado(), EstadoUsuario.class);
        List<DTO> dtos = new ArrayList<>();
        List<AdministradorEntity> entities = administradorRepository.filterBy(nombre, correo, estado, pageable).getContent();
        entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Administradores filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    public GenericResponse importar(String idTransaccion, Path archivo) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.printf(">> Importando administradores desde '%s'..%n", archivo.getFileName().toString());
            BufferedReader br = Files.newBufferedReader(archivo, G4DUtility.Reader.getFileCharset(archivo));
            List<AdministradorEntity> administradores = new ArrayList<>();
            Map<String, AdministradorEntity> poolAdministradores = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, AdministradorEntity> eldest) {
                    return size() > 500;
                }
            };
            this.findAll(PageRequest.of(0, 250)).forEach(c -> poolAdministradores.put(c.getCorreo(), c));
            int maxCodigo = this.findMaxCode();
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0;
            String linea;
            WebSocketService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    String[] partes = linea.split("\\s{2,}");
                    String nombre = partes[0];
                    String correo = partes[1];
                    if (!poolAdministradores.containsKey(correo) || !this.existsByCorreo(correo)) {
                        AdministradorEntity administrador = new AdministradorEntity();
                        administrador.setCodigo(String.format("ADMIN%02d", ++maxCodigo));
                        administrador.setNombre(nombre);
                        administrador.setCorreo(correo);
                        administrador.setContrasenia("12345678");
                        administradores.add(administrador);
                        poolAdministradores.put(correo, administrador);
                    }
                }
                lProcesadas++;
                WebSocketService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (lProcesadas % 500 == 0 || lProcesadas == lTotales) {
                    importService.batchSave(administradores, "administradores");
                    System.out.printf("[<] ADMINISTRADORES IMPORTADOS! ('%d')%n", administradores.size());
                    administradores.clear();
                }
            }
            poolAdministradores.clear();
            br.close();
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            return new GenericResponse(true, "Administradores importados correctamente!");
        } catch (ArrayIndexOutOfBoundsException e) {
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getFileName()));
        } catch (IOException e) {
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getFileName()));
        }
    }
}
