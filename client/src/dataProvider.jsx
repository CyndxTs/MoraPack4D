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
  const [loadingData, setLoadingData] = useState(true);
  const [pedidos, setPedidos] = useState([]);
  const [clientes, setClientes] = useState([]);
  const [aeropuertos, setAeropuertos] = useState([]);
  const [rutas, setRutas] = useState([]);
  const [vuelos, setVuelos] = useState([]);
  const [planes, setPlanes] = useState([]);
  const ultimoCountRef = useRef(0);
  const [pedidosOriginales, setPedidosOriginales] = useState([]);
  const [flights, setFlights] = useState([]);

  useEffect(() => {
    async function loadAll() {
      try {
        const [
          pedidosData,
          clientesData,
          aeropuertosData,
          rutasData,
          vuelosData,
          planesData
        ] = await Promise.all([
          listarPedidos(),
          listarClientes(),
          listarAeropuertos(),
          listarRutas(),
          listarVuelos(),
          listarPlanes()
        ]);

        // PEDIDOS
        setPedidos(pedidosData.dtos || []);
        setPedidosOriginales(pedidosData.dtos || []);
        ultimoCountRef.current = pedidosData.dtos.length;

        // CLIENTES / AEROPUERTOS / RUTAS
        setClientes(clientesData.dtos || []);
        setAeropuertos(aeropuertosData.dtos || []);
        setRutas(rutasData.dtos || []);

        // VUELOS (raw)
        setVuelos(vuelosData.dtos || []);

        // VUELOS mapeados
        const mappedFlights = vuelosData.dtos.map(v => ({
          code: v.codigo,
          origin: v.plan.codOrigen,
          destination: v.plan.codDestino,
          capacity: v.plan.capacidad,
          occupied: v.capacidadOcupada,

          departure: new Date(
            v.fechaHoraSalida.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$3-$2-$1")
          ),
          arrival: new Date(
            v.fechaHoraLlegada.replace(/(\d{2})\/(\d{2})\/(\d{4})/, "$3-$2-$1")
          ),

          distance: v.plan.distancia,
          visible: true
        }));

        setFlights(mappedFlights);

        // PLANES
        setPlanes(planesData.dtos || []);

      } catch (e) {
        console.error("Error cargando data inicial:", e);

        setPedidos([]);
        setClientes([]);
        setAeropuertos([]);
        setRutas([]);
        setVuelos([]);
        setFlights([]);
      } finally {
        setLoadingData(false);
      }
    }

    loadAll();
  }, []);

  // 
  function parseFecha(fecha) {
    if (!fecha) return null;

    // Si viene en formato DD/MM/YYYY HH:mm
    const regexDMY = /^(\d{2})\/(\d{2})\/(\d{4}) (\d{2}):(\d{2})$/;
    const match = fecha.match(regexDMY);

    if (match) {
      const [, dd, mm, yyyy, HH, MM] = match;
      return new Date(`${yyyy}-${mm}-${dd}T${HH}:${MM}:00`);
    }

    // Si viene en el formato correcto YYYY-MM-DD HH:mm:ss
    const regexYMD = /^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})$/;
    if (regexYMD.test(fecha)) {
      const [f, h] = fecha.split(" ");
      return new Date(`${f}T${h}`);
    }

    console.error("Formato de fecha no reconocido:", fecha);
    return null;
  }

  function formatFechaHora(date) {
    const pad = (n) => n.toString().padStart(2, "0");

    const yyyy = date.getFullYear();
    const MM = pad(date.getMonth() + 1);
    const dd = pad(date.getDate());
    const HH = pad(date.getHours());
    const mm = pad(date.getMinutes());
    const ss = pad(date.getSeconds());

    return `${yyyy}-${MM}-${dd} ${HH}:${mm}:${ss}`;
  }

  useEffect(() => {
    const interval = setInterval(async () => {
      console.log("Ejecutando verificación automática de pedidos...");

      const updated = await listarPedidos();
      const countActual = updated.dtos.length;

      const nuevos = countActual - (ultimoCountRef.current ?? 0);
      console.log("Se agregaron " + nuevos + " pedidos");

      if (nuevos > 0) {
        const lista = updated.dtos;
        const ultimos = lista.slice(-nuevos);
        const primero = ultimos[0];
        const ultimo = ultimos[ultimos.length - 1];

        const fechaObj = parseFecha(primero.fechaHoraGeneracion);
        const fechaInicio = formatFechaHora(fechaObj);
        const fechaFinObj = parseFecha(ultimo.fechaHoraGeneracion);
        const fechaFin = formatFechaHora(fechaFinObj);

        await ejecutarPlanificarConNuevos(fechaInicio, fechaFin);

        const final = await listarPedidos();
        setPedidos(final.dtos);

        ultimoCountRef.current = final.dtos.length;
        return;
      }

      setPedidos(updated.dtos);
      ultimoCountRef.current = countActual;

    }, 1 * 60 * 1000);  // cada 10 min

    return () => clearInterval(interval);
  }, []); // <-- súper importante: vacío

  async function ejecutarPlanificarConNuevos(fechaInicio, fechaFin) {
    const p = (await listarParametros()).dtos[0];
    const body = {
      replanificar: true,
      guardarPlanificacion: true,
      reparametrizar: true,
      guardarParametrizacion: true,
      considerarDesfaseTemporal: true,
      parameters: {
        fechaHoraInicio: fechaInicio,
        fechaHoraFin: fechaFin,

        // todos los parámetros actuales:
        maxDiasEntregaIntercontinental: p.maxDiasEntregaIntercontinental,
        maxDiasEntregaIntracontinental: p.maxDiasEntregaIntracontinental,
        maxHorasRecojo: p.maxHorasRecojo,
        minHorasEstancia: p.minHorasEstancia,
        maxHorasEstancia: p.maxHorasEstancia,
        codOrigenes: p.codOrigenes,
        dMin: p.dMin,
        iMax: p.iMax,
        eleMin: p.eleMin,
        eleMax: p.eleMax,
        kMin: p.kMin,
        kMax: p.kMax,
        tMax: p.tMax,
        maxIntentos: p.maxIntentos,
        factorDeUmbralDeAberracion: p.factorDeUmbralDeAberracion,
        factorDeUtilizacionTemporal: p.factorDeUtilizacionTemporal,
        factorDeDesviacionEspacial: p.factorDeDesviacionEspacial,
        factorDeDisposicionOperacional: p.factorDeDisposicionOperacional
      }
    };

    console.log("PLANIFICANDO AUTOMÁTICAMENTE:", body);

    try {
      const result = await planificar(body);
      if (result.success) {
        showNotification("success", "Plan generado automáticamente para nuevos pedidos");
      } else {
        showNotification("danger", result.message);
      }
    } catch (e) {
      showNotification("danger", "Error al planificar automáticamente");
    }
  }



  return (
    <DataContext.Provider value={{
      loadingData,
      pedidos,
      clientes,
      aeropuertos,
      rutas,
      vuelos,
      planes,
      flights,
      setPedidos,
      setPedidosOriginales,
      setFlights
    }}>
      {children}
    </DataContext.Provider>
  );
}

export function useAppData() {
  return useContext(DataContext);
}
