/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.adapter.UsuarioAdapter;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.FilterResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.repository.ClienteRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioAdapter usuarioAdapter;

    public ClienteService(ClienteRepository clienteRepository, UsuarioAdapter usuarioAdapter) {
        this.clienteRepository = clienteRepository;
        this.usuarioAdapter = usuarioAdapter;
    }

    public List<ClienteEntity> findAll() {
        return clienteRepository.findAll();
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

    public List<ClienteEntity> findByDateTimeRange(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, Integer desfaseDeDias) {
        return  clienteRepository.findByDateTimeRange(fechaHoraInicio, fechaHoraFin, desfaseDeDias);
    }

    public FilterResponse filtrar(FilterRequest request) {
        try {
            UsuarioDTO dto = (UsuarioDTO) request.getDto();
            String nombre = dto.getNombre();
            String correo = dto.getCorreo();
            String estado = dto.getEstado();
            EstadoUsuario estadoFiltro = null;
            if (estado != null && !estado.isBlank()) {
                try {
                    estadoFiltro = EstadoUsuario.valueOf(estado.toUpperCase());
                } catch (IllegalArgumentException e) {
                    estadoFiltro = null;
                }
            }
            String nombreFiltro = (nombre == null || nombre.isBlank()) ? null : nombre;
            String correoFiltro = (correo == null || correo.isBlank()) ? null : correo;
            List<DTO> clientesDTO = new ArrayList<>();
            List<ClienteEntity> clientesEntity = clienteRepository.filterBy(nombreFiltro, correoFiltro, estadoFiltro);
            clientesEntity.forEach(c -> clientesDTO.add(usuarioAdapter.toDTO(c)));
            return new FilterResponse(true, "Filtro aplicado correctamente!", clientesDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new FilterResponse(false, "ERROR - FILTRADO: " + e.getMessage());
        }
    }

    public void importar(MultipartFile archivo) {
        List<ClienteEntity> clientes = new ArrayList<>();
        try {
            G4D.Logger.logf("Cargando clientes desde '%s'..%n", archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                ClienteEntity cliente = new ClienteEntity();
                cliente.setCodigo(lineaSC.next());
                cliente.setNombre(lineaSC.next());
                cliente.setCorreo(lineaSC.next());
                cliente.setContrasenia(lineaSC.next());
                clientes.add(cliente);
                lineaSC.close();
            }
            archivoSC.close();
            clientes.forEach(this::save);
            G4D.Logger.logf("[<] CLIENTES CARGADOS! ('%d')%n", clientes.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! ('%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
