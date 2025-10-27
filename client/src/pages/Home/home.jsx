import React from 'react'
import './home.scss'

import moraBanner from '../../assets/images/moraBanner.png'
import limaMora from '../../assets/images/limaMora.png'
import bruselasMora from '../../assets/images/bruselasMora.webp'
import arzeMora from '../../assets/images/arzeMora.webp'
import empresa from '../../assets/images/empresa.png'

export default function Home() {
  return (
    <div className="home-page">
      <figure id="main-banner">
        <img src={moraBanner} alt="Banner MoraPack" className="img-fluid" />
      </figure>

      <section id="nosotros" className="padded">
        <div className="container">
          <h2>Nosotros</h2>
          <p>
            En MoraPack nos dedicamos a brindar soluciones logísticas innovadoras y confiables a través de nuestro producto estrella, el MPE.
            Creemos que la puntualidad, la transparencia y la confianza son la base de cada entrega. Por eso, trabajamos día a día para ofrecer
            a nuestros clientes un servicio eficiente, seguro y adaptable a sus necesidades.
          </p>
        </div>
      </section>

      <aside id="impacto" className="padded">
        <div className="container">
          <p className="display-1">
            La innovación logística,<br />
            <span className="texto-importante">MoraPack</span>, conecta continentes<br />
            con su red global.<br />
            Garantizamos <br />
            <span className="texto-importante">entregas seguras y a tiempo</span><br />
            con tecnología y confianza.
          </p>
        </div>
      </aside>

      <section id="sedes" className="padded">
        <div className="container">
          <h2>Sedes</h2>
          <div className="row">
            <article className="col">
              <figure>
                <img src={limaMora} alt="Lima" className="img-fluid" />
              </figure>
              <h3>Lima (Perú)</h3>
            </article>
            <article className="col">
              <figure>
                <img src={bruselasMora} alt="Bruselas" className="img-fluid" />
              </figure>
              <h3>Bruselas (Bélgica)</h3>
            </article>
            <article className="col">
              <figure>
                <img src={arzeMora} alt="Bakú" className="img-fluid" />
              </figure>
              <h3>Bakú (Azerbaiyán)</h3>
            </article>
          </div>
        </div>
      </section>

      <section id="empresa">
        <div className="container">
          <div className="row">
            <div className="col centrado-vertical">
              <h2>Nuestra empresa</h2>
              <p>
                MoraPack es una empresa con presencia internacional, con sedes en Lima (Perú), Bruselas (Bélgica) y Bakú (Azerbaiyán). 
                Nuestra red global nos permite gestionar envíos hacia las principales ciudades de América, Asia y Europa. 
                Contamos con un sistema de monitoreo en tiempo real y un planificador inteligente que optimiza rutas y garantiza 
                el cumplimiento de los plazos de entrega. Nuestra alianza con Aerolínea PACK refuerza nuestra capacidad de transporte 
                y asegura la continuidad de las operaciones con vuelos regulares entre ciudades de distintos continentes.
              </p>
            </div>
            <figure className="col">
              <img src={empresa} alt="Nuestra empresa" className="img-fluid" />
            </figure>
          </div>
        </div>
      </section>

      <section id="historia" className="padded">
        <div className="container">
          <h2>Historia</h2>
          <p>
            MoraPack nació con el objetivo de revolucionar la forma en que los productos son enviados y monitoreados en mercados internacionales. 
            Empezamos con operaciones locales, priorizando siempre la eficiencia en los plazos de entrega. Gracias al éxito alcanzado, 
            expandimos nuestras operaciones a otros continentes, consolidándonos como un socio estratégico para nuestros clientes. 
            Hoy, MoraPack se distingue por su crecimiento constante, la innovación en sus procesos y la implementación de soluciones digitales 
            que hacen más sencilla y confiable la experiencia logística.
          </p>
        </div>
      </section>

      <section id="equipo" className="padded">
        <div className="container">
          <h2>Nuestro equipo</h2>
          <div className="row">
            <div className="col"><h3>César Augusto Napurí de la Cruz</h3></div>
            <div className="col"><h3>Josué Manuel Sivincha Bailón</h3></div>
            <div className="col"><h3>José Armando Moncada Silva</h3></div>
          </div>
        </div>
      </section>
    </div>
  )
}
