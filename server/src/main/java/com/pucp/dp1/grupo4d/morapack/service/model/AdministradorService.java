/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.repository.AdministradorRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final UsuarioMapper usuarioMapper;
    private final HashMap<String, AdministradorEntity> administradores = new HashMap<>();

    public AdministradorService(AdministradorRepository administradorRepository, UsuarioMapper usuarioMapper) {
        this.administradorRepository = administradorRepository;
        this.usuarioMapper = usuarioMapper;
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

    public ListResponse listar(ListRequest request) {
        try {
            int page = (G4D.isAdmissible(request.getPage())) ? request.getPage() : 0;
            int size = (G4D.isAdmissible(request.getSize())) ? request.getSize() : 10;
            Pageable pageable = PageRequest.of(page, size, Sort.by("codigo").ascending());
            List<DTO> administradoresDTO = new ArrayList<>();
            List<AdministradorEntity> administradoresEntity = this.findAll(pageable);
            administradoresEntity.forEach(a -> administradoresDTO.add(usuarioMapper.toDTO(a)));
            return new ListResponse(true, "Administradores listados correctamente!", administradoresDTO);
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
            List<DTO> administradoresDTO = new ArrayList<>();
            List<AdministradorEntity> administradoresEntity = administradorRepository.filterBy(nombre, correo, estado, pageable).getContent();
            administradoresEntity.forEach(a -> administradoresDTO.add(usuarioMapper.toDTO(a)));
            return new ListResponse(true, "Administradores filtrados correctamente!", administradoresDTO);
        } catch (Exception e) {
            return new ListResponse(false, "ERROR - FILTRADO: " + e.getMessage());
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo) {
        try {
            G4D.Logger.logf("Cargando administradores desde '%s'..%n", archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4D.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                AdministradorEntity administrador = new AdministradorEntity();
                administrador.setCodigo(lineaSC.next());
                administrador.setNombre(lineaSC.next());
                administrador.setCorreo(lineaSC.next());
                administrador.setContrasenia(lineaSC.next());
                administradores.put(administrador.getCodigo(), administrador);
                lineaSC.close();
            }
            archivoSC.close();
            administradores.values().forEach(this::save);
            G4D.Logger.logf("[<] ADMINISTRADORES CARGADOS! ('%d')%n", administradores.size());
            return new GenericResponse(true, "Administradores importados correctamente!");
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
        administradores.clear();
        usuarioMapper.clearPools();
    }
}
