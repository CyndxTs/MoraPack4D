import React, { useCallback, useEffect, useMemo, useState } from "react";
import {
  BarraFiltros, Campo, Confirmacion, Etiqueta, IconoMas, Modal, PaginaGestion, Paginador, Segmentado, Selector, Tabla,
} from "../../components/Gestion/Gestion";
import { cambiarEstadoUsuario, cambiarRolUsuario, crearUsuario, eliminarUsuario, filtrarUsuarios, listarPaises } from "../../api/catalogos";
import { useAuth } from "../../auth/AuthContext";
import { ETIQUETA_ROL, ROLES } from "../../auth/session";
import { useNotify } from "../../notifications/NotificationContext";
import { TAMANIO_PAGINA } from "../../config";

const VACIOS = { correo: "", tipoUsuario: "", estado: "", pais: "" };
const TONO_ROL = { ADMINISTRADOR: "ambar", TRABAJADOR: "info", CLIENTE: "neutro" };
const ESTADO = { ONLINE: ["ok", "En línea"], OFFLINE: ["neutro", "Desconectado"], DISABLED: ["alerta", "Deshabilitado"] };
const PREFIJO = { CLIENTE: "0000001", TRABAJADOR: "TRA0001", ADMINISTRADOR: "ADM0001" };

export default function Usuarios() {
  const notify = useNotify();
  const { usuario: yo } = useAuth();
  const [filtros, setFiltros] = useState(VACIOS);
  const [aplicados, setAplicados] = useState(VACIOS);
  const [panel, setPanel] = useState(false);
  const [pagina, setPagina] = useState(1);
  const [usuarios, setUsuarios] = useState([]);
  const [hayMas, setHayMas] = useState(false);
  const [cargando, setCargando] = useState(true);
  const [modal, setModal] = useState(null); // "crear" | { rol } | { estado } | { eliminar }

  const cargar = useCallback(async (pag, f) => {
    setCargando(true);
    try {
      const modelo = { correo: f.correo.trim() || null, tipoUsuario: f.tipoUsuario || null, estado: f.estado || null, pais: f.pais.trim() || null };
      const lista = await filtrarUsuarios(modelo, pag - 1, TAMANIO_PAGINA);
      setUsuarios(lista); setPagina(pag); setHayMas(lista.length === TAMANIO_PAGINA);
    } catch (e) { notify("danger", e.message); } finally { setCargando(false); }
  }, [notify]);
  useEffect(() => { cargar(1, VACIOS); }, [cargar]);

  const aplicar = (f = filtros) => { setAplicados(f); setFiltros(f); setPanel(false); cargar(1, f); };
  const activos = [];
  if (aplicados.correo.trim()) activos.push({ clave: "correo", texto: `Correo contiene "${aplicados.correo.trim()}"` });
  if (aplicados.tipoUsuario) activos.push({ clave: "tipoUsuario", texto: ETIQUETA_ROL[aplicados.tipoUsuario] });
  if (aplicados.estado) activos.push({ clave: "estado", texto: ESTADO[aplicados.estado][1] });
  if (aplicados.pais.trim()) activos.push({ clave: "pais", texto: `País ${aplicados.pais.trim()}` });

  const esYo = (u) => u.codigo === yo?.codigo;
  const columnas = [
    { label: "Código", key: "codigo", render: (u) => <span className="codigo">{u.codigo}</span> },
    { label: "Nombre", key: "nombres", render: (u) => <>{u.nombres} {u.apellidos ?? ""}{esYo(u) && <span className="tenue"> (tú)</span>}</> },
    { label: "Correo", key: "correo" },
    { label: "País", key: "pais" },
    { label: "Rol", key: "tipoUsuario", render: (u) => <Etiqueta tono={TONO_ROL[u.tipoUsuario]}>{ETIQUETA_ROL[u.tipoUsuario]}</Etiqueta> },
    { label: "Estado", key: "estado", render: (u) => { const [t, l] = ESTADO[u.estado] ?? ["neutro", u.estado]; return <Etiqueta tono={t}>{l}</Etiqueta>; } },
    { label: "Acceso", key: "acceso", render: (u) => <span className="tenue">{u.tieneContrasenia ? "Contraseña" : "Solo Google"}</span> },
    {
      label: "", key: "acciones", alinear: "derecha",
      render: (u) => (esYo(u) ? null : (
        <>
          <button type="button" className="accion-tabla" onClick={() => setModal({ rol: u })}>Cambiar rol</button>
          <button type="button" className="accion-tabla" onClick={() => setModal({ estado: u })}>{u.estado === "DISABLED" ? "Habilitar" : "Deshabilitar"}</button>
          <button type="button" className="accion-tabla accion-tabla--peligro" onClick={() => setModal({ eliminar: u })}>Eliminar</button>
        </>
      )),
    },
  ];

  const recargar = () => { setModal(null); cargar(pagina, aplicados); };

  return (
    <PaginaGestion
      titulo="Usuarios y roles"
      descripcion="Los clientes se registran solos. Aquí se crean trabajadores y administradores, y se cambian roles. Al cambiar de rol, el usuario recibe el siguiente código libre de su nuevo rol."
      acciones={<button type="button" className="boton boton--primario" onClick={() => setModal("crear")}><IconoMas /> Crear usuario</button>}
    >
      <BarraFiltros
        busqueda={filtros.correo}
        onBusqueda={(v) => setFiltros((f) => ({ ...f, correo: v }))}
        placeholder="Buscar por correo (Enter)"
        abierto={panel}
        onAlternar={() => setPanel((v) => !v)}
        activos={activos}
        onQuitar={(k) => aplicar({ ...aplicados, [k]: VACIOS[k] })}
        onLimpiar={() => aplicar(VACIOS)}
        onAplicar={() => aplicar()}
      >
        <Campo label="Rol">
          <Segmentado nombre="Rol" value={filtros.tipoUsuario} onChange={(v) => setFiltros({ ...filtros, tipoUsuario: v })}
            opciones={[{ value: "", label: "Todos" }, { value: ROLES.CLIENTE, label: "Clientes" }, { value: ROLES.TRABAJADOR, label: "Trabajadores" }, { value: ROLES.ADMINISTRADOR, label: "Admins" }]} />
        </Campo>
        <Campo label="Estado">
          <select value={filtros.estado} onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })}>
            <option value="">Todos</option><option value="ONLINE">En línea</option><option value="OFFLINE">Desconectado</option><option value="DISABLED">Deshabilitado</option>
          </select>
        </Campo>
        <Campo label="País"><input value={filtros.pais} onChange={(e) => setFiltros({ ...filtros, pais: e.target.value })} placeholder="Contiene" /></Campo>
      </BarraFiltros>

      <Tabla columnas={columnas} filas={usuarios} cargando={cargando} vacio="No hay usuarios con estos filtros." />
      <Paginador pagina={pagina} hayMas={hayMas} onCambiar={(p) => cargar(p, aplicados)} />

      {modal === "crear" && <CrearUsuario onCerrar={() => setModal(null)} onListo={recargar} />}
      {modal?.rol && <CambiarRol usuario={modal.rol} onCerrar={() => setModal(null)} onListo={recargar} />}
      {modal?.estado && <AccionConfirmada
        titulo={modal.estado.estado === "DISABLED" ? `Habilitar a ${modal.estado.nombres}` : `Deshabilitar a ${modal.estado.nombres}`}
        mensaje={modal.estado.estado === "DISABLED"
          ? `${modal.estado.correo} podrá volver a iniciar sesión.`
          : `${modal.estado.correo} no podrá iniciar sesión y se cerrarán todas sus sesiones abiertas.`}
        accion={modal.estado.estado === "DISABLED" ? "Habilitar" : "Deshabilitar"}
        peligro={modal.estado.estado !== "DISABLED"}
        ejecutar={() => cambiarEstadoUsuario(modal.estado.codigo, modal.estado.estado === "DISABLED" ? "OFFLINE" : "DISABLED")}
        onCerrar={() => setModal(null)} onListo={recargar} />}
      {modal?.eliminar && <AccionConfirmada
        titulo={`Eliminar a ${modal.eliminar.nombres}`}
        mensaje={`Se eliminará la cuenta ${modal.eliminar.correo} (${modal.eliminar.codigo}). Si tiene pedidos no se podrá eliminar: deshabilítala en su lugar. Esta acción no se puede deshacer.`}
        accion="Eliminar usuario" peligro
        ejecutar={() => eliminarUsuario(modal.eliminar.codigo)}
        onCerrar={() => setModal(null)} onListo={recargar} />}
    </PaginaGestion>
  );
}

function AccionConfirmada({ ejecutar, onListo, ...props }) {
  const notify = useNotify();
  const [enviando, setEnviando] = useState(false);
  return (
    <Confirmacion {...props} enviando={enviando} onConfirmar={async () => {
      setEnviando(true);
      try { const r = await ejecutar(); notify("success", r.mensaje); onListo(); } catch (e) { notify("danger", e.message); setEnviando(false); }
    }} />
  );
}

function CambiarRol({ usuario, onCerrar, onListo }) {
  const [rol, setRol] = useState(usuario.tipoUsuario);
  const [confirmar, setConfirmar] = useState(false);
  if (confirmar) {
    return (
      <AccionConfirmada
        titulo={`Cambiar el rol de ${usuario.nombres}`}
        mensaje={`${usuario.correo} pasará de ${ETIQUETA_ROL[usuario.tipoUsuario]} a ${ETIQUETA_ROL[rol]}. Su código ${usuario.codigo} se reemplaza por el siguiente libre del nuevo rol (formato ${PREFIJO[rol]}). Sus sesiones abiertas reciben el nuevo rol en unos segundos.`}
        accion="Cambiar rol" peligro={usuario.tipoUsuario === ROLES.ADMINISTRADOR}
        ejecutar={() => cambiarRolUsuario(usuario.codigo, rol)}
        onCerrar={onCerrar} onListo={onListo} />
    );
  }
  return (
    <Modal titulo={`Rol de ${usuario.nombres}`} descripcion={`${usuario.correo} · ${usuario.codigo}`} onCerrar={onCerrar} ancho="angosto"
      pie={<>
        <button type="button" className="boton boton--fantasma" onClick={onCerrar}>Cancelar</button>
        <button type="button" className="boton boton--primario" disabled={rol === usuario.tipoUsuario} onClick={() => setConfirmar(true)}>Continuar</button>
      </>}>
      <Segmentado nombre="Rol" value={rol} onChange={setRol}
        opciones={[{ value: ROLES.CLIENTE, label: "Cliente" }, { value: ROLES.TRABAJADOR, label: "Trabajador" }, { value: ROLES.ADMINISTRADOR, label: "Administrador" }]} />
      <p className="modal2__texto">
        {rol === ROLES.CLIENTE && "Envía pedidos y sigue sus envíos."}
        {rol === ROLES.TRABAJADOR && "Opera la red: operación en tiempo real, simulaciones, pedidos, planes y eventos."}
        {rol === ROLES.ADMINISTRADOR && "Administra el sistema: parámetros, países, aeropuertos, sedes y usuarios."}
      </p>
    </Modal>
  );
}

function CrearUsuario({ onCerrar, onListo }) {
  const notify = useNotify();
  const [paises, setPaises] = useState([]);
  const [f, setF] = useState({ nombres: "", apellidos: "", correo: "", pais: null, rol: ROLES.TRABAJADOR, contrasenia: "", confirmacion: "" });
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState(null);
  const set = (k) => (e) => setF({ ...f, [k]: e?.target ? e.target.value : e });
  useEffect(() => { listarPaises().then(setPaises).catch(() => {}); }, []);
  const opciones = useMemo(() => [...paises].sort((a, b) => a.nombre.localeCompare(b.nombre, "es")).map((p) => ({ value: p.nombre, label: p.nombre, detalle: p.continente })), [paises]);

  const crear = async () => {
    if (!f.nombres.trim() || !f.correo.trim() || !f.pais) return setError("Completa nombres, correo y país.");
    if (f.contrasenia.length < 8) return setError("La contraseña inicial debe tener al menos 8 caracteres.");
    if (f.contrasenia !== f.confirmacion) return setError("Las contraseñas no coinciden.");
    setEnviando(true);
    setError(null);
    try {
      const r = await crearUsuario(f);
      notify("success", r.mensaje);
      onListo();
    } catch (e) {
      setError(e.message);
      setEnviando(false);
    }
  };

  return (
    <Modal titulo="Crear usuario" descripcion="El usuario podrá cambiar su contraseña en Mi cuenta o con “¿Olvidaste tu contraseña?”." onCerrar={onCerrar} bloqueado={enviando} ancho="ancho"
      pie={<>
        <button type="button" className="boton boton--fantasma" onClick={onCerrar} disabled={enviando}>Cancelar</button>
        <button type="button" className="boton boton--primario" onClick={crear} disabled={enviando}>{enviando ? "Creando..." : "Crear usuario"}</button>
      </>}>
      <Segmentado nombre="Rol" value={f.rol} onChange={set("rol")}
        opciones={[{ value: ROLES.TRABAJADOR, label: "Trabajador" }, { value: ROLES.ADMINISTRADOR, label: "Administrador" }, { value: ROLES.CLIENTE, label: "Cliente" }]} />
      <div className="campos">
        <Campo req label="Nombres"><input value={f.nombres} onChange={set("nombres")} autoComplete="off" /></Campo>
        <Campo opc label="Apellidos"><input value={f.apellidos} onChange={set("apellidos")} autoComplete="off" /></Campo>
        <Campo req label="Correo"><input type="email" value={f.correo} onChange={set("correo")} autoComplete="off" /></Campo>
        <Campo req label="País"><Selector opciones={opciones} value={f.pais} onChange={set("pais")} placeholder="Busca el país" /></Campo>
        <Campo req label="Contraseña inicial" hint="Mínimo 8 caracteres"><input type="password" value={f.contrasenia} onChange={set("contrasenia")} autoComplete="new-password" /></Campo>
        <Campo req label="Repite la contraseña"><input type="password" value={f.confirmacion} onChange={set("confirmacion")} autoComplete="new-password" /></Campo>
      </div>
      <p className="modal2__nota">Recibirá el siguiente código libre de su rol ({PREFIJO[f.rol].replace(/1$/, "…")}).</p>
      {error && <p className="modal2__error" role="alert">{error}</p>}
    </Modal>
  );
}
