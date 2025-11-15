import axios from "axios";

const API_URL = "http://localhost:8080/api/aeropuertos";

// Listar solo los aeropuertos básicos
export const listarAeropuertos = async () => {
  try {
    // Usamos el endpoint optimizado
    const response = await axios.get(`${API_URL}/simple`);

    // El backend ya devuelve solo los campos requeridos,
    // pero igual normalizamos nombres por seguridad
    const aeropuertos = response.data.map((aeropuerto) => ({
      codigo: aeropuerto.codigo,
      ciudad: aeropuerto.ciudad,
      pais: aeropuerto.pais,
      continente: aeropuerto.continente,
      husoHorario: aeropuerto.husoHorario,
      capacidad: aeropuerto.capacidad,
    }));

    return aeropuertos;
  } catch (error) {
    console.error("Error al listar aeropuertos:", error);
    throw error;
  }
};

//Filtrar aeropuertos por uno o más criterios
export const filtrarAeropuertos = async ({
  codigo,
  ciudad,
  continente,
  ordenCapacidad, // 'asc' o 'desc'
}) => {
  try {
    const params = new URLSearchParams();

    if (codigo) params.append("codigo", codigo);
    if (ciudad) params.append("ciudad", ciudad);
    if (continente) params.append("continente", continente);
    if (ordenCapacidad) params.append("ordenCapacidad", ordenCapacidad);

    const response = await axios.get(`${API_URL}/filtrar`, { params });

    // Devuelve solo los campos básicos
    const aeropuertos = response.data.map((aeropuerto) => ({
      codigo: aeropuerto.codigo,
      ciudad: aeropuerto.ciudad,
      pais: aeropuerto.pais,
      continente: aeropuerto.continente,
      husoHorario: aeropuerto.husoHorario,
      capacidad: aeropuerto.capacidad,
    }));

    return aeropuertos;
  } catch (error) {
    console.error("Error al filtrar aeropuertos:", error);
    throw error;
  }
};