import axios from "axios";

const API_URL = "/api/rutas";

// Listar solo los aeropuertos básicos
export const listarRutas = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error("Error al listar rutas:", error);
    throw error;
  }
};

