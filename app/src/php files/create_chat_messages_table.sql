-- SQL Script to create chat_messages table
-- Run this in phpMyAdmin or MySQL

CREATE TABLE IF NOT EXISTS `chat_messages` (
    `message_id` INT AUTO_INCREMENT PRIMARY KEY,
    `baker_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `sender_type` ENUM('customer', 'baker') NOT NULL,
    `message` TEXT,
    `image_url` VARCHAR(255),
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_baker_user` (`baker_id`, `user_id`),
    INDEX `idx_created` (`created_at`)
);

-- Note: You may need to add foreign key constraints if your tables support them:
-- ALTER TABLE `chat_messages` ADD FOREIGN KEY (`baker_id`) REFERENCES `bakers`(`baker_id`);
-- ALTER TABLE `chat_messages` ADD FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`);
