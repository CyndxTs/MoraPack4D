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
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.ClienteRepository;
import com.pucp.dp1.grupo4d.morapack.service.ImportService;
import com.pucp.dp1.grupo4d.morapack.service.WebSocketService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClienteService {
    private final ImportService importService;
    private final ClienteRepository clienteRepository;
    private final UsuarioMapper usuarioMapper;

    public ClienteService(ClienteRepository clienteRepository, UsuarioMapper usuarioMapper, ImportService importService) {
        this.clienteRepository = clienteRepository;
        this.usuarioMapper = usuarioMapper;
        this.importService = importService;
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

    public List<ClienteEntity> findAllInRangeByScenario(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario, List<String> codOrigenes) {
        return clienteRepository.findAllInRangeByScenario(fechaHoraInicio, fechaHoraFin, tipoEscenario, codOrigenes);
    }

    public Integer findMaxCode() {
        return G4DUtility.Convertor.toAdmissible(clienteRepository.findMaxCode(), 0);
    }

    public ListResponse listar(ListRequest request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        List<DTO> dtos = new ArrayList<>();
        List<ClienteEntity> entities = this.findAll(pageable);
        entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Clientes listados correctamente! ('%d')", dtos.size()), dtos);
    }

    public ListResponse filtrar(FilterRequest<UsuarioDTO> request) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        UsuarioDTO modelo = request.getModelo();
        String nombre = G4DUtility.Convertor.toAdmissible(modelo.getNombre());
        String correo = G4DUtility.Convertor.toAdmissible(modelo.getCorreo());
        String estado = G4DUtility.Convertor.toAdmissibleEnumString(modelo.getEstado(), EstadoUsuario.class);
        List<DTO> dtos = new ArrayList<>();
        List<ClienteEntity> entities = clienteRepository.filterBy(nombre, correo, estado, pageable).getContent();
        entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Clientes filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    public GenericResponse importar(String idTransaccion, Path archivo) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.printf(">> Importando clientes desde '%s'..%n", archivo.getFileName());
            BufferedReader br = Files.newBufferedReader(archivo, G4DUtility.Reader.getFileCharset(archivo));
            List<ClienteEntity> clientes = new ArrayList<>();
            Map<String, ClienteEntity> poolClientes = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ClienteEntity> eldest) {
                    return size() > 500;
                }
            };
            this.findAll(PageRequest.of(0, 250)).forEach(c -> poolClientes.put(c.getCorreo(), c));
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
                    if (!poolClientes.containsKey(correo) || !this.existsByCorreo(correo)) {
                        ClienteEntity cliente = new ClienteEntity();
                        cliente.setCodigo(String.format("%07d", ++maxCodigo));
                        cliente.setNombre(nombre);
                        cliente.setCorreo(correo);
                        cliente.setContrasenia("12345678");
                        clientes.add(cliente);
                        poolClientes.put(correo, cliente);
                    }
                }
                lProcesadas++;
                WebSocketService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (lProcesadas % 500 == 0 || lProcesadas == lTotales) {
                    importService.batchSave(clientes, "clientes");
                    System.out.printf("[<] CLIENTES IMPORTADOS! ('%d')%n", clientes.size());
                    clientes.clear();
                }
            }
            poolClientes.clear();
            br.close();
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.EXITOSO));
            return new GenericResponse(true, "Clientes importados correctamente!");
        } catch (ArrayIndexOutOfBoundsException e) {
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado.", archivo.getFileName()));
        } catch (IOException e) {
            WebSocketService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.DETENIDO, EstadoFinalizacion.ERRONEO));
            throw new G4DException(String.format("No se pudo cargar el archivo '%s'.", archivo.getFileName()));
        }
    }
}
