/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthenticactionService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignInRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignOutRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignUpRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.service.model.AdministradorService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.stereotype.Service;

@Service
public class AuthenticactionService {

    private final ClienteService clienteService;
    private final AdministradorService administradorService;
    private final UsuarioMapper usuarioMapper;

    public AuthenticactionService(ClienteService clienteService, AdministradorService administradorService, UsuarioMapper usuarioMapper) {
        this.clienteService = clienteService;
        this.administradorService = administradorService;
        this.usuarioMapper = usuarioMapper;
    }

    public AuthenticationResponse signIn(SignInRequest request) {
        try {
            String correo = G4DUtility.Convertor.toAdmissible(request.getCorreo(), "");
            String contrasenia = G4DUtility.Convertor.toAdmissible(request.getContrasenia(), "");
            TipoUsuario tipoUsuario = G4DUtility.Convertor.toAdmissible(request.getTipoUsuario(), TipoUsuario.class);
            switch (tipoUsuario) {
                case CLIENTE:
                    ClienteEntity cliente = clienteService.findByCorreo(correo).orElse(null);
                    if (cliente == null) {
                        throw new G4DException("Correo no registrado.");
                    }
                    if (!contrasenia.equals(cliente.getContrasenia())) {
                        throw new G4DException("Contraseña incorrecta.");
                    }
                    if (cliente.getEstado() == EstadoUsuario.DISABLED) {
                        throw new G4DException("Cuenta deshabilitada.");
                    }
                    cliente.setEstado(EstadoUsuario.ONLINE);
                    clienteService.save(cliente);
                    return new AuthenticationResponse(true, "SignIn exitoso!", usuarioMapper.toDTO(cliente));
                case ADMINISTRADOR:
                    AdministradorEntity administrador = administradorService.findByCorreo(correo).orElse(null);
                    if (administrador == null) {
                        throw new G4DException("Correo no registrado.");
                    }
                    if (!contrasenia.equals(administrador.getContrasenia())) {
                        throw new G4DException("Contraseña incorrecta.");
                    }
                    if (administrador.getEstado() == EstadoUsuario.DISABLED) {
                        throw new G4DException("Cuenta deshabilitada.");
                    }
                    administrador.setEstado(EstadoUsuario.ONLINE);
                    administradorService.save(administrador);
                    return new AuthenticationResponse(true, "SignIn exitoso!", usuarioMapper.toDTO(administrador));
                default:
                    throw new G4DException("Tipo de usuario inválido.");
            }
        } finally {
            limpiarPools();
        }
    }

    public AuthenticationResponse signUp(SignUpRequest request) {
        try {
            String nombre = G4DUtility.Convertor.toAdmissible(request.getNombre(), "");
            String correo = G4DUtility.Convertor.toAdmissible(request.getCorreo(), "");
            String contrasenia = G4DUtility.Convertor.toAdmissible(request.getContrasenia(), "");
            TipoUsuario tipoUsuario = G4DUtility.Convertor.toAdmissible(request.getTipoUsuario(), TipoUsuario.class);
            switch (tipoUsuario) {
                case CLIENTE:
                    if (clienteService.findByCorreo(correo).isPresent()) {
                        throw new G4DException("Correo en uso.");
                    }
                    ClienteEntity cliente = new ClienteEntity();
                    cliente.setCodigo(this.obtenerNuevoCodigo(tipoUsuario));
                    cliente.setNombre(nombre);
                    cliente.setCorreo(correo);
                    cliente.setContrasenia(contrasenia);
                    cliente.setEstado(EstadoUsuario.ONLINE);
                    clienteService.save(cliente);
                    return new AuthenticationResponse(true, "SignUp Exitoso!", usuarioMapper.toDTO(cliente));
                case ADMINISTRADOR:
                    if (administradorService.findByCorreo(correo).isPresent()) {
                        throw new G4DException("Correo en uso.");
                    }
                    AdministradorEntity administrador = new AdministradorEntity();
                    administrador.setCodigo(obtenerNuevoCodigo(tipoUsuario));
                    administrador.setNombre(nombre);
                    administrador.setCorreo(correo);
                    administrador.setContrasenia(contrasenia);
                    administrador.setEstado(EstadoUsuario.ONLINE);
                    administradorService.save(administrador);
                    return new AuthenticationResponse(true, "SignUp Exitoso!", usuarioMapper.toDTO(administrador));
                default:
                    throw new G4DException("Tipo de usuario inválido.");
            }
        } finally {
            limpiarPools();
        }
    }

    private String obtenerNuevoCodigo(TipoUsuario tipoUsuario) {
        return switch (tipoUsuario) {
            case CLIENTE -> clienteService.obtenerNuevoCodigo();
            case ADMINISTRADOR -> administradorService.obtenerNuevoCodigo();
        };
    }

    public AuthenticationResponse signOut(SignOutRequest request) {
        try {
            String correo = G4DUtility.Convertor.toAdmissible(request.getCorreo(), "");
            TipoUsuario tipoUsuario = G4DUtility.Convertor.toAdmissible(request.getTipoUsuario(), TipoUsuario.class);
            switch (tipoUsuario) {
                case CLIENTE:
                    ClienteEntity cliente = clienteService.findByCorreo(correo).orElse(null);
                    if (cliente == null) {
                        throw new G4DException("Correo no registrado.");
                    }
                    if (cliente.getEstado() != EstadoUsuario.ONLINE) {
                        throw new G4DException("El usuario no está en línea.");
                    }
                    cliente.setEstado(EstadoUsuario.OFFLINE);
                    clienteService.save(cliente);
                    return new AuthenticationResponse(true, "SignOut exitoso!");
                case ADMINISTRADOR:
                    AdministradorEntity administrador = administradorService.findByCorreo(correo).orElse(null);
                    if (administrador == null) {
                        throw new G4DException("Correo no registrado.");
                    }
                    if (administrador.getEstado() != EstadoUsuario.ONLINE) {
                        throw new G4DException("El usuario no está en línea.");
                    }
                    administrador.setEstado(EstadoUsuario.OFFLINE);
                    administradorService.save(administrador);
                    return new AuthenticationResponse(true, "SignOut exitoso!");
                default:
                    throw new G4DException("Tipo de usuario inválido.");
            }
        } finally {
            limpiarPools();
        }
    }

    private void limpiarPools() {
        clienteService.clearPools();
        administradorService.clearPools();
        usuarioMapper.clearPools();
    }
}
