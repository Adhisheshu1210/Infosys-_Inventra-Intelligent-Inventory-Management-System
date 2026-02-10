-- Clear all user data from inventra database
-- Run this script to reset authentication system

USE inventra;

-- Show current record count
SELECT COUNT(*) as 'Records BEFORE clearing' FROM user;

-- Clear all user data
TRUNCATE TABLE user;

-- Confirm table is empty
SELECT COUNT(*) as 'Records AFTER clearing' FROM user;

-- Show table structure (confirm table still exists)
DESCRIBE user;

SELECT 'Database cleared successfully! You can now restart authentication testing.' as Status;
