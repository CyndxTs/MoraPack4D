-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema morapack4d
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `morapack4d` ;

-- -----------------------------------------------------
-- Schema morapack4d
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `morapack4d` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `morapack4d` ;

-- -----------------------------------------------------
-- Table `morapack4d`.`continente`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`continente` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`continente` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_CONTINENTE_NOMBRE` (`nombre` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`pais`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`pais` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`pais` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(30) NOT NULL,
  `huso_horario` INT NOT NULL,
  `id_continente` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_PAIS_NOMBRE` (`nombre` ASC) VISIBLE,
  INDEX `IDX_PAIS_CONTINENTE` (`id_continente` ASC) VISIBLE,
  CONSTRAINT `FK_PAIS_CONTINENTE`
    FOREIGN KEY (`id_continente`)
    REFERENCES `morapack4d`.`continente` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 46
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`aeropuerto`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`aeropuerto` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`aeropuerto` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(4) NOT NULL,
  `ciudad` VARCHAR(30) NOT NULL,
  `alias` VARCHAR(4) NOT NULL,
  `capacidad` INT NOT NULL,
  `latitud_dms` VARCHAR(20) NOT NULL,
  `latitud_dec` DOUBLE NOT NULL,
  `longitud_dms` VARCHAR(20) NOT NULL,
  `longitud_dec` DOUBLE NOT NULL,
  `es_sede` TINYINT(1) NOT NULL DEFAULT '0',
  `id_pais` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_AEROPUERTO_CODIGO` (`codigo` ASC) VISIBLE,
  UNIQUE INDEX `UK_AEROPUERTO_ALIAS` (`alias` ASC) VISIBLE,
  INDEX `IDX_AEROPUERTO_PAIS` (`id_pais` ASC) VISIBLE,
  CONSTRAINT `FK_AEROPUERTO_PAIS`
    FOREIGN KEY (`id_pais`)
    REFERENCES `morapack4d`.`pais` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 31
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`plan` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`plan` (
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
  UNIQUE INDEX `UK_PLAN_CODIGO` (`codigo` ASC) VISIBLE,
  INDEX `IDX_PLAN_ORIGEN` (`id_aeropuerto_origen` ASC) VISIBLE,
  INDEX `IDX_PLAN_DESTINO` (`id_aeropuerto_destino` ASC) VISIBLE,
  CONSTRAINT `FK_PLAN_AEROPUERTO_DESTINO`
    FOREIGN KEY (`id_aeropuerto_destino`)
    REFERENCES `morapack4d`.`aeropuerto` (`id`),
  CONSTRAINT `FK_PLAN_AEROPUERTO_ORIGEN`
    FOREIGN KEY (`id_aeropuerto_origen`)
    REFERENCES `morapack4d`.`aeropuerto` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 2867
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`evento`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`evento` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`evento` (
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
  UNIQUE INDEX `UK_EVENTO_CODIGO` (`codigo` ASC) VISIBLE,
  INDEX `IDX_EVENTO_PLAN` (`id_plan` ASC) VISIBLE,
  INDEX `IDX_EVENTO_ESCENARIO_INICIO` (`tipo_escenario` ASC, `fh_inicio` ASC) VISIBLE,
  CONSTRAINT `FK_EVENTO_PLAN`
    FOREIGN KEY (`id_plan`)
    REFERENCES `morapack4d`.`plan` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`ruta`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`ruta` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`ruta` (
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
  UNIQUE INDEX `UK_RUTA_CODIGO` (`codigo` ASC) VISIBLE,
  INDEX `IDX_RUTA_ORIGEN` (`id_aeropuerto_origen` ASC) VISIBLE,
  INDEX `IDX_RUTA_DESTINO` (`id_aeropuerto_destino` ASC) VISIBLE,
  CONSTRAINT `FK_RUTA_AEROPUERTO_DESTINO`
    FOREIGN KEY (`id_aeropuerto_destino`)
    REFERENCES `morapack4d`.`aeropuerto` (`id`),
  CONSTRAINT `FK_RUTA_AEROPUERTO_ORIGEN`
    FOREIGN KEY (`id_aeropuerto_origen`)
    REFERENCES `morapack4d`.`aeropuerto` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 4
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`usuario`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`usuario` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`usuario` (
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
  UNIQUE INDEX `UK_USUARIO_CODIGO` (`codigo` ASC) VISIBLE,
  UNIQUE INDEX `UK_USUARIO_CORREO` (`correo` ASC) VISIBLE,
  INDEX `IDX_USUARIO_ROL` (`rol` ASC) VISIBLE,
  INDEX `IDX_USUARIO_PAIS` (`id_pais` ASC) VISIBLE,
  CONSTRAINT `FK_USUARIO_PAIS`
    FOREIGN KEY (`id_pais`)
    REFERENCES `morapack4d`.`pais` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`pedido`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`pedido` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`pedido` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `cantidad_solicitada` INT NOT NULL,
  `fh_generacion` DATETIME NOT NULL,
  `fh_procesamiento` DATETIME NULL DEFAULT NULL,
  `fh_expiracion` DATETIME NULL DEFAULT NULL,
  `fue_atendido` TINYINT(1) NOT NULL DEFAULT '0',
  `estado` ENUM('ACTIVO', 'CANCELADO', 'CANCELADO_POR_ERROR') NOT NULL DEFAULT 'ACTIVO',
  `tipo_escenario` ENUM('OPERACION', 'SIMULACION') NOT NULL DEFAULT 'SIMULACION',
  `cod_cliente_simulado` VARCHAR(20) NULL DEFAULT NULL,
  `id_cliente` INT UNSIGNED NULL DEFAULT NULL,
  `id_aeropuerto_destino` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_PEDIDO_CODIGO_TIPO_ESCENARIO` (`codigo` ASC, `tipo_escenario` ASC) VISIBLE,
  INDEX `IDX_PEDIDO_ESCENARIO_GENERACION` (`tipo_escenario` ASC, `estado` ASC, `fh_generacion` ASC) VISIBLE,
  INDEX `IDX_PEDIDO_CLIENTE` (`id_cliente` ASC) VISIBLE,
  INDEX `IDX_PEDIDO_DESTINO` (`id_aeropuerto_destino` ASC) VISIBLE,
  CONSTRAINT `FK_PEDIDO_AEROPUERTO`
    FOREIGN KEY (`id_aeropuerto_destino`)
    REFERENCES `morapack4d`.`aeropuerto` (`id`),
  CONSTRAINT `FK_PEDIDO_CLIENTE`
    FOREIGN KEY (`id_cliente`)
    REFERENCES `morapack4d`.`usuario` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 4227723
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`segmentacion`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`segmentacion` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`segmentacion` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `fh_aplicacion` DATETIME NOT NULL,
  `fh_sustitucion` DATETIME NULL DEFAULT NULL,
  `id_pedido` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_SEGMENTACION_CODIGO` (`codigo` ASC) VISIBLE,
  INDEX `IDX_SEGMENTACION_PEDIDO` (`id_pedido` ASC) VISIBLE,
  CONSTRAINT `FK_SEGMENTACION_PEDIDO`
    FOREIGN KEY (`id_pedido`)
    REFERENCES `morapack4d`.`pedido` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`lote`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`lote` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`lote` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `tamanio` INT NOT NULL,
  `estado` ENUM('PLANIFICADO', 'POR_REPLANIFICAR', 'REPLANIFICADO', 'ENTREGADO') NOT NULL DEFAULT 'PLANIFICADO',
  `id_segmentacion` BIGINT UNSIGNED NOT NULL,
  `id_ruta` BIGINT UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_LOTE_CODIGO` (`codigo` ASC) VISIBLE,
  INDEX `IDX_LOTE_SEGMENTACION` (`id_segmentacion` ASC) VISIBLE,
  INDEX `IDX_LOTE_RUTA` (`id_ruta` ASC) VISIBLE,
  CONSTRAINT `FK_LOTE_RUTA`
    FOREIGN KEY (`id_ruta`)
    REFERENCES `morapack4d`.`ruta` (`id`),
  CONSTRAINT `FK_LOTE_SEGMENTACION`
    FOREIGN KEY (`id_segmentacion`)
    REFERENCES `morapack4d`.`segmentacion` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 5
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`parametros`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`parametros` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`parametros` (
  `id` INT UNSIGNED NOT NULL DEFAULT '1',
  `max_dias_entrega_intracontinental` INT NOT NULL DEFAULT '2',
  `max_dias_entrega_intercontinental` INT NOT NULL DEFAULT '3',
  `max_horas_recojo` DOUBLE NOT NULL DEFAULT '2',
  `max_horas_estancia` DOUBLE NOT NULL DEFAULT '12',
  `min_horas_estancia` DOUBLE NOT NULL DEFAULT '1',
  `prob_replanificacion` DOUBLE NOT NULL DEFAULT '0.35',
  `d_min` DOUBLE NOT NULL DEFAULT '0.005',
  `i_max` INT NOT NULL DEFAULT '3',
  `ele_min` INT NOT NULL DEFAULT '1',
  `ele_max` INT NOT NULL DEFAULT '2',
  `k_min` INT NOT NULL DEFAULT '3',
  `k_max` INT NOT NULL DEFAULT '5',
  `n_max` INT NOT NULL DEFAULT '6',
  `t_max` INT NOT NULL DEFAULT '7',
  `f_ua` DOUBLE NOT NULL DEFAULT '1.015',
  `f_ut` DOUBLE NOT NULL DEFAULT '5000',
  `f_de` DOUBLE NOT NULL DEFAULT '2000',
  `f_do` DOUBLE NOT NULL DEFAULT '3000',
  `f_lj` DOUBLE NOT NULL DEFAULT '0.0085',
  `replanificacion_automatica` TINYINT(1) NOT NULL DEFAULT '1',
  `minutos_espera_replanificacion` INT NOT NULL DEFAULT '15',
  `replanificar_al_registrar_evento` TINYINT(1) NOT NULL DEFAULT '1',
  `segundos_umbral_replanificacion` INT NOT NULL DEFAULT '180',
  `purga_automatica` TINYINT(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`registro`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`registro` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`registro` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `sigue_vigente` TINYINT(1) NOT NULL DEFAULT '1',
  `fh_ingreso` DATETIME NOT NULL,
  `fh_egreso` DATETIME NOT NULL,
  `id_aeropuerto` INT UNSIGNED NOT NULL,
  `id_lote` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_REGISTRO_CODIGO` (`codigo` ASC) VISIBLE,
  INDEX `IDX_REGISTRO_AEROPUERTO` (`id_aeropuerto` ASC) VISIBLE,
  INDEX `IDX_REGISTRO_LOTE` (`id_lote` ASC) VISIBLE,
  CONSTRAINT `FK_REGISTRO_AEROPUERTO`
    FOREIGN KEY (`id_aeropuerto`)
    REFERENCES `morapack4d`.`aeropuerto` (`id`),
  CONSTRAINT `FK_REGISTRO_LOTE`
    FOREIGN KEY (`id_lote`)
    REFERENCES `morapack4d`.`lote` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`vuelo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`vuelo` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`vuelo` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `capacidad` INT NOT NULL,
  `capacidad_disponible` INT NOT NULL,
  `fh_salida` DATETIME NOT NULL,
  `fh_llegada` DATETIME NOT NULL,
  `id_plan` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_VUELO_CODIGO` (`codigo` ASC) VISIBLE,
  INDEX `IDX_VUELO_PLAN` (`id_plan` ASC) VISIBLE,
  CONSTRAINT `FK_VUELO_PLAN`
    FOREIGN KEY (`id_plan`)
    REFERENCES `morapack4d`.`plan` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 6
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`ruta_por_vuelo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`ruta_por_vuelo` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`ruta_por_vuelo` (
  `id_ruta` BIGINT UNSIGNED NOT NULL,
  `id_vuelo` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id_ruta`, `id_vuelo`),
  INDEX `IDX_RUTA_POR_VUELO_VUELO` (`id_vuelo` ASC) VISIBLE,
  CONSTRAINT `FK_RUTA_POR_VUELO_RUTA`
    FOREIGN KEY (`id_ruta`)
    REFERENCES `morapack4d`.`ruta` (`id`),
  CONSTRAINT `FK_RUTA_POR_VUELO_VUELO`
    FOREIGN KEY (`id_vuelo`)
    REFERENCES `morapack4d`.`vuelo` (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`sesion`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`sesion` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`sesion` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `id_usuario` INT UNSIGNED NOT NULL,
  `hash_token` CHAR(64) NOT NULL,
  `dispositivo` VARCHAR(255) NULL DEFAULT NULL,
  `fh_inicio` DATETIME NOT NULL,
  `fh_expiracion` DATETIME NOT NULL,
  `fh_revocacion` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_SESION_HASH` (`hash_token` ASC) VISIBLE,
  INDEX `IDX_SESION_USUARIO` (`id_usuario` ASC) VISIBLE,
  INDEX `IDX_SESION_EXPIRACION` (`fh_expiracion` ASC) VISIBLE,
  CONSTRAINT `FK_SESION_USUARIO`
    FOREIGN KEY (`id_usuario`)
    REFERENCES `morapack4d`.`usuario` (`id`)
    ON DELETE CASCADE)
ENGINE = InnoDB
AUTO_INCREMENT = 7
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `morapack4d`.`verificacion_correo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`verificacion_correo` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`verificacion_correo` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `correo` VARCHAR(80) NOT NULL,
  `proposito` ENUM('REGISTRO', 'RECUPERACION') NOT NULL,
  `hash_codigo` VARCHAR(255) NOT NULL,
  `nombres` VARCHAR(60) NULL DEFAULT NULL,
  `apellidos` VARCHAR(60) NULL DEFAULT NULL,
  `contrasenia` VARCHAR(255) NULL DEFAULT NULL,
  `id_pais` INT UNSIGNED NULL DEFAULT NULL,
  `intentos` INT NOT NULL DEFAULT '0',
  `fh_envio` DATETIME NOT NULL,
  `fh_expiracion` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_VERIFICACION_CORREO_PROPOSITO` (`correo` ASC, `proposito` ASC) VISIBLE,
  INDEX `FK_VERIFICACION_PAIS` (`id_pais` ASC) VISIBLE,
  CONSTRAINT `FK_VERIFICACION_PAIS`
    FOREIGN KEY (`id_pais`)
    REFERENCES `morapack4d`.`pais` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 2
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
