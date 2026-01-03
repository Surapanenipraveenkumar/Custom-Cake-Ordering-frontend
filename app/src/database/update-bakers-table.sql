-- Make sure your bakers table has these columns
-- Run this ALTER TABLE if the columns are missing

-- Add missing columns to bakers table
ALTER TABLE bakers 
ADD COLUMN IF NOT EXISTS shop_name VARCHAR(255) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS owner_name VARCHAR(255) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS phone VARCHAR(20) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS address VARCHAR(500) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS description TEXT DEFAULT NULL;

-- If your table uses 'name' instead of 'shop_name', you can update it:
-- UPDATE bakers SET shop_name = name WHERE shop_name IS NULL;
-- UPDATE bakers SET owner_name = name WHERE owner_name IS NULL;

-- Make sure your orders table has these columns for stats:
-- total_amount (for calculating income)
-- status (for filtering delivered/completed orders)
-- created_at (for filtering by month)
-- rating (optional, for calculating average rating)

-- Check your bakers table structure:
-- DESCRIBE bakers;

-- Check your orders table structure:
-- DESCRIBE orders;
