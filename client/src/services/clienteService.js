import axios from "axios";

const API_URL = "/api/clientes"; 

export const listarClientes = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error("Error al listar clientes:", error);
    throw error;
  }
};
