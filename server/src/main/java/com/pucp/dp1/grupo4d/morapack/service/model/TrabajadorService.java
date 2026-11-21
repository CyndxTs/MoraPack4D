/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       TrabajadorService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.ProgressPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.payload.StatusPayload;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.TrabajadorEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PaisEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.TrabajadorRepository;
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
import java.util.*;

@Service
public class TrabajadorService {
    // Contraseña inicial para usuarios cargados por el administrador (se cambiará con el flujo de autenticación)
    private static final String CONTRASENIA_INICIAL = "12345678";

    private final TrabajadorRepository trabajadorRepository;
    private final UsuarioMapper usuarioMapper;
    private final PaisService paisService;
    private final CommunicationService communicationService;
    private final ImportationService importationService;

    public TrabajadorService(TrabajadorRepository trabajadorRepository, UsuarioMapper usuarioMapper, PaisService paisService, CommunicationService communicationService, ImportationService importationService) {
        this.trabajadorRepository = trabajadorRepository;
        this.usuarioMapper = usuarioMapper;
        this.paisService = paisService;
        this.communicationService = communicationService;
        this.importationService = importationService;
    }

    public TrabajadorEntity save(TrabajadorEntity trabajador) {
        return trabajadorRepository.save(trabajador);
    }

    public List<TrabajadorEntity> findAll() {
        return trabajadorRepository.findAll();
    }

    public List<TrabajadorEntity> findAll(Pageable pageable) {
        return trabajadorRepository.findAll(pageable).getContent();
    }

    public List<TrabajadorEntity> findAllByAttributes(String codigo, String nombres, String apellidos, String correo, String estado, String pais, Pageable pageable) {
        return trabajadorRepository.findAllByAttributes(codigo, nombres, apellidos, correo, estado, pais, pageable).getContent();
    }

    public Optional<TrabajadorEntity> findById(Integer id) {
        return trabajadorRepository.findById(id);
    }

    public boolean existsById(Integer id) {
        return trabajadorRepository.existsById(id);
    }

    public void deleteById(Integer id) {
        trabajadorRepository.deleteById(id);
    }

    public Optional<TrabajadorEntity> findByCodigo(String codigo) {
        return trabajadorRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return trabajadorRepository.existsByCodigo(codigo);
    }

    public Optional<TrabajadorEntity> findByCorreo(String correo) {
        return trabajadorRepository.findByCorreo(correo);
    }

    public boolean existsByCorreo(String correo) {
        return trabajadorRepository.existsByCorreo(correo);
    }

    public Integer findMaxCode() {
        return G4DUtility.Convertor.toAdmissible(trabajadorRepository.findMaxCode(), 0);
    }

    public ListResponse listar(ListRequest request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        List<DTO> dtos = new ArrayList<>();
        List<TrabajadorEntity> entities = this.findAll(pageable);
        entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Trabajadores listados correctamente! ('%d')", dtos.size()), dtos);
    }

    public ListResponse filtrar(FilterRequest<UsuarioDTO> request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        UsuarioDTO modelo = request.getModelo();
        String codigo = G4DUtility.Convertor.toAdmissible(modelo.getCodigo());
        String nombres = G4DUtility.Convertor.toAdmissible(modelo.getNombres());
        String apellidos = G4DUtility.Convertor.toAdmissible(modelo.getApellidos());
        String correo = G4DUtility.Convertor.toAdmissible(modelo.getCorreo());
        String estado = G4DUtility.Convertor.toAdmissibleEnumString(modelo.getEstado(), EstadoUsuario.class);
        String pais = G4DUtility.Convertor.toAdmissible(modelo.getPais());
        List<DTO> dtos = new ArrayList<>();
        List<TrabajadorEntity> entities = this.findAllByAttributes(codigo, nombres, apellidos, correo, estado, pais, pageable);
        entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Trabajadores filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    // Carga unitaria
    public void importar(String idTransaccion, UsuarioDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando trabajador..");
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando trabajador", 0, 1));
            String correo = G4DUtility.Convertor.toAdmissible(dto.getCorreo());
            if (correo == null) {
                throw new G4DException("El correo del trabajador es obligatorio.");
            }
            if (this.existsByCorreo(correo)) {
                throw new G4DException(String.format("El correo ('%s') ya está registrado.", correo));
            }
            TrabajadorEntity trabajador = new TrabajadorEntity();
            trabajador.setCodigo(String.format("%07d", this.findMaxCode() + 1));
            trabajador.setNombres(G4DUtility.Convertor.toAdmissible(dto.getNombres(), ""));
            trabajador.setApellidos(G4DUtility.Convertor.toAdmissible(dto.getApellidos(), ""));
            trabajador.setCorreo(correo);
            trabajador.setContrasenia(CONTRASENIA_INICIAL);
            trabajador.setPais(paisService.obtenerParaUsuario(dto.getPais()));
            this.save(trabajador);
            System.out.println("[<] TRABAJADOR IMPORTADO!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando trabajador", 1, 1));
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (Exception e) {
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw (e instanceof G4DException g4dEx) ? g4dEx : new RuntimeException(e);
        }
    }

    // Carga por archivo. Formato por línea: NOMBRE COMPLETO<2+ espacios>CORREO[<2+ espacios>PAIS]
    public void importar(String idTransaccion, Path archivo) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.printf(">> Importando trabajadores desde '%s'..%n", archivo.getFileName());
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 0, 3));
            BufferedReader br = Files.newBufferedReader(archivo, G4DUtility.Reader.getFileCharset(archivo));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 1, 3));
            Set<String> correosRegistrados = new HashSet<>();
            int maxCodigo = this.findMaxCode();
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 2, 3));
            Map<String, PaisEntity> poolPaises = new HashMap<>();
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 0, omitidos = 0;
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando recursos de importación", 3, 3));
            List<TrabajadorEntity> trabajadores = new ArrayList<>();
            String linea;
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            while ((linea = br.readLine()) != null) {
                linea = linea.replace("\uFEFF", "").trim();
                if (!linea.isEmpty()) {
                    String[] partes = linea.split("\\s{2,}");
                    String[] nombreCompleto = G4DUtility.Convertor.toNombresApellidos(partes[0]);
                    String correo = partes[1].trim();
                    String nombrePais = (partes.length > 2) ? partes[2].trim() : null;
                    if (!correosRegistrados.contains(correo) && !this.existsByCorreo(correo)) {
                        PaisEntity pais = poolPaises.computeIfAbsent(String.valueOf(nombrePais), k -> paisService.obtenerParaUsuario(nombrePais));
                        TrabajadorEntity trabajador = new TrabajadorEntity();
                        trabajador.setCodigo(String.format("%07d", ++maxCodigo));
                        trabajador.setNombres(nombreCompleto[0]);
                        trabajador.setApellidos(nombreCompleto[1]);
                        trabajador.setCorreo(correo);
                        trabajador.setContrasenia(CONTRASENIA_INICIAL);
                        trabajador.setPais(pais);
                        trabajadores.add(trabajador);
                        correosRegistrados.add(correo);
                    } else omitidos++;
                }
                lProcesadas++;
                communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (trabajadores.size() >= 500 || (lProcesadas == lTotales && !trabajadores.isEmpty())) {
                    importationService.batchSave(trabajadores, progressDestination, "trabajadores");
                    System.out.printf("[<] TRABAJADORES IMPORTADOS! ('%d')%n", trabajadores.size());
                    trabajadores.clear();
                }
            }
            if (omitidos > 0) System.out.printf("[*] TRABAJADORES OMITIDOS POR CORREO REPETIDO: '%d'%n", omitidos);
            br.close();
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
        } catch (ArrayIndexOutOfBoundsException e) {
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
