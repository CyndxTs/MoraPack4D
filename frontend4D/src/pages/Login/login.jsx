import React from 'react'
import './Login.scss'

export default function Login() {
  return (
    <div className="login-page">
      <h1>Iniciar Sesión</h1>
      <form className="login-form">
        <input type="text" placeholder="Usuario" />
        <input type="password" placeholder="Contraseña" />
        <button type="submit">Entrar</button>
      </form>
    </div>
  )
}
