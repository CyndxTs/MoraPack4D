import { crearSeguidor } from "./seguimiento";

// Solo el estado: el nombre del archivo se conoce de antemano ("<prefijo>__<idTransaccion>.txt")
const seguidor = crearSeguidor(null, "/topic/exportation-status-");

export const prepararSeguimientoDeExportaciones = () => seguidor.preparar();
export const esperarExportacion = (id) => seguidor.esperar(id, null, 60000);
