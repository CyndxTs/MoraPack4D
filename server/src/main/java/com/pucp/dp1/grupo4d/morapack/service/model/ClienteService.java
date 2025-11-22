/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       ClienteService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.repository.ClienteRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioMapper usuarioMapper;
    private final HashMap<String, ClienteEntity> clientes = new HashMap<>();

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
        return  clienteRepository.findAllByDateTimeRange(fechaHoraInicio, fechaHoraFin, tipoEscenario);
    }

    public ClienteEntity obtenerPorCodigo(String codigo) {
        ClienteEntity cliente = clientes.get(codigo);
        if (cliente == null) {
            cliente = this.findByCodigo(codigo).orElse(null);
            if (cliente == null) {
                cliente = new ClienteEntity();
                cliente.setCodigo(codigo);
                cliente.setNombre(G4D.Generator.getUniqueName());
                String correo = G4D.Generator.getUniqueEmail();
                boolean existeCorreo = this.existsByCorreo(correo);
                if(existeCorreo) {
                    String newCorreo = "";
                    while (existeCorreo) {
                        newCorreo = G4D.Generator.addRandomInteger(correo, correo.indexOf('@'));
                        existeCorreo = this.existsByCorreo(newCorreo);
                    }
                    cliente.setCorreo(newCorreo);
                } else cliente.setCorreo(correo);
                cliente.setContrasenia("12345678");
                this.save(cliente);
            }
            clientes.put(codigo, cliente);
        }
        return cliente;
    }

    public ListResponse listar(ListRequest request) {
        try {
            int page = (G4D.isAdmissible(request.getPage())) ? request.getPage() : 0;
            int size = (G4D.isAdmissible(request.getSize())) ? request.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            List<DTO> clientesDTO = new ArrayList<>();
            List<ClienteEntity> clientesEntity = this.findAll(pageable);
            clientesEntity.forEach(c -> clientesDTO.add(usuarioMapper.toDTO(c)));
            return new ListResponse(true, "Clientes listados correctamente!", clientesDTO);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public ListResponse filtrar(FilterRequest<UsuarioDTO> request) {
        try {
            int page = (G4D.isAdmissible(request.getPage())) ? request.getPage() : 0;
            int size = (G4D.isAdmissible(request.getSize())) ? request.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            UsuarioDTO dto = request.getDto();
            String nombre = G4D.toAdmissibleValue(dto.getNombre());
            String correo = G4D.toAdmissibleValue(dto.getCorreo());
            EstadoUsuario estado = G4D.toAdmissibleValue(dto.getEstado(), EstadoUsuario.class);
            List<DTO> clientesDTO = new ArrayList<>();
            List<ClienteEntity> clientesEntity = clienteRepository.filterBy(nombre, correo, estado, pageable).getContent();
            clientesEntity.forEach(c -> clientesDTO.add(usuarioMapper.toDTO(c)));
            return new ListResponse(true, "Clientes filtrados correctamente!", clientesDTO);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - FILTRADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo) {
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
                clientes.put(cliente.getCodigo(), cliente);
                lineaSC.close();
            }
            archivoSC.close();
            clientes.values().forEach(this::save);
            G4D.Logger.logf("[<] CLIENTES CARGADOS! ('%d')%n", clientes.size());
            return new  GenericResponse(true, "Clientes importados correctamente!");
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! ('%s')%n", archivo.getName());
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } catch (Exception e) {
            return new  GenericResponse(false, "ERROR - IMPORTACIÓN: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        clientes.clear();
        usuarioMapper.clearPools();
    }
}
