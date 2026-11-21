import React, { useEffect, useState } from "react";
import { DateTimeInline, RangeSelector } from "../../../components/UI/ui";
import ParametrosForm from "../../../scenario/ParametrosForm";
import { fromInputs, toInputDate, toInputTime } from "../../../utils/fechas";
import { zonaLarga } from "../../../utils/zona";
import { parseNumber } from "../../../utils/format";

const SEMANA_MS = 7 * 24 * 3600 * 1000;

// El multiplicador y el salto del algoritmo van ligados (más velocidad -> bloques más cortos), como antes
const saltoPara = (multiplicador) => 1.5 + 0.25 * (2 - (multiplicador - 120) / 30);
const multiplicadorPara = (salto) => 120 + 30 * (2 - (salto - 1.5) / 0.25);

export default function ConfigurarSimulacionModal({ parametrosBase, aeropuertos, onCancelar, onIniciar, iniciando }) {
  const [fechaI, setFechaI] = useState("");
  const [horaI, setHoraI] = useState("");
  const [multiplicador, setMultiplicador] = useState(120);
  const [salto, setSalto] = useState(2);
  const [parametros, setParametros] = useState(parametrosBase);
  const [error, setError] = useState(null);

  useEffect(() => setParametros(parametrosBase), [parametrosBase]);

  const inicioMs = fromInputs(fechaI, horaI);
  const finMs = inicioMs != null ? inicioMs + SEMANA_MS : null;

  const iniciar = () => {
    if (inicioMs == null) return setError("Elige la fecha y hora de inicio.");
    if (!parametros) return setError("Aún no se cargan los parámetros.");
    if (!parametros.codOrigenes?.length) return setError("Elige al menos una sede.");
    setError(null);
    onIniciar({ inicioMs, finMs, multiplicador, salto, parametros });
  };

  return (
    <div className="modal" onClick={onCancelar}>
      <div className="modal-content modal-content--wide" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3 className="modal-title">Planificar simulación</h3>
        </div>
        <div className="modal-body">
          <span className="sidebar-subtitle">Rango de simulación (una semana)</span>
          <label>Fecha y hora de inicio ({zonaLarga()})</label>
          <DateTimeInline dateValue={fechaI} timeValue={horaI} onDateChange={(e) => setFechaI(e.target.value)} onTimeChange={(e) => setHoraI(e.target.value)} />
          <label>Fecha y hora de fin ({zonaLarga()})</label>
          <DateTimeInline
            dateValue={finMs != null ? toInputDate(finMs) : ""}
            timeValue={finMs != null ? toInputTime(finMs) : ""}
            onDateChange={() => {}} onTimeChange={() => {}}
            disabled
          />

          <span className="sidebar-subtitle">Configuración temporal</span>
          <label>Multiplicador temporal (segundos simulados por segundo real)</label>
          <RangeSelector min={120} max={180} step={30} value={multiplicador}
            onChange={(n) => { const v = parseNumber(n); setMultiplicador(v); setSalto(saltoPara(v)); }} />
          <label>Salto del algoritmo (minutos reales por bloque)</label>
          <RangeSelector min={1.5} max={2} step={0.25} value={salto}
            onChange={(n) => { const v = parseNumber(n); setSalto(v); setMultiplicador(multiplicadorPara(v)); }} />

          <ParametrosForm value={parametros} onChange={setParametros} aeropuertos={aeropuertos} />

          {error && <p className="form-error">{error}</p>}
        </div>
        <div className="modal-footer">
          <button className="btn red" onClick={onCancelar} disabled={iniciando}>Cancelar</button>
          <button className="btn green" onClick={iniciar} disabled={iniciando}>{iniciando ? "Iniciando..." : "Planear"}</button>
        </div>
      </div>
    </div>
  );
}
