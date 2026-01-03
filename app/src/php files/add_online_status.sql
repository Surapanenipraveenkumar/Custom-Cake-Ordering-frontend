-- SQL to add is_online column to bakers table
-- Run this in phpMyAdmin on your Custom-Cake database

ALTER TABLE bakers ADD COLUMN is_online TINYINT(1) DEFAULT 1;

-- Set all existing bakers to online by default
UPDATE bakers SET is_online = 1 WHERE is_online IS NULL;
