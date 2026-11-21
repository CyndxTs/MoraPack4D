/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignInRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignOutRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.GenericResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.TrabajadorEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.model.AdministradorService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import com.pucp.dp1.grupo4d.morapack.service.model.TrabajadorService;
import com.pucp.dp1.grupo4d.morapack.util.G4DSecurity;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Inicio y cierre de sesión (sin tokens: proyecto académico). La contraseña se compara contra el hash BCrypt.
 * Si no se indica el tipo de usuario se busca el correo en TRABAJADOR, ADMINISTRADOR y CLIENTE (en ese orden).
 */
@Service
public class AuthService {
    private final ClienteService clienteService;
    private final TrabajadorService trabajadorService;
    private final AdministradorService administradorService;
    private final UsuarioMapper usuarioMapper;

    public AuthService(ClienteService clienteService, TrabajadorService trabajadorService, AdministradorService administradorService, UsuarioMapper usuarioMapper) {
        this.clienteService = clienteService;
        this.trabajadorService = trabajadorService;
        this.administradorService = administradorService;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public AuthenticationResponse iniciarSesion(SignInRequest request) {
        String correo = G4DUtility.Convertor.toAdmissible(request.getCorreo());
        String contrasenia = request.getContrasenia();
        if (correo == null || contrasenia == null || contrasenia.isBlank()) {
            throw new G4DException("Ingrese su correo y su contraseña.");
        }
        UsuarioEntity usuario = this.buscar(correo.trim(), this.tiposDeBusqueda(request.getTipoUsuario()));
        if (usuario == null || !G4DSecurity.verificar(contrasenia, usuario.getContrasenia())) {
            throw new G4DException("Correo o contraseña incorrectos.");
        }
        if (usuario.getEstado() == EstadoUsuario.DISABLED) {
            throw new G4DException("La cuenta está deshabilitada.");
        }
        usuario.setEstado(EstadoUsuario.ONLINE);
        this.guardar(usuario);
        return new AuthenticationResponse(true, "Sesión iniciada!", usuarioMapper.toDTO(usuario));
    }

    @Transactional
    public GenericResponse cerrarSesion(SignOutRequest request) {
        String correo = G4DUtility.Convertor.toAdmissible(request.getCorreo());
        if (correo != null) {
            UsuarioEntity usuario = this.buscar(correo.trim(), this.tiposDeBusqueda(request.getTipoUsuario()));
            if (usuario != null && usuario.getEstado() == EstadoUsuario.ONLINE) {
                usuario.setEstado(EstadoUsuario.OFFLINE);
                this.guardar(usuario);
            }
        }
        return new GenericResponse(true, "Sesión cerrada!");
    }

    private List<TipoUsuario> tiposDeBusqueda(String tipoUsuario) {
        TipoUsuario tipo = G4DUtility.Convertor.toAdmissible(tipoUsuario, TipoUsuario.class);
        return (tipo != null) ? List.of(tipo) : List.of(TipoUsuario.TRABAJADOR, TipoUsuario.ADMINISTRADOR, TipoUsuario.CLIENTE);
    }

    private UsuarioEntity buscar(String correo, List<TipoUsuario> tipos) {
        for (TipoUsuario tipo : tipos) {
            UsuarioEntity usuario = switch (tipo) {
                case CLIENTE -> clienteService.findByCorreo(correo).orElse(null);
                case TRABAJADOR -> trabajadorService.findByCorreo(correo).orElse(null);
                case ADMINISTRADOR -> administradorService.findByCorreo(correo).orElse(null);
            };
            if (usuario != null) return usuario;
        }
        return null;
    }

    private void guardar(UsuarioEntity usuario) {
        if (usuario instanceof ClienteEntity cliente) clienteService.save(cliente);
        else if (usuario instanceof TrabajadorEntity trabajador) trabajadorService.save(trabajador);
        else if (usuario instanceof AdministradorEntity administrador) administradorService.save(administrador);
    }
}
