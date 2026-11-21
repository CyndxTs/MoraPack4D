import { http, post } from "./http";

// Espacio del cliente: todo sale de su sesión (el server nunca acepta otro código de cliente)
export async function listarAeropuertosCliente() {
  const { dtos } = await post("/cliente/aeropuertos");
  return dtos ?? [];
}

export const obtenerCondiciones = () => post("/cliente/condiciones");

export async function obtenerMiOperacion() {
  const { data } = await http.post("/cliente/operacion");
  return data?.solucion ?? null;
}

export async function listarMisPedidos(pagina = 0, tamanio = 100) {
  const r = await post("/cliente/pedidos", { pagina, tamanio });
  return { pedidos: r.dtos ?? [], total: Number(r.mensaje) || (r.dtos ?? []).length };
}

// Devuelve { codigo, mensaje }
export async function registrarMiPedido(codDestino, cantidadSolicitada) {
  const r = await post("/cliente/pedidos/registrar", { codDestino, cantidadSolicitada });
  return { codigo: r.token, mensaje: r.mensaje };
}

export const cancelarMiPedido = (codigo) => post("/cliente/pedidos/cancelar", { codigo });
