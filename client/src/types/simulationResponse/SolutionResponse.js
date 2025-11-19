/**
 * @typedef {import("./PedidoDTO.js").PedidoDTO} PedidoDTO */
/**
 * @typedef {import("./AeropuertoDTO.js").AeropuertoDTO} AeropuertoDTO */
/**
 * @typedef {import("./VueloDTO.js").VueloDTO} VueloDTO */
/**
 * @typedef {import("./RutaDTO.js").RutaDTO} RutaDTO */

/**
 * @typedef {Object} SolutionResponse
 * @property {string} token
 * @property {boolean} success
 * @property {string} message
 * @property {PedidoDTO[]} pedidosAtendidos
 * @property {AeropuertoDTO[]} aeropuertosTransitados
 * @property {VueloDTO[]} vuelosEnTransito
 * @property {RutaDTO[]} rutasEnOperacion
 */

export {};