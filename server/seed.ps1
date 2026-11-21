# Reinicia la base de datos (elimina todas las tablas y ejecuta src/main/resources/db/seed/*.sql en orden)
# Uso (desde la carpeta server/):  .\seed.ps1
& "$PSScriptRoot\mvnw.cmd" -f "$PSScriptRoot\pom.xml" spring-boot:run "-Dspring-boot.run.arguments=--seed"
exit $LASTEXITCODE
