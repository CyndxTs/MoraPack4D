import axios from "axios";

const API_URL = "http://localhost:8080/api/pedidos";

// Listar pedidos simplificados
export const listarPedidos = async () => {
  try {
    const response = await axios.get(`${API_URL}/listar`);

    // Normalizamos los datos recibidos del backend
    const pedidos = response.data.map((p) => ({
      id: p.id,
      codigo: p.codigo,
      cantidadSolicitada: p.cantidadSolicitada,
      fechaHoraGeneracionLocal: p.fechaHoraGeneracionLocal,
      fechaHoraGeneracionUTC: p.fechaHoraGeneracionUTC,
      fechaHoraExpiracionLocal: p.fechaHoraExpiracionLocal,
      fechaHoraExpiracionUTC: p.fechaHoraExpiracionUTC,
      idCliente: p.idCliente,
      nombreCliente: p.nombreCliente || "Sin cliente",
      idAeropuertoDestino: p.idAeropuertoDestino,
      codigoAeropuertoDestino: p.codigoAeropuertoDestino || "N/A",
    }));

    return pedidos;
  } catch (error) {
    console.error("Error al listar pedidos:", error);
    throw error;
  }
};

// === FILTRADO ===
export const filtrarPedidos = async (campos = ["codigo"], orden = ["asc"]) => {
  try {
    // Construimos los parámetros tipo ?campos=codigo&campos=nombreCliente&orden=asc&orden=desc
    const params = new URLSearchParams();
    campos.forEach((campo) => params.append("campos", campo));
    orden.forEach((ord) => params.append("orden", ord));

    const response = await axios.get(`${API_URL}/filtrar?${params.toString()}`);

    // Normalizamos el resultado como en listarPedidos
    const pedidos = response.data.map((p) => ({
      id: p.id,
      codigo: p.codigo,
      cantidadSolicitada: p.cantidadSolicitada,
      fechaHoraGeneracionLocal: p.fechaHoraGeneracionLocal,
      fechaHoraGeneracionUTC: p.fechaHoraGeneracionUTC,
      fechaHoraExpiracionLocal: p.fechaHoraExpiracionLocal,
      fechaHoraExpiracionUTC: p.fechaHoraExpiracionUTC,
      idCliente: p.idCliente,
      nombreCliente: p.nombreCliente || "Sin cliente",
      idAeropuertoDestino: p.idAeropuertoDestino,
      codigoAeropuertoDestino: p.codigoAeropuertoDestino || "N/A",
    }));

    return pedidos;
  } catch (error) {
    console.error("Error al filtrar pedidos:", error);
    throw error;
  }
};
