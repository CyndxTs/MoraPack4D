-- =====================================================================================
--  MoraPack4D - DML: continentes y países soportados
--  - Tomados del archivo de aeropuertos (mismos nombres y huso horario GMT), para que la importación de
--    aeropuertos los encuentre. Un aeropuerto cuyo país no esté aquí cancela la importación.
--  - Para agregar un país nuevo después: importación unitaria PAIS.
-- =====================================================================================

INSERT INTO `CONTINENTE` (`nombre`) VALUES
  ('America del Sur'),
  ('Europa'),
  ('Asia');

-- America del Sur
INSERT INTO `PAIS` (`nombre`, `huso_horario`, `id_continente`)
SELECT v.`nombre`, v.`huso_horario`, c.`id`
FROM (
  SELECT 'Colombia' AS `nombre`, -5 AS `huso_horario`
  UNION ALL SELECT 'Ecuador' AS `nombre`, -5 AS `huso_horario`
  UNION ALL SELECT 'Venezuela' AS `nombre`, -4 AS `huso_horario`
  UNION ALL SELECT 'Brasil' AS `nombre`, -3 AS `huso_horario`
  UNION ALL SELECT 'Perú' AS `nombre`, -5 AS `huso_horario`
  UNION ALL SELECT 'Bolivia' AS `nombre`, -4 AS `huso_horario`
  UNION ALL SELECT 'Chile' AS `nombre`, -3 AS `huso_horario`
  UNION ALL SELECT 'Argentina' AS `nombre`, -3 AS `huso_horario`
  UNION ALL SELECT 'Paraguay' AS `nombre`, -4 AS `huso_horario`
  UNION ALL SELECT 'Uruguay' AS `nombre`, -3 AS `huso_horario`
) v
JOIN `CONTINENTE` c ON c.`nombre` = 'America del Sur';

-- Europa
INSERT INTO `PAIS` (`nombre`, `huso_horario`, `id_continente`)
SELECT v.`nombre`, v.`huso_horario`, c.`id`
FROM (
  SELECT 'Albania' AS `nombre`, 2 AS `huso_horario`
  UNION ALL SELECT 'Alemania' AS `nombre`, 2 AS `huso_horario`
  UNION ALL SELECT 'Austria' AS `nombre`, 2 AS `huso_horario`
  UNION ALL SELECT 'Belgica' AS `nombre`, 2 AS `huso_horario`
  UNION ALL SELECT 'Bielorrusia' AS `nombre`, 3 AS `huso_horario`
  UNION ALL SELECT 'Bulgaria' AS `nombre`, 3 AS `huso_horario`
  UNION ALL SELECT 'Checa' AS `nombre`, 2 AS `huso_horario`
  UNION ALL SELECT 'Croacia' AS `nombre`, 2 AS `huso_horario`
  UNION ALL SELECT 'Dinamarca' AS `nombre`, 2 AS `huso_horario`
  UNION ALL SELECT 'Holanda' AS `nombre`, 2 AS `huso_horario`
) v
JOIN `CONTINENTE` c ON c.`nombre` = 'Europa';

-- Asia
INSERT INTO `PAIS` (`nombre`, `huso_horario`, `id_continente`)
SELECT v.`nombre`, v.`huso_horario`, c.`id`
FROM (
  SELECT 'India' AS `nombre`, 5 AS `huso_horario`
  UNION ALL SELECT 'Siria' AS `nombre`, 3 AS `huso_horario`
  UNION ALL SELECT 'Arabia Saudita' AS `nombre`, 3 AS `huso_horario`
  UNION ALL SELECT 'Emiratos A.U' AS `nombre`, 4 AS `huso_horario`
  UNION ALL SELECT 'Afganistan' AS `nombre`, 4 AS `huso_horario`
  UNION ALL SELECT 'Oman' AS `nombre`, 4 AS `huso_horario`
  UNION ALL SELECT 'Yemen' AS `nombre`, 3 AS `huso_horario`
  UNION ALL SELECT 'Pakistan' AS `nombre`, 5 AS `huso_horario`
  UNION ALL SELECT 'Azerbaiyan' AS `nombre`, 2 AS `huso_horario`
  UNION ALL SELECT 'Jordania' AS `nombre`, 3 AS `huso_horario`
) v
JOIN `CONTINENTE` c ON c.`nombre` = 'Asia';

