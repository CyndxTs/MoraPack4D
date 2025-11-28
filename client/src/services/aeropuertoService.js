import axios from "axios";

const API_URL = "/api/aeropuertos";

export const listarAeropuertos = async (page = 0, size = 30) => {
  try {
    const response = await axios.post(`${API_URL}/listar`, {
      page,
      size,
    });

    return response.data;
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

