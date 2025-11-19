import axios from "axios";

const API_URL = "/api/pedidos";

// Listar solo los aeropuertos básicos
export const listarPedidos = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error("Error al listar pedidos:", error);
    throw error;
  }
};


