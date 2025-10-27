import React from 'react'
import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar/navbar'
import Footer from '../components/Footer/footer'
import './layout.scss'

const Layout = () => {
  return (
    <div className="layout">
      <Navbar />
      <main id="main-content">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}

export default Layout