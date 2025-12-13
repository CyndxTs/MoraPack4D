import React, { createContext, useContext, useEffect, useState } from "react";

// --- SERVICIOS DE DATOS ---
import { listarClientes } from "./services/clienteService";
import { listarAeropuertos } from "./services/aeropuertoService";

// --- SERVICIO DEL WORKER ---
import { onEvent } from "./services/operationManager";

// --- UI COMPONENTS ---
// Ajusta esta ruta según donde esté realmente tu archivo "ui.jsx" o "ui/index.js"
// Si dataProvider está en "src/" y ui en "src/components/UI/ui", usa:
import { Notification } from "./components/UI/ui"; 

export const DataContext = createContext();

export function DataProvider({ children }) {
  // === ESTADOS DE DATOS ===
  const [clientes, setClientes] = useState([]);
  const [aeropuertos, setAeropuertos] = useState([]);

  // === ESTADO DE NOTIFICACIÓN GLOBAL ===
  const [globalNotification, setGlobalNotification] = useState(null);

  // 1. CARGA DE DATOS INICIALES
  useEffect(() => {
    async function loadAll() {
      try {
        const [clientesData, aeropuertosData] = await Promise.all([
          listarClientes(0, 300),
          listarAeropuertos(),
        ]);

        setClientes(clientesData.dtos || []);
        setAeropuertos(aeropuertosData.dtos || []);
      } catch (e) {
        console.error("Error cargando data inicial:", e);
        setClientes([]);
        setAeropuertos([]);
      }
    }
    loadAll();
  }, []);

  // 2. ESCUCHA DE EVENTOS DEL SHARED WORKER (GLOBAL)
  useEffect(() => {
    // Suscribirse a eventos del manager
    const removeListener = onEvent((msg) => {
      if (!msg) return;

      // Filtramos solo los mensajes de tipo notificación global que definimos en el Worker
      if (msg.type === "notification-global") {
        setGlobalNotification({
          type: msg.variant, // 'info', 'warning', 'success', 'danger'
          message: msg.message
        });

        // Auto-ocultar después de 5 segundos
        setTimeout(() => setGlobalNotification(null), 5000);
      }
    });

    return () => removeListener();
  }, []);

  return (
    <DataContext.Provider
      value={{
        clientes,
        aeropuertos,
      }}
    >
      {/* === CAPA DE NOTIFICACIÓN FLOTANTE === 
         Usamos un div contenedor para posicionarlo fijo en la pantalla,
         ya que el componente Notification se encarga del estilo interno (colores, iconos)
         pero no de su posición en la ventana.
      */}
      {globalNotification && (
        <div
          style={{
            position: "fixed",
            top: "20px",
            right: "20px",
            zIndex: 99999, // Z-Index muy alto para que se vea sobre modales
            minWidth: "320px",
            maxWidth: "450px"
          }}
        >
          <Notification 
             type={globalNotification.type} 
             message={globalNotification.message} // <-- CORREGIDO: Pasamos message como prop
             onClose={() => setGlobalNotification(null)}
          />
        </div>
      )}

      {children}
    </DataContext.Provider>
  );
}

export function useAppData() {
  return useContext(DataContext);
}