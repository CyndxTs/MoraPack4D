-- =====================================================================================
--  MoraPack4D - DML: cuentas de prueba (una por tipo de usuario)
--  - Contraseña de las 3 cuentas: 12345678 (guardada con BCrypt, nunca en texto plano).
--  - Todo usuario necesita un país: se usa 'Perú' (creado en 02_paises.sql).
--  - Códigos con el mismo formato que la importación ('%07d'): los usuarios importados continúan desde 0000002.
-- =====================================================================================

INSERT INTO `ADMINISTRADOR` (`codigo`, `nombres`, `apellidos`, `correo`, `contrasenia`, `estado`, `id_pais`)
SELECT '0000001', 'Administrador', 'G4D', 'administrador@g4d.com', '$2a$10$VaieX.Ypss96B9W9kYndFu/ZW6f4EGApGFqgJKsBDdCEcu9.dQ2Qq', 'OFFLINE', p.`id`
FROM `PAIS` p WHERE p.`nombre` = 'Perú';

INSERT INTO `TRABAJADOR` (`codigo`, `nombres`, `apellidos`, `correo`, `contrasenia`, `estado`, `id_pais`)
SELECT '0000001', 'Trabajador', 'G4D', 'trabajador@g4d.com', '$2a$10$VaieX.Ypss96B9W9kYndFu/ZW6f4EGApGFqgJKsBDdCEcu9.dQ2Qq', 'OFFLINE', p.`id`
FROM `PAIS` p WHERE p.`nombre` = 'Perú';

INSERT INTO `CLIENTE` (`codigo`, `nombres`, `apellidos`, `correo`, `contrasenia`, `estado`, `id_pais`)
SELECT '0000001', 'Cliente', 'G4D', 'cliente@g4d.com', '$2a$10$VaieX.Ypss96B9W9kYndFu/ZW6f4EGApGFqgJKsBDdCEcu9.dQ2Qq', 'OFFLINE', p.`id`
FROM `PAIS` p WHERE p.`nombre` = 'Perú';
