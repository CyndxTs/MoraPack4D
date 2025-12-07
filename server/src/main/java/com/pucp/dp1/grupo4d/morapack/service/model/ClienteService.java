/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteService.java
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
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.ClienteRepository;
import com.pucp.dp1.grupo4d.morapack.service.WebSocketService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final UsuarioMapper usuarioMapper;
    private final List<ClienteEntity> clientes = new ArrayList<>();

    public ClienteService(ClienteRepository clienteRepository, UsuarioMapper usuarioMapper) {
        this.clienteRepository = clienteRepository;
        this.usuarioMapper = usuarioMapper;
    }

    public List<ClienteEntity> findAll() {
        return clienteRepository.findAll();
    }

    public List<ClienteEntity> findAll(Pageable pageable) {
        return clienteRepository.findAll(pageable).getContent();
    }

    public Optional<ClienteEntity> findById(Integer id) {
        return clienteRepository.findById(id);
    }

    public ClienteEntity save(ClienteEntity cliente) {
        return clienteRepository.save(cliente);
    }

    public void deleteById(Integer id) {
        clienteRepository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return clienteRepository.existsById(id);
    }

    public Optional<ClienteEntity> findByCodigo(String codigo) {
        return clienteRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return clienteRepository.findByCodigo(codigo).isPresent();
    }

    public Optional<ClienteEntity> findByCorreo(String correo) {
        return clienteRepository.findByCorreo(correo);
    }

    public boolean existsByCorreo(String correo) {
        return clienteRepository.findByCorreo(correo).isPresent();
    }

    public List<ClienteEntity> findAllByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario) {
        return clienteRepository.findAllByDateTimeRange(fechaHoraInicio, fechaHoraFin, tipoEscenario);
    }

    public ClienteEntity obtenerPorCodigo(String codigo) {
        ClienteEntity cliente = clientes.stream().filter(entity -> entity.getCodigo().equals(codigo)).findFirst().orElse(null);
        if (cliente == null) {
            cliente = this.findByCodigo(codigo).orElse(null);
            if (cliente == null) {
                cliente = new ClienteEntity();
                cliente.setCodigo(codigo);
                cliente.setNombre(G4DUtility.Generator.getUniqueName());
                String correo = G4DUtility.Generator.getUniqueEmail();
                boolean existeCorreo = this.existsByCorreo(correo);
                if(existeCorreo) {
                    String newCorreo = "";
                    while (existeCorreo) {
                        newCorreo = G4DUtility.Generator.addRandomInteger(correo, correo.indexOf('@'));
                        existeCorreo = this.existsByCorreo(newCorreo);
                    }
                    cliente.setCorreo(newCorreo);
                } else cliente.setCorreo(correo);
                cliente.setContrasenia("12345678");
                this.save(cliente);
            }
            clientes.add(cliente);
        }
        return cliente;
    }

    public String generarNuevoCodigo() {
        OptionalInt maxCodigo;
        if (!clientes.isEmpty()) {
            maxCodigo = clientes.stream().mapToInt(entity -> Integer.parseInt(entity.getCodigo())).max();
        } else maxCodigo = this.findAll().stream().mapToInt(entity -> Integer.parseInt(entity.getCodigo())).max();
        return String.format("%07d", maxCodigo.orElse(0) + 1);
    }

    public ListResponse listar(ListRequest request) {
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
            List<DTO> dtos = new ArrayList<>();
            List<ClienteEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Clientes listados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public ListResponse filtrar(FilterRequest<UsuarioDTO> request) {
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
            UsuarioDTO modelo = request.getModelo();
            String nombre = G4DUtility.Convertor.toAdmissible(modelo.getNombre());
            String correo = G4DUtility.Convertor.toAdmissible(modelo.getCorreo());
            String estado = G4DUtility.Convertor.toAdmissibleEnumString(modelo.getEstado(), EstadoUsuario.class);
            List<DTO> dtos = new ArrayList<>();
            List<ClienteEntity> entities = clienteRepository.filterBy(nombre, correo, estado, pageable).getContent();
            entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Clientes filtrados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo) {
        try {
            System.out.printf("Importando clientes desde '%s'..%n", archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4DUtility.Reader.getFileCharset(archivo));
            int lTotales = (int) G4DUtility.Reader.getLineCount(archivo);
            int lProcesadas = 2;
            WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.INICIADO));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                ClienteEntity cliente = new ClienteEntity();
                cliente.setCodigo(this.generarNuevoCodigo());
                cliente.setNombre(lineaSC.next());
                cliente.setCorreo(lineaSC.next());
                cliente.setContrasenia("12345678");
                clientes.add(cliente);
                lineaSC.close();
                lProcesadas++;
                WebSocketService.enviar("/topic/loader", new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if(lProcesadas % 500 == 0 || !archivoSC.hasNextLine()) {
                    List<ClienteEntity> entities = clientes.stream().filter(entity -> !this.existsByCorreo(entity.getCorreo())).toList();
                    int eTotales = entities.size();
                    int eProcesadas = 0;
                    WebSocketService.enviar("/topic/loader", new ProgressPayload("Guardando clientes", eProcesadas, eTotales));
                    for(ClienteEntity entity : entities) {
                        this.save(entity);
                        eProcesadas++;
                        WebSocketService.enviar("/topic/loader", new ProgressPayload("Guardando clientes", eProcesadas, eTotales));
                    }
                    System.out.printf("[<] CLIENTES IMPORTADOS! ('%d')%n", clientes.size());
                    clearPools();
                }
            }
            archivoSC.close();
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            return new  GenericResponse(true, "Clientes importados correctamente!");
        } catch (NoSuchElementException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getName()));
        } catch (IOException e) {
            WebSocketService.enviar("/topic/loader-status", new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getName()));
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        clientes.clear();
    }
}
