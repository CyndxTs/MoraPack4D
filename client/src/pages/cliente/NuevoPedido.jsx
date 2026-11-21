import React, { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Campo, Selector } from "../../components/Gestion/Gestion";
import { useNotify } from "../../notifications/NotificationContext";
import { listarAeropuertosCliente, obtenerCondiciones, registrarMiPedido } from "../../api/cliente";
import { formatFecha } from "../../utils/fechas";
import { zonaCorta } from "../../utils/zona";
import "./cliente.scss";

// Nuevo pedido del cliente: destino + cantidad. La fecha es "ahora" y el origen lo elige MoraPack.
export default function NuevoPedido() {
  const notify = useNotify();
  const navigate = useNavigate();
  const [aeropuertos, setAeropuertos] = useState([]);
  const [condiciones, setCondiciones] = useState(null);
  const [destino, setDestino] = useState("");
  const [cantidad, setCantidad] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    let vivo = true;
    Promise.all([listarAeropuertosCliente(), obtenerCondiciones()])
      .then(([a, c]) => { if (vivo) { setAeropuertos(a); setCondiciones(c); } })
      .catch((e) => notify("danger", e.message));
    return () => { vivo = false; };
  }, [notify]);

  const maxPaquetes = condiciones?.maxPaquetes ?? 999;
  const opciones = useMemo(() => aeropuertos.map((a) => ({
    value: a.codigo,
    label: `${a.ciudad}, ${a.pais}`,
    detalle: a.esSede ? `${a.codigo} · sede` : a.codigo,
  })), [aeropuertos]);

  const elegido = aeropuertos.find((a) => a.codigo === destino);
  const sedes = aeropuertos.filter((a) => a.esSede);
  const n = Number(cantidad);
  const cantidadValida = Number.isInteger(n) && n >= 1 && n <= maxPaquetes;

  // Plazo: sede -> al instante; si hay una sede en su continente, plazo continental; si no, intercontinental
  const plazo = useMemo(() => {
    if (!elegido || !condiciones) return null;
    if (elegido.esSede) return { titulo: "Entrega inmediata", detalle: "El destino es una sede de MoraPack: el pedido se entrega al registrarlo." };
    const mismoContinente = sedes.some((s) => s.continente === elegido.continente);
    const dias = mismoContinente ? condiciones.maxDiasEntregaIntracontinental : condiciones.maxDiasEntregaIntercontinental;
    return {
      titulo: `Llega en ${dias} ${dias === 1 ? "día" : "días"} como máximo`,
      detalle: `${mismoContinente ? "Sale de una sede del mismo continente" : "Viaja desde otro continente"}. Luego tienes ${condiciones.maxHorasRecojo} h para recogerlo en ${elegido.ciudad}.`,
      limite: Date.now() + dias * 86400000,
    };
  }, [elegido, condiciones, sedes]);

  const enviar = async (e) => {
    e.preventDefault();
    if (!destino) return setError("Elige el destino.");
    if (!cantidadValida) return setError(`La cantidad debe estar entre 1 y ${maxPaquetes} paquetes.`);
    setError(null);
    setEnviando(true);
    try {
      const r = await registrarMiPedido(destino, n);
      notify("success", r.mensaje);
      navigate("/cliente", { state: { nuevo: r.codigo } });
    } catch (err) {
      setError(err.message);
      setEnviando(false);
    }
  };

  const paso = (d) => setCantidad((c) => String(Math.min(maxPaquetes, Math.max(1, (Number(c) || 0) + d))));

  return (
    <div className="gestion-scroll">
      <div className="nuevo-pedido">
        <header className="nuevo-pedido__cabecera">
          <h1>Nuevo pedido</h1>
          <p>Dinos a dónde va y cuántos paquetes son. MoraPack elige la sede de salida y la ruta más rápida.</p>
        </header>

        <form className="nuevo-pedido__cuerpo" onSubmit={enviar} noValidate>
          <section className="tarjeta nuevo-pedido__form">
            <Campo req label="Destino" ancho="completo">
              <Selector opciones={opciones} value={destino} onChange={setDestino} placeholder="Busca la ciudad o el país" disabled={enviando} />
            </Campo>
            <Campo req label="Cantidad de paquetes" hint={`Entre 1 y ${maxPaquetes}.`}>
              <div className="contador">
                <button type="button" onClick={() => paso(-1)} disabled={enviando || n <= 1} aria-label="Uno menos">−</button>
                <input inputMode="numeric" value={cantidad} placeholder="0" disabled={enviando}
                  onChange={(e) => /^\d{0,3}$/.test(e.target.value) && setCantidad(e.target.value)} />
                <button type="button" onClick={() => paso(1)} disabled={enviando || n >= maxPaquetes} aria-label="Uno más">+</button>
              </div>
            </Campo>
            <Campo label="Fecha del pedido" hint={`Se registra al confirmar (${zonaCorta()}).`}>
              <input value={formatFecha(Date.now())} disabled />
            </Campo>
            {error && <p className="modal2__error" role="alert">{error}</p>}
            <div className="nuevo-pedido__acciones">
              <Link to="/cliente" className="boton boton--fantasma">Volver</Link>
              <button type="submit" className="boton boton--primario" disabled={enviando || !destino || !cantidadValida}>
                {enviando ? "Registrando..." : "Confirmar pedido"}
              </button>
            </div>
          </section>

          <aside className="tarjeta nuevo-pedido__resumen" aria-live="polite">
            <span className="nuevo-pedido__etiqueta">Resumen</span>
            <div className="nuevo-pedido__ruta">
              <div>
                <small>Sale de</small>
                <strong>{elegido?.esSede ? elegido.ciudad : "Sede más conveniente"}</strong>
              </div>
              <span className="nuevo-pedido__flecha" aria-hidden="true" />
              <div>
                <small>Llega a</small>
                <strong>{elegido ? elegido.ciudad : "—"}</strong>
              </div>
            </div>
            <div className="nuevo-pedido__dato"><small>Paquetes</small><strong>{cantidadValida ? n : "—"}</strong></div>
            {plazo ? (
              <div className="nuevo-pedido__plazo">
                <strong>{plazo.titulo}</strong>
                <span>{plazo.detalle}</span>
                {plazo.limite && <span>Fecha límite aproximada: {formatFecha(plazo.limite)}</span>}
              </div>
            ) : (
              <p className="nuevo-pedido__ayuda">Elige un destino para ver el plazo de entrega.</p>
            )}
            {sedes.length > 0 && (
              <p className="nuevo-pedido__ayuda">Sedes de salida: {sedes.map((s) => s.ciudad).join(", ")}.</p>
            )}
          </aside>
        </form>
      </div>
    </div>
  );
}
