import { useEffect, useState } from "react";
import { obtenerProgramacion } from "../api/escenarios";
import { suscribir } from "../realtime/stompClient";
import { parseServerDateTime } from "../utils/fechas";

// Instante (ms) de la próxima replanificación automática que programó el server, o null
export function useProgramacion() {
  const [programada, setProgramada] = useState(null);
  useEffect(() => {
    let vivo = true;
    const aplicar = (p) => vivo && setProgramada(p?.programadaPara ? parseServerDateTime(p.programadaPara) : null);
    obtenerProgramacion().then(aplicar).catch(() => {});
    const off = suscribir("/topic/operation-schedule", aplicar);
    return () => { vivo = false; off(); };
  }, []);
  return programada;
}
