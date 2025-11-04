// simulationService.js
import { Client } from '@stomp/stompjs';

const SOCKET_URL = "ws://localhost:8080/ws";
let client = null;

function ensureClient() {
  return new Promise((resolve, reject) => {
    if (client && client.active) return resolve(); // ya activándose o activo
    client = new Client({
      brokerURL: SOCKET_URL,
      reconnectDelay: 5000,
      debug: (s) => console.log('STOMP DEBUG:', s),
      onConnect: () => resolve(),
      onStompError: (frame) => reject(new Error(frame.headers['message'] || 'STOMP error'))
    });
    client.activate();
  });
}

export async function getAeropuertosMapWS() {
  await ensureClient();
  return new Promise((resolve, reject) => {
    const sub = client.subscribe('/topic/simulator', (message) => {
      const data = JSON.parse(message.body);
      const aeropuertosMap = Object.fromEntries(
        data.map(a => [
          a.codigo,
          {
            name: a.ciudad,
            lat: a.latitudDEC,
            lng: a.longitudDEC,
            gmt: a.husoHorario,
            capacity: a.capacidad,
            isHQ: a.esSede,
          },
        ])
      );
      sub.unsubscribe();
      resolve(aeropuertosMap);
    });
    client.publish({ destination: '/app/simulator' });
  });
}

// pedir vuelos por WS (devuelve el array crudo del back)
export async function getVuelosWS() {
  await ensureClient();
  return new Promise((resolve, reject) => {
    const sub = client.subscribe('/topic/simulator/flights', (message) => {
      const vuelos = JSON.parse(message.body);
      sub.unsubscribe();       // solo la primera “tanda”
      resolve(vuelos);
    });
    client.publish({ destination: '/app/simulator/flights' });
  });
}

export function disconnectWS() {
  if (client) {
    client.deactivate();
    console.log('🔌 Conexión WebSocket cerrada');
  }
}





/*import axios from "axios";

const API = "http://localhost:8080/api/aeropuertos";

export async function getAeropuertosMap(signal) {
  const res = await fetch(API, { signal });
  if (!res.ok) throw new Error("No se pudieron obtener los aeropuertos");
  const data = await res.json(); 
  return Object.fromEntries(
    data.map(a => [
      a.codigo,
      {
        name: a.ciudad,
        lat: a.latitudDEC,
        lng: a.longitudDEC,
        gmt: a.husoHorario,
        capacity: a.capacidad,
        isHQ: a.esSede,
      },
    ])
  );
}*/