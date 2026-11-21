import { ROLES } from "../auth/session";

// Opciones del espacio de cada rol (centro de la barra). El logo de MoraPack es el inicio para todos.
export const NAV_POR_ROL = {
  [ROLES.CLIENTE]: [
    { to: "/cliente", label: "Mis envíos", end: true },
    { to: "/cliente/pedidos/nuevo", label: "Nuevo pedido" },
  ],
  [ROLES.TRABAJADOR]: [
    { to: "/trabajador", label: "Operación", end: true },
    { to: "/trabajador/simulacion", label: "Simulación" },
    { to: "/trabajador/pedidos", label: "Pedidos" },
    { to: "/trabajador/planes", label: "Planes y eventos" },
  ],
  [ROLES.ADMINISTRADOR]: [
    { to: "/admin", label: "Parámetros", end: true },
    { to: "/admin/ubicaciones", label: "Países y continentes" },
    { to: "/admin/aeropuertos", label: "Aeropuertos y sedes" },
    { to: "/admin/usuarios", label: "Usuarios y roles" },
  ],
};

export const navDe = (usuario) => (usuario ? NAV_POR_ROL[usuario.tipoUsuario] ?? [] : []);
