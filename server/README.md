# MoraPack4D — Server

Spring Boot 3.5.6 · Java 21 · MySQL (local) / TiDB Cloud (despliegue). Todo se guarda en **UTC**.

## Primer uso (localhost)

1. Instala **JDK 21** y **MySQL 8** (el server crea la base `morapack4d` si no existe).
2. Copia `.env.example` como `.env` (en esta carpeta) y pon tu contraseña en `DB_PASSWORD`.
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
| `db/seed/03_usuarios.sql` | Una cuenta por tipo: `administrador@g4d.com`, `trabajador@g4d.com`, `cliente@g4d.com` (contraseña `12345678`, guardada con BCrypt). |
| `db/seed/04_parametros.sql` | Parámetros iniciales (fila `id = 1`). |

## Purga de operación

Cada hora (`morapack.purga.cron`, en UTC) el server borra lo que la operación ya no usa: registros dados de baja,
la planificación de pedidos cancelados (el pedido se conserva), lotes y segmentaciones reemplazados, rutas y vuelos
sin uso, y eventos de operación vencidos. No corre mientras hay una replanificación. Manual: `POST /api/operation-purge`.

## Orden de carga recomendado

1. Aeropuertos (el país de cada aeropuerto debe existir; si no, se cancela toda la importación. Marca SPIM, EBCI y UBBB como sedes si aún no hay ninguna).
2. Planes de vuelo.
3. Usuarios (clientes / trabajadores / administradores). El país debe existir.
4. Eventos (necesitan los planes).
5. Pedidos:
   - SIMULACION: el cliente no se busca en BD; se genera en RAM al simular.
   - OPERACION: se omiten los pedidos de clientes inexistentes; si el destino es una sede, se guardan como atendidos al instante.

## Endpoints agregados para el cliente

- `POST /api/auth/iniciar-sesion` `{ correo, contrasenia, tipoUsuario? }` → `{ exito, mensaje, usuario }` (BCrypt; marca ONLINE).
- `POST /api/auth/cerrar-sesion` `{ correo, tipoUsuario? }` → marca OFFLINE.
- `POST /api/operation-current` → última solución de la operación (`{ solucion }`) o 204 si el server aún no replanificó.

## Producción (Render + TiDB Cloud)

- Render despliega con el `Dockerfile` de esta carpeta (perfil `tidb` por defecto).
- Variables en Render: `DB_HOST`, `DB_PORT` (4000), `DB_NAME`, `DB_USER` (con prefijo `xxxx.root`), `DB_PASSWORD`,
  `CORS_ORIGINS` (URL del front). `PORT` lo pone Render.
- El seed contra TiDB se corre desde tu PC: en `.env` pon `SPRING_PROFILES_ACTIVE=tidb` y los datos de TiDB, y ejecuta `.\seed.ps1`.
