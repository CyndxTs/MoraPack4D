import axios from "axios";

const API_URL = "http://localhost:8080/api/planes";

// Listar todos los planes
export const listarPlanes = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data; 
  } catch (error) {
    console.error("Error al listar planes:", error);
    throw error;
  }
};
