import { crearSeguidor } from "./seguimiento";

const seguidor = crearSeguidor("/topic/importation-", "/topic/importation-status-");

export const prepararSeguimientoDeImportaciones = () => seguidor.preparar();

// onProgress recibe { proceso, completado, total }
export const esperarImportacion = (id, onProgress) => seguidor.esperar(id, onProgress);
