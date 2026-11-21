import React, { useEffect, useMemo, useState } from "react";
import { Campo, Interruptor, PaginaGestion } from "../../components/Gestion/Gestion";
import { obtenerParametros } from "../../api/catalogos";
import { guardarParametros } from "../../api/importacion";
import { useNotify } from "../../notifications/NotificationContext";

// Rangos válidos (mismas reglas que el server)
const NUMEROS = {
  maxDiasEntregaIntracontinental: { min: 1, max: 15, paso: 1 },
  maxDiasEntregaIntercontinental: { min: 1, max: 15, paso: 1 },
  maxHorasRecojo: { min: 0, max: 48, paso: 0.5 },
  minHorasEstancia: { min: 0, max: 48, paso: 0.5 },
  maxHorasEstancia: { min: 0, max: 72, paso: 0.5 },
  minutosEsperaReplanificacion: { min: 0, max: 1440, paso: 1 },
  segundosUmbralReplanificacion: { min: 120, max: 240, paso: 10 },
  probabilidadReplanificacion: { min: 0, max: 1, paso: 0.05 },
  dMin: { min: 0, max: 1, paso: 0.001 },
  iMax: { min: 1, max: 50, paso: 1 }, eleMin: { min: 1, max: 20, paso: 1 }, eleMax: { min: 1, max: 20, paso: 1 },
  kMin: { min: 1, max: 20, paso: 1 }, kMax: { min: 1, max: 20, paso: 1 }, nMax: { min: 1, max: 50, paso: 1 }, tMax: { min: 1, max: 50, paso: 1 },
  factorDeUmbralDeAberracion: { min: 1, max: 2, paso: 0.001 }, factorDeUtilizacionTemporal: { min: 0, max: 100000, paso: 100 },
  factorDeDesviacionEspacial: { min: 0, max: 100000, paso: 100 }, factorDeDisposicionOperacional: { min: 0, max: 100000, paso: 100 },
  factorDeLejania: { min: 0, max: 1, paso: 0.0005 },
};

function validar(p) {
  for (const [k, r] of Object.entries(NUMEROS)) {
    const v = Number(p[k]);
    if (!Number.isFinite(v) || v < r.min || v > r.max) return `Revisa "${k}": debe estar entre ${r.min} y ${r.max}.`;
  }
  if (Number(p.minHorasEstancia) > Number(p.maxHorasEstancia)) return "La estancia mínima no puede superar a la máxima.";
  if (Number(p.eleMin) > Number(p.eleMax) || Number(p.kMin) > Number(p.kMax)) return "En el algoritmo, cada mínimo debe ser menor o igual a su máximo.";
  return null;
}

export default function Parametros() {
  const notify = useNotify();
  const [original, setOriginal] = useState(null);
  const [p, setP] = useState(null);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    obtenerParametros().then((r) => { setOriginal(r); setP(r); }).catch((e) => notify("danger", e.message));
  }, [notify]);

  const cambios = useMemo(() => p && original && JSON.stringify(p) !== JSON.stringify(original), [p, original]);
  const set = (k) => (v) => setP((x) => ({ ...x, [k]: v }));
  const num = (k, label, hint) => (
    <Campo label={label} hint={hint}>
      <input type="number" min={NUMEROS[k].min} max={NUMEROS[k].max} step={NUMEROS[k].paso} value={p[k] ?? ""} onChange={(e) => set(k)(e.target.value === "" ? "" : Number(e.target.value))} />
    </Campo>
  );

  const guardar = async () => {
    const err = validar(p);
    if (err) return setError(err);
    setError(null);
    setGuardando(true);
    try {
      await guardarParametros(p);
      const r = await obtenerParametros();
      setOriginal(r); setP(r);
      notify("success", "Parámetros guardados.");
    } catch (e) {
      setError(e.message);
    } finally {
      setGuardando(false);
    }
  };

  return (
    <PaginaGestion titulo="Parámetros" descripcion="Valores con los que el server planifica la operación. La simulación parte de estos valores, pero cada simulación puede ajustarlos.">
      {!p ? <p className="tenue">Cargando parámetros...</p> : (
        <>
          <div className="rejilla-2">
            <section className="tarjeta">
              <h2>Operación automática</h2>
              <p>Lo que el server hace solo, sin que un trabajador lo lance.</p>
              <Interruptor etiqueta="Replanificar al registrar pedidos" descripcion="Al registrar o importar pedidos de operación se programa una replanificación." activo={p.replanificacionAutomatica} onChange={set("replanificacionAutomatica")} />
              <Interruptor etiqueta="Replanificar al registrar eventos" descripcion="Un evento de operación (cancelación, reprogramación, reajuste) replanifica de inmediato." activo={p.replanificarAlRegistrarEvento} onChange={set("replanificarAlRegistrarEvento")} />
              <Interruptor etiqueta="Purga automática" descripcion="Cada hora se borran los datos de operación que ya no se usan." activo={p.purgaAutomatica} onChange={set("purgaAutomatica")} />
              <div className="campos" style={{ marginTop: 12 }}>
                {num("minutosEsperaReplanificacion", "Espera antes de replanificar (min)", "Desde el primer pedido nuevo. 0 = de inmediato.")}
                {num("segundosUmbralReplanificacion", "Umbral de replanificación (s)", "Entre 120 y 240. Margen para lo que despega durante el cálculo.")}
              </div>
            </section>

            <section className="tarjeta">
              <h2>Plazos y almacenes</h2>
              <p>Reglas del negocio para entregar a tiempo.</p>
              <div className="campos">
                {num("maxDiasEntregaIntracontinental", "Entrega en el mismo continente (días)")}
                {num("maxDiasEntregaIntercontinental", "Entrega entre continentes (días)")}
                {num("maxHorasRecojo", "Tiempo para recoger en destino (h)")}
                {num("minHorasEstancia", "Estancia mínima en escala (h)")}
                {num("maxHorasEstancia", "Estancia máxima en escala (h)")}
              </div>
            </section>
          </div>

          <section className="tarjeta">
            <details className="avanzado">
              <summary>Algoritmo de planificación (GVNS)</summary>
              <div className="campos">
                {num("probabilidadReplanificacion", "Probabilidad de replanificación")}
                {num("dMin", "dMin")}
                {num("iMax", "iMax")}
                {num("eleMin", "ℓ mínimo")}
                {num("eleMax", "ℓ máximo")}
                {num("kMin", "k mínimo")}
                {num("kMax", "k máximo")}
                {num("nMax", "nMax")}
                {num("tMax", "tMax")}
                {num("factorDeUmbralDeAberracion", "Factor de umbral de aberración")}
                {num("factorDeUtilizacionTemporal", "Factor de utilización temporal")}
                {num("factorDeDesviacionEspacial", "Factor de desviación espacial")}
                {num("factorDeDisposicionOperacional", "Factor de disposición operacional")}
                {num("factorDeLejania", "Factor de lejanía")}
              </div>
            </details>
          </section>

          {error && <p className="modal2__error" role="alert" style={{ marginTop: 16 }}>{error}</p>}
          <div className="barra-guardar">
            <span>Las sedes se configuran en "Aeropuertos y sedes".</span>
            <button type="button" className="boton boton--fantasma" disabled={!cambios || guardando} onClick={() => { setP(original); setError(null); }}>Descartar</button>
            <button type="button" className="boton boton--primario" disabled={!cambios || guardando} onClick={guardar}>{guardando ? "Guardando..." : "Guardar cambios"}</button>
          </div>
        </>
      )}
    </PaginaGestion>
  );
}
