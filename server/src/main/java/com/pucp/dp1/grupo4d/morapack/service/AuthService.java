/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       AuthService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service;

import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignInRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.SignUpRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.AuthResponse;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.UserResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.ClienteEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.AdministradorEntity;
import com.pucp.dp1.grupo4d.morapack.model.enums.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.service.model.AdministradorService;
import com.pucp.dp1.grupo4d.morapack.service.model.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AdministradorService administradorService;

    public AuthResponse signIn(SignInRequest request) {
        try {
            String correo = request.getCorreo();
            String contrasenia = request.getContrasenia();
            String tipoUsuario = request.getTipoUsuario();

            if (tipoUsuario == null || (!tipoUsuario.equals("CLIENTE") && !tipoUsuario.equals("ADMINISTRADOR"))) {
                return new AuthResponse(null, "Tipo de usuario inválido.");
            }

            if (tipoUsuario.equals("CLIENTE")) {
                Optional<ClienteEntity> clienteOpt = clienteService.findByCorreo(correo);
                if (clienteOpt.isEmpty()) {
                    return new AuthResponse(null, "Correo no registrado.");
                }

                ClienteEntity cliente = clienteOpt.get();
                if (contrasenia.compareTo(cliente.getContrasenia()) != 0) {
                    return new AuthResponse(null,"Contraseña incorrecta");
                }

                if (cliente.getEstado() == EstadoUsuario.DISABLED) {
                    return new AuthResponse(null,"Cuenta deshabilitada.");
                }

                cliente.setEstado(EstadoUsuario.ONLINE);
                clienteService.save(cliente);
                UserResponse userResponse = new UserResponse(cliente);
                return new AuthResponse(userResponse, "Login exitoso!");
            } else {
                Optional<AdministradorEntity> adminOpt = administradorService.findByCorreo(correo);
                if (adminOpt.isEmpty()) {
                    return new AuthResponse(null, "Correo no registrado.");
                }

                AdministradorEntity administrador = adminOpt.get();
                if (contrasenia.compareTo(administrador.getContrasenia()) != 0) {
                    return new AuthResponse(null,"Contraseña incorrecta");
                }

                if (administrador.getEstado() == EstadoUsuario.DISABLED) {
                    return new AuthResponse(null, "Cuenta deshabilitada.");
                }

                administrador.setEstado(EstadoUsuario.ONLINE);
                administradorService.save(administrador);

                UserResponse userResponse = new UserResponse(administrador);
                return new AuthResponse(userResponse, "SignIn exitoso!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResponse(null, "ERROR - SIGNIN: " + e.getMessage());
        }
    }

    public AuthResponse signUp(SignUpRequest request) {
        try {
            String codigo = request.getCodigo();
            String nombre = request.getNombre();
            String correo = request.getCorreo();
            String contrasenia = request.getContrasenia();
            String tipoUsuario = request.getTipoUsuario();

            if (tipoUsuario == null || (!tipoUsuario.equals("CLIENTE") && !tipoUsuario.equals("ADMINISTRADOR"))) {
                return new AuthResponse(null, "Tipo de usuario inválido.");
            }

            if (tipoUsuario.equals("CLIENTE")) {
                if (clienteService.findByCorreo(correo).isPresent()) {
                    return new AuthResponse(null, "Correo en uso.");
                }

                ClienteEntity cliente = new ClienteEntity();
                cliente.setCodigo(codigo);
                cliente.setNombre(nombre);
                cliente.setCorreo(correo);
                cliente.setContrasenia(contrasenia);
                ClienteEntity clienteGuardado = clienteService.save(cliente);
                UserResponse userResponse = new UserResponse(clienteGuardado);
                return new AuthResponse(userResponse, "SignUp Exitoso!");
            } else {
                if (administradorService.findByCorreo(correo).isPresent()) {
                    return new AuthResponse(null, "Correo en uso.");
                }

                AdministradorEntity administrador = new AdministradorEntity();
                administrador.setCodigo(codigo);
                administrador.setNombre(nombre);
                administrador.setCorreo(correo);
                administrador.setContrasenia(contrasenia);
                AdministradorEntity adminGuardado = administradorService.save(administrador);
                UserResponse userResponse = new UserResponse(adminGuardado);
                return new AuthResponse(userResponse, "SignUp Exitoso!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResponse(null, "ERROR - SIGNUP: " + e.getMessage());
        }
    }
}