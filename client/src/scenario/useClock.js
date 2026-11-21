import { useEffect, useState } from "react";
import { RELOJ_TICK_MS } from "../config";

// Reloj del escenario. 'calcular' devuelve el instante (ms) a mostrar; se re-evalúa cada 'tickMs'.
// Operación: () => Date.now(). Simulación: inicio + (ahoraReal - inicioReal) * multiplicador.
export function useClock(calcular, activo = true, tickMs = RELOJ_TICK_MS) {
  const [now, setNow] = useState(() => calcular());
  useEffect(() => {
    setNow(calcular());
    if (!activo) return undefined;
    const id = setInterval(() => setNow(calcular()), tickMs);
    return () => clearInterval(id);
  }, [calcular, activo, tickMs]);
  return now;
}
