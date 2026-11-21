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

Variables (en `.env.local`, que no se sube; en Render, en el static site):

| Variable | Para qué |
|---|---|
| `VITE_API_URL` | URL del server (vacía en desarrollo: Vite redirige `/api` y `/ws`) |
| `VITE_GOOGLE_CLIENT_ID` | Client ID de Google (sin valor no aparece el botón de Google) |

## Rutas

| Ruta | Rol | Estado |
|---|---|---|
| `/`, `/privacidad`, `/terminos` | público | Landing y páginas legales. El acceso es un **modal** (no hay página de login; `/login` solo abre el modal) |
| `*/cuenta` | todos | Mi cuenta: datos y contraseña |
| `/trabajador` | TRABAJADOR (y ADMINISTRADOR) | Operación en tiempo real + dashboard (modal) |
| `/trabajador/simulacion[/:idTransaccion]` | TRABAJADOR | Simulación semanal (el id va en la URL: cada pestaña sigue la suya y F5 la retoma) |
| `/trabajador/pedidos` | TRABAJADOR | Pedidos: filtrar, registrar (operación), importar archivo, cancelar |
| `/trabajador/planes` | TRABAJADOR | Planes de vuelo y eventos (listar e importar) |
| `/cliente`, `/cliente/pedidos/nuevo` | CLIENTE | Pendiente (pantallas provisionales) |
| `/admin`, `/admin/ubicaciones`, `/admin/aeropuertos` | ADMINISTRADOR | Pendiente (pantallas provisionales) |

## Sesión

- Una sesión por dispositivo: vive en `localStorage` (usuario + token de acceso de 10 min + token de refresco) y todas
  las pestañas la comparten. El token de acceso se renueva solo; la sesión dura 12 h desde el inicio.
- Una ruta protegida sin sesión abre el modal de acceso sobre la página; al vencer la sesión aparece el aviso y el modal,
  y las peticiones pendientes se reintentan al volver a entrar.
- STOMP manda el token en el CONNECT; la conexión sigue aunque el token venza (las simulaciones no se cortan).
- Las transacciones (simulaciones) son por pestaña: su id va en la URL.

## Estructura

```
src/
  api/            Llamadas REST por recurso (http.js normaliza errores: siempre ApiError con mensaje)
  realtime/       UN cliente STOMP para toda la pestaña + seguimiento de importaciones/exportaciones
  auth/           Sesión, tokens, modal de acceso (login, registro, códigos, Google) y guarda por rol
  notifications/  Notificaciones globales
  operacion/      Próxima replanificación automática (la programa el server)
  scenario/       Mapa, panel lateral, paneles de detalle y transformación de la solución del server
                  (común a Operación y Simulación)
  layouts/        Layout por rol (barra superior distinta)
  pages/          public/, trabajador/, cliente/, admin/, comun/
  components/     UI reutilizable (botones, tablas, selectores, overlays)
  utils/          Fechas (todo en UTC) y formatos
  config.js       Constantes (URL del server, Google, tamaños de página, tick del reloj)
```

## Replanificación automática

La programa el server según los parámetros del administrador (activar/desactivar, minutos de espera, replanificar al
registrar un evento). La cuenta regresiva se ve en "Mostrar controles" de la Operación y "Replanificar" la adelanta.

## Producción (Render, static site)

- Build: `npm ci && npm run build` · Publish directory: `dist`
- Variables: `VITE_API_URL` y `VITE_GOOGLE_CLIENT_ID` (se leen al compilar; si cambian, vuelve a desplegar).
- Rewrite: `/*` → `/index.html` (para que las rutas como `/trabajador/pedidos` funcionen al recargar).
- `Dockerfile` y `nginx.conf` no se usan en Render (quedan por si se despliega en otro lado).
