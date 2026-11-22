import React, { useEffect, useState } from "react";
import { Modal, Segmentado } from "../Gestion/Gestion";
import { descargarReporte, enviarReportePorCorreo, pdfDe, previsualizarPdf, previsualizarReporte } from "../../api/escenarios";
import { esperarEnvio, prepararSeguimientoDeEnvios } from "../../realtime/exportaciones";
import { useNotify } from "../../notifications/NotificationContext";
import "./reporte.scss";

const FORMATOS = [{ value: "pdf", label: "PDF" }, { value: "txt", label: "Texto (.txt)" }];
const CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;

/**
 * Reporte de una simulación o de la operación: vista previa del PDF y del TXT (pestañas, PDF por defecto),
 * descargar el formato elegido o enviarlo por correo.
 *  - archivo: { nombre: "<prefijo>__<id>.txt", ruta }
 *  - onCerrar({ enviado }): enviado = true si el server ya borró los archivos tras el envío
 */
export default function ReporteModal({ titulo = "Reporte", descripcion, archivo, aviso, onCerrar }) {
  const notify = useNotify();
  const [formato, setFormato] = useState("pdf");
  const [pdfUrl, setPdfUrl] = useState(null);
  const [texto, setTexto] = useState(null);
  const [error, setError] = useState({ pdf: null, txt: null });
  const [modoCorreo, setModoCorreo] = useState(false);
  const [correo, setCorreo] = useState("");
  const [estadoEnvio, setEstadoEnvio] = useState(null); // null | "enviando"
  const [descargando, setDescargando] = useState(false);
  const archivoPdf = { nombre: pdfDe(archivo.nombre), ruta: archivo.ruta };
  const actual = formato === "pdf" ? archivoPdf : archivo;

  // Vistas previas (cada formato se pide una sola vez)
  useEffect(() => {
    let vivo = true;
    let url = null;
    previsualizarPdf(archivoPdf)
      .then((blob) => { if (!vivo) return; url = URL.createObjectURL(blob); setPdfUrl(url); })
      .catch((e) => vivo && setError((x) => ({ ...x, pdf: e.message })));
    previsualizarReporte(archivo)
      .then((t) => vivo && setTexto(t))
      .catch((e) => vivo && setError((x) => ({ ...x, txt: e.message })));
    return () => { vivo = false; if (url) URL.revokeObjectURL(url); };
  }, [archivo.nombre]); // eslint-disable-line react-hooks/exhaustive-deps

  const enviando = estadoEnvio === "enviando";

  const descargar = async () => {
    setDescargando(true);
    try { await descargarReporte(actual); } catch (e) { notify("danger", e.message); } finally { setDescargando(false); }
  };

  const enviar = async () => {
    const destino = correo.trim();
    if (!CORREO.test(destino)) return setError((x) => ({ ...x, correo: "Escribe un correo válido." }));
    setError((x) => ({ ...x, correo: null }));
    setEstadoEnvio("enviando");
    try {
      await prepararSeguimientoDeEnvios();
      const id = await enviarReportePorCorreo(actual.nombre, destino);
      const fin = await esperarEnvio(id);
      if (fin.estadoFinalizacion === "EXITOSO") notify("success", fin.mensaje || `Reporte enviado a ${destino}.`);
      else if (fin.estadoFinalizacion === "ERRONEO") notify("danger", fin.mensaje || "No se pudo enviar el reporte.");
      else notify("warning", "No se pudo confirmar el envío. Revisa la bandeja del destinatario.");
      // Tras la respuesta de Brevo el server ya borró el reporte: el modal se cierra
      onCerrar({ enviado: true });
    } catch (e) {
      setEstadoEnvio(null);
      setError((x) => ({ ...x, correo: e.message }));
    }
  };

  const pie = modoCorreo ? (
    <>
      <button type="button" className="boton boton--fantasma" onClick={() => setModoCorreo(false)} disabled={enviando}>Volver</button>
      <button type="button" className="boton boton--primario" onClick={enviar} disabled={enviando}>
        {enviando ? "Enviando..." : `Enviar ${formato === "pdf" ? "PDF" : "TXT"}`}
      </button>
    </>
  ) : (
    <>
      <button type="button" className="boton boton--fantasma" onClick={() => onCerrar({ enviado: false })}>Cerrar</button>
      <button type="button" className="boton boton--fantasma" onClick={() => setModoCorreo(true)}>Enviar por correo</button>
      <button type="button" className="boton boton--primario" onClick={descargar} disabled={descargando}>
        {descargando ? "Descargando..." : `Descargar ${formato === "pdf" ? "PDF" : "TXT"}`}
      </button>
    </>
  );

  return (
    <Modal titulo={titulo} descripcion={descripcion} onCerrar={() => !enviando && onCerrar({ enviado: false })} bloqueado={enviando} ancho="reporte" pie={pie}>
      {aviso}
      <div className="reporte__barra">
        <Segmentado nombre="Formato del reporte" opciones={FORMATOS} value={formato} onChange={setFormato} />
        <span className="reporte__archivo" title={actual.nombre}>{actual.nombre}</span>
      </div>

      <div className="reporte__vista">
        {formato === "pdf" ? (
          error.pdf ? <p className="reporte__error">No se pudo cargar el PDF: {error.pdf}</p>
            : pdfUrl ? <iframe title="Vista previa del PDF" src={`${pdfUrl}#toolbar=0&navpanes=0&view=FitH`} />
              : <div className="reporte__cargando">Preparando la vista previa del PDF...</div>
        ) : (
          error.txt ? <p className="reporte__error">No se pudo cargar el texto: {error.txt}</p>
            : texto != null ? <pre>{texto}</pre>
              : <div className="reporte__cargando">Cargando el reporte de texto...</div>
        )}
      </div>

      {modoCorreo && (
        <div className="reporte__correo">
          <label htmlFor="reporte-correo">Enviar el {formato === "pdf" ? "PDF" : "TXT"} a<span className="campo__req" aria-hidden="true"> *</span></label>
          <input id="reporte-correo" type="email" autoFocus value={correo} placeholder="nombre@empresa.com" disabled={enviando}
            onChange={(e) => setCorreo(e.target.value)} onKeyDown={(e) => e.key === "Enter" && enviar()} />
          <small>Al terminar el envío (bien o mal) el reporte se borra del server.</small>
          {error.correo && <p className="modal2__error" role="alert">{error.correo}</p>}
        </div>
      )}
    </Modal>
  );
}
