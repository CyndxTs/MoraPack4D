import axios from "axios";

const API_URL = "http://localhost:8080/api/clientes"; 

export const listarClientes = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error("Error al listar clientes:", error);
    throw error;
  }
};

//filtrado
export const filtrarClientes = async (nombre, correo, estado) => {
  try {
    const params = {};
    if (nombre) params.nombre = nombre;
    if (correo) params.correo = correo;
    if (estado) params.estado = estado;

    const response = await axios.get(`${API_URL}/filtrar`, { params });
    return response.data;
  } catch (error) {
    console.error("Error al filtrar clientes:", error);
    throw error;
  }
};
