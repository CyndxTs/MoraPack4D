import axios from "axios";

/**
 * @typedef {import("../types/parametros/ParametrosResponse.js").ParametrosResponse} ParametrosResponse
 */

const API_URL = "/api/parametros";
const API_URL2 = "/api";

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

/**
 * Importa un conjunto de parámetros al backend.
 * POST /api/parametros/importar
 *
 * @param {ParametrosDTO} dto
 * @returns {Promise<GenericResponse>}
 */
export const importarParametros = async (dto) => {
  try {
    const payload = {
      tipoDto: "PARAMETROS",
      dto: dto,
    };

    const response = await axios.post(
      `${API_URL2}/importation-init`,
      payload
    );

    return response.data;
  } catch (error) {
    console.error("Error al importar parámetros:", error);
    throw error;
  }
};

