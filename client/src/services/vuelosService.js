import axios from "axios";

const API_URL = "/api/planes";

// Listar solo los aeropuertos básicos
export const listarVuelos = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error("Error al listar vuelos:", error);
    throw error;
  }
};
