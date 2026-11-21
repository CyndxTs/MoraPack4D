import React, { useEffect, useState } from "react";
import { useAuth } from "../../../auth/AuthContext";
import { ETIQUETA_ROL } from "../../../auth/session";
import * as api from "../../../api/auth";
import { useNotify } from "../../../notifications/NotificationContext";
import { Campo, SelectPais } from "../../../auth/modal/Campo";
import "../../../auth/modal/authModal.scss";
import "./miCuenta.scss";

// Datos personales y contraseña. Con contraseña se exige la actual; una cuenta de Google puede crear una sin ella.
export default function MiCuenta() {
  const { usuario, actualizarUsuario } = useAuth();
  const notify = useNotify();
  const [cuenta, setCuenta] = useState(usuario);
  const [paises, setPaises] = useState([]);
  const [perfil, setPerfil] = useState({ nombres: usuario?.nombres ?? "", apellidos: usuario?.apellidos ?? "", pais: usuario?.pais ?? "" });
  const ap = (u) => u?.apellidos ?? "";
  const [clave, setClave] = useState({ contraseniaActual: "", contrasenia: "", confirmacion: "" });
  const [guardando, setGuardando] = useState(null);
  const [errorPerfil, setErrorPerfil] = useState(null);
  const [errorClave, setErrorClave] = useState(null);

  useEffect(() => {
    api.obtenerMiCuenta().then((r) => {
      setCuenta(r.usuario);
      setPerfil({ nombres: r.usuario.nombres, apellidos: ap(r.usuario), pais: r.usuario.pais ?? "" });
      actualizarUsuario(r.usuario);
    }).catch(() => {});
    api.listarPaisesPublico().then((l) => setPaises([...l].sort((a, b) => a.nombre.localeCompare(b.nombre, "es")))).catch(() => {});
  }, [actualizarUsuario]);

  const tieneContrasenia = cuenta?.tieneContrasenia !== false;
  const cambiosPerfil = cuenta && (perfil.nombres !== cuenta.nombres || perfil.apellidos !== ap(cuenta) || perfil.pais !== (cuenta.pais ?? ""));

  const guardarPerfil = async (e) => {
    e.preventDefault();
    setErrorPerfil(null);
    setGuardando("perfil");
    try {
      const r = await api.actualizarPerfil(perfil);
      setCuenta(r.usuario);
      actualizarUsuario(r.usuario);
      notify("success", r.mensaje);
    } catch (err) {
      setErrorPerfil(err.message);
    } finally {
      setGuardando(null);
    }
  };

  const guardarClave = async (e) => {
    e.preventDefault();
    setErrorClave(null);
    if (clave.contrasenia !== clave.confirmacion) { setErrorClave("Las contraseñas no coinciden."); return; }
    setGuardando("clave");
    try {
      const r = await api.cambiarContrasenia(tieneContrasenia ? clave : { ...clave, contraseniaActual: null });
      setCuenta(r.usuario);
      actualizarUsuario(r.usuario);
      setClave({ contraseniaActual: "", contrasenia: "", confirmacion: "" });
      notify("success", r.mensaje);
    } catch (err) {
      setErrorClave(err.message);
    } finally {
      setGuardando(null);
    }
  };

  if (!cuenta) return null;

  return (
    <div className="mi-cuenta">
      <header className="mi-cuenta__cabecera">
        <div className="mi-cuenta__avatar" aria-hidden="true">{(cuenta.nombres?.[0] ?? "") + (cuenta.apellidos?.[0] ?? "")}</div>
        <div>
          <h1>{cuenta.nombres} {ap(cuenta)}</h1>
          <p>{cuenta.correo} · <span className="mi-cuenta__rol">{ETIQUETA_ROL[cuenta.tipoUsuario]}</span> · Código <strong>{cuenta.codigo}</strong></p>
        </div>
      </header>

      <div className="mi-cuenta__grid">
        <form className="mi-cuenta__tarjeta" onSubmit={guardarPerfil} noValidate>
          <h2>Datos personales</h2>
          <div className="auth-fila">
            <Campo label="Nombres" value={perfil.nombres} onChange={(v) => setPerfil((p) => ({ ...p, nombres: v }))} autoComplete="given-name" disabled={guardando === "perfil"} />
            <Campo label="Apellidos (opcional)" value={perfil.apellidos} onChange={(v) => setPerfil((p) => ({ ...p, apellidos: v }))} autoComplete="family-name" disabled={guardando === "perfil"} />
          </div>
          <Campo label="Correo" type="email" value={cuenta.correo} onChange={() => {}} disabled hint="El correo no se puede cambiar." />
          <SelectPais value={perfil.pais} onChange={(v) => setPerfil((p) => ({ ...p, pais: v }))} paises={paises} disabled={guardando === "perfil"} />
          {errorPerfil && <p className="auth-msg auth-msg--error" role="alert">{errorPerfil}</p>}
          <button type="submit" className="auth-primario" disabled={!cambiosPerfil || guardando === "perfil" || !perfil.nombres.trim() || !perfil.pais}>
            {guardando === "perfil" ? "Guardando..." : "Guardar cambios"}
          </button>
        </form>

        <form className="mi-cuenta__tarjeta" onSubmit={guardarClave} noValidate>
          <h2>{tieneContrasenia ? "Cambiar contraseña" : "Crear contraseña"}</h2>
          {!tieneContrasenia && <p className="mi-cuenta__nota">Tu cuenta usa Google. Crea una contraseña para ingresar también con tu correo.</p>}
          {tieneContrasenia && (
            <Campo label="Contraseña actual" type="password" value={clave.contraseniaActual} onChange={(v) => setClave((c) => ({ ...c, contraseniaActual: v }))} autoComplete="current-password" disabled={guardando === "clave"} />
          )}
          <Campo label="Nueva contraseña" type="password" value={clave.contrasenia} onChange={(v) => setClave((c) => ({ ...c, contrasenia: v }))} autoComplete="new-password" disabled={guardando === "clave"} hint="Mínimo 8 caracteres" />
          <Campo label="Repite la nueva contraseña" type="password" value={clave.confirmacion} onChange={(v) => setClave((c) => ({ ...c, confirmacion: v }))} autoComplete="new-password" disabled={guardando === "clave"} />
          {errorClave && <p className="auth-msg auth-msg--error" role="alert">{errorClave}</p>}
          <button type="submit" className="auth-primario" disabled={guardando === "clave" || clave.contrasenia.length < 8 || !clave.confirmacion || (tieneContrasenia && !clave.contraseniaActual)}>
            {guardando === "clave" ? "Guardando..." : tieneContrasenia ? "Cambiar contraseña" : "Crear contraseña"}
          </button>
          {tieneContrasenia && <p className="mi-cuenta__nota">Al cambiarla se cierran tus sesiones en otros dispositivos.</p>}
        </form>
      </div>
    </div>
  );
}
