-- MySQL Script generated by MySQL Workbench
-- Thu 18 Oct 2018 04:27:04 PM CEST
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema reports
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema reports
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `reports` ;
USE `reports` ;

-- -----------------------------------------------------
-- Table `reports`.`report`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `reports`.`report` (
  `report_id` INT NOT NULL AUTO_INCREMENT,
  `test_id` INT NOT NULL,
  `test_number` INT NULL,
  `test_name` VARCHAR(45) NULL,
  `test_script` VARCHAR(128) NULL,
  `test_host` VARCHAR(128) NULL,
  `test_host_role` VARCHAR(64) NULL,
  `test_result` VARCHAR(45) NULL,
  `location` VARCHAR(1024) NULL,
  `aggregated` TINYINT NULL DEFAULT 0,
  `test_date` DATETIME NULL DEFAULT now(),
  `test_description` VARCHAR(2048) NULL,
  `test_comments` VARCHAR(2048) NULL,
  `valid` TINYINT NULL,
  `retired` TINYINT NULL,
  `retired_date` DATETIME NULL,
  PRIMARY KEY (`report_id`, `test_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `reports`.`sut_node_info`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `reports`.`sut_node_info` (
  `sut_node_id` INT NOT NULL AUTO_INCREMENT,
  `sut_node_name` VARCHAR(128) NULL,
  `sut_node_os_name` VARCHAR(64) NULL,
  `sut_node_os_arch` VARCHAR(45) NULL,
  `sut_node_os_version` VARCHAR(45) NULL,
  `sut_node_os_other` VARCHAR(512) NULL,
  `sut_node_hw_name` VARCHAR(64) NULL,
  `sut_node_hw_model` VARCHAR(64) NULL,
  `sut_node_hw_cpu` VARCHAR(45) NULL,
  `sut_node_hw_cpu_count` INT NULL,
  `sut_node_hw_ram` INT NULL,
  `sut_node_hw_disk_type` VARCHAR(45) NULL,
  `sut_node_hw_other` VARCHAR(512) NULL,
  PRIMARY KEY (`sut_node_id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `reports`.`test_sut_node_link`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `reports`.`test_sut_node_link` (
  `test_id` INT NOT NULL,
  `sut_node_id` INT NOT NULL,
  PRIMARY KEY (`test_id`, `sut_node_id`),
  INDEX `fk_report_sut_node_link_sut_node_info1_idx` (`sut_node_id` ASC),
  CONSTRAINT `fk_report_sut_node_link_sut_node_info1`
    FOREIGN KEY (`sut_node_id`)
    REFERENCES `reports`.`sut_node_info` (`sut_node_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_test_sut_node_link_report1`
    FOREIGN KEY (`test_id`)
    REFERENCES `reports`.`report` (`test_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `reports`.`report_history`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `reports`.`report_history` (
  `report_id` INT NOT NULL,
  `test_id` INT NOT NULL,
  `test_number` INT NULL,
  `test_name` VARCHAR(45) NULL,
  `test_script` VARCHAR(128) NULL,
  `test_host` VARCHAR(128) NULL,
  `test_host_role` VARCHAR(64) NULL,
  `test_result` VARCHAR(45) NULL,
  `location` VARCHAR(1024) NULL,
  `aggregated` TINYINT NULL DEFAULT 0,
  `test_date` DATETIME NULL DEFAULT now(),
  `test_description` VARCHAR(2048) NULL,
  `test_comments` VARCHAR(2048) NULL,
  `valid` TINYINT NULL,
  `retired` TINYINT NULL,
  `retired_date` DATETIME NULL,
  `report_update_history` DATETIME NULL)
ENGINE = InnoDB;

USE `reports`;

DELIMITER $$
USE `reports`$$
CREATE DEFINER = CURRENT_USER TRIGGER `reports`.`report_BEFORE_UPDATE` BEFORE UPDATE ON `report` FOR EACH ROW
BEGIN
	INSERT INTO report_history select *,now() as report_update_history from report where report_id = NEW.report_id;
END$$


DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
