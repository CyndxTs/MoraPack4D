import axios from "axios";

const API_URL = "http://localhost:8080/api/algorithm/planificar";

export async function planificar(request) {
  try {
    const response = await axios.post(API_URL, request);
    return response.data;
  } catch (error) {
    if (error.response) {
      throw new Error(error.response.data.message || "Error en planificación");
    }
    throw new Error("No se pudo conectar con el servidor");
  }
}
