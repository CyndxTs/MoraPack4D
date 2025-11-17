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
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enums.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.service.model.AdministradorService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import com.pucp.dp1.grupo4d.morapack.util.G4D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.OptionalInt;

@Service
public class AuthenticactionService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private UsuarioMapper usuarioMapper;

    public AuthenticationResponse signIn(SignInRequest request) {
        try {
            String correo = G4D.toAdmissibleValue(request.getCorreo());
            String contrasenia = G4D.toAdmissibleValue(request.getContrasenia());
            TipoUsuario tipoUsuario = G4D.toAdmissibleValue(request.getTipoUsuario(), TipoUsuario.class);
            switch (tipoUsuario) {
                case CLIENTE:
                    ClienteEntity cliente = clienteService.findByCorreo(correo).orElse(null);
                    if (cliente == null) {
                        return new AuthenticationResponse(false, "Correo no registrado.");
                    }
                    if (cliente.getEstado() == EstadoUsuario.DISABLED) {
                        return new AuthenticationResponse(false, "Cuenta deshabilitada.");
                    }
                    if (!contrasenia.equals(cliente.getContrasenia())) {
                        return new AuthenticationResponse(false, "Contraseña incorrecta.");
                    }
                    cliente.setEstado(EstadoUsuario.ONLINE);
                    clienteService.save(cliente);
                    return new AuthenticationResponse(false, "SignIn exitoso!", usuarioMapper.toDTO(cliente));
                case ADMINISTRADOR:
                    AdministradorEntity administrador = administradorService.findByCorreo(correo).orElse(null);
                    if (administrador == null) {
                        return new AuthenticationResponse(false, "Correo no registrado.");
                    }
                    if (administrador.getEstado() == EstadoUsuario.DISABLED) {
                        return new AuthenticationResponse(false, "Cuenta deshabilitada.");
                    }
                    if (!contrasenia.equals(administrador.getContrasenia())) {
                        return new AuthenticationResponse(false, "Contraseña incorrecta.");
                    }
                    administrador.setEstado(EstadoUsuario.ONLINE);
                    administradorService.save(administrador);
                    return new AuthenticationResponse(true, "SignIn exitoso!", usuarioMapper.toDTO(administrador));
                default:
                    return new AuthenticationResponse(false, "Tipo de usuario inválido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse(false, "ERROR - SIGNIN: " + e.getMessage());
        } finally {
            limpiarPools();
        }
    }

    public AuthenticationResponse signUp(SignUpRequest request) {
        try {
            String nombre = G4D.toAdmissibleValue(request.getNombre());
            String correo = G4D.toAdmissibleValue(request.getCorreo());
            String contrasenia = G4D.toAdmissibleValue(request.getContrasenia());
            TipoUsuario tipoUsuario = G4D.toAdmissibleValue(request.getTipoUsuario(), TipoUsuario.class);
            switch (tipoUsuario) {
                case CLIENTE:
                    if (clienteService.findByCorreo(correo).isPresent()) {
                        return new AuthenticationResponse(false, "Correo en uso.");
                    }
                    ClienteEntity cliente = new ClienteEntity();
                    cliente.setCodigo(obtenerNuevoCodigo(tipoUsuario));
                    cliente.setNombre(nombre);
                    cliente.setCorreo(correo);
                    cliente.setContrasenia(contrasenia);
                    cliente.setEstado(EstadoUsuario.ONLINE);
                    clienteService.save(cliente);
                    return new AuthenticationResponse(false, "SignUp Exitoso!", usuarioMapper.toDTO(cliente));
                case ADMINISTRADOR:
                    if (administradorService.findByCorreo(correo).isPresent()) {
                        return new AuthenticationResponse(false, "Correo en uso.");
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
                    return new AuthenticationResponse(false, "Tipo de usuario inválido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse(false, "ERROR - SIGNUP: " + e.getMessage());
        } finally {
            limpiarPools();
        }
    }

    private String obtenerNuevoCodigo(TipoUsuario tipoUsuario) {
        OptionalInt maxCodigo = switch (tipoUsuario) {
            case CLIENTE -> clienteService.findAll().stream().mapToInt(c -> Integer.parseInt(c.getCodigo())).max();
            case ADMINISTRADOR -> administradorService.findAll().stream().mapToInt(a -> Integer.parseInt(a.getCodigo().substring(5))).max();
        };
        return String.format("%07d", maxCodigo.orElse(0) + 1);
    }

    public AuthenticationResponse signOut(SignOutRequest request) {
        try {
            String correo = G4D.toAdmissibleValue(request.getCorreo());
            TipoUsuario tipoUsuario = G4D.toAdmissibleValue(request.getTipoUsuario(), TipoUsuario.class);
            switch (tipoUsuario) {
                case CLIENTE:
                    ClienteEntity cliente = clienteService.findByCorreo(correo).orElse(null);
                    if (cliente == null) {
                        return new AuthenticationResponse(false, "Correo no registrado.");
                    }
                    if (cliente.getEstado() != EstadoUsuario.ONLINE) {
                        return new AuthenticationResponse(false, "El usuario no está en línea.");
                    }
                    cliente.setEstado(EstadoUsuario.OFFLINE);
                    clienteService.save(cliente);
                    return new AuthenticationResponse(true, "SignOut exitoso!");
                case ADMINISTRADOR:
                    AdministradorEntity administrador = administradorService.findByCorreo(correo).orElse(null);
                    if (administrador == null) {
                        return new AuthenticationResponse(false, "Correo no registrado.");
                    }
                    if (administrador.getEstado() != EstadoUsuario.ONLINE) {
                        return new AuthenticationResponse(false, "El usuario no está en línea.");
                    }
                    administrador.setEstado(EstadoUsuario.OFFLINE);
                    administradorService.save(administrador);
                    return new AuthenticationResponse(true, "SignOut exitoso!");
                default:
                    return new AuthenticationResponse(false, "Tipo de usuario inválido.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse(false, "ERROR - SIGNOUT: " + e.getMessage());
        } finally {
            limpiarPools();
        }
    }

    private void limpiarPools() {
        usuarioMapper.clearPools();
    }
}
