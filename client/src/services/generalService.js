import axios from "axios";

const API_URL = "http://localhost:8080/api/algorithm";

export const importarClientes = async (file) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("type", "CLIENTES"); // clave: usa el tipo para AlgorithmController

  try {
    const response = await axios.post(`${API_URL}/importar`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data;
  } catch (error) {
    console.error("Error al importar clientes:", error);
    throw error;
  }
};

export const importarAeropuertos = async (file) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("type", "AEROPUERTOS"); // Clave esperada en el backend

  try {
    const response = await axios.post(`${API_URL}/importar`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data; // contiene { exito: boolean, mensaje: string }
  } catch (error) {
    console.error("Error al importar aeropuertos:", error);
    throw error;
  }
};

export const importarPedidos = async (file) => {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("type", "PEDIDOS");

  try {
    const response = await axios.post(`${API_URL}/importar`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return response.data;
  } catch (error) {
    console.error("Error al importar pedidos:", error);
    throw error;
  }
};
