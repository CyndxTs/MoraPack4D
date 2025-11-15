/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthenticactionService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.adapter.UsuarioAdapter;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignInRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignOutRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignUpRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.service.model.AdministradorService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.OptionalInt;

@Service
public class AuthenticactionService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AdministradorService administradorService;
    @Autowired
    private UsuarioAdapter usuarioAdapter;

    public AuthenticationResponse signIn(SignInRequest request) {
        try {
            String correo = request.getCorreo();
            String contrasenia = request.getContrasenia();
            String tipoUsuario = request.getTipoUsuario();

            if (tipoUsuario == null || (!tipoUsuario.equals("CLIENTE") && !tipoUsuario.equals("ADMINISTRADOR"))) {
                return new AuthenticationResponse(false, "Tipo de usuario inválido.");
            }

            if (tipoUsuario.equals("CLIENTE")) {
                Optional<ClienteEntity> clienteOpt = clienteService.findByCorreo(correo);
                if (clienteOpt.isEmpty()) {
                    return new AuthenticationResponse(false, "Correo no registrado.");
                }
                ClienteEntity cliente = clienteOpt.get();
                if (contrasenia.equals(cliente.getContrasenia())) {
                    return new AuthenticationResponse(false, "Contraseña incorrecta.");
                }
                if (cliente.getEstado() == EstadoUsuario.DISABLED) {
                    return new AuthenticationResponse(false, "Cuenta deshabilitada.");
                }
                cliente.setEstado(EstadoUsuario.ONLINE);
                clienteService.save(cliente);
                UsuarioDTO usuarioDTO = usuarioAdapter.toDTO(cliente);
                return new AuthenticationResponse(false, "SignIn exitoso!", usuarioDTO);
            } else {
                Optional<AdministradorEntity> adminOpt = administradorService.findByCorreo(correo);
                if (adminOpt.isEmpty()) {
                    return new AuthenticationResponse(false, "Correo no registrado.");
                }
                AdministradorEntity administrador = adminOpt.get();
                if (contrasenia.compareTo(administrador.getContrasenia()) != 0) {
                    return new AuthenticationResponse(false, "Contraseña incorrecta.");
                }
                if (administrador.getEstado() == EstadoUsuario.DISABLED) {
                    return new AuthenticationResponse(false, "Cuenta deshabilitada.");
                }
                administrador.setEstado(EstadoUsuario.ONLINE);
                administradorService.save(administrador);
                UsuarioDTO usuarioDTO = usuarioAdapter.toDTO(administrador);
                return new AuthenticationResponse(true, "SignIn exitoso!", usuarioDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse(false, "ERROR - SIGNIN: " + e.getMessage());
        }
    }

    public AuthenticationResponse signUp(SignUpRequest request) {
        try {
            String nombre = request.getNombre();
            String correo = request.getCorreo();
            String contrasenia = request.getContrasenia();
            String tipoUsuario = request.getTipoUsuario();
            if (tipoUsuario == null || (!tipoUsuario.equals("CLIENTE") && !tipoUsuario.equals("ADMINISTRADOR"))) {
                return new AuthenticationResponse(false, "Tipo de usuario inválido.");
            }
            if (tipoUsuario.equals("CLIENTE")) {
                if (clienteService.findByCorreo(correo).isPresent()) {
                    return new AuthenticationResponse(false, "Correo en uso.");
                }
                ClienteEntity cliente = new ClienteEntity();
                cliente.setCodigo(obtenerNuevoCodigo(tipoUsuario));
                cliente.setNombre(nombre);
                cliente.setCorreo(correo);
                cliente.setContrasenia(contrasenia);
                cliente.setEstado(EstadoUsuario.ONLINE);
                ClienteEntity clienteGuardado = clienteService.save(cliente);
                UsuarioDTO usuarioDTO = usuarioAdapter.toDTO(clienteGuardado);
                return new AuthenticationResponse(false, "SignUp Exitoso!", usuarioDTO);
            } else {
                if (administradorService.findByCorreo(correo).isPresent()) {
                    return new AuthenticationResponse(false, "Correo en uso.");
                }
                AdministradorEntity administrador = new AdministradorEntity();
                administrador.setCodigo(obtenerNuevoCodigo(tipoUsuario));
                administrador.setNombre(nombre);
                administrador.setCorreo(correo);
                administrador.setContrasenia(contrasenia);
                administrador.setEstado(EstadoUsuario.ONLINE);
                AdministradorEntity adminGuardado = administradorService.save(administrador);
                UsuarioDTO usuarioDTO = usuarioAdapter.toDTO(adminGuardado);
                return new AuthenticationResponse(true, "SignUp Exitoso!", usuarioDTO);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse(false, "ERROR - SIGNUP: " + e.getMessage());
        }
    }

    private String obtenerNuevoCodigo(String tipoUsuario) {
        OptionalInt maxCodigo;
        if (tipoUsuario.equals("ADMINISTRADOR")) {
            maxCodigo = administradorService.findAll().stream().mapToInt(a -> Integer.parseInt(a.getCodigo().substring(5))).max();
        } else {
            maxCodigo = clienteService.findAll().stream().mapToInt(c -> Integer.parseInt(c.getCodigo())).max();
        }
        return String.format("%07d", maxCodigo.orElse(0) + 1);
    }

    public AuthenticationResponse signOut(SignOutRequest request) {
        try {
            String correo = request.getCorreo();
            String tipoUsuario = request.getTipoUsuario();
            if (tipoUsuario == null || (!tipoUsuario.equals("CLIENTE") && !tipoUsuario.equals("ADMINISTRADOR"))) {
                return new AuthenticationResponse(false, "Tipo de usuario inválido.");
            }
            if (tipoUsuario.equals("CLIENTE")) {
                Optional<ClienteEntity> clienteOpt = clienteService.findByCorreo(correo);
                if (clienteOpt.isEmpty()) {
                    return new AuthenticationResponse(false, "Correo no registrado.");
                }
                ClienteEntity cliente = clienteOpt.get();
                if (cliente.getEstado() != EstadoUsuario.ONLINE) {
                    return new AuthenticationResponse(false, "El usuario no está en línea.");
                }
                cliente.setEstado(EstadoUsuario.OFFLINE);
                clienteService.save(cliente);
                return new AuthenticationResponse(false, "SignOut exitoso!");
            } else {
                Optional<AdministradorEntity> adminOpt = administradorService.findByCorreo(correo);
                if (adminOpt.isEmpty()) {
                    return new AuthenticationResponse(false, "Correo no registrado.");
                }
                AdministradorEntity administrador = adminOpt.get();
                if (administrador.getEstado() != EstadoUsuario.ONLINE) {
                    return new AuthenticationResponse(false, "El usuario no está en línea.");
                }
                administrador.setEstado(EstadoUsuario.OFFLINE);
                administradorService.save(administrador);
                return new AuthenticationResponse(true, "SignOut exitoso!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse(false, "ERROR - SIGNOUT: " + e.getMessage());
        }
    }
}