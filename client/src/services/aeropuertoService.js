import axios from "axios";

const API_URL = "http://localhost:8080/api/aeropuertos";

// Listar solo los aeropuertos básicos (sin relaciones)
export const listarAeropuertos = async () => {
  try {
    const response = await axios.get(`${API_URL}/basico`);
    return response.data;
  } catch (error) {
    console.error("Error al listar aeropuertos:", error);
    throw error;
  }
};