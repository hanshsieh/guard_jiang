CREATE TABLE IF NOT EXISTS `guard_account` (
    `id` VARCHAR(40) PRIMARY KEY NOT NULL,
    `email` VARCHAR(128) NOT NULL,
    `password` VARCHAR(128) NOT NULL,
    `certificate` VARCHAR(128) DEFAULT NULL,
    `auth_token` VARCHAR(128) DEFAULT NULL,
    -- Partition ID used to separate accounts into different partitions
    `partition` INTEGER NOT NULL
) ENGINE=InnoDB DEFAULT CHARACTER SET ascii COLLATE ascii_bin;

CREATE TABLE IF NOT EXISTS `license` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    `license_key` VARCHAR(128) UNIQUE NOT NULL,
    `user_id` VARCHAR(128) NOT NULL,
    `created_ts` BIGINT NOT NULL,
    `expired_ts` BIGINT DEFAULT NULL,
    `max_defenders` INTEGER NOT NULL,
    `num_defenders` INTEGER NOT NULL DEFAULT 0,
    `max_supporters` INTEGER NOT NULL,
    `num_supporters` INTEGER NOT NULL DEFAULT 0,
    `max_admins` INTEGER NOT NULL,
    `num_admins` INTEGER NOT NULL DEFAULT 0,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_license_key` (`license_key`)
) ENGINE=InnoDB DEFAULT CHARACTER SET ascii COLLATE ascii_bin;

CREATE TABLE IF NOT EXISTS `group` (
    `id` VARCHAR(128) NOT NULL,
    `license_id` BIGINT NOT NULL,
    `recovery_expired_ts` BIGINT DEFAULT NULL,
    `members_backup_ts` BIGINT DEFAULT NULL,
     FOREIGN KEY(`license_id`) REFERENCES `license`(`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET ascii COLLATE ascii_bin;

CREATE TABLE IF NOT EXISTS `group_role` (
     `id` BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
     `group_id` VARCHAR(128) NOT NULL REFERENCES `group`(`id`),
     -- It may reference a user not in the "guard_account" table
     `user_id` VARCHAR(128) NOT NULL,
     -- 0: defender, 1: supporter, 2: admin
     `role` TINYINT UNSIGNED NOT NULL,
     `license_id` BIGINT NOT NULL REFERENCES `license`(`id`)
     UNIQUE KEY `uk_group_user_id` (`group_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET ascii COLLATE ascii_bin;

CREATE TABLE IF NOT EXISTS `group_blocking_record` (
     `id` BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
     `group_id` VARCHAR(128) NOT NULL REFERENCES `group`(`id`),
     `user_id` VARCHAR(128) NOT NULL,
     `expired_ts` BIGINT DEFAULT NULL,
     UNIQUE KEY `uk_group_user_id` (`group_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET ascii COLLATE ascii_bin;

CREATE TABLE IF NOT EXISTS `group_members_backup` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    `group_id` VARCHAR(128) NOT NULL REFERENCES `group`(`id`),
    `user_id` VARCHAR(128) NOT NULL,
    UNIQUE KEY `uk_group_user_id` (`group_id`, `user_id`),
    INDEX `idx_group_id` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET ascii COLLATE ascii_bin;

CREATE TABLE IF NOT EXISTS `chat` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    `guard_id` VARCHAR(128) NOT NULL REFERENCES `guard_account`(`id`),
    `user_id` VARCHAR(128) NOT NULL,
    `env_type` TINYINT NOT NULL,
    `env_id` VARCHAR(128) NOT NULL,
    `updated_ts` BIGINT NOT NULL,
    `stack` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    UNIQUE KEY `uk_guard_user_env` (`guard_id`, `user_id`, `env_type`, `env_id`),
INDEX `idx_guard_id` (`guard_id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET ascii COLLATE ascii_bin;
