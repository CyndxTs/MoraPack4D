import axios from "axios";

const API_URL = "/api/pedidos";

// Listar solo los aeropuertos básicos
export const listarPedidos = async (page,size) => {
  try {
    const response = await axios.get(`${API_URL}/listar`, {
      params: { page, size },
    });

    return response.data; // ListResponse con { success, message, dtos }
  } catch (error) {
    console.error("Error al listar pedidos:", error);
    throw error;
  }
};

export const importarPedido = async (pedidoDTO) => {
  try {
    const requestBody = {
      dto: pedidoDTO
    };

    // Ruta correcta: /api/pedidos/importar
    const response = await axios.post(`${API_URL}/importar`, requestBody);

    return response.data; // GenericResponse
  } catch (error) {
    if (error.response) {
      return error.response.data; // devuelve GenericResponse desde el backend
    }
    return { success: false, message: "Error en la conexión con el servidor" };
  }
};

export const importarPedidos = async (file, importFileRequest) => {
  try {
    const formData = new FormData();

    // Parte 1: el archivo
    formData.append("file", file);

    // Parte 2: el objeto JSON como string
    formData.append("request", new Blob([JSON.stringify(importFileRequest)], {
      type: "application/json"
    }));

    const response = await axios.post(`${API_URL}/importar-archivo`, formData, {
      headers: {
        "Content-Type": "multipart/form-data"
      }
    });

    return response.data; // GenericResponse
  } catch (error) {
    if (error.response) {
      return error.response.data;
    }
    return { success: false, message: "Error en la conexión con el servidor" };
  }
};