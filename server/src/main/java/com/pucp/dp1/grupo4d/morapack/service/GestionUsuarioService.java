/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       GestionUsuarioService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.RoleChangeRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.UserCodeRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.UserCreateRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.UserStateRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.security.SesionActual;
import com.pucp.dp1.grupo4d.morapack.security.SesionService;
import com.pucp.dp1.grupo4d.morapack.security.UsuarioPrincipal;
import com.pucp.dp1.grupo4d.morapack.service.model.PaisService;
import com.pucp.dp1.grupo4d.morapack.service.model.UsuarioService;
import com.pucp.dp1.grupo4d.morapack.util.G4DSecurity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de usuarios por el administrador (uno a la vez; ya no hay importación por archivo).
 * El administrador no puede cambiar su propio rol, deshabilitarse ni eliminarse (evita quedarse sin acceso).
 */
@Service
public class GestionUsuarioService {
    private final UsuarioService usuarioService;
    private final PaisService paisService;
    private final SesionService sesionService;
    private final UsuarioMapper usuarioMapper;

    public GestionUsuarioService(UsuarioService usuarioService, PaisService paisService, SesionService sesionService, UsuarioMapper usuarioMapper) {
        this.usuarioService = usuarioService;
        this.paisService = paisService;
        this.sesionService = sesionService;
        this.usuarioMapper = usuarioMapper;
    }

    private UsuarioEntity buscar(String codigo) {
        String c = G4DUtility.Convertor.toAdmissible(codigo);
        if (c == null) throw new G4DException("Indica el código del usuario.");
        return usuarioService.findByCodigo(c.trim()).orElseThrow(() -> new G4DException(String.format("El usuario ('%s') no existe.", codigo)));
    }

    private static void noSobreSiMismo(UsuarioEntity usuario, String accion) {
        UsuarioPrincipal yo = SesionActual.requerir();
        if (yo.id().equals(usuario.getId())) throw new G4DException(String.format("No puedes %s tu propia cuenta.", accion));
    }

    private static TipoUsuario rol(String valor) {
        TipoUsuario rol = G4DUtility.Convertor.toAdmissible(valor, TipoUsuario.class);
        if (rol == null) throw new G4DException("Selecciona un rol válido.");
        return rol;
    }

    public AuthenticationResponse crear(UserCreateRequest request) {
        String nombres = UsuarioService.validarNombre(request.getNombres(), "nombres");
        String apellidos = UsuarioService.validarNombre(request.getApellidos(), "apellidos");
        String correo = UsuarioService.validarCorreo(request.getCorreo());
        UsuarioService.validarContrasenia(request.getContrasenia(), request.getConfirmacion());
        if (G4DUtility.Convertor.toAdmissible(request.getPais()) == null) throw new G4DException("Selecciona el país.");
        UsuarioEntity usuario = usuarioService.crear(nombres, apellidos, correo, G4DSecurity.hashear(request.getContrasenia()),
                paisService.obtenerParaUsuario(request.getPais()), rol(request.getRol()));
        return new AuthenticationResponse(true, String.format("Usuario '%s' creado!", usuario.getCodigo()), usuarioMapper.toDTO(usuario));
    }

    // El código se reasigna con el siguiente libre del rol destino (misma transacción). Sus tokens se renuevan con el rol nuevo.
    public AuthenticationResponse cambiarRol(RoleChangeRequest request) {
        UsuarioEntity usuario = this.buscar(request.getCodigo());
        noSobreSiMismo(usuario, "cambiar el rol de");
        String anterior = usuario.getCodigo();
        UsuarioEntity actualizado = usuarioService.cambiarRol(usuario.getId(), rol(request.getRol()), sesionService::invalidarTokens);
        return new AuthenticationResponse(true, String.format("Rol actualizado: '%s' ahora es '%s' (%s).", anterior, actualizado.getCodigo(), actualizado.getRol()), usuarioMapper.toDTO(actualizado));
    }

    // DISABLED cierra todas sus sesiones; cualquier otro valor la habilita (OFFLINE)
    @Transactional
    public AuthenticationResponse cambiarEstado(UserStateRequest request) {
        UsuarioEntity usuario = this.buscar(request.getCodigo());
        boolean deshabilitar = EstadoUsuario.DISABLED.toString().equalsIgnoreCase(String.valueOf(request.getEstado()).trim());
        if (deshabilitar) {
            noSobreSiMismo(usuario, "deshabilitar");
            usuario.setEstado(EstadoUsuario.DISABLED);
            usuarioService.save(usuario);
            sesionService.revocarTodas(usuario, null);
            return new AuthenticationResponse(true, String.format("Usuario '%s' deshabilitado.", usuario.getCodigo()), usuarioMapper.toDTO(usuario));
        }
        if (usuario.getEstado() == EstadoUsuario.DISABLED) {
            usuario.setEstado(EstadoUsuario.OFFLINE);
            usuarioService.save(usuario);
        }
        return new AuthenticationResponse(true, String.format("Usuario '%s' habilitado.", usuario.getCodigo()), usuarioMapper.toDTO(usuario));
    }

    // Solo si no tiene pedidos (si los tiene, deshabilitarlo)
    @Transactional
    public GenericResponse eliminar(UserCodeRequest request) {
        UsuarioEntity usuario = this.buscar(request.getCodigo());
        noSobreSiMismo(usuario, "eliminar");
        if (usuarioService.contarPedidos(usuario) > 0) {
            throw new G4DException(String.format("El usuario '%s' tiene pedidos registrados: deshabilítalo en lugar de eliminarlo.", usuario.getCodigo()));
        }
        sesionService.revocarTodas(usuario, null);
        usuarioService.delete(usuario);
        return new GenericResponse(true, String.format("Usuario '%s' eliminado.", usuario.getCodigo()));
    }
}
