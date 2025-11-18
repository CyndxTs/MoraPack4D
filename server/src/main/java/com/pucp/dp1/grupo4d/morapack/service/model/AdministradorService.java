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
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.repository.AdministradorRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class AdministradorService {

    @Autowired
    private UsuarioMapper usuarioMapper;

    private final AdministradorRepository administradorRepository;

    public AdministradorService(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }

    public List<AdministradorEntity> findAll() {
        return administradorRepository.findAll();
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

    public ListResponse listar() {
        try {
            List<DTO> administradoresDTO = new ArrayList<>();
            List<AdministradorEntity> administradoresEntity = this.findAll();
            administradoresEntity.forEach(a -> administradoresDTO.add(usuarioMapper.toDTO(a)));
            return new ListResponse(true, "Administradores listados correctamente!", administradoresDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - LISTADO: " + e.getMessage());
        }
    }

    public ListResponse filtrar(FilterRequest request) {
        try {
            UsuarioDTO dto = (UsuarioDTO) request.getDto();
            String nombre = G4D.toAdmissibleValue(dto.getNombre());
            String correo = G4D.toAdmissibleValue(dto.getCorreo());
            EstadoUsuario estado = G4D.toAdmissibleValue(dto.getEstado(), EstadoUsuario.class);
            List<DTO> administradoresDTO = new ArrayList<>();
            List<AdministradorEntity> administradoresEntity = administradorRepository.filterBy(nombre, correo, estado);
            administradoresEntity.forEach(a -> administradoresDTO.add(usuarioMapper.toDTO(a)));
            return new ListResponse(true, "Administradores filtrados correctamente!", administradoresDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new ListResponse(false, "ERROR - FILTRADO: " + e.getMessage());
        }
    }

    public void importar(MultipartFile archivo) {
        int posCarga = 0;
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
                this.save(administrador);
                posCarga++;
                lineaSC.close();
            }
            archivoSC.close();
            G4D.Logger.logf("[<] ADMINISTRADORES CARGADOS! ('%d')%n", posCarga);
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! ('%s')%n", archivo.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
