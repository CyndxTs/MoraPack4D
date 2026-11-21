import React from "react";
import EnConstruccion from "../comun/EnConstruccion";

export default function ClienteInicio() {
  return (
    <EnConstruccion
      titulo="Mis envíos"
      descripcion="Aquí el cliente verá la operación en el mapa, filtrada solo a sus pedidos."
      puntos={["Mapa de la operación con sus lotes y vuelos", "Estado y fecha estimada de cada pedido", "Cancelar pedidos que aún no despegaron"]}
    />
  );
}
