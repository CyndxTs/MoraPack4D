# MoraPack4D — Client

React 18 + Vite 7 · react-router 7 · react-leaflet 4 · recharts · STOMP (@stomp/stompjs) · SCSS.

## Correr en localhost

```powershell
npm install
npm run dev        # http://localhost:5173  (el server debe estar en http://localhost:8080)
```

Vite redirige `/api` y `/ws` al server (ver `vite.config.js`), así que no hace falta CORS en desarrollo.

Cuentas de prueba (las crea el seed del server, contraseña `12345678`):
`trabajador@g4d.com`, `cliente@g4d.com`, `administrador@g4d.com`.

## Rutas

| Ruta | Rol | Estado |
|---|---|---|
| `/` , `/login` | público | Landing de MoraPack + inicio de sesión |
| `/trabajador` | TRABAJADOR | Operación en tiempo real + dashboard (modal) |
| `/trabajador/simulacion[/:idTransaccion]` | TRABAJADOR | Simulación semanal (el id va en la URL: cada pestaña sigue la suya y F5 la retoma) |
| `/trabajador/pedidos` | TRABAJADOR | Pedidos: filtrar, registrar (operación), importar archivo, cancelar |
| `/trabajador/planes` | TRABAJADOR | Planes de vuelo y eventos (listar e importar) |
| `/cliente`, `/cliente/pedidos/nuevo` | CLIENTE | Pendiente (pantallas provisionales) |
| `/admin`, `/admin/ubicaciones`, `/admin/aeropuertos` | ADMINISTRADOR | Pendiente (pantallas provisionales) |

## Sesión

- Una sola cuenta por navegador: la sesión vive en `localStorage` y todas las pestañas la comparten
  (si se inicia o cierra sesión en una, las demás se actualizan solas).
- Las transacciones (simulaciones) son por pestaña: su id va en la URL.

## Estructura

```
src/
  api/            Llamadas REST por recurso (http.js normaliza errores: siempre ApiError con mensaje)
  realtime/       UN cliente STOMP para toda la pestaña + seguimiento de importaciones/exportaciones
  auth/           Sesión, contexto y guarda por rol
  notifications/  Notificaciones globales
  operacion/      Replanificación automática de la operación (compartida entre pestañas)
  scenario/       Mapa, panel lateral, paneles de detalle y transformación de la solución del server
                  (común a Operación y Simulación)
  layouts/        Layout por rol (barra superior distinta)
  pages/          public/, trabajador/, cliente/, admin/, comun/
  components/     UI reutilizable (botones, tablas, selectores, overlays)
  utils/          Fechas (todo en UTC) y formatos
  config.js       Constantes (tamaños de página, minutos de replanificación diferida, tick del reloj)
```

## Replanificación automática

Al registrar o importar pedidos de OPERACIÓN se programa una replanificación para dentro de
`REPLANIFICACION_DIFERIDA_MIN` minutos (`config.js`, 15 por defecto). La cuenta regresiva se ve en
"Mostrar controles" de la Operación, y el botón "Replanificar" la adelanta.
