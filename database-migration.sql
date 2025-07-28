-- Database Migration Script
-- Add missing address column to tenants table

-- Add address column to tenants table if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'tenants' AND column_name = 'address'
    ) THEN
        ALTER TABLE tenants ADD COLUMN address TEXT;
        RAISE NOTICE 'Added address column to tenants table';
    ELSE
        RAISE NOTICE 'Address column already exists in tenants table';
    END IF;
END $$;

-- Commit the migration
COMMIT; 