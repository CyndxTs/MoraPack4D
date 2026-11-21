import React, { lazy, Suspense } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../../auth/AuthContext";
import { RUTA_INICIO_POR_ROL } from "../../../auth/session";
import { SEDES } from "../../../data/redMoraPack";
import "./home.scss";

import limaMora from "../../../assets/images/limaMora.png";
import bruselasMora from "../../../assets/images/bruselasMora.webp";
import arzeMora from "../../../assets/images/arzeMora.webp";
import empresa from "../../../assets/images/empresa.png";

const Globo = lazy(() => import("../../../components/Globo/Globo"));

const CTA_POR_ROL = { CLIENTE: "Ver mis envíos", TRABAJADOR: "Ir a la operación", ADMINISTRADOR: "Ir a parámetros" };
const FOTO_SEDE = { SPIM: limaMora, EBCI: bruselasMora, UBBB: arzeMora };
const PAIS_SEDE = { SPIM: "Perú", EBCI: "Bélgica", UBBB: "Azerbaiyán" };
const CIUDAD_SEDE = { SPIM: "Lima", EBCI: "Bruselas", UBBB: "Bakú" };
const CONTINENTE_SEDE = { SPIM: "América", EBCI: "Europa", UBBB: "Asia" };

const PASOS = [
  ["Registras tu pedido", "Indicas el destino y la cantidad de paquetes. El pedido queda registrado al instante."],
  ["Planificamos la ruta", "El planificador elige vuelos y escalas desde la sede más conveniente, dentro del plazo de entrega."],
  ["Lo sigues en el mapa", "Ves cada vuelo en tiempo real y sabes cuándo tu carga llega a destino."],
];

export default function Home() {
  const { usuario, abrirAuth } = useAuth();

  return (
    <div className="portada">
      <section className="hero" aria-labelledby="hero-titulo">
        <div className="hero__globo">
          <Suspense fallback={null}><Globo /></Suspense>
        </div>
        <div className="hero__contenido container">
          <h1 id="hero-titulo" className="hero__titulo">Tu carga cruza continentes. Tú la ves volar.</h1>
          <p className="hero__texto">
            MoraPack lleva tus paquetes por vía aérea desde Lima, Bruselas y Bakú hacia 30 ciudades de América,
            Europa y Asia, y te muestra cada vuelo en tiempo real.
          </p>
          <div className="hero__acciones">
            {usuario ? (
              <>
                <Link to={RUTA_INICIO_POR_ROL[usuario.tipoUsuario]} className="boton boton--primario boton--grande">{CTA_POR_ROL[usuario.tipoUsuario] ?? "Continuar"}</Link>
                {usuario.tipoUsuario === "CLIENTE" && <Link to="/cliente/pedidos/nuevo" className="boton boton--fantasma boton--grande">Nuevo pedido</Link>}
              </>
            ) : (
              <>
                <button type="button" className="boton boton--primario boton--grande" onClick={() => abrirAuth("registro")}>Crear cuenta</button>
                <button type="button" className="boton boton--fantasma boton--grande" onClick={() => abrirAuth("login")}>Iniciar sesión</button>
              </>
            )}
          </div>
          <dl className="hero__plazos">
            <div><dt>Dentro del mismo continente</dt><dd>hasta 2 días</dd></div>
            <div><dt>Entre continentes</dt><dd>hasta 3 días</dd></div>
          </dl>
        </div>

        <div className="tablero" aria-label="Sedes de MoraPack">
          {SEDES.map((s) => (
            <div className="tablero__fila" key={s.codigo}>
              <span className="tablero__codigo">{s.codigo}</span>
              <span className="tablero__ciudad">{CIUDAD_SEDE[s.codigo]}</span>
              <span className="tablero__estado">Sede {CONTINENTE_SEDE[s.codigo]}</span>
            </div>
          ))}
        </div>
      </section>

      <section className="seccion sedes" id="sedes" aria-labelledby="sedes-titulo">
        <div className="container">
          <div className="seccion__cabecera">
            <h2 id="sedes-titulo">Tres sedes, una sola red</h2>
            <p>Cada sede despacha hacia su continente y conecta con las otras dos para llegar al resto del mundo.</p>
          </div>
          <div className="sedes__grilla">
            {SEDES.map((s, i) => (
              <figure key={s.codigo} className={`sede${i === 0 ? " sede--principal" : ""}`}>
                <img src={FOTO_SEDE[s.codigo]} alt={`Sede de MoraPack en ${CIUDAD_SEDE[s.codigo]}`} loading="lazy" />
                <figcaption>
                  <strong>{CIUDAD_SEDE[s.codigo]}</strong>
                  <span>{PAIS_SEDE[s.codigo]}</span>
                  <span className="sede__codigo">{s.codigo}</span>
                </figcaption>
              </figure>
            ))}
          </div>
        </div>
      </section>

      <section className="seccion proceso" aria-labelledby="proceso-titulo">
        <div className="container">
          <div className="seccion__cabecera">
            <h2 id="proceso-titulo">De tu pedido al aterrizaje</h2>
          </div>
          <ol className="proceso__ruta">
            {PASOS.map(([titulo, texto], i) => (
              <li key={titulo} className="proceso__paso">
                <span className="proceso__nodo" aria-hidden="true">{i + 1}</span>
                <h3>{titulo}</h3>
                <p>{texto}</p>
              </li>
            ))}
          </ol>
          {!usuario && (
            <button type="button" className="boton boton--primario boton--grande proceso__cta" onClick={() => abrirAuth("registro")}>Registrar mi primer pedido</button>
          )}
        </div>
      </section>

      <section className="seccion empresa" id="nosotros" aria-labelledby="empresa-titulo">
        <div className="container empresa__fila">
          <div className="empresa__texto">
            <h2 id="empresa-titulo">Nuestra empresa</h2>
            <p>
              En MoraPack brindamos soluciones logísticas confiables a través de nuestro producto estrella, el MPE. La puntualidad,
              la transparencia y la confianza son la base de cada entrega.
            </p>
            <p>
              Con sedes en Lima, Bruselas y Bakú gestionamos envíos hacia las principales ciudades de América, Asia y Europa.
              Un sistema de monitoreo en tiempo real y un planificador inteligente optimizan las rutas y cuidan los plazos, y nuestra
              alianza con Aerolínea PACK asegura vuelos regulares entre ciudades de distintos continentes.
            </p>
            <p>
              Empezamos con operaciones locales, priorizando la eficiencia en los plazos de entrega, y crecimos hasta convertirnos en
              un socio estratégico en tres continentes.
            </p>
          </div>
          <figure className="empresa__foto">
            <img src={empresa} alt="Equipo de MoraPack preparando envíos" loading="lazy" />
          </figure>
        </div>
      </section>

      <section className="seccion equipo" aria-labelledby="equipo-titulo">
        <div className="container">
          <h2 id="equipo-titulo">Equipo</h2>
          <ul className="equipo__lista">
            <li>César Augusto Napurí de la Cruz</li>
            <li>Josué Manuel Sivincha Bailón</li>
            <li>José Armando Moncada Silva</li>
          </ul>
          <div className="equipo__inversor">
            <span>Inversionista principal</span>
            <strong>Eduardo Valladares</strong>
            <p>Hizo posible MoraPack.</p>
          </div>
        </div>
      </section>
    </div>
  );
}
