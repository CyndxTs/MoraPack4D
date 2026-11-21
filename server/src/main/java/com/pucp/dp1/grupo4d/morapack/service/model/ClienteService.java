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
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.PaisEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoEjecucion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoFinalizacion;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.ClienteRepository;
import com.pucp.dp1.grupo4d.morapack.service.CommunicationService;
import com.pucp.dp1.grupo4d.morapack.service.ImportationService;
import com.pucp.dp1.grupo4d.morapack.util.G4DSecurity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClienteService {
    // Contraseña inicial para usuarios cargados por el administrador (se cambiará con el flujo de autenticación)
    private static final String CONTRASENIA_INICIAL = "12345678";

    private final ClienteRepository clienteRepository;
    private final UsuarioMapper usuarioMapper;
    private final PaisService paisService;
    private final CommunicationService communicationService;
    private final ImportationService importationService;

    public ClienteService(ClienteRepository clienteRepository, UsuarioMapper usuarioMapper, PaisService paisService, CommunicationService communicationService, ImportationService importationService) {
        this.clienteRepository = clienteRepository;
        this.usuarioMapper = usuarioMapper;
        this.paisService = paisService;
        this.communicationService = communicationService;
        this.importationService = importationService;
    }

    public ClienteEntity save(ClienteEntity cliente) {
        return clienteRepository.save(cliente);
    }

    public List<ClienteEntity> findAll() {
        return clienteRepository.findAll();
    }

    public List<ClienteEntity> findAll(Pageable pageable) {
        return clienteRepository.findAll(pageable).getContent();
    }

    public List<ClienteEntity> findAllByAttributes(String codigo, String nombres, String apellidos, String correo, String estado, String pais, Pageable pageable) {
        return clienteRepository.findAllByAttributes(codigo, nombres, apellidos, correo, estado, pais, pageable).getContent();
    }

    public List<ClienteEntity> findAllInRangeByScenario(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario, List<String> codOrigenes) {
        return clienteRepository.findAllInRangeByScenario(fechaHoraInicio, fechaHoraFin, tipoEscenario, codOrigenes);
    }

    public Optional<ClienteEntity> findById(Integer id) {
        return clienteRepository.findById(id);
    }

    public boolean existsById(Integer id) {
        return clienteRepository.existsById(id);
    }

    public void deleteById(Integer id) {
        clienteRepository.deleteById(id);
    }

    public Optional<ClienteEntity> findByCodigo(String codigo) {
        return clienteRepository.findByCodigo(codigo);
    }

    public boolean existsByCodigo(String codigo) {
        return clienteRepository.existsByCodigo(codigo);
    }

    public Optional<ClienteEntity> findByCorreo(String correo) {
        return clienteRepository.findByCorreo(correo);
    }

    public boolean existsByCorreo(String correo) {
        return clienteRepository.existsByCorreo(correo);
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
        String codigo = G4DUtility.Convertor.toAdmissible(modelo.getCodigo());
        String nombres = G4DUtility.Convertor.toAdmissible(modelo.getNombres());
        String apellidos = G4DUtility.Convertor.toAdmissible(modelo.getApellidos());
        String correo = G4DUtility.Convertor.toAdmissible(modelo.getCorreo());
        String estado = G4DUtility.Convertor.toAdmissibleEnumString(modelo.getEstado(), EstadoUsuario.class);
        String pais = G4DUtility.Convertor.toAdmissible(modelo.getPais());
        List<DTO> dtos = new ArrayList<>();
        List<ClienteEntity> entities = this.findAllByAttributes(codigo, nombres, apellidos, correo, estado, pais, pageable);
        entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
        return new ListResponse(true, String.format("Clientes filtrados correctamente! ('%d')", dtos.size()), dtos);
    }

    // Carga unitaria
    public void importar(String idTransaccion, UsuarioDTO dto) {
        String progressDestination = String.format("/topic/importation-%s", idTransaccion), statusDestination = String.format("/topic/importation-status-%s", idTransaccion);
        try {
            System.out.println("Importando cliente..");
            communicationService.enviar(statusDestination, new StatusPayload(EstadoEjecucion.INICIADO));
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando cliente", 0, 1));
            String correo = G4DUtility.Convertor.toAdmissible(dto.getCorreo());
            if (correo == null) {
                throw new G4DException("El correo del cliente es obligatorio.");
            }
            if (this.existsByCorreo(correo)) {
                throw new G4DException(String.format("El correo ('%s') ya está registrado.", correo));
            }
            ClienteEntity cliente = new ClienteEntity();
            cliente.setCodigo(String.format("%07d", this.findMaxCode() + 1));
            cliente.setNombres(G4DUtility.Convertor.toAdmissible(dto.getNombres(), ""));
            cliente.setApellidos(G4DUtility.Convertor.toAdmissible(dto.getApellidos(), ""));
            cliente.setCorreo(correo);
            cliente.setContrasenia(G4DSecurity.hashear(CONTRASENIA_INICIAL));
            cliente.setPais(paisService.obtenerParaUsuario(dto.getPais()));
            this.save(cliente);
            System.out.println("[<] CLIENTE IMPORTADO!");
            communicationService.enviar(progressDestination, new ProgressPayload("Cargando cliente", 1, 1));
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
            System.out.printf(">> Importando clientes desde '%s'..%n", archivo.getFileName());
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
            List<ClienteEntity> clientes = new ArrayList<>();
            String contraseniaInicial = G4DSecurity.hashear(CONTRASENIA_INICIAL); // un solo hash por importación (BCrypt es lento a propósito)
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
                        ClienteEntity cliente = new ClienteEntity();
                        cliente.setCodigo(String.format("%07d", ++maxCodigo));
                        cliente.setNombres(nombreCompleto[0]);
                        cliente.setApellidos(nombreCompleto[1]);
                        cliente.setCorreo(correo);
                        cliente.setContrasenia(contraseniaInicial);
                        cliente.setPais(pais);
                        clientes.add(cliente);
                        correosRegistrados.add(correo);
                    } else omitidos++;
                }
                lProcesadas++;
                communicationService.enviar(progressDestination, new ProgressPayload("Leyendo archivo", lProcesadas, lTotales));
                if (clientes.size() >= 500 || (lProcesadas == lTotales && !clientes.isEmpty())) {
                    importationService.batchSave(clientes, progressDestination, "clientes");
                    System.out.printf("[<] CLIENTES IMPORTADOS! ('%d')%n", clientes.size());
                    clientes.clear();
                }
            }
            if (omitidos > 0) System.out.printf("[*] CLIENTES OMITIDOS POR CORREO REPETIDO: '%d'%n", omitidos);
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
