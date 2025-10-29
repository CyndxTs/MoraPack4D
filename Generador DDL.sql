-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema morapack4d
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema morapack4d
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `morapack4d` ;
USE `morapack4d` ;

-- -----------------------------------------------------
-- Table `morapack4d`.`CLIENTE`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`CLIENTE` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`CLIENTE` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(7) NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `correo` VARCHAR(40) NOT NULL,
  `contrasenia` VARCHAR(255) NOT NULL,
  `estado` ENUM('ONLINE', 'OFFLINE', 'DISABLED') NOT NULL DEFAULT 'OFFLINE',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  UNIQUE INDEX `correo_UNIQUE` (`correo` ASC) VISIBLE);


-- -----------------------------------------------------
-- Table `morapack4d`.`AEROPUERTO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`AEROPUERTO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`AEROPUERTO` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(4) NOT NULL,
  `ciudad` VARCHAR(30) NOT NULL,
  `pais` VARCHAR(20) NOT NULL,
  `continente` VARCHAR(20) NOT NULL,
  `alias` VARCHAR(4) NOT NULL,
  `huso_horario` INT NOT NULL,
  `capacidad` INT NOT NULL,
  `latitud_dms` VARCHAR(20) NOT NULL,
  `latitud_dec` DOUBLE NOT NULL,
  `longitud_dms` VARCHAR(20) NOT NULL,
  `longitud_dec` DOUBLE NOT NULL,
  `es_sede` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  UNIQUE INDEX `alias_UNIQUE` (`alias` ASC) VISIBLE);


-- -----------------------------------------------------
-- Table `morapack4d`.`PEDIDO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`PEDIDO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`PEDIDO` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `cantidad_solicitada` INT NOT NULL,
  `fecha_hora_generacion_local` TIMESTAMP NOT NULL,
  `fecha_hora_generacion_utc` TIMESTAMP NOT NULL,
  `fecha_hora_expiracion_local` TIMESTAMP NULL DEFAULT NULL,
  `fecha_hora_expiracion_utc` TIMESTAMP NULL DEFAULT NULL,
  `id_cliente` INT NOT NULL,
  `id_aeropuerto_destino` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
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
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`RUTA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`RUTA` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`RUTA` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `duracion` DOUBLE NOT NULL,
  `distancia` DOUBLE NOT NULL,
  `fecha_hora_salida_local` TIMESTAMP NOT NULL,
  `fecha_hora_salida_utc` TIMESTAMP NOT NULL,
  `fecha_hora_llegada_local` TIMESTAMP NOT NULL,
  `fecha_hora_llegada_utc` TIMESTAMP NOT NULL,
  `tipo` ENUM('INTRACONTINENTAL', 'INTERCONTINENTAL') NOT NULL,
  `id_aeropuerto_origen` INT NOT NULL,
  `id_aeropuerto_destino` INT NOT NULL,
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
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`LOTE`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`LOTE` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`LOTE` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `tamanio` INT NOT NULL,
  `id_pedido` INT NOT NULL,
  `id_ruta` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_LOTE_PEDIDO1_idx` (`id_pedido` ASC) VISIBLE,
  INDEX `fk_LOTE_RUTA1_idx` (`id_ruta` ASC) VISIBLE,
  CONSTRAINT `fk_LOTE_PEDIDO1`
    FOREIGN KEY (`id_pedido`)
    REFERENCES `morapack4d`.`PEDIDO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_LOTE_RUTA1`
    FOREIGN KEY (`id_ruta`)
    REFERENCES `morapack4d`.`RUTA` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`REGISTRO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`REGISTRO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`REGISTRO` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `fecha_hora_ingreso_local` TIMESTAMP NOT NULL,
  `fecha_hora_ingreso_utc` TIMESTAMP NOT NULL,
  `fecha_hora_egreso_local` TIMESTAMP NOT NULL,
  `fecha_hora_egreso_utc` TIMESTAMP NOT NULL,
  `id_aeropuerto` INT NOT NULL,
  `id_lote` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_REGISTRO_AEROPUERTO1_idx` (`id_aeropuerto` ASC) VISIBLE,
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
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
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`PRODUCTO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`PRODUCTO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`PRODUCTO` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `id_lote` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_PRODUCTO_LOTE_DE_PRODUCTOS_idx` (`id_lote` ASC) VISIBLE,
  CONSTRAINT `fk_PRODUCTO_LOTE_DE_PRODUCTOS`
    FOREIGN KEY (`id_lote`)
    REFERENCES `morapack4d`.`LOTE` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`PLAN`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`PLAN` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`PLAN` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `capacidad` INT NOT NULL,
  `duracion` DOUBLE NOT NULL,
  `distancia` DOUBLE NOT NULL,
  `hora_salida_local` TIME NOT NULL,
  `hora_salida_utc` TIME NOT NULL,
  `hora_llegada_local` TIME NOT NULL,
  `hora_llegada_utc` TIME NOT NULL,
  `id_aeropuerto_origen` INT NOT NULL,
  `id_aeropuerto_destino` INT NOT NULL,
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
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(20) NOT NULL,
  `capacidad_disponible` INT NOT NULL,
  `fecha_hora_salida_local` TIMESTAMP NOT NULL,
  `fecha_hora_salida_utc` TIMESTAMP NOT NULL,
  `fecha_hora_llegada_local` TIMESTAMP NOT NULL,
  `fecha_hora_llegada_utc` TIMESTAMP NOT NULL,
  `id_plan` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE,
  INDEX `fk_VUELO_PLAN1_idx` (`id_plan` ASC) VISIBLE,
  CONSTRAINT `fk_VUELO_PLAN1`
    FOREIGN KEY (`id_plan`)
    REFERENCES `morapack4d`.`PLAN` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`RUTA_POR_VUELO`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`RUTA_POR_VUELO` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`RUTA_POR_VUELO` (
  `id_ruta` INT NOT NULL,
  `id_vuelo` INT NOT NULL,
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
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`PEDIDO_POR_RUTA`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`PEDIDO_POR_RUTA` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`PEDIDO_POR_RUTA` (
  `id_pedido` INT NOT NULL,
  `id_ruta` INT NOT NULL,
  PRIMARY KEY (`id_pedido`, `id_ruta`),
  INDEX `fk_PEDIDO_has_RUTA_RUTA1_idx` (`id_ruta` ASC) VISIBLE,
  INDEX `fk_PEDIDO_has_RUTA_PEDIDO1_idx` (`id_pedido` ASC) VISIBLE,
  CONSTRAINT `fk_PEDIDO_has_RUTA_PEDIDO1`
    FOREIGN KEY (`id_pedido`)
    REFERENCES `morapack4d`.`PEDIDO` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PEDIDO_has_RUTA_RUTA1`
    FOREIGN KEY (`id_ruta`)
    REFERENCES `morapack4d`.`RUTA` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


-- -----------------------------------------------------
-- Table `morapack4d`.`ADMINISTRADOR`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `morapack4d`.`ADMINISTRADOR` ;

CREATE TABLE IF NOT EXISTS `morapack4d`.`ADMINISTRADOR` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `codigo` VARCHAR(7) NOT NULL,
  `nombre` VARCHAR(50) NOT NULL,
  `correo` VARCHAR(40) NOT NULL,
  `contrasenia` VARCHAR(255) NOT NULL,
  `estado` ENUM('ONLINE', 'OFFLINE', 'DISABLED') NOT NULL DEFAULT 'OFFLINE',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `correo_UNIQUE` (`correo` ASC) VISIBLE,
  UNIQUE INDEX `codigo_UNIQUE` (`codigo` ASC) VISIBLE);


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
