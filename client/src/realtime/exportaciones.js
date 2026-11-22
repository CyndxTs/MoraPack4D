import { crearSeguidor } from "./seguimiento";

// Solo el estado: el nombre del archivo se conoce de antemano ("<prefijo>__<idTransaccion>.txt")
const seguidor = crearSeguidor(null, "/topic/exportation-status-");

export const prepararSeguimientoDeExportaciones = () => seguidor.preparar();
export const esperarExportacion = (id) => seguidor.esperar(id, null, 60000);

// Envío de reportes por correo: el resultado (EXITOSO / ERRONEO con mensaje) llega cuando Brevo responde
const seguidorEnvios = crearSeguidor(null, "/topic/exportation-send-status-");
export const prepararSeguimientoDeEnvios = () => seguidorEnvios.preparar();
export const esperarEnvio = (id) => seguidorEnvios.esperar(id, null, 120000);
