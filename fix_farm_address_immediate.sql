-- Immediate fix for farm_address column issue
-- This script can be run directly in Supabase SQL editor

-- Check current farmers table structure
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
AND table_name = 'farmers'
ORDER BY ordinal_position;

-- Add farm_address column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'farmers'
        AND column_name = 'farm_address'
    ) THEN
        ALTER TABLE public.farmers ADD COLUMN farm_address TEXT;

        -- Update existing records
        UPDATE public.farmers
        SET farm_address = CASE
            WHEN location IS NOT NULL AND location != ''
            THEN location || ' Farm'
            ELSE 'Farm Location'
        END
        WHERE farm_address IS NULL;

        RAISE NOTICE 'farm_address column added successfully';
    ELSE
        RAISE NOTICE 'farm_address column already exists';
    END IF;
END $$;

-- Verify the column was added
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
AND table_name = 'farmers'
AND column_name = 'farm_address';
