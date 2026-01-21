CREATE TABLE `member` (
    `member_rowid` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `email` VARCHAR(100) NOT NULL,
    `pw` VARCHAR(200) NOT NULL,
    `regdate` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedate` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_members_email (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `_type` (
    `_type_rowid` INT PRIMARY KEY,
    `name_kr` VARCHAR(30) NOT NULL,
    `name_jp` VARCHAR(30) NOT NULL,
    `name_en` VARCHAR(30) NOT NULL,
    `contents` JSON NOT NULL,
    `regdate` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `_version` (
    `_version_rowid` INT PRIMARY KEY,
    `name_kr` VARCHAR(30) NOT NULL,
    `name_jp` VARCHAR(30) NOT NULL,
    `name_en` VARCHAR(30) NOT NULL,
    `groupkey` VARCHAR(100) NOT NULL,
    `regdate` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `_move` (
    `_move_rowid` INT PRIMARY KEY,
    `_type_rowid` INT NOT NULL,
    `name_kr` VARCHAR(30) NOT NULL,
    `name_jp` VARCHAR(30) NOT NULL,
    `name_en` VARCHAR(30) NOT NULL,
    `description_kr` VARCHAR(300) NOT NULL,
    `description_jp` VARCHAR(300) NOT NULL,
    `description_en` VARCHAR(300) NOT NULL,
    `details` JSON NOT NULL,
    `regdate` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `_ability` (
    `_ability_rowid` INT PRIMARY KEY,
    `name_kr` VARCHAR(30) NOT NULL,
    `name_jp` VARCHAR(30) NOT NULL,
    `name_en` VARCHAR(30) NOT NULL,
    `description_kr` VARCHAR(300) NOT NULL,
    `description_jp` VARCHAR(300) NOT NULL,
    `description_en` VARCHAR(300) NOT NULL,
    `regdate` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `pokemon` (
    `pokemon_rowid` INT PRIMARY KEY,
    `base_rowid` INT NOT NULL,
    `_type1_rowid` INT NOT NULL,
    `_type2_rowid` INT NOT NULL,
    `name_kr` VARCHAR(30) NOT NULL,
    `name_jp` VARCHAR(30) NOT NULL,
    `name_en` VARCHAR(30) NOT NULL,
    `hp` INT NOT NULL,
    `atk` INT NOT NULL,
    `satk` INT NOT NULL,
    `spd` INT NOT NULL,
    `def` INT NOT NULL,
    `sdef` INT NOT NULL,
    `details` JSON NOT NULL,
    `regdate` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `fail` (
    `fail_rowid` BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    `domain` VARCHAR(30) NOT NULL,
    `id` INT NOT NULL,
    `error` JSON NOT NULL,
    `updatedate` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    UNIQUE KEY uk_fail_domain_id (`domain`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
