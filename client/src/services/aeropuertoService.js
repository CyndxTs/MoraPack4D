import axios from "axios";

const API_URL = "/api/aeropuertos";

// Listar solo los aeropuertos básicos
export const listarAeropuertos = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error("Error al listar aeropuertos:", error);
    throw error;
  }
};

