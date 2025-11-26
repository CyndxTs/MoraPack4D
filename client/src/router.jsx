import React from 'react'
import { createBrowserRouter } from 'react-router-dom'
import Layout from './layout/layout'
import Home from './pages/Home/home'
import Aeropuertos from './pages/Aeropuertos/aeropuertos'
import Clientes from './pages/Clientes/clientes'
import Planes from './pages/Planes/planes'
import Pedidos from './pages/Pedidos/pedidos'
import Planificacion from './pages/Planificacion/planificacion'
import Simulacion from './pages/Simulacion/simulacion'
import Reportes from './pages/Reportes/reportes'
import Login from './pages/Login/login'

const router = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <Home /> },
      { path: 'home', element: <Home /> },
      { path: 'clientes', element: <Clientes /> },
      { path: 'aeropuertos', element: <Aeropuertos /> },
      { path: 'planes', element: <Planes /> },
      { path: 'pedidos', element: <Pedidos /> },
      { path: 'planificacion', element: <Planificacion /> },
      { path: 'simulacion', element: <Simulacion /> },
      { path: 'reportes', element: <Reportes /> },
    ],
  },
  { path: '/login', element: <Login /> },
])

export default router
