-- SQL Script to update bakers table for profile functionality
-- Run this in phpMyAdmin or MySQL

-- First, let's see current table structure
-- DESCRIBE bakers;

-- Add missing columns if they don't exist
-- Run each ALTER statement separately if any fails

-- Add shop_name column
ALTER TABLE `bakers` ADD COLUMN IF NOT EXISTS `shop_name` VARCHAR(255) DEFAULT NULL;

-- Add owner_name column  
ALTER TABLE `bakers` ADD COLUMN IF NOT EXISTS `owner_name` VARCHAR(255) DEFAULT NULL;

-- Add phone column
ALTER TABLE `bakers` ADD COLUMN IF NOT EXISTS `phone` VARCHAR(20) DEFAULT NULL;

-- Add address column
ALTER TABLE `bakers` ADD COLUMN IF NOT EXISTS `address` TEXT DEFAULT NULL;

-- Add description column
ALTER TABLE `bakers` ADD COLUMN IF NOT EXISTS `description` TEXT DEFAULT NULL;

-- Add profile_image column
ALTER TABLE `bakers` ADD COLUMN IF NOT EXISTS `profile_image` VARCHAR(255) DEFAULT NULL;

-- Update existing baker with sample data (replace baker_id with your actual baker ID)
-- UPDATE bakers SET 
--     shop_name = 'Sweet Delights Bakery',
--     owner_name = 'John Baker',
--     phone = '+91 98765 43210',
--     address = '123 Baker Street, Chennai',
--     description = 'We specialize in custom cakes for all occasions. From birthdays to weddings, we create delicious and beautiful cakes.'
-- WHERE baker_id = 1;

-- To see all bakers and their data:
-- SELECT * FROM bakers;
