-- Fix farm_address column addition
-- Migration: 20250706002_fix_farm_address

-- First check if the column exists and drop it if it does (for clean slate)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'farmers'
        AND column_name = 'farm_address'
    ) THEN
        ALTER TABLE public.farmers DROP COLUMN farm_address;
    END IF;
END $$;

-- Add farm_address column
ALTER TABLE public.farmers
ADD COLUMN farm_address TEXT;

-- Update existing records with default values
UPDATE public.farmers
SET farm_address = COALESCE(location, '') || ' Farm'
WHERE farm_address IS NULL;

-- Add a comment to the column
COMMENT ON COLUMN public.farmers.farm_address IS 'Physical address of the farm';

-- Refresh the schema cache
NOTIFY pgrst, 'reload schema';
