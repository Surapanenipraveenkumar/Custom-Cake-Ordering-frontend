-- Firebase Cloud Messaging Token Storage
-- Run these SQL commands to add FCM token columns to your database

-- Add fcm_token column to users table (customers)
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(255) NULL;

-- Add fcm_token column to bakers table
ALTER TABLE bakers ADD COLUMN fcm_token VARCHAR(255) NULL;

-- Add fcm_token column to delivery_partners table
ALTER TABLE delivery_partners ADD COLUMN fcm_token VARCHAR(255) NULL;

-- Optional: Add index for faster token lookups
CREATE INDEX idx_users_fcm_token ON users(fcm_token);
CREATE INDEX idx_bakers_fcm_token ON bakers(fcm_token);
CREATE INDEX idx_delivery_partners_fcm_token ON delivery_partners(fcm_token);
