// src/dataProvider.jsx
import React, { createContext, useContext, useEffect, useState, useRef } from "react";
import { listarPedidos } from "./services/pedidoService";
import { listarClientes } from "./services/clienteService";
import { listarAeropuertos } from "./services/aeropuertoService";
import { listarRutas } from "./services/rutaService";
import { listarVuelos } from "./services/vuelosService";
import { listarPlanes } from "./services/planesService";

export const DataContext = createContext();

export function DataProvider({ children }) {
  const [clientes, setClientes] = useState([]);
  const [aeropuertos, setAeropuertos] = useState([]);

  useEffect(() => {
    async function loadAll() {
      try {
        const [
          clientesData,
          aeropuertosData,
        ] = await Promise.all([
          listarClientes(0,300),
          listarAeropuertos(),
        ]);

        // CLIENTES / AEROPUERTOS / RUTAS
        setClientes(clientesData.dtos || []);
        setAeropuertos(aeropuertosData.dtos || []);

      } catch (e) {
        console.error("Error cargando data inicial:", e);

        setClientes([]);
        setAeropuertos([]);

      } finally {
        
      }
    }

    loadAll();
  }, []);

  return (
    <DataContext.Provider value={{
      clientes,
      aeropuertos,
    }}>
      {children}
    </DataContext.Provider>
  );
}

export function useAppData() {
  return useContext(DataContext);
}
