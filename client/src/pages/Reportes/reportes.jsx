import React, { useState } from "react";
import './reportes.scss'
import { ButtonAdd, Input, SidebarActions, Notification } from "../../components/UI/ui";
import plus from '../../assets/icons/plus.svg';
import hideIcon from '../../assets/icons/hide-sidebar.png';

// Recharts
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from "recharts";

export default function Reportes() {
  const [collapsed, setCollapsed] = useState(false);
  const [codigoFiltro, setCodigoFiltro] = useState("");
  const [notification, setNotification] = useState(null);

  const handleFilter = () => {};
  const handleCleanFilters = () => setCodigoFiltro("");

  const showNotification = (type, message) => {
    setNotification({ type, message });
    setTimeout(() => setNotification(null), 5000);
  };

  // Datos hardcodeados de estadísticas
  const estadisticas = [
    {
      categoria: "General",
      titulo: "Usuarios Activos",
      descripcion: "Número de usuarios que ingresaron al sistema hoy.",
      valor: "312",
      detalle: "Usuarios distintos registrados en las últimas 24h."
    },
    {
      categoria: "Pedidos",
      titulo: "Total de Pedidos",
      descripcion: "Cantidad total de pedidos registrados en el sistema.",
      valor: "1,245",
      detalle: "Pedidos procesados en el periodo completed."
    },
    {
      categoria: "Pedidos",
      titulo: "Pedidos Entregados",
      descripcion: "Porcentaje de pedidos entregados exitosamente.",
      valor: "92%",
      detalle: "Porcentaje de cumplimiento de entregas."
    },
    {
      categoria: "Vuelos",
      titulo: "Vuelos Programados",
      descripcion: "Número total de vuelos programados.",
      valor: "87",
      detalle: "Incluye vuelos nacionales e internacionales."
    },
  ];

  // Datos para el gráfico (ejemplo)
  const dataGrafico = [
    { name: "Lun", pedidos: 120 },
    { name: "Mar", pedidos: 98 },
    { name: "Mié", pedidos: 150 },
    { name: "Jue", pedidos: 80 },
    { name: "Vie", pedidos: 170 },
  ];

  const categorias = [...new Set(estadisticas.map(e => e.categoria))];

  return (
    <div className="page">

      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

      <aside className={`sidebar ${collapsed ? "collapsed" : ""}`}>
        <div className="sidebar-header">
          <span className="sidebar-title">Herramientas</span>
          <img
            src={hideIcon}
            alt="Ocultar"
            className="hide-icon"
            onClick={() => setCollapsed(!collapsed)}
          />
        </div>

        {!collapsed && (
          <div className="sidebar-content">
            <span className="sidebar-subtitle">Filtros</span>

            <div className="filter-group">
              <span className="sidebar-subtitle-strong">Código</span>
              <Input
                placeholder="Escribir..."
                value={codigoFiltro}
                onChange={(e) => setCodigoFiltro(e.target.value)}
              />
            </div>

            <SidebarActions
              onFilter={handleFilter}
              onClean={handleCleanFilters}
            />
          </div>
        )}
      </aside>

      <section className="contenido">
        <div className="content-header">
          <h4>Reportes</h4>
        </div>

        {/* --- ESTADISTICAS --- */}
        <div className="estadisticas-container">
          {categorias.map((cat) => (
            <div key={cat} className="categoria-bloque">
              <h5 className="categoria-titulo">{cat}</h5>

              <div className="cards-grid">
                {estadisticas.filter(e => e.categoria === cat).map((item, i) => (
                  <div key={i} className="stat-card">
                    <span className="stat-title body">{item.titulo}</span>
                    <span className="stat-desc body grey">{item.descripcion}</span>
                    <span className="stat-value data">{item.valor}</span>
                    <span className="stat-detail body">{item.detalle}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}

          {/* ---------- GRAFICO ---------- */}
          <div className="categoria-bloque">
            <h5 className="categoria-titulo">Gráficos</h5>

            <div className="grafico-card">
              <span className="stat-title body">Pedidos por día</span>
              <span className="stat-desc body grey">
                Comparativa de pedidos procesados durante la semana.
              </span>

              <div className="grafico-container">
                <ResponsiveContainer width="100%" height={280}>
                  <BarChart data={dataGrafico}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="pedidos" fill="var(--color-dark-blue)" />
                  </BarChart>
                </ResponsiveContainer>
              </div>

              <span className="stat-detail body">
                Representación visual del volumen diario de pedidos.
              </span>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
