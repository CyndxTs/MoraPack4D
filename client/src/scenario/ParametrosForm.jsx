import React from "react";
import { Dropdown2, RangeSelector, TriPieSelector } from "../components/UI/ui";
import { parseNumber } from "../utils/format";

/**
 * Editor de ParametrosDTO (se reutiliza en la simulación y, más adelante, en el administrador).
 * 'value' es el DTO completo; 'onChange' recibe el DTO actualizado. Mantiene las reglas min < max.
 */
export default function ParametrosForm({ value, onChange, aeropuertos = [], mostrarSedes = true, mostrarAvanzados = true }) {
  if (!value) return <p className="sidebar-subtitle">Cargando parámetros...</p>;
  const set = (cambios) => onChange({ ...value, ...cambios });
  const num = (k) => (n) => set({ [k]: parseNumber(n) });
  const sedes = value.codOrigenes || [];

  return (
    <>
      {mostrarSedes && (
        <>
          <span className="sidebar-subtitle">Ciudades sede</span>
          <div className="selected-codes">
            {sedes.map((cod) => (
              <div key={cod} className="chip">
                <span>{cod}</span>
                <button className="chip-remove" onClick={() => set({ codOrigenes: sedes.filter((c) => c !== cod) })}>×</button>
              </div>
            ))}
          </div>
          <Dropdown2
            placeholder="Elegir sedes..."
            multiple
            value={sedes}
            onChange={(codOrigenes) => set({ codOrigenes })}
            options={aeropuertos.map((a) => ({ label: `${a.codigo} - ${a.ciudad} - ${a.pais}`, value: a.codigo }))}
          />
        </>
      )}

      <span className="sidebar-subtitle">Plazos</span>
      <label>Máx. días de entrega intercontinental</label>
      <RangeSelector min={1} max={3} step={1} value={value.maxDiasEntregaIntercontinental} onChange={num("maxDiasEntregaIntercontinental")} />
      <label>Máx. días de entrega intracontinental</label>
      <RangeSelector min={1} max={3} step={1} value={value.maxDiasEntregaIntracontinental} onChange={num("maxDiasEntregaIntracontinental")} />
      <label>Máx. horas de recojo</label>
      <RangeSelector min={1} max={3} step={1} value={value.maxHorasRecojo} onChange={num("maxHorasRecojo")} />
      <label>Mín. horas de estancia</label>
      <RangeSelector
        min={1} max={6} step={1} value={value.minHorasEstancia}
        onChange={(n) => {
          const v = parseNumber(n);
          set({ minHorasEstancia: v, ...(v >= value.maxHorasEstancia ? { maxHorasEstancia: Math.max(12, v + 1) } : {}) });
        }}
      />
      <label>Máx. horas de estancia</label>
      <RangeSelector
        min={12} max={32} step={4} value={value.maxHorasEstancia}
        onChange={(n) => {
          const v = parseNumber(n);
          set({ maxHorasEstancia: v, ...(v <= value.minHorasEstancia ? { minHorasEstancia: Math.max(1, v - 1) } : {}) });
        }}
      />
      <label>Probabilidad de replanificación</label>
      <RangeSelector min={0} max={0.5} step={0.05} value={value.probabilidadReplanificacion} onChange={num("probabilidadReplanificacion")} />

      {mostrarAvanzados && (
        <>
          <span className="sidebar-subtitle">Metaheurística (GVNS)</span>
          <label>dMin (diferencia mínima de fitness)</label>
          <RangeSelector min={0.001} max={0.01} step={0.001} value={value.dMin} onChange={num("dMin")} />
          <label>iMax (iteraciones de la solución inicial)</label>
          <RangeSelector min={1} max={5} step={1} value={value.iMax} onChange={num("iMax")} />
          <label>eleMin</label>
          <RangeSelector
            min={1} max={2} step={1} value={value.eleMin}
            onChange={(n) => { const v = parseNumber(n); set({ eleMin: v, ...(v >= value.eleMax ? { eleMax: v + 1 } : {}) }); }}
          />
          <label>eleMax</label>
          <RangeSelector
            min={2} max={3} step={1} value={value.eleMax}
            onChange={(n) => { const v = parseNumber(n); set({ eleMax: v, ...(v <= value.eleMin ? { eleMin: v - 1 } : {}) }); }}
          />
          <label>kMin</label>
          <RangeSelector
            min={1} max={5} step={1} value={value.kMin}
            onChange={(n) => { const v = parseNumber(n); set({ kMin: v, ...(v >= value.kMax ? { kMax: Math.min(7, v + 2) } : {}) }); }}
          />
          <label>kMax</label>
          <RangeSelector
            min={3} max={7} step={1} value={value.kMax}
            onChange={(n) => { const v = parseNumber(n); set({ kMax: v, ...(v <= value.kMin ? { kMin: Math.max(1, v - 2) } : {}) }); }}
          />
          <label>tMax (segundos de búsqueda VNS)</label>
          <RangeSelector min={1} max={30} step={1} value={value.tMax} onChange={num("tMax")} />
          <label>nMax (intentos de agitación)</label>
          <RangeSelector min={5} max={12} step={1} value={value.nMax} onChange={num("nMax")} />
          <label>Factor de umbral de aberración</label>
          <RangeSelector min={1.015} max={1.075} step={0.015} value={value.factorDeUmbralDeAberracion} onChange={num("factorDeUmbralDeAberracion")} />
          <label>Factor de lejanía</label>
          <input
            type="number" className="custom-input" step="0.0005" min="0"
            value={value.factorDeLejania ?? ""}
            onChange={(e) => set({ factorDeLejania: parseNumber(e.target.value) })}
          />
          <label>Pesos de la función objetivo</label>
          <TriPieSelector
            labels={["Factor de utilización temporal", "Factor de desviación espacial", "Factor de disposición operacional"]}
            valores={[value.factorDeUtilizacionTemporal, value.factorDeDesviacionEspacial, value.factorDeDisposicionOperacional]}
            setters={[
              (v) => onChange((prev) => ({ ...(prev ?? value), factorDeUtilizacionTemporal: v })),
              (v) => onChange((prev) => ({ ...(prev ?? value), factorDeDesviacionEspacial: v })),
              (v) => onChange((prev) => ({ ...(prev ?? value), factorDeDisposicionOperacional: v })),
            ]}
          />
        </>
      )}
    </>
  );
}
