/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AdministradorService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.adapter.UsuarioAdapter;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.FilterResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.repository.AdministradorRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final UsuarioAdapter usuarioAdapter;

    public AdministradorService(AdministradorRepository administradorRepository, UsuarioAdapter usuarioAdapter) {
        this.administradorRepository = administradorRepository;
        this.usuarioAdapter = usuarioAdapter;
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
            List<DTO> administradoresDTO = new ArrayList<>();
            List<AdministradorEntity> administradoresEntity = administradorRepository.filterBy(nombreFiltro, correoFiltro, estadoFiltro);
            administradoresEntity.forEach(a -> administradoresDTO.add(usuarioAdapter.toDTO(a)));
            return new FilterResponse(true, "Filtro aplicado correctamente!", administradoresDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return new FilterResponse(false, "ERROR - FILTRADO: " + e.getMessage());
        }
    }

    public void importar(MultipartFile archivo) {
        List<AdministradorEntity> administradores = new ArrayList<>();
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
                administradores.add(administrador);
                lineaSC.close();
            }
            archivoSC.close();
            administradores.forEach(this::save);
            G4D.Logger.logf("[<] ADMINISTRADORES CARGADOS! ('%d')%n", administradores.size());
        } catch (NoSuchElementException e) {
            G4D.Logger.logf_err("[X] FORMATO DE ARCHIVO INVALIDO! ('%s')%n", archivo.getName());
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
