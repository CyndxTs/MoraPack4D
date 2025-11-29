import axios from "axios";

const API_URL = "/api/clientes"; 

export const listarClientes = async (pagina = 0, tamanio = 30) => {
  try {
    const response = await axios.post(`${API_URL}/listar`, {
      params: { pagina, tamanio },
    });

    return response.data; // ListResponse con { success, mensaje, dtos }
  } catch (error) {
    console.error("Error al listar clientes:", error);
    throw error;
  }
};

export const importarClientes = async (archivo) => {
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
    console.error("Error al importar archivo de clientes:", error);
    throw error;
  }
};