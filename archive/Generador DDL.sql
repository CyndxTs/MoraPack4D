-- MoraPack4D - DDL
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

CREATE SCHEMA IF NOT EXISTS `morapack4d` ;
USE `morapack4d` ;

-- -----------------------------------------------------
-- Table `morapack4d`.`CONTINENTE`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`CONTINENTE` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`CONTINENTE` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `nombre_UNIQUE` (`nombre` ASC) VISIBLE
);

-- -----------------------------------------------------
-- Table `morapack4d`.`PAIS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`PAIS` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`PAIS` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(30) NOT NULL,
  `huso_horario` INT NOT NULL,
  `id_continente` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `nombre_UNIQUE` (`nombre` ASC) VISIBLE,
  INDEX `fk_PAIS_CONTINENTE1_idx` (`id_continente` ASC) VISIBLE,
  CONSTRAINT `fk_PAIS_CONTINENTE1`
    FOREIGN KEY (`id_continente`)
    REFERENCES `morapack4d`.`CONTINENTE` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`CLIENTE`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`CLIENTE` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`CLIENTE` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `nombres` VARCHAR(60) NOT NULL,
  `apellidos` VARCHAR(60) NOT NULL,
  `correo` VARCHAR(80) NOT NULL,
  `contrasenia` VARCHAR(255) NOT NULL,
  `estado` ENUM('ONLINE', 'OFFLINE', 'DISABLED') NOT NULL DEFAULT 'OFFLINE',
  `tipo_escenario` ENUM('OPERACION', 'SIMULACION') NOT NULL DEFAULT 'SIMULACION',
  `id_pais` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_CODIGO_TIPO_ESCENARIO` (`codigo` ASC, `tipo_escenario` ASC) VISIBLE,
  UNIQUE INDEX `UK_CORREO_TIPO_ESCENARIO` (`correo` ASC, `tipo_escenario` ASC) VISIBLE,
  INDEX `fk_CLIENTE_PAIS1_idx` (`id_pais` ASC) VISIBLE,
  CONSTRAINT `fk_CLIENTE_PAIS1`
    FOREIGN KEY (`id_pais`)
    REFERENCES `morapack4d`.`PAIS` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`ADMINISTRADOR`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`ADMINISTRADOR` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`ADMINISTRADOR` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `nombres` VARCHAR(60) NOT NULL,
  `apellidos` VARCHAR(60) NOT NULL,
  `correo` VARCHAR(80) NOT NULL,
  `contrasenia` VARCHAR(255) NOT NULL,
  `estado` ENUM('ONLINE', 'OFFLINE', 'DISABLED') NOT NULL DEFAULT 'OFFLINE',
  `id_pais` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  UNIQUE INDEX `correo_UNIQUE` (`correo` ASC) VISIBLE,
  INDEX `fk_ADMINISTRADOR_PAIS1_idx` (`id_pais` ASC) VISIBLE,
  CONSTRAINT `fk_ADMINISTRADOR_PAIS1`
    FOREIGN KEY (`id_pais`)
    REFERENCES `morapack4d`.`PAIS` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`AEROPUERTO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`AEROPUERTO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`AEROPUERTO` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(4) NOT NULL,
  `ciudad` VARCHAR(30) NOT NULL,
  `alias` VARCHAR(4) NOT NULL,
  `capacidad` INT NOT NULL,
  `latitud_dms` VARCHAR(20) NOT NULL,
  `latitud_dec` DOUBLE NOT NULL,
  `longitud_dms` VARCHAR(20) NOT NULL,
  `longitud_dec` DOUBLE NOT NULL,
  `es_sede` TINYINT NOT NULL DEFAULT 0,
  `id_pais` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  UNIQUE INDEX `alias_UNIQUE` (`alias` ASC) VISIBLE,
  INDEX `fk_AEROPUERTO_PAIS1_idx` (`id_pais` ASC) VISIBLE,
  CONSTRAINT `fk_AEROPUERTO_PAIS1`
    FOREIGN KEY (`id_pais`)
    REFERENCES `morapack4d`.`PAIS` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`PEDIDO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`PEDIDO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`PEDIDO` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `cantidad_solicitada` INT NOT NULL,
  `fh_generacion` DATETIME NOT NULL,
  `fh_procesamiento` DATETIME NULL DEFAULT NULL,
  `fh_expiracion` DATETIME NULL DEFAULT NULL,
  `fue_atendido` TINYINT NOT NULL DEFAULT 0,
  `tipo_escenario` ENUM('OPERACION', 'SIMULACION') NOT NULL DEFAULT 'SIMULACION',
  `id_cliente` INT UNSIGNED NOT NULL,
  `id_aeropuerto_destino` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `UK_CODIGO_TIPO_ESCENARIO` (`codigo` ASC, `tipo_escenario` ASC) VISIBLE,
  INDEX `fk_PEDIDO_CLIENTE1_idx` (`id_cliente` ASC) VISIBLE,
  INDEX `fk_PEDIDO_AEROPUERTO1_idx` (`id_aeropuerto_destino` ASC) VISIBLE,
  CONSTRAINT `fk_PEDIDO_CLIENTE1`
    FOREIGN KEY (`id_cliente`)
    REFERENCES `morapack4d`.`CLIENTE` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PEDIDO_AEROPUERTO1`
    FOREIGN KEY (`id_aeropuerto_destino`)
    REFERENCES `morapack4d`.`AEROPUERTO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`SEGMENTACION`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`SEGMENTACION` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`SEGMENTACION` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `fh_aplicacion` DATETIME NOT NULL,
  `fh_sustitucion` DATETIME NULL DEFAULT NULL,
  `id_pedido` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_SEGMENTACION_PEDIDO1_idx` (`id_pedido` ASC) VISIBLE,
  CONSTRAINT `fk_SEGMENTACION_PEDIDO1`
    FOREIGN KEY (`id_pedido`)
    REFERENCES `morapack4d`.`PEDIDO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`RUTA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`RUTA` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`RUTA` (
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
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_RUTA_AEROPUERTO1_idx` (`id_aeropuerto_origen` ASC) VISIBLE,
  INDEX `fk_RUTA_AEROPUERTO2_idx` (`id_aeropuerto_destino` ASC) VISIBLE,
  CONSTRAINT `fk_RUTA_AEROPUERTO1`
    FOREIGN KEY (`id_aeropuerto_origen`)
    REFERENCES `morapack4d`.`AEROPUERTO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_RUTA_AEROPUERTO2`
    FOREIGN KEY (`id_aeropuerto_destino`)
    REFERENCES `morapack4d`.`AEROPUERTO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`LOTE`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`LOTE` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`LOTE` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `tamanio` INT NOT NULL,
  `estado` ENUM('PLANIFICADO', 'POR_REPLANIFICAR', 'REPLANIFICADO', 'ENTREGADO') NOT NULL DEFAULT 'PLANIFICADO',
  `id_segmentacion` BIGINT UNSIGNED NOT NULL,
  `id_ruta` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_LOTE_SEGMENTACION1_idx` (`id_segmentacion` ASC) VISIBLE,
  INDEX `fk_LOTE_RUTA1_idx` (`id_ruta` ASC) VISIBLE,
  CONSTRAINT `fk_LOTE_SEGMENTACION1`
    FOREIGN KEY (`id_segmentacion`)
    REFERENCES `morapack4d`.`SEGMENTACION` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_LOTE_RUTA1`
    FOREIGN KEY (`id_ruta`)
    REFERENCES `morapack4d`.`RUTA` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`REGISTRO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`REGISTRO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`REGISTRO` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `sigue_vigente` TINYINT NOT NULL DEFAULT 1,
  `fh_ingreso` DATETIME NOT NULL,
  `fh_egreso` DATETIME NOT NULL,
  `id_aeropuerto` INT UNSIGNED NOT NULL,
  `id_lote` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_REGISTRO_AEROPUERTO1_idx` (`id_aeropuerto` ASC) VISIBLE,
  INDEX `fk_REGISTRO_LOTE1_idx` (`id_lote` ASC) VISIBLE,
  CONSTRAINT `fk_REGISTRO_AEROPUERTO1`
    FOREIGN KEY (`id_aeropuerto`)
    REFERENCES `morapack4d`.`AEROPUERTO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_REGISTRO_LOTE1`
    FOREIGN KEY (`id_lote`)
    REFERENCES `morapack4d`.`LOTE` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`PLAN`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`PLAN` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`PLAN` (
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
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_PLAN_AEROPUERTO1_idx` (`id_aeropuerto_origen` ASC) VISIBLE,
  INDEX `fk_PLAN_AEROPUERTO2_idx` (`id_aeropuerto_destino` ASC) VISIBLE,
  CONSTRAINT `fk_PLAN_AEROPUERTO1`
    FOREIGN KEY (`id_aeropuerto_origen`)
    REFERENCES `morapack4d`.`AEROPUERTO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PLAN_AEROPUERTO2`
    FOREIGN KEY (`id_aeropuerto_destino`)
    REFERENCES `morapack4d`.`AEROPUERTO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`VUELO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`VUELO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`VUELO` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `capacidad_disponible` INT NOT NULL,
  `fh_salida` DATETIME NOT NULL,
  `fh_llegada` DATETIME NOT NULL,
  `id_plan` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_VUELO_PLAN1_idx` (`id_plan` ASC) VISIBLE,
  CONSTRAINT `fk_VUELO_PLAN1`
    FOREIGN KEY (`id_plan`)
    REFERENCES `morapack4d`.`PLAN` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`RUTA_POR_VUELO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`RUTA_POR_VUELO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`RUTA_POR_VUELO` (
  `id_ruta` BIGINT UNSIGNED NOT NULL,
  `id_vuelo` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id_ruta`, `id_vuelo`),
  INDEX `fk_RUTA_has_VUELO_VUELO1_idx` (`id_vuelo` ASC) VISIBLE,
  INDEX `fk_RUTA_has_VUELO_RUTA1_idx` (`id_ruta` ASC) VISIBLE,
  CONSTRAINT `fk_RUTA_has_VUELO_RUTA1`
    FOREIGN KEY (`id_ruta`)
    REFERENCES `morapack4d`.`RUTA` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_RUTA_has_VUELO_VUELO1`
    FOREIGN KEY (`id_vuelo`)
    REFERENCES `morapack4d`.`VUELO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- -----------------------------------------------------
-- Table `morapack4d`.`PARAMETROS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`PARAMETROS` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`PARAMETROS` (
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
  PRIMARY KEY (`id`)
);

-- -----------------------------------------------------
-- Table `morapack4d`.`EVENTO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`EVENTO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`EVENTO` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `tipo` ENUM('REPROGRAMACION', 'CANCELACION') NOT NULL DEFAULT 'CANCELACION',
  `fh_inicio` DATETIME NOT NULL,
  `fh_fin` DATETIME NOT NULL,
  `fh_nueva_salida` DATETIME NULL DEFAULT NULL,
  `fh_nueva_llegada` DATETIME NULL DEFAULT NULL,
  `id_plan` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_EVENTO_PLAN1_idx` (`id_plan` ASC) VISIBLE,
  CONSTRAINT `fk_EVENTO_PLAN1`
    FOREIGN KEY (`id_plan`)
    REFERENCES `morapack4d`.`PLAN` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;