import { post } from "./http";

export async function iniciarSesion(correo, contrasenia, tipoUsuario = null) {
  const r = await post("/auth/iniciar-sesion", { correo, contrasenia, tipoUsuario });
  return r.usuario;
}

export const cerrarSesion = (correo, tipoUsuario) => post("/auth/cerrar-sesion", { correo, tipoUsuario });
