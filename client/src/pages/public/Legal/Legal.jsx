import React from "react";
import "./legal.scss";

const ACTUALIZADO = "noviembre de 2026";

const CONTENIDO = {
  privacidad: {
    titulo: "Política de privacidad",
    secciones: [
      ["Quiénes somos", "MoraPack4D es un sistema académico de planificación de envíos de paquetes por vía aérea desarrollado por el Grupo 4D. Esta política explica qué datos personales tratamos y para qué."],
      ["Datos que recopilamos", "Al crear una cuenta guardamos tus nombres, apellidos, correo electrónico, país y, si la defines, tu contraseña (solo como hash BCrypt; nunca en texto plano). Si ingresas con Google recibimos tu correo, nombres y apellidos verificados por Google; no recibimos tu contraseña de Google. También guardamos los pedidos que registres y datos técnicos de cada sesión (fecha de inicio y el navegador o dispositivo)."],
      ["Para qué los usamos", "Para identificarte, mantener tu sesión, enviarte códigos de verificación por correo, gestionar y mostrar el estado de tus envíos, y operar la planificación de rutas. No vendemos ni compartimos tus datos con terceros para publicidad."],
      ["Proveedores", "Usamos servicios de terceros para operar: Google (inicio de sesión), Brevo (envío de correos con códigos), Render (alojamiento) y TiDB Cloud (base de datos). Cada uno trata los datos solo para prestar su servicio."],
      ["Conservación", "Las sesiones caducan a las 12 horas y se eliminan periódicamente. Los códigos de verificación vencen a los 10 minutos. Los datos de tu cuenta se conservan mientras la cuenta exista."],
      ["Tus derechos", "Puedes ver y actualizar tus datos desde \"Mi cuenta\". Para eliminar tu cuenta o hacer consultas sobre tus datos, escribe al equipo del proyecto desde el correo con el que te registraste."],
    ],
  },
  terminos: {
    titulo: "Términos y condiciones",
    secciones: [
      ["Alcance", "MoraPack4D es un proyecto académico. El servicio se ofrece \"tal cual\", con fines demostrativos, sin garantías de disponibilidad ni de exactitud de los planes de envío."],
      ["Cuentas", "Debes proporcionar datos verdaderos y mantener la confidencialidad de tu contraseña. Las cuentas nuevas son de tipo cliente; solo un administrador puede asignar otros roles. Podemos deshabilitar cuentas que hagan un uso indebido del sistema."],
      ["Pedidos", "Los pedidos registrados se planifican automáticamente. Un pedido solo puede cancelarse mientras ninguno de sus vuelos haya salido."],
      ["Uso aceptable", "No intentes acceder a datos de otros usuarios, vulnerar la seguridad del sistema ni sobrecargarlo de forma deliberada."],
      ["Cambios", "Podemos actualizar estos términos; la fecha de la última actualización figura al final de esta página."],
    ],
  },
};

export default function Legal({ tipo }) {
  const { titulo, secciones } = CONTENIDO[tipo] ?? CONTENIDO.terminos;
  return (
    <article className="legal">
      <h1>{titulo}</h1>
      {secciones.map(([h, p]) => (
        <section key={h}>
          <h2>{h}</h2>
          <p>{p}</p>
        </section>
      ))}
      <p className="legal__fecha">Última actualización: {ACTUALIZADO}.</p>
    </article>
  );
}
