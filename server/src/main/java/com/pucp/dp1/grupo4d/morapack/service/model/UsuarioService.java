/**]
 >> Project:    MoraPack
 >> Author:     Grupo 4D
 >> File:       UsuarioService.java
 [**/

package com.pucp.dp1.grupo4d.morapack.service.model;

import com.pucp.dp1.grupo4d.morapack.mapper.UsuarioMapper;
import com.pucp.dp1.grupo4d.morapack.model.dto.DTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.UsuarioDTO;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.FilterRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.request.ListRequest;
import com.pucp.dp1.grupo4d.morapack.model.dto.response.ListResponse;
import com.pucp.dp1.grupo4d.morapack.model.entity.PaisEntity;
import com.pucp.dp1.grupo4d.morapack.model.entity.UsuarioEntity;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.EstadoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.enumeration.TipoUsuario;
import com.pucp.dp1.grupo4d.morapack.model.exception.G4DException;
import com.pucp.dp1.grupo4d.morapack.repository.UsuarioRepository;
import com.pucp.dp1.grupo4d.morapack.util.CodigoUsuario;
import com.pucp.dp1.grupo4d.morapack.util.G4DUtility;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Service
public class UsuarioService {
    private static final int REINTENTOS_DE_CODIGO = 5;
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final TransactionTemplate nuevaTransaccion;

    public UsuarioService(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper, PlatformTransactionManager transactionManager) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.nuevaTransaccion = new TransactionTemplate(transactionManager);
        this.nuevaTransaccion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // ---------- Normalización / validación ----------
    public static String normalizarCorreo(String correo) {
        String c = G4DUtility.Convertor.toAdmissible(correo);
        return (c != null) ? c.trim().toLowerCase(Locale.ROOT) : null;
    }

    public static String validarCorreo(String correo) {
        String c = normalizarCorreo(correo);
        if (c == null) throw new G4DException("Ingrese su correo.");
        if (c.length() > 80 || !PATRON_CORREO.matcher(c).matches()) throw new G4DException("El correo no es válido.");
        return c;
    }

    public static String validarNombre(String valor, String campo) {
        String v = G4DUtility.Convertor.toAdmissible(valor);
        if (v == null) throw new G4DException(String.format("Ingrese sus %s.", campo));
        v = v.trim().replaceAll("\\s+", " ");
        if (v.length() > 60) throw new G4DException(String.format("Los %s no pueden superar 60 caracteres.", campo));
        return v;
    }

    // Campo opcional (apellidos): null si viene vacío
    public static String validarNombreOpcional(String valor, String campo) {
        String v = G4DUtility.Convertor.toAdmissible(valor);
        return (v == null) ? null : validarNombre(v, campo);
    }

    public static void validarContrasenia(String contrasenia, String confirmacion) {
        if (contrasenia == null || contrasenia.length() < 8) throw new G4DException("La contraseña debe tener al menos 8 caracteres.");
        if (contrasenia.length() > 72) throw new G4DException("La contraseña no puede superar 72 caracteres.");
        if (!contrasenia.equals(confirmacion)) throw new G4DException("Las contraseñas no coinciden.");
    }

    // ---------- Consultas ----------
    public UsuarioEntity save(UsuarioEntity usuario) {
        return usuarioRepository.save(usuario);
    }

    public Optional<UsuarioEntity> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    public Optional<UsuarioEntity> findByCodigo(String codigo) {
        return usuarioRepository.findByCodigo(codigo);
    }

    // Solo clientes (los pedidos de operación se asocian a un cliente)
    public Optional<UsuarioEntity> findClienteByCodigo(String codigo) {
        return usuarioRepository.findByCodigo(codigo).filter(u -> u.getRol() == TipoUsuario.CLIENTE);
    }

    public Optional<UsuarioEntity> findByCorreo(String correo) {
        String c = normalizarCorreo(correo);
        return (c != null) ? usuarioRepository.findByCorreo(c) : Optional.empty();
    }

    public boolean existsByCorreo(String correo) {
        String c = normalizarCorreo(correo);
        return c != null && usuarioRepository.existsByCorreo(c);
    }

    public List<UsuarioEntity> findAllClientesInRangeByScenario(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, String tipoEscenario, List<String> codOrigenes) {
        return usuarioRepository.findAllClientesInRangeByScenario(fechaHoraInicio, fechaHoraFin, tipoEscenario, codOrigenes);
    }

    public long contarPedidos(UsuarioEntity usuario) {
        return usuarioRepository.countPedidos(usuario.getId());
    }

    public void delete(UsuarioEntity usuario) {
        usuarioRepository.delete(usuario);
    }

    // ---------- Códigos ----------
    // Siguiente código libre del rol: máximo numérico válido del rol + 1 (error si se desborda)
    public String siguienteCodigo(TipoUsuario rol) {
        List<String> codigos = usuarioRepository.findCodigosByRol(rol.toString(), CodigoUsuario.prefijo(rol));
        int siguiente = CodigoUsuario.maximoActual(rol, codigos) + 1;
        if (siguiente > CodigoUsuario.maximo(rol)) {
            throw new G4DException(String.format("Se agotaron los códigos disponibles para el rol %s.", rol));
        }
        return CodigoUsuario.formatear(rol, siguiente);
    }

    /**
     * Crea un usuario con el siguiente código de su rol. Cada intento va en su propia transacción:
     * si otro proceso tomó el mismo código (UNIQUE), se recalcula y se reintenta.
     */
    public UsuarioEntity crear(String nombres, String apellidos, String correo, String hashContrasenia, PaisEntity pais, TipoUsuario rol) {
        String c = validarCorreo(correo);
        for (int intento = 1; intento <= REINTENTOS_DE_CODIGO; intento++) {
            if (usuarioRepository.existsByCorreo(c)) throw new G4DException(String.format("El correo ('%s') ya está registrado.", c));
            try {
                return nuevaTransaccion.execute(status -> {
                    UsuarioEntity usuario = new UsuarioEntity();
                    usuario.setCodigo(this.siguienteCodigo(rol));
                    usuario.setRol(rol);
                    usuario.setNombres(nombres);
                    usuario.setApellidos(apellidos);
                    usuario.setCorreo(c);
                    usuario.setContrasenia(hashContrasenia);
                    usuario.setEstado(EstadoUsuario.OFFLINE);
                    usuario.setPais(pais);
                    return usuarioRepository.saveAndFlush(usuario);
                });
            } catch (DataIntegrityViolationException e) {
                if (usuarioRepository.existsByCorreo(c)) throw new G4DException(String.format("El correo ('%s') ya está registrado.", c));
                System.out.printf("[*] CÓDIGO DE USUARIO EN USO, REINTENTANDO.. ('%d/%d')%n", intento, REINTENTOS_DE_CODIGO);
            }
        }
        throw new G4DException("No se pudo asignar un código al usuario, intente nuevamente.");
    }

    /**
     * Cambia el rol y, en la misma transacción, reasigna el código con el siguiente libre del rol destino.
     * 'despues' corre dentro de esa transacción (p. ej. revocar sesiones).
     */
    public UsuarioEntity cambiarRol(Integer idUsuario, TipoUsuario rol, Consumer<UsuarioEntity> despues) {
        for (int intento = 1; intento <= REINTENTOS_DE_CODIGO; intento++) {
            try {
                return nuevaTransaccion.execute(status -> {
                    UsuarioEntity usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new G4DException("El usuario no existe."));
                    if (usuario.getRol() == rol) throw new G4DException(String.format("El usuario ya tiene el rol %s.", rol));
                    usuario.setRol(rol);
                    usuario.setCodigo(this.siguienteCodigo(rol));
                    UsuarioEntity guardado = usuarioRepository.saveAndFlush(usuario);
                    if (despues != null) despues.accept(guardado);
                    return guardado;
                });
            } catch (DataIntegrityViolationException e) {
                System.out.printf("[*] CÓDIGO DE USUARIO EN USO, REINTENTANDO.. ('%d/%d')%n", intento, REINTENTOS_DE_CODIGO);
            }
        }
        throw new G4DException("No se pudo reasignar el código del usuario, intente nuevamente.");
    }

    // ---------- Listados ----------
    public ListResponse listar(ListRequest request, TipoUsuario rol) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        String r = (rol != null) ? rol.toString() : null;
        List<DTO> dtos = new ArrayList<>();
        usuarioRepository.findAllByAttributes(r, null, null, null, null, null, null, pageable).getContent().forEach(u -> dtos.add(usuarioMapper.toDTO(u)));
        return new ListResponse(true, String.format("Usuarios listados correctamente! ('%d')", dtos.size()), dtos);
    }

    // 'rolForzado' (p. ej. CLIENTE para el trabajador) ignora el rol del modelo
    public ListResponse filtrar(FilterRequest<UsuarioDTO> request, TipoUsuario rolForzado) {
        Pageable pageable = G4DUtility.Convertor.toAdmissible(request.getPagina(), request.getTamanio(), Sort.Order.asc("codigo"));
        UsuarioDTO modelo = (request.getModelo() != null) ? request.getModelo() : new UsuarioDTO();
        String rol = (rolForzado != null) ? rolForzado.toString() : G4DUtility.Convertor.toAdmissibleEnumString(modelo.getTipoUsuario(), TipoUsuario.class);
        String codigo = G4DUtility.Convertor.toAdmissible(modelo.getCodigo());
        String nombres = G4DUtility.Convertor.toAdmissible(modelo.getNombres());
        String apellidos = G4DUtility.Convertor.toAdmissible(modelo.getApellidos());
        String correo = G4DUtility.Convertor.toAdmissible(modelo.getCorreo());
        String estado = G4DUtility.Convertor.toAdmissibleEnumString(modelo.getEstado(), EstadoUsuario.class);
        String pais = G4DUtility.Convertor.toAdmissible(modelo.getPais());
        List<DTO> dtos = new ArrayList<>();
        usuarioRepository.findAllByAttributes(rol, codigo, nombres, apellidos, correo, estado, pais, pageable).getContent().forEach(u -> dtos.add(usuarioMapper.toDTO(u)));
        return new ListResponse(true, String.format("Usuarios filtrados correctamente! ('%d')", dtos.size()), dtos);
    }
}
