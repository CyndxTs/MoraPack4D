#!/usr/bin/env sh
# Reinicia la base de datos (elimina todas las tablas y ejecuta src/main/resources/db/seed/*.sql en orden)
cd "$(dirname "$0")" && ./mvnw spring-boot:run -Dspring-boot.run.arguments=--seed
