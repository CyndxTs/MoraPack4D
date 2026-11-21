# MoraPack4D — Server

Spring Boot 3.5.6 · Java 21 · MySQL (local) / TiDB Cloud (despliegue). Todo se guarda en **UTC**.

## Primer uso (localhost)

1. Instala **JDK 21** y **MySQL 8** (el server crea la base `morapack4d` si no existe).
2. Crea tu `.env` en esta carpeta (ver "Variables de entorno").
   Si el proyecto vino de un `.zip`, desbloquea los archivos (si no, PowerShell no deja ejecutar `seed.ps1`):

   ```powershell
   Get-ChildItem -Recurse | Unblock-File
   ```

3. Reinicia la BD (borra todas las tablas y ejecuta `src/main/resources/db/seed/*.sql` en orden):

   ```powershell
   .\seed.ps1
   ```

4. Levanta el server:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

En VS Code también puedes usar **Run and Debug** → `MoraPack (dev)` / `MoraPack (seed: reiniciar BD)`, o las tareas de `Terminal → Run Task`.

## Perfiles

| Perfil | Uso | Se activa con |
|---|---|---|
| `dev` (por defecto) | MySQL en localhost | `SPRING_PROFILES_ACTIVE=dev` |
| `tidb` | TiDB Cloud (más adelante, Render) | `SPRING_PROFILES_ACTIVE=tidb` |

Las credenciales nunca van en los `.properties`: van en `.env` (ignorado por git) o en las variables de entorno de Render.

## Seed

`.\seed.ps1` borra todas las tablas y ejecuta, en orden:

| Script | Qué hace |
|---|---|
| `db/seed/01_ddl.sql` | Esquema completo (sin procedimientos/funciones: TiDB Cloud no los soporta). |
| `db/seed/02_paises.sql` | Los 3 continentes y 30 países del archivo de aeropuertos (con su huso horario). |
| `db/seed/03_usuarios.sql` | Una cuenta por rol en `USUARIO`: `administrador@g4d.com` (ADM0001), `trabajador@g4d.com` (TRA0001), `cliente@g4d.com` (0000001). Contraseña `12345678` (BCrypt). |
| `db/seed/04_parametros.sql` | Parámetros iniciales (fila `id = 1`). |

## Usuarios y seguridad

- Tabla única `USUARIO` con `rol` (CLIENTE | TRABAJADOR | ADMINISTRADOR). Códigos: cliente `0000001`, trabajador `TRA0001`,
  administrador `ADM0001`. El siguiente código es el máximo válido del rol + 1 (los mal formados se ignoran; error si se agota).
  Al cambiar de rol se reasigna el código del rol destino en la misma transacción.
- Ya no hay importación de usuarios por archivo: los clientes se registran (código de 6 dígitos por correo) y el
  administrador crea/gestiona usuarios uno a uno en `/api/usuarios/**`.
- Spring Security sin estado: token de acceso JWT (HS256, 10 min) + sesión por dispositivo de 12 h (tabla `SESION`,
  solo el hash del token de refresco). La sesión no se extiende. STOMP valida el token en el CONNECT y autoriza las
  suscripciones por rol; una conexión abierta no se corta al vencer el token.
- Reglas: `/api/auth/**` público (salvo `mi-cuenta`), `/api/usuarios/**` administrador, `/api/cliente/**` cliente,
  resto de `/api/**` trabajador (el administrador también). PARAMETROS, PAIS y AEROPUERTO(S) solo los importa el administrador.

| Endpoint | Uso |
|---|---|
| `POST /api/auth/iniciar-sesion` | `{ correo, contrasenia }` → usuario + `tokenAcceso`, `tokenRefresco`, `expiraAcceso`, `expiraSesion` |
| `POST /api/auth/google` | `{ credencial, nombres?, apellidos?, pais? }` (si es nuevo y faltan datos: `requiereDatos: true`) |
| `POST /api/auth/refrescar` · `cerrar-sesion` | `{ tokenRefresco }` |
| `POST /api/auth/registrar` → `registrar/verificar` | Registro de CLIENTE con código de 6 dígitos (el usuario se crea con el código correcto) |
| `POST /api/auth/codigo/reenviar` | `{ correo, proposito: REGISTRO \| RECUPERACION }` (cada 120 s) |
| `POST /api/auth/contrasenia/olvido` → `contrasenia/restablecer` | Recuperación con código |
| `POST /api/auth/mi-cuenta[/perfil \| /contrasenia]` | Datos propios y contraseña (cuentas de Google la crean sin la actual) |
| `POST /api/usuarios/{listar,filtrar,crear,cambiar-rol,cambiar-estado,eliminar}` | Administrador |

## Espacio del cliente (`/api/cliente/**`)

El código del cliente siempre sale de la sesión (nunca del cuerpo).

| Endpoint (POST) | Qué hace |
|---|---|
| `/api/cliente/aeropuertos` | Catálogo mínimo de aeropuertos (sin almacenes) |
| `/api/cliente/condiciones` | Plazos vigentes (días intra/intercontinental, horas de recojo, máx. paquetes) |
| `/api/cliente/operacion` | Última solución de la operación recortada a sus pedidos, vuelos y paquetes |
| `/api/cliente/pedidos` | Sus pedidos de operación (más recientes primero) con primera salida, última llegada y si se pueden cancelar |
| `/api/cliente/pedidos/registrar` | `{ codDestino, cantidadSolicitada }`: pedido con fecha "ahora"; si el destino es sede se entrega al instante |
| `/api/cliente/pedidos/cancelar` | `{ codigo }`: solo pedidos propios, activos y sin vuelos despegados |

Tras cada replanificación cada cliente recibe su parte en `/topic/cliente/<codigo>/operacion`.

## Colapso en la simulación

Un pedido sin atender en un bloque se reintenta en el siguiente (desde el umbral actual). La simulación se detiene
por colapso solo cuando algún pedido sin atender ya no puede llegar dentro de su plazo. La consola muestra cada
pedido sin atender con su motivo (`[!] SIN ATENDER: ...`). La simulación arranca con los almacenes vacíos: no usa
los registros de la operación.

## Replanificación automática y purga

Las gobierna el server con los parámetros de `PARAMETROS` (los edita el administrador):

| Columna | Qué hace |
|---|---|
| `replanificacion_automatica` | Al registrar/importar pedidos de OPERACIÓN se programa una replanificación |
| `minutos_espera_replanificacion` | Espera desde el primer pedido nuevo (15 por defecto) |
| `replanificar_al_registrar_evento` | Un evento de OPERACIÓN replanifica de inmediato |
| `segundos_umbral_replanificacion` | Umbral de replanificación (120–240 s, 180 por defecto) |
| `purga_automatica` | Activa la purga horaria |

La programación se consulta con `POST /api/operation-schedule` y se publica en `/topic/operation-schedule`.

## Purga de operación

Cada hora (`morapack.purga.cron`, en UTC, si `purga_automatica` está activa) el server borra lo que la operación ya no usa: registros dados de baja,
la planificación de pedidos cancelados (el pedido se conserva), lotes y segmentaciones reemplazados, rutas y vuelos
sin uso, y eventos de operación vencidos. No corre mientras hay una replanificación. Manual: `POST /api/operation-purge`.

## Orden de carga recomendado

1. Aeropuertos (el país de cada aeropuerto debe existir; si no, se cancela toda la importación. No marca sedes, salvo las líneas que terminen con la palabra SEDE; las sedes las administra el administrador).
2. Planes de vuelo.
3. Eventos (necesitan los planes).
4. Pedidos:
   - SIMULACION: el cliente no se busca en BD; se crea en RAM la primera vez y se mantiene toda la simulación.
   - OPERACION: se omiten las líneas cuyo cliente no existe en BD (el estado final informa cuántas); si el destino es una sede, se guardan como atendidos al instante.

## Variables de entorno

`.env` local (nunca se sube) y variables del servicio en Render:

| Variable | Para qué |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (MySQL local) o `tidb` |
| `LOCAL_DB_HOST`, `LOCAL_DB_PORT`, `LOCAL_DB_NAME`, `LOCAL_DB_USER`, `LOCAL_DB_PASSWORD` | MySQL local |
| `TIDB_DB_HOST`, `TIDB_DB_PORT`, `TIDB_DB_NAME`, `TIDB_DB_USER`, `TIDB_DB_PASSWORD` | TiDB Cloud |
| `CORS_ORIGINS` | URL(s) del front, separadas por comas |
| `JWT_SECRET` | Clave larga y aleatoria para firmar los tokens de acceso (si falta se genera una al arrancar) |
| `GOOGLE_CLIENT_ID` | Client ID de Google (el mismo que `VITE_GOOGLE_CLIENT_ID` del front) |
| `BREVO_API_KEY`, `MAIL_REMITENTE`, `MAIL_REMITENTE_NOMBRE` | Correos con el código (sin API key, el código sale en la consola) |

## Producción (Render + TiDB Cloud)

- Render despliega con el `Dockerfile` de esta carpeta (perfil `tidb` por defecto).
- Variables en Render: las de TiDB, `CORS_ORIGINS`, `JWT_SECRET`, `GOOGLE_CLIENT_ID` y las de Brevo. `PORT` lo pone Render.
- El `.env` local tiene las dos configuraciones (`LOCAL_*` y `TIDB_*`); solo se cambia `SPRING_PROFILES_ACTIVE` (dev | tidb).
  Para el seed contra TiDB: `SPRING_PROFILES_ACTIVE=tidb` y `.\seed.ps1`.
