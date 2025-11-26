import axios from "axios";

const API_URL = "/api/aeropuertos";

export const listarAeropuertos = async (page,size) => {
  try {
    const response = await axios.get(`${API_URL}/listar`, {
      params: { page, size },
    });

    return response.data; // ListResponse con { success, message, dtos }
  } catch (error) {
    console.error("Error al listar aeropuertos:", error);
    throw error;
  }
};

export const importarAeropuertos = async (archivo) => {
  const formData = new FormData();
  formData.append("file", archivo);

  try {
    const response = await axios.post(`${API_URL}/importar-archivo`, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    return response.data;
  } catch (error) {
    console.error("Error al importar archivo de aeropuertos:", error);
    throw error;
  }
};

