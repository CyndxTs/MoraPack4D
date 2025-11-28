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
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.AdministradorRepository;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileNotFoundException;
import java.util.*;

@Service
public class AdministradorService {

    private final AdministradorRepository administradorRepository;
    private final UsuarioMapper usuarioMapper;
    private final List<AdministradorEntity> administradores = new ArrayList<>();

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

    public String obtenerNuevoCodigo() {
        OptionalInt maxCodigo;
        if(!administradores.isEmpty()) {
            maxCodigo = administradores.stream().mapToInt(entity -> Integer.parseInt(entity.getCodigo().substring(5))).max();
        } else maxCodigo = this.findAll().stream().mapToInt(entity -> Integer.parseInt(entity.getCodigo().substring(5))).max();
        return String.format("ADMIN%02d", maxCodigo.orElse(0) + 1);
    }

    public ListResponse listar(ListRequest request) throws Exception {
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
            List<DTO> dtos = new ArrayList<>();
            List<AdministradorEntity> entities = this.findAll(pageable);
            entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Administradores listados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public ListResponse filtrar(FilterRequest<UsuarioDTO> request) throws Exception {
        try {
            Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
            UsuarioDTO modelo = request.getModelo();
            String nombre = G4DUtility.Convertor.toAdmissible(modelo.getNombre());
            String correo = G4DUtility.Convertor.toAdmissible(modelo.getCorreo());
            EstadoUsuario estado = G4DUtility.Convertor.toAdmissible(modelo.getEstado(), EstadoUsuario.class);
            List<DTO> dtos = new ArrayList<>();
            List<AdministradorEntity> entities = administradorRepository.filterBy(nombre, correo, estado, pageable).getContent();
            entities.forEach(entity -> dtos.add(usuarioMapper.toDTO(entity)));
            return new ListResponse(true, String.format("Administradores filtrados correctamente! ('%d')", dtos.size()), dtos);
        } finally {
            clearPools();
        }
    }

    public GenericResponse importar(MultipartFile archivo) throws Exception {
        try {
            System.out.printf("Importando administradores desde '%s'..%n", archivo.getName());
            Scanner archivoSC = new Scanner(archivo.getInputStream(), G4DUtility.Reader.getFileCharset(archivo));
            while (archivoSC.hasNextLine()) {
                String linea = archivoSC.nextLine().trim();
                Scanner lineaSC = new Scanner(linea);
                lineaSC.useDelimiter("\\s{2,}");
                AdministradorEntity administrador = new AdministradorEntity();
                administrador.setCodigo(this.obtenerNuevoCodigo());
                administrador.setNombre(lineaSC.next());
                administrador.setCorreo(lineaSC.next());
                administrador.setContrasenia("12345678");
                administradores.add(administrador);
                lineaSC.close();
            }
            archivoSC.close();
            administradores.forEach(this::save);
            System.out.printf("[<] ADMINISTRADORES IMPORTADOS! ('%d')%n", administradores.size());
            return new GenericResponse(true, String.format("Administradores importados correctamente! ('%d')", administradores.size()));
        } catch (NoSuchElementException | FileNotFoundException e) {
            throw new G4DException(String.format("El archivo '%s' no sigue el formato esperado o está vacío.", archivo.getName()));
        } finally {
            clearPools();
        }
    }

    public void clearPools() {
        administradores.clear();
        usuarioMapper.clearPools();
    }
}
