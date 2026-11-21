-- =====================================================================================
--  MoraPack4D - DML: cuentas de prueba (una por rol)
--  - Contraseña de las 3 cuentas: 12345678 (guardada con BCrypt, nunca en texto plano).
--  - Todo usuario necesita un país: se usa 'Perú' (creado en 02_paises.sql).
--  - Códigos por rol: CLIENTE '0000001', TRABAJADOR 'TRA0001', ADMINISTRADOR 'ADM0001'.
--    Los siguientes usuarios de cada rol continúan desde el máximo de su rol + 1.
-- =====================================================================================

INSERT INTO `USUARIO` (`codigo`, `rol`, `nombres`, `apellidos`, `correo`, `contrasenia`, `estado`, `id_pais`)
SELECT 'ADM0001', 'ADMINISTRADOR', 'Administrador', 'G4D', 'administrador@g4d.com', '$2a$10$VaieX.Ypss96B9W9kYndFu/ZW6f4EGApGFqgJKsBDdCEcu9.dQ2Qq', 'OFFLINE', p.`id`
FROM `PAIS` p WHERE p.`nombre` = 'Perú';

INSERT INTO `USUARIO` (`codigo`, `rol`, `nombres`, `apellidos`, `correo`, `contrasenia`, `estado`, `id_pais`)
SELECT 'TRA0001', 'TRABAJADOR', 'Trabajador', 'G4D', 'trabajador@g4d.com', '$2a$10$VaieX.Ypss96B9W9kYndFu/ZW6f4EGApGFqgJKsBDdCEcu9.dQ2Qq', 'OFFLINE', p.`id`
FROM `PAIS` p WHERE p.`nombre` = 'Perú';

INSERT INTO `USUARIO` (`codigo`, `rol`, `nombres`, `apellidos`, `correo`, `contrasenia`, `estado`, `id_pais`)
SELECT '0000001', 'CLIENTE', 'Cliente', 'G4D', 'cliente@g4d.com', '$2a$10$VaieX.Ypss96B9W9kYndFu/ZW6f4EGApGFqgJKsBDdCEcu9.dQ2Qq', 'OFFLINE', p.`id`
FROM `PAIS` p WHERE p.`nombre` = 'Perú';
