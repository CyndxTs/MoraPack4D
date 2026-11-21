import React, { useEffect, useRef, useState } from "react";
import { GOOGLE_CLIENT_ID } from "../../config";

// Botón oficial de Google Identity Services. Devuelve el ID token ("credential") en onCredencial.
let cargaScript = null;
function cargarGis() {
  if (window.google?.accounts?.id) return Promise.resolve();
  if (!cargaScript) {
    cargaScript = new Promise((resolve, reject) => {
      const s = document.createElement("script");
      s.src = "https://accounts.google.com/gsi/client";
      s.async = true;
      s.defer = true;
      s.onload = () => resolve();
      s.onerror = () => { cargaScript = null; reject(new Error("No se pudo cargar Google")); };
      document.head.appendChild(s);
    });
  }
  return cargaScript;
}

export default function GoogleButton({ onCredencial, texto = "continue_with" }) {
  const ref = useRef(null);
  const callbackRef = useRef(onCredencial);
  const [error, setError] = useState(null);
  callbackRef.current = onCredencial;

  useEffect(() => {
    if (!GOOGLE_CLIENT_ID) return undefined;
    let vivo = true;
    cargarGis()
      .then(() => {
        if (!vivo || !ref.current) return;
        window.google.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          callback: (r) => r?.credential && callbackRef.current?.(r.credential),
          ux_mode: "popup",
          auto_select: false,
          cancel_on_tap_outside: true,
        });
        const ancho = Math.min(360, Math.max(220, ref.current.offsetWidth || 320));
        window.google.accounts.id.renderButton(ref.current, {
          type: "standard", theme: "filled_black", size: "large", shape: "pill", text: texto, width: ancho, locale: "es",
        });
      })
      .catch((e) => vivo && setError(e.message));
    return () => { vivo = false; };
  }, [texto]);

  if (!GOOGLE_CLIENT_ID) return null;
  return (
    <div className="auth-google">
      <div ref={ref} className="auth-google__slot" />
      {error && <p className="auth-msg auth-msg--error">{error}</p>}
    </div>
  );
}
