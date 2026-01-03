-- SQL to update bakers table for nearby bakers feature
-- Run this in phpMyAdmin on your Custom-Cake database

-- Add location columns
ALTER TABLE bakers ADD COLUMN latitude DECIMAL(10, 8) DEFAULT NULL;
ALTER TABLE bakers ADD COLUMN longitude DECIMAL(11, 8) DEFAULT NULL;
ALTER TABLE bakers ADD COLUMN specialty VARCHAR(100) DEFAULT 'Custom Cakes';
ALTER TABLE bakers ADD COLUMN years_experience INT DEFAULT 0;

-- Example: Update a baker with Chennai location (replace baker_id with actual ID)
-- UPDATE bakers SET 
--     latitude = 13.0827,
--     longitude = 80.2707,
--     specialty = 'Wedding Cakes',
--     years_experience = 5
-- WHERE baker_id = 1;
