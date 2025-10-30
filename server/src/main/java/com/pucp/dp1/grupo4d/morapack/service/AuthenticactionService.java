/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthenticactionService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignInRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignOutRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignUpRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthenticationResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.UserResponse;
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

    public AuthenticationResponse signIn(SignInRequest request) {
        try {
            String correo = request.getCorreo();
            String contrasenia = request.getContrasenia();
            String tipoUsuario = request.getTipoUsuario();

            if (tipoUsuario == null || (!tipoUsuario.equals("CLIENTE") && !tipoUsuario.equals("ADMINISTRADOR"))) {
                return new AuthenticationResponse("Tipo de usuario inválido.", null);
            }

            if (tipoUsuario.equals("CLIENTE")) {
                Optional<ClienteEntity> clienteOpt = clienteService.findByCorreo(correo);
                if (clienteOpt.isEmpty()) {
                    return new AuthenticationResponse("Correo no registrado.", null);
                }
                ClienteEntity cliente = clienteOpt.get();
                if (contrasenia.equals(cliente.getContrasenia())) {
                    return new AuthenticationResponse("Contraseña incorrecta.", null);
                }
                if (cliente.getEstado() == EstadoUsuario.DISABLED) {
                    return new AuthenticationResponse("Cuenta deshabilitada.", null);
                }
                cliente.setEstado(EstadoUsuario.ONLINE);
                clienteService.save(cliente);
                UserResponse userResponse = new UserResponse(cliente);
                return new AuthenticationResponse("SignIn exitoso!", userResponse);
            } else {
                Optional<AdministradorEntity> adminOpt = administradorService.findByCorreo(correo);
                if (adminOpt.isEmpty()) {
                    return new AuthenticationResponse("Correo no registrado.", null);
                }
                AdministradorEntity administrador = adminOpt.get();
                if (contrasenia.compareTo(administrador.getContrasenia()) != 0) {
                    return new AuthenticationResponse("Contraseña incorrecta.", null);
                }
                if (administrador.getEstado() == EstadoUsuario.DISABLED) {
                    return new AuthenticationResponse("Cuenta deshabilitada.", null);
                }
                administrador.setEstado(EstadoUsuario.ONLINE);
                administradorService.save(administrador);
                UserResponse userResponse = new UserResponse(administrador);
                return new AuthenticationResponse("SignIn exitoso!", userResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse("ERROR - SIGNIN: " + e.getMessage(), null);
        }
    }

    public AuthenticationResponse signUp(SignUpRequest request) {
        try {
            String nombre = request.getNombre();
            String correo = request.getCorreo();
            String contrasenia = request.getContrasenia();
            String tipoUsuario = request.getTipoUsuario();
            if (tipoUsuario == null || (!tipoUsuario.equals("CLIENTE") && !tipoUsuario.equals("ADMINISTRADOR"))) {
                return new AuthenticationResponse("Tipo de usuario inválido.", null);
            }
            if (tipoUsuario.equals("CLIENTE")) {
                if (clienteService.findByCorreo(correo).isPresent()) {
                    return new AuthenticationResponse("Correo en uso.", null);
                }
                ClienteEntity cliente = new ClienteEntity();
                cliente.setCodigo(obtenerNuevoCodigo(tipoUsuario));
                cliente.setNombre(nombre);
                cliente.setCorreo(correo);
                cliente.setContrasenia(contrasenia);
                cliente.setEstado(EstadoUsuario.ONLINE);
                ClienteEntity clienteGuardado = clienteService.save(cliente);
                UserResponse userResponse = new UserResponse(clienteGuardado);
                return new AuthenticationResponse("SignUp Exitoso!", userResponse);
            } else {
                if (administradorService.findByCorreo(correo).isPresent()) {
                    return new AuthenticationResponse("Correo en uso.", null);
                }
                AdministradorEntity administrador = new AdministradorEntity();
                administrador.setCodigo(obtenerNuevoCodigo(tipoUsuario));
                administrador.setNombre(nombre);
                administrador.setCorreo(correo);
                administrador.setContrasenia(contrasenia);
                administrador.setEstado(EstadoUsuario.ONLINE);
                AdministradorEntity adminGuardado = administradorService.save(administrador);
                UserResponse userResponse = new UserResponse(adminGuardado);
                return new AuthenticationResponse("SignUp Exitoso!", userResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse("ERROR - SIGNUP: " + e.getMessage(), null);
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
                return new AuthenticationResponse("Tipo de usuario inválido.", null);
            }
            if (tipoUsuario.equals("CLIENTE")) {
                Optional<ClienteEntity> clienteOpt = clienteService.findByCorreo(correo);
                if (clienteOpt.isEmpty()) {
                    return new AuthenticationResponse("Correo no registrado.", null);
                }
                ClienteEntity cliente = clienteOpt.get();
                if (cliente.getEstado() != EstadoUsuario.ONLINE) {
                    return new AuthenticationResponse("El usuario no está en línea.", null);
                }
                cliente.setEstado(EstadoUsuario.OFFLINE);
                clienteService.save(cliente);
                return new AuthenticationResponse("SignOut exitoso!", null);
            } else {
                Optional<AdministradorEntity> adminOpt = administradorService.findByCorreo(correo);
                if (adminOpt.isEmpty()) {
                    return new AuthenticationResponse("Correo no registrado.", null);
                }
                AdministradorEntity administrador = adminOpt.get();
                if (administrador.getEstado() != EstadoUsuario.ONLINE) {
                    return new AuthenticationResponse("El usuario no está en línea.", null);
                }
                administrador.setEstado(EstadoUsuario.OFFLINE);
                administradorService.save(administrador);
                return new AuthenticationResponse("SignOut exitoso!", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthenticationResponse("ERROR - SIGNOUT: " + e.getMessage(), null);
        }
    }
}