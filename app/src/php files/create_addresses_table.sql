-- SQL to create addresses table
-- Run this in your phpMyAdmin or MySQL client

CREATE TABLE IF NOT EXISTS `addresses` (
  `address_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `label` varchar(50) DEFAULT 'Home',
  `full_address` text NOT NULL,
  `pincode` varchar(10) DEFAULT '',
  `landmark` varchar(255) DEFAULT '',
  `phone` varchar(20) DEFAULT '',
  `is_default` tinyint(1) DEFAULT 0,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`address_id`),
  KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
