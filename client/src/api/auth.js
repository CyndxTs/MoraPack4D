import { post } from "./http";

// Todas devuelven el AuthenticationResponse completo (usuario, tokens, segundosParaReenvio, requiereDatos...)
export const iniciarSesion = (correo, contrasenia) => post("/auth/iniciar-sesion", { correo, contrasenia });
export const iniciarSesionGoogle = (credencial, datos = {}) => post("/auth/google", { credencial, ...datos });
export const cerrarSesion = (tokenRefresco) => post("/auth/cerrar-sesion", { tokenRefresco });

export const registrar = (datos) => post("/auth/registrar", datos);
export const verificarRegistro = (correo, codigo) => post("/auth/registrar/verificar", { correo, codigo });
export const reenviarCodigo = (correo, proposito) => post("/auth/codigo/reenviar", { correo, proposito });

export const olvideContrasenia = (correo) => post("/auth/contrasenia/olvido", { correo });
export const restablecerContrasenia = (datos) => post("/auth/contrasenia/restablecer", datos);

// Mi cuenta (requiere sesión)
export const obtenerMiCuenta = () => post("/auth/mi-cuenta");
export const actualizarPerfil = (datos) => post("/auth/mi-cuenta/perfil", datos);
export const cambiarContrasenia = (datos) => post("/auth/mi-cuenta/contrasenia", datos);

// Países para el registro (público)
export const listarPaisesPublico = async () => (await post("/paises/listar", { pagina: 0, tamanio: 500 })).dtos ?? [];
