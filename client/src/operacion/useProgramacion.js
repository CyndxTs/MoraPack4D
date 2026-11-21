import { useEffect, useState } from "react";
import { obtenerProgramada, suscribirProgramacion } from "./replanificador";

// Instante (ms) de la próxima replanificación automática, o null
export function useProgramacion() {
  const [programada, setProgramada] = useState(() => obtenerProgramada());
  useEffect(() => suscribirProgramacion(setProgramada), []);
  return programada;
}
