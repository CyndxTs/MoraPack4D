import React, { lazy, Suspense, useCallback, useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import * as api from "../../api/auth";
import { useAuth } from "../AuthContext";
import { ROLES, RUTA_INICIO_POR_ROL, puedeEntrar } from "../session";
import { useNotify } from "../../notifications/NotificationContext";
import GoogleButton from "./GoogleButton";
import { GOOGLE_CLIENT_ID } from "../../config";
import CodigoInput from "./CodigoInput";
import { Campo, SelectPais } from "./Campo";
import { LogoMark } from "../../components/Brand/Logo";

const Globo = lazy(() => import("../../components/Globo/Globo"));
import "./authModal.scss";

// Roles que pueden ver una ruta (para decidir si quedarse en la página tras iniciar sesión)
function rolesDeRuta(path) {
  if (path.startsWith("/trabajador")) return [ROLES.TRABAJADOR];
  if (path.startsWith("/cliente")) return [ROLES.CLIENTE];
  if (path.startsWith("/admin")) return [ROLES.ADMINISTRADOR];
  return null; // pública
}

function useCuentaRegresiva(inicial) {
  const [restante, setRestante] = useState(inicial);
  useEffect(() => {
    if (restante <= 0) return undefined;
    const t = setTimeout(() => setRestante((r) => r - 1), 1000);
    return () => clearTimeout(t);
  }, [restante]);
  return [restante, setRestante];
}

const TITULOS = {
  login: ["Bienvenido de vuelta", "Ingresa para continuar con tus envíos y operaciones."],
  registro: ["Crea tu cuenta", "Registra tus datos para enviar y seguir tus paquetes."],
  codigo: ["Revisa tu correo", ""],
  olvido: ["Recupera tu contraseña", "Te enviaremos un código de 6 dígitos a tu correo."],
  google: ["Un último paso", "Es la primera vez que ingresas con Google. Elige tu país para crear tu cuenta."],
};

export default function AuthModal() {
  const { modal, cerrarAuth, abrirAuth, login, completarIngreso } = useAuth();
  if (!modal) return null;
  return <Modal key={modal.vista + (modal.aviso ?? "")} modal={modal} cerrar={cerrarAuth} abrir={abrirAuth} login={login} completarIngreso={completarIngreso} />;
}

function Modal({ modal, cerrar, abrir, login, completarIngreso }) {
  const navigate = useNavigate();
  const location = useLocation();
  const notify = useNotify();
  const [vista, setVista] = useState(modal.vista);
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);
  const [info, setInfo] = useState(modal.aviso ?? null);
  const [paises, setPaises] = useState([]);

  // Formulario compartido entre vistas
  const [correo, setCorreo] = useState(modal.correo ?? "");
  const [contrasenia, setContrasenia] = useState("");
  const [confirmacion, setConfirmacion] = useState("");
  const [nombres, setNombres] = useState("");
  const [apellidos, setApellidos] = useState("");
  const [pais, setPais] = useState("");
  const [codigo, setCodigo] = useState("");
  const [proposito, setProposito] = useState("REGISTRO");
  const [credencialGoogle, setCredencialGoogle] = useState(null);
  const [aceptaTerminos, setAceptaTerminos] = useState(false);
  const [reenvio, setReenvio] = useCuentaRegresiva(0);
  const panelRef = useRef(null);

  const irA = (v) => { setError(null); setInfo(null); setVista(v); };

  // Escape cierra; foco inicial dentro del modal; sin scroll de fondo
  useEffect(() => {
    const onKey = (e) => { if (e.key === "Escape" && !enviando) cerrar(); };
    document.addEventListener("keydown", onKey);
    const overflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => { document.removeEventListener("keydown", onKey); document.body.style.overflow = overflow; };
  }, [cerrar, enviando]);

  // Países (solo cuando se necesitan)
  useEffect(() => {
    if ((vista === "registro" || vista === "google") && paises.length === 0) {
      api.listarPaisesPublico().then((l) => setPaises([...l].sort((a, b) => a.nombre.localeCompare(b.nombre, "es")))).catch(() => {});
    }
  }, [vista, paises.length]);

  // Tras iniciar sesión: se queda en la página si su rol puede verla; si no, a su inicio (registro: área del cliente)
  const alIngresar = useCallback((usuario, mensaje) => {
    cerrar();
    if (mensaje) notify("success", mensaje);
    const destino = modal.desde ?? location.pathname + location.search;
    const roles = rolesDeRuta(destino.split("?")[0]);
    if (roles && puedeEntrar(usuario, roles)) {
      if (destino !== location.pathname + location.search) navigate(destino, { replace: true });
      return;
    }
    navigate(RUTA_INICIO_POR_ROL[usuario.tipoUsuario] ?? "/", { replace: true });
  }, [cerrar, notify, modal.desde, location.pathname, location.search, navigate]);

  const ejecutar = async (fn) => {
    setError(null);
    setEnviando(true);
    try {
      await fn();
    } catch (e) {
      setError(e.message);
    } finally {
      setEnviando(false);
    }
  };

  // ---------- Acciones ----------
  const ingresar = (e) => {
    e.preventDefault();
    ejecutar(async () => {
      const usuario = await login(correo, contrasenia);
      alIngresar(usuario, `Hola, ${usuario.nombres}.`);
    });
  };

  const conGoogle = (credencial, datos = {}) => ejecutar(async () => {
    const r = await api.iniciarSesionGoogle(credencial, datos);
    if (r.requiereDatos) {
      setCredencialGoogle(credencial);
      setCorreo(r.correo ?? r.usuario?.correo ?? "");
      setNombres(r.usuario?.nombres ?? "");
      setAceptaTerminos(false);
      irA("google");
      return;
    }
    const usuario = completarIngreso(r);
    alIngresar(usuario, r.mensaje);
  });

  const completarGoogle = (e) => {
    e.preventDefault();
    conGoogle(credencialGoogle, { pais, aceptaTerminos });
  };

  const registrarse = (e) => {
    e.preventDefault();
    if (contrasenia !== confirmacion) { setError("Las contraseñas no coinciden."); return; }
    ejecutar(async () => {
      const r = await api.registrar({ nombres, apellidos, correo, contrasenia, confirmacion, pais, aceptaTerminos });
      setCorreo(r.correo ?? correo);
      setProposito("REGISTRO");
      setCodigo("");
      setReenvio(r.segundosParaReenvio ?? 120);
      irA("codigo");
    });
  };

  const pedirRecuperacion = (e) => {
    e.preventDefault();
    ejecutar(async () => {
      const r = await api.olvideContrasenia(correo);
      setCorreo(r.correo ?? correo);
      setProposito("RECUPERACION");
      setCodigo("");
      setContrasenia("");
      setConfirmacion("");
      setReenvio(r.segundosParaReenvio ?? 120);
      irA("codigo");
    });
  };

  const confirmarCodigo = (e) => {
    e?.preventDefault();
    if (codigo.length !== 6) { setError("Ingresa los 6 dígitos del código."); return; }
    if (proposito === "RECUPERACION" && contrasenia !== confirmacion) { setError("Las contraseñas no coinciden."); return; }
    ejecutar(async () => {
      const r = proposito === "REGISTRO"
        ? await api.verificarRegistro(correo, codigo)
        : await api.restablecerContrasenia({ correo, codigo, contrasenia, confirmacion });
      const usuario = completarIngreso(r);
      alIngresar(usuario, r.mensaje);
    });
  };

  const reenviar = () => ejecutar(async () => {
    const r = await api.reenviarCodigo(correo, proposito);
    setReenvio(r.segundosParaReenvio ?? 120);
    setCodigo("");
    setInfo(r.mensaje);
  });

  // Registro: el código se envía solo al completar los 6 dígitos
  useEffect(() => {
    if (vista === "codigo" && proposito === "REGISTRO" && codigo.length === 6 && !enviando) confirmarCodigo();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [codigo]);

  const [titulo, subtitulo] = TITULOS[vista];
  const subtituloCodigo = proposito === "REGISTRO"
    ? <>Enviamos un código de 6 dígitos a <strong>{correo}</strong>. Ingrésalo para crear tu cuenta.</>
    : <>Si <strong>{correo}</strong> tiene una cuenta, recibirás un código. Ingrésalo junto con tu nueva contraseña.</>;

  const registroIncompleto = !nombres.trim() || !correo.trim() || !pais || contrasenia.length < 8 || !confirmacion || !aceptaTerminos;

  const terminos = (
    <label className="auth-check">
      <input type="checkbox" checked={aceptaTerminos} onChange={(e) => setAceptaTerminos(e.target.checked)} disabled={enviando} />
      <span>Acepto los <a href="/terminos" target="_blank" rel="noreferrer">Términos y condiciones</a> y la <a href="/privacidad" target="_blank" rel="noreferrer">Política de privacidad</a>.</span>
    </label>
  );

  return (
    <div className="auth-overlay" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget && !enviando) cerrar(); }}>
      {location.pathname !== "/" && (
        <div className="auth-globo" aria-hidden="true"><Suspense fallback={null}><Globo /></Suspense></div>
      )}
      <div className="auth-panel" role="dialog" aria-modal="true" aria-labelledby="auth-titulo" ref={panelRef}>
        <button type="button" className="auth-cerrar" onClick={cerrar} aria-label="Cerrar" disabled={enviando}>✕</button>

        <header className="auth-header">
          <LogoMark size={46} className="auth-logo" />
          <h2 id="auth-titulo">{titulo}</h2>
          <p>{vista === "codigo" ? subtituloCodigo : subtitulo}</p>
        </header>

        {info && <p className="auth-msg auth-msg--info">{info}</p>}

        <div className="auth-vista" key={vista}>
          {vista === "login" && (
            <form onSubmit={ingresar} noValidate>
              <Campo label="Correo" type="email" value={correo} onChange={setCorreo} autoComplete="username" autoFocus disabled={enviando} />
              <Campo label="Contraseña" type="password" value={contrasenia} onChange={setContrasenia} autoComplete="current-password" disabled={enviando} />
              <button type="button" className="auth-link auth-link--derecha" onClick={() => irA("olvido")}>¿Olvidaste tu contraseña?</button>
              {error && <p className="auth-msg auth-msg--error" role="alert">{error}</p>}
              <button type="submit" className="auth-primario" disabled={enviando || !correo.trim() || !contrasenia}>
                {enviando ? "Ingresando..." : "Ingresar"}
              </button>
              {GOOGLE_CLIENT_ID && <div className="auth-separador"><span>o</span></div>}
              <GoogleButton onCredencial={(c) => conGoogle(c)} />
              <p className="auth-pie">¿No tienes cuenta? <button type="button" className="auth-link" onClick={() => irA("registro")}>Regístrate</button></p>
            </form>
          )}

          {vista === "registro" && (
            <form onSubmit={registrarse} noValidate>
              <div className="auth-fila">
                <Campo label="Nombres" value={nombres} onChange={setNombres} autoComplete="given-name" autoFocus disabled={enviando} />
                <Campo label="Apellidos (opcional)" value={apellidos} onChange={setApellidos} autoComplete="family-name" disabled={enviando} />
              </div>
              <Campo label="Correo" type="email" value={correo} onChange={setCorreo} autoComplete="email" disabled={enviando} />
              <SelectPais value={pais} onChange={setPais} paises={paises} disabled={enviando} />
              <div className="auth-fila">
                <Campo label="Contraseña" type="password" value={contrasenia} onChange={setContrasenia} autoComplete="new-password" disabled={enviando} hint="Mínimo 8 caracteres" />
                <Campo label="Repite la contraseña" type="password" value={confirmacion} onChange={setConfirmacion} autoComplete="new-password" disabled={enviando} />
              </div>
              {terminos}
              {error && <p className="auth-msg auth-msg--error" role="alert">{error}</p>}
              <button type="submit" className="auth-primario" disabled={enviando || registroIncompleto}>
                {enviando ? "Enviando código..." : "Crear cuenta"}
              </button>
              {GOOGLE_CLIENT_ID && <div className="auth-separador"><span>o</span></div>}
              <GoogleButton texto="signup_with" onCredencial={(c) => conGoogle(c)} />
              <p className="auth-pie">¿Ya tienes cuenta? <button type="button" className="auth-link" onClick={() => irA("login")}>Inicia sesión</button></p>
            </form>
          )}

          {vista === "codigo" && (
            <form onSubmit={confirmarCodigo} noValidate>
              <CodigoInput value={codigo} onChange={setCodigo} disabled={enviando} />
              {proposito === "RECUPERACION" && (
                <div className="auth-fila">
                  <Campo label="Nueva contraseña" type="password" value={contrasenia} onChange={setContrasenia} autoComplete="new-password" disabled={enviando} hint="Mínimo 8 caracteres" />
                  <Campo label="Repite la contraseña" type="password" value={confirmacion} onChange={setConfirmacion} autoComplete="new-password" disabled={enviando} />
                </div>
              )}
              {error && <p className="auth-msg auth-msg--error" role="alert">{error}</p>}
              <button type="submit" className="auth-primario" disabled={enviando || codigo.length !== 6 || (proposito === "RECUPERACION" && (contrasenia.length < 8 || !confirmacion))}>
                {enviando ? "Verificando..." : proposito === "REGISTRO" ? "Verificar y crear cuenta" : "Cambiar contraseña"}
              </button>
              <p className="auth-pie">
                ¿No te llegó?{" "}
                {reenvio > 0
                  ? <span className="auth-espera">Podrás reenviarlo en {Math.floor(reenvio / 60)}:{String(reenvio % 60).padStart(2, "0")}</span>
                  : <button type="button" className="auth-link" onClick={reenviar} disabled={enviando}>Reenviar código</button>}
              </p>
              <p className="auth-pie"><button type="button" className="auth-link auth-link--tenue" onClick={() => irA(proposito === "REGISTRO" ? "registro" : "olvido")}>← Cambiar correo</button></p>
            </form>
          )}

          {vista === "olvido" && (
            <form onSubmit={pedirRecuperacion} noValidate>
              <Campo label="Correo" type="email" value={correo} onChange={setCorreo} autoComplete="email" autoFocus disabled={enviando} />
              {error && <p className="auth-msg auth-msg--error" role="alert">{error}</p>}
              <button type="submit" className="auth-primario" disabled={enviando || !correo.trim()}>
                {enviando ? "Enviando..." : "Enviar código"}
              </button>
              <p className="auth-pie"><button type="button" className="auth-link" onClick={() => irA("login")}>← Volver a iniciar sesión</button></p>
            </form>
          )}

          {vista === "google" && (
            <form onSubmit={completarGoogle} noValidate>
              <div className="auth-cuenta-google">
                <span className="auth-cuenta-google__avatar" aria-hidden="true">{(nombres?.[0] ?? correo?.[0] ?? "?").toUpperCase()}</span>
                <div><strong>{nombres}</strong><small>{correo}</small></div>
              </div>
              <SelectPais value={pais} onChange={setPais} paises={paises} disabled={enviando} />
              {terminos}
              {error && <p className="auth-msg auth-msg--error" role="alert">{error}</p>}
              <button type="submit" className="auth-primario" disabled={enviando || !pais || !aceptaTerminos}>
                {enviando ? "Creando cuenta..." : "Crear mi cuenta"}
              </button>
              <p className="auth-pie"><button type="button" className="auth-link auth-link--tenue" onClick={() => abrir("login")}>Cancelar</button></p>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
