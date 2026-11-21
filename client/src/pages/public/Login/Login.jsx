import React, { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../auth/AuthContext";
import { ROLES, RUTA_INICIO_POR_ROL } from "../../../auth/session";
import logo from "../../../assets/images/mora.png";
import "./login.scss";

const TIPOS = [
  { value: ROLES.CLIENTE, label: "Cliente" },
  { value: ROLES.TRABAJADOR, label: "Trabajador" },
  { value: ROLES.ADMINISTRADOR, label: "Administrador" },
];

export default function Login() {
  const { usuario, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [tipo, setTipo] = useState(ROLES.CLIENTE);
  const [correo, setCorreo] = useState("");
  const [contrasenia, setContrasenia] = useState("");
  const [error, setError] = useState(null);
  const [enviando, setEnviando] = useState(false);

  // Ya hay una cuenta abierta en este navegador
  if (usuario) return <Navigate to={RUTA_INICIO_POR_ROL[usuario.tipoUsuario] ?? "/"} replace />;

  const enviar = async (e) => {
    e.preventDefault();
    setError(null);
    setEnviando(true);
    try {
      const u = await login(correo, contrasenia, tipo);
      const desde = location.state?.desde;
      const inicio = RUTA_INICIO_POR_ROL[u.tipoUsuario];
      navigate(desde && desde.startsWith(inicio) ? desde : inicio, { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="login-page">
      <form className="login-card" onSubmit={enviar}>
        <img src={logo} alt="MoraPack" className="login-logo" />
        <h1>Iniciar sesión</h1>

        <div className="login-tipos" role="tablist">
          {TIPOS.map((t) => (
            <button key={t.value} type="button" className={tipo === t.value ? "active" : ""} onClick={() => setTipo(t.value)}>
              {t.label}
            </button>
          ))}
        </div>

        <label htmlFor="correo">Correo</label>
        <input id="correo" type="email" autoComplete="username" value={correo} onChange={(e) => setCorreo(e.target.value)} required />

        <label htmlFor="contrasenia">Contraseña</label>
        <input id="contrasenia" type="password" autoComplete="current-password" value={contrasenia} onChange={(e) => setContrasenia(e.target.value)} required />

        {error && <p className="login-error">{error}</p>}

        <button type="submit" className="login-submit" disabled={enviando}>{enviando ? "Ingresando..." : "Entrar"}</button>
        <Link to="/" className="login-back">← Volver a MoraPack</Link>
      </form>
    </div>
  );
}
