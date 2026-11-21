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

- `db/seed/01_ddl.sql`: esquema completo.
- Los DML irán como `02_*.sql`, `03_*.sql`, … y se ejecutan en orden alfabético con el mismo comando.

## Orden de carga recomendado

1. Aeropuertos (crea continentes y países automáticamente).
2. Parámetros (define las sedes).
3. Planes de vuelo.
4. Usuarios (clientes / trabajadores / administradores).
5. Pedidos:
   - SIMULACION: el cliente no se busca en BD; se genera en RAM al simular.
   - OPERACION: se omiten los pedidos de clientes inexistentes; si el destino es una sede, se guardan como atendidos al instante.
