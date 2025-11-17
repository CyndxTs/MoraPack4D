import axios from "axios";

const API_URL = "http://localhost:8080/api/algorithm";

export const importarArchivo = async (file, tipoArchivo) => {
  const formData = new FormData();

  // archivo
  formData.append("file", file);

  // JSON requerido por backend -> @RequestPart("request")
  formData.append(
    "request",
    new Blob(
      [
        JSON.stringify({
          tipoArchivo: tipoArchivo,  
          fechaHoraInicio: "",
          fechaHoraFin: "",
          desfaseTemporal: -1
        })
      ],
      { type: "application/json" }
    )
  );

  try {
    const response = await axios.post(
      `${API_URL}/importarDesdeArchivo`,
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );

    return response.data;
  } catch (error) {
    console.error("Error al importar:", error);
    throw error;
  }
};

export const importarClientes = (file) =>
  importarArchivo(file, "CLIENTES");

export const importarAeropuertos = (file) =>
  importarArchivo(file, "AEROPUERTOS");

export const importarVuelos = (file) =>
  importarArchivo(file, "PLANES");

export const importarPedidos = async (file, fechaInicioUTC, fechaFinUTC) => {
  const formData = new FormData();

  formData.append("file", file);

  formData.append(
    "request",
    new Blob(
      [
        JSON.stringify({
          tipoArchivo: "PEDIDOS",
          fechaHoraInicio: fechaInicioUTC || "",
          fechaHoraFin: fechaFinUTC || "",
          desfaseTemporal: -1
        })
      ],
      { type: "application/json" }
    )
  );

  try {
    const response = await axios.post(
      `${API_URL}/importarDesdeArchivo`,
      formData,
      { headers: { "Content-Type": "multipart/form-data" } }
    );

    return response.data;

  } catch (error) {
    console.error("Error al importar:", error);
    throw error;
  }
};
