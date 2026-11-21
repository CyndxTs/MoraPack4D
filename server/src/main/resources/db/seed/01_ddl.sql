-- =====================================================================================
--  MoraPack4D - DDL
--  - Sin schema fijo: se ejecuta sobre la BD indicada en la URL JDBC (local o TiDB Cloud).
--  - Todas las fechas/horas se guardan en UTC. Convención: fh_ = DATETIME, f_ = DATE, h_ = TIME.
--  - duracion (PLAN, RUTA) se expresa en horas (DOUBLE).
-- =====================================================================================
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `RUTA_POR_VUELO`;
DROP TABLE IF EXISTS `REGISTRO`;
DROP TABLE IF EXISTS `LOTE`;
DROP TABLE IF EXISTS `SEGMENTACION`;
DROP TABLE IF EXISTS `PEDIDO`;
DROP TABLE IF EXISTS `RUTA`;
DROP TABLE IF EXISTS `VUELO`;
DROP TABLE IF EXISTS `EVENTO`;
DROP TABLE IF EXISTS `PLAN`;
DROP TABLE IF EXISTS `AEROPUERTO`;
DROP TABLE IF EXISTS `VERIFICACION_CORREO`;
DROP TABLE IF EXISTS `SESION`;
DROP TABLE IF EXISTS `USUARIO`;
DROP TABLE IF EXISTS `CLIENTE`;
DROP TABLE IF EXISTS `TRABAJADOR`;
DROP TABLE IF EXISTS `ADMINISTRADOR`;
DROP TABLE IF EXISTS `PAIS`;
DROP TABLE IF EXISTS `CONTINENTE`;
DROP TABLE IF EXISTS `PARAMETROS`;

-- -----------------------------------------------------
-- CONTINENTE
-- -----------------------------------------------------
CREATE TABLE `CONTINENTE` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_CONTINENTE_NOMBRE` (`nombre`)
);

-- -----------------------------------------------------
-- PAIS (países soportados por el sistema)
-- -----------------------------------------------------
CREATE TABLE `PAIS` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(30) NOT NULL,
  `huso_horario` INT NOT NULL,
  `id_continente` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_PAIS_NOMBRE` (`nombre`),
  INDEX `IDX_PAIS_CONTINENTE` (`id_continente`),
  CONSTRAINT `FK_PAIS_CONTINENTE` FOREIGN KEY (`id_continente`) REFERENCES `CONTINENTE` (`id`)
);

-- -----------------------------------------------------
-- USUARIO (clientes, trabajadores y administradores; los clientes de simulación viven en RAM)
--  - codigo     : CLIENTE '0000001' (7 dígitos) | TRABAJADOR 'TRA0001' | ADMINISTRADOR 'ADM0001'.
--                 Al cambiar de rol se reasigna con el siguiente código libre del rol destino.
--  - contrasenia: NULL si la cuenta se creó con Google y aún no definió una contraseña.
--  - apellidos  : opcional (se completa en "Mi cuenta").
-- -----------------------------------------------------
CREATE TABLE `USUARIO` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `rol` ENUM('CLIENTE', 'TRABAJADOR', 'ADMINISTRADOR') NOT NULL DEFAULT 'CLIENTE',
  `nombres` VARCHAR(60) NOT NULL,
  `apellidos` VARCHAR(60) NULL DEFAULT NULL,
  `correo` VARCHAR(80) NOT NULL,
  `contrasenia` VARCHAR(255) NULL DEFAULT NULL,
  `estado` ENUM('ONLINE', 'OFFLINE', 'DISABLED') NOT NULL DEFAULT 'OFFLINE',
  `id_pais` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_USUARIO_CODIGO` (`codigo`),
  UNIQUE INDEX `UK_USUARIO_CORREO` (`correo`),
  INDEX `IDX_USUARIO_ROL` (`rol`),
  INDEX `IDX_USUARIO_PAIS` (`id_pais`),
  CONSTRAINT `FK_USUARIO_PAIS` FOREIGN KEY (`id_pais`) REFERENCES `PAIS` (`id`)
);

-- -----------------------------------------------------
-- SESION (una por dispositivo; dura 12 h desde el inicio de sesión, no se extiende)
--  - Solo se guarda el hash SHA-256 del token de refresco.
-- -----------------------------------------------------
CREATE TABLE `SESION` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_usuario` INT UNSIGNED NOT NULL,
  `hash_token` CHAR(64) NOT NULL,
  `dispositivo` VARCHAR(255) NULL DEFAULT NULL,
  `fh_inicio` DATETIME NOT NULL,
  `fh_expiracion` DATETIME NOT NULL,
  `fh_revocacion` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_SESION_HASH` (`hash_token`),
  INDEX `IDX_SESION_USUARIO` (`id_usuario`),
  INDEX `IDX_SESION_EXPIRACION` (`fh_expiracion`),
  CONSTRAINT `FK_SESION_USUARIO` FOREIGN KEY (`id_usuario`) REFERENCES `USUARIO` (`id`) ON DELETE CASCADE
);

-- -----------------------------------------------------
-- VERIFICACION_CORREO (códigos de 6 dígitos enviados por correo)
--  - REGISTRO  : guarda los datos del registro pendiente (el USUARIO se crea solo con el código correcto).
--  - RECUPERACION: restablecer contraseña.
--  - Solo se guarda el hash del código. Una fila vigente por (correo, proposito).
-- -----------------------------------------------------
CREATE TABLE `VERIFICACION_CORREO` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `correo` VARCHAR(80) NOT NULL,
  `proposito` ENUM('REGISTRO', 'RECUPERACION') NOT NULL,
  `hash_codigo` VARCHAR(255) NOT NULL,
  `nombres` VARCHAR(60) NULL DEFAULT NULL,
  `apellidos` VARCHAR(60) NULL DEFAULT NULL,
  `contrasenia` VARCHAR(255) NULL DEFAULT NULL,
  `id_pais` INT UNSIGNED NULL DEFAULT NULL,
  `intentos` INT NOT NULL DEFAULT 0,
  `fh_envio` DATETIME NOT NULL,
  `fh_expiracion` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_VERIFICACION_CORREO_PROPOSITO` (`correo`, `proposito`),
  CONSTRAINT `FK_VERIFICACION_PAIS` FOREIGN KEY (`id_pais`) REFERENCES `PAIS` (`id`)
);

-- -----------------------------------------------------
-- AEROPUERTO
-- -----------------------------------------------------
CREATE TABLE `AEROPUERTO` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(4) NOT NULL,
  `ciudad` VARCHAR(30) NOT NULL,
  `alias` VARCHAR(4) NOT NULL,
  `capacidad` INT NOT NULL,
  `latitud_dms` VARCHAR(20) NOT NULL,
  `latitud_dec` DOUBLE NOT NULL,
  `longitud_dms` VARCHAR(20) NOT NULL,
  `longitud_dec` DOUBLE NOT NULL,
  `es_sede` TINYINT(1) NOT NULL DEFAULT 0,
  `id_pais` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_AEROPUERTO_CODIGO` (`codigo`),
  UNIQUE INDEX `UK_AEROPUERTO_ALIAS` (`alias`),
  INDEX `IDX_AEROPUERTO_PAIS` (`id_pais`),
  CONSTRAINT `FK_AEROPUERTO_PAIS` FOREIGN KEY (`id_pais`) REFERENCES `PAIS` (`id`)
);

-- -----------------------------------------------------
-- PEDIDO
--  - OPERACION : id_cliente obligatorio (el cliente existe en BD).
--  - SIMULACION: id_cliente NULL; el cliente se identifica por cod_cliente_simulado y se genera en RAM.
--  - estado    : ACTIVO | CANCELADO (por el cliente, antes de que salga su primer vuelo)
--                | CANCELADO_POR_ERROR (no se pudo atender/reatender dentro de su plazo)
-- -----------------------------------------------------
CREATE TABLE `PEDIDO` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `cantidad_solicitada` INT NOT NULL,
  `fh_generacion` DATETIME NOT NULL,
  `fh_procesamiento` DATETIME NULL DEFAULT NULL,
  `fh_expiracion` DATETIME NULL DEFAULT NULL,
  `fue_atendido` TINYINT(1) NOT NULL DEFAULT 0,
  `estado` ENUM('ACTIVO', 'CANCELADO', 'CANCELADO_POR_ERROR') NOT NULL DEFAULT 'ACTIVO',
  `tipo_escenario` ENUM('OPERACION', 'SIMULACION') NOT NULL DEFAULT 'SIMULACION',
  `cod_cliente_simulado` VARCHAR(20) NULL DEFAULT NULL,
  `id_cliente` INT UNSIGNED NULL DEFAULT NULL,
  `id_aeropuerto_destino` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_PEDIDO_CODIGO_TIPO_ESCENARIO` (`codigo`, `tipo_escenario`),
  INDEX `IDX_PEDIDO_ESCENARIO_GENERACION` (`tipo_escenario`, `estado`, `fh_generacion`),
  INDEX `IDX_PEDIDO_CLIENTE` (`id_cliente`),
  INDEX `IDX_PEDIDO_DESTINO` (`id_aeropuerto_destino`),
  CONSTRAINT `FK_PEDIDO_CLIENTE` FOREIGN KEY (`id_cliente`) REFERENCES `USUARIO` (`id`),
  CONSTRAINT `FK_PEDIDO_AEROPUERTO` FOREIGN KEY (`id_aeropuerto_destino`) REFERENCES `AEROPUERTO` (`id`),
  CONSTRAINT `CK_PEDIDO_CLIENTE_ESCENARIO` CHECK (
    (`tipo_escenario` = 'OPERACION' AND `id_cliente` IS NOT NULL) OR
    (`tipo_escenario` = 'SIMULACION' AND `cod_cliente_simulado` IS NOT NULL)
  )
);

-- -----------------------------------------------------
-- SEGMENTACION
-- -----------------------------------------------------
CREATE TABLE `SEGMENTACION` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `fh_aplicacion` DATETIME NOT NULL,
  `fh_sustitucion` DATETIME NULL DEFAULT NULL,
  `id_pedido` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_SEGMENTACION_CODIGO` (`codigo`),
  INDEX `IDX_SEGMENTACION_PEDIDO` (`id_pedido`),
  CONSTRAINT `FK_SEGMENTACION_PEDIDO` FOREIGN KEY (`id_pedido`) REFERENCES `PEDIDO` (`id`)
);

-- -----------------------------------------------------
-- RUTA
-- -----------------------------------------------------
CREATE TABLE `RUTA` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `duracion` DOUBLE NOT NULL,
  `distancia` DOUBLE NOT NULL,
  `fh_salida` DATETIME NOT NULL,
  `fh_llegada` DATETIME NOT NULL,
  `tipo` ENUM('INTRACONTINENTAL', 'INTERCONTINENTAL') NOT NULL,
  `estado` ENUM('REVISION_PENDIENTE', 'FINALIZADA', 'OPERATIVA', 'DESHABILITADA') NOT NULL DEFAULT 'OPERATIVA',
  `id_aeropuerto_origen` INT UNSIGNED NOT NULL,
  `id_aeropuerto_destino` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_RUTA_CODIGO` (`codigo`),
  INDEX `IDX_RUTA_ORIGEN` (`id_aeropuerto_origen`),
  INDEX `IDX_RUTA_DESTINO` (`id_aeropuerto_destino`),
  CONSTRAINT `FK_RUTA_AEROPUERTO_ORIGEN` FOREIGN KEY (`id_aeropuerto_origen`) REFERENCES `AEROPUERTO` (`id`),
  CONSTRAINT `FK_RUTA_AEROPUERTO_DESTINO` FOREIGN KEY (`id_aeropuerto_destino`) REFERENCES `AEROPUERTO` (`id`)
);

-- -----------------------------------------------------
-- LOTE
--  id_ruta NULL solo para entregas directas (pedido de OPERACION cuyo destino es una sede)
-- -----------------------------------------------------
CREATE TABLE `LOTE` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `tamanio` INT NOT NULL,
  `estado` ENUM('PLANIFICADO', 'POR_REPLANIFICAR', 'REPLANIFICADO', 'ENTREGADO') NOT NULL DEFAULT 'PLANIFICADO',
  `id_segmentacion` BIGINT UNSIGNED NOT NULL,
  `id_ruta` BIGINT UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_LOTE_CODIGO` (`codigo`),
  INDEX `IDX_LOTE_SEGMENTACION` (`id_segmentacion`),
  INDEX `IDX_LOTE_RUTA` (`id_ruta`),
  CONSTRAINT `FK_LOTE_SEGMENTACION` FOREIGN KEY (`id_segmentacion`) REFERENCES `SEGMENTACION` (`id`),
  CONSTRAINT `FK_LOTE_RUTA` FOREIGN KEY (`id_ruta`) REFERENCES `RUTA` (`id`)
);

-- -----------------------------------------------------
-- REGISTRO (log de ingreso/egreso de lotes en aeropuertos; sigue_vigente = soft delete)
--  fh_egreso siempre existe: en destino final = llegada + tiempo de recojo.
-- -----------------------------------------------------
CREATE TABLE `REGISTRO` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `sigue_vigente` TINYINT(1) NOT NULL DEFAULT 1,
  `fh_ingreso` DATETIME NOT NULL,
  `fh_egreso` DATETIME NOT NULL,
  `id_aeropuerto` INT UNSIGNED NOT NULL,
  `id_lote` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_REGISTRO_CODIGO` (`codigo`),
  INDEX `IDX_REGISTRO_AEROPUERTO` (`id_aeropuerto`),
  INDEX `IDX_REGISTRO_LOTE` (`id_lote`),
  CONSTRAINT `FK_REGISTRO_AEROPUERTO` FOREIGN KEY (`id_aeropuerto`) REFERENCES `AEROPUERTO` (`id`),
  CONSTRAINT `FK_REGISTRO_LOTE` FOREIGN KEY (`id_lote`) REFERENCES `LOTE` (`id`)
);

-- -----------------------------------------------------
-- PLAN (plantilla de vuelo diario; horas en UTC)
-- -----------------------------------------------------
CREATE TABLE `PLAN` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `capacidad` INT NOT NULL,
  `duracion` DOUBLE NOT NULL,
  `distancia` DOUBLE NOT NULL,
  `h_salida` TIME NOT NULL,
  `h_llegada` TIME NOT NULL,
  `id_aeropuerto_origen` INT UNSIGNED NOT NULL,
  `id_aeropuerto_destino` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_PLAN_CODIGO` (`codigo`),
  INDEX `IDX_PLAN_ORIGEN` (`id_aeropuerto_origen`),
  INDEX `IDX_PLAN_DESTINO` (`id_aeropuerto_destino`),
  CONSTRAINT `FK_PLAN_AEROPUERTO_ORIGEN` FOREIGN KEY (`id_aeropuerto_origen`) REFERENCES `AEROPUERTO` (`id`),
  CONSTRAINT `FK_PLAN_AEROPUERTO_DESTINO` FOREIGN KEY (`id_aeropuerto_destino`) REFERENCES `AEROPUERTO` (`id`)
);

-- -----------------------------------------------------
-- VUELO (instancia de un plan; 1 vuelo activo por día por plan, validado por el algoritmo)
--  capacidad = capacidad de esta instancia (la del plan, o la de un REAJUSTE_CAPACIDAD)
-- -----------------------------------------------------
CREATE TABLE `VUELO` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `capacidad` INT NOT NULL,
  `capacidad_disponible` INT NOT NULL,
  `fh_salida` DATETIME NOT NULL,
  `fh_llegada` DATETIME NOT NULL,
  `id_plan` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_VUELO_CODIGO` (`codigo`),
  INDEX `IDX_VUELO_PLAN` (`id_plan`),
  CONSTRAINT `FK_VUELO_PLAN` FOREIGN KEY (`id_plan`) REFERENCES `PLAN` (`id`)
);

-- -----------------------------------------------------
-- RUTA_POR_VUELO
-- -----------------------------------------------------
CREATE TABLE `RUTA_POR_VUELO` (
  `id_ruta` BIGINT UNSIGNED NOT NULL,
  `id_vuelo` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id_ruta`, `id_vuelo`),
  INDEX `IDX_RUTA_POR_VUELO_VUELO` (`id_vuelo`),
  CONSTRAINT `FK_RUTA_POR_VUELO_RUTA` FOREIGN KEY (`id_ruta`) REFERENCES `RUTA` (`id`),
  CONSTRAINT `FK_RUTA_POR_VUELO_VUELO` FOREIGN KEY (`id_vuelo`) REFERENCES `VUELO` (`id`)
);

-- -----------------------------------------------------
-- EVENTO (afecta al vuelo de un plan cuya salida/llegada originales son [fh_inicio, fh_fin], en UTC)
--  - REPROGRAMACION     : fh_nueva_salida y fh_nueva_llegada obligatorias.
--  - CANCELACION        : sin datos adicionales.
--  - REAJUSTE_CAPACIDAD : nueva_capacidad obligatoria.
-- -----------------------------------------------------
CREATE TABLE `EVENTO` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `tipo` ENUM('REPROGRAMACION', 'CANCELACION', 'REAJUSTE_CAPACIDAD') NOT NULL DEFAULT 'CANCELACION',
  `fh_inicio` DATETIME NOT NULL,
  `fh_fin` DATETIME NOT NULL,
  `fh_nueva_salida` DATETIME NULL DEFAULT NULL,
  `fh_nueva_llegada` DATETIME NULL DEFAULT NULL,
  `nueva_capacidad` INT NULL DEFAULT NULL,
  `tipo_escenario` ENUM('OPERACION', 'SIMULACION') NOT NULL DEFAULT 'SIMULACION',
  `id_plan` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_EVENTO_CODIGO` (`codigo`),
  INDEX `IDX_EVENTO_PLAN` (`id_plan`),
  INDEX `IDX_EVENTO_ESCENARIO_INICIO` (`tipo_escenario`, `fh_inicio`),
  CONSTRAINT `FK_EVENTO_PLAN` FOREIGN KEY (`id_plan`) REFERENCES `PLAN` (`id`),
  CONSTRAINT `CK_EVENTO_TIPO` CHECK (
    (`tipo` = 'CANCELACION') OR
    (`tipo` = 'REPROGRAMACION' AND `fh_nueva_salida` IS NOT NULL AND `fh_nueva_llegada` IS NOT NULL) OR
    (`tipo` = 'REAJUSTE_CAPACIDAD' AND `nueva_capacidad` IS NOT NULL)
  )
);

-- -----------------------------------------------------
-- PARAMETROS (fila única id = 1; valores default de la operación)
-- -----------------------------------------------------
CREATE TABLE `PARAMETROS` (
  `id` INT UNSIGNED NOT NULL DEFAULT 1,
  `max_dias_entrega_intracontinental` INT NOT NULL DEFAULT 2,
  `max_dias_entrega_intercontinental` INT NOT NULL DEFAULT 3,
  `max_horas_recojo` DOUBLE NOT NULL DEFAULT 2.0,
  `max_horas_estancia` DOUBLE NOT NULL DEFAULT 12.0,
  `min_horas_estancia` DOUBLE NOT NULL DEFAULT 1.0,
  `prob_replanificacion` DOUBLE NOT NULL DEFAULT 0.350,
  `d_min` DOUBLE NOT NULL DEFAULT 0.005,
  `i_max` INT NOT NULL DEFAULT 3,
  `ele_min` INT NOT NULL DEFAULT 1,
  `ele_max` INT NOT NULL DEFAULT 2,
  `k_min` INT NOT NULL DEFAULT 3,
  `k_max` INT NOT NULL DEFAULT 5,
  `n_max` INT NOT NULL DEFAULT 6,
  `t_max` INT NOT NULL DEFAULT 7,
  `f_ua` DOUBLE NOT NULL DEFAULT 1.015,
  `f_ut` DOUBLE NOT NULL DEFAULT 5000.0,
  `f_de` DOUBLE NOT NULL DEFAULT 2000.0,
  `f_do` DOUBLE NOT NULL DEFAULT 3000.0,
  `f_lj` DOUBLE NOT NULL DEFAULT 0.0085,
  `replanificacion_automatica` BOOLEAN NOT NULL DEFAULT TRUE,
  `minutos_espera_replanificacion` INT NOT NULL DEFAULT 15,
  `replanificar_al_registrar_evento` BOOLEAN NOT NULL DEFAULT TRUE,
  `segundos_umbral_replanificacion` INT NOT NULL DEFAULT 180,
  `purga_automatica` BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`id`),
  CONSTRAINT `CK_PARAMETROS_FILA_UNICA` CHECK (`id` = 1),
  CONSTRAINT `CK_PARAMETROS_ESPERA` CHECK (`minutos_espera_replanificacion` BETWEEN 0 AND 1440),
  CONSTRAINT `CK_PARAMETROS_UMBRAL` CHECK (`segundos_umbral_replanificacion` BETWEEN 120 AND 240)
);

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
