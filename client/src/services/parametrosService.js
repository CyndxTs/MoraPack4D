import axios from "axios";

/**
 * @typedef {import("../types/parametros/ParametrosResponse.js").ParametrosResponse} ParametrosResponse
 */

const API_URL = "/api/parametros";

/**
 * Obtiene los parámetros globales de planificación.
 * @returns {Promise<ParametrosResponse>}
 */
export const listarParametros = async () => {
  try {
    const response = await axios.get(API_URL);
    /** @type {ParametrosResponse} */
    const data = response.data;
    return data;
  } catch (error) {
    console.error("Error al listar parámetros:", error);
    throw error;
  }
};
