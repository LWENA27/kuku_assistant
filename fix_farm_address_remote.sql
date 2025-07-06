-- Fix farm_address column for remote Supabase database
-- Run this in your Supabase Dashboard SQL Editor

-- Check if the column exists and add it if it doesn't
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'farmers'
        AND column_name = 'farm_address'
    ) THEN
        ALTER TABLE public.farmers
        ADD COLUMN farm_address TEXT;

        -- Update existing records with default values
        UPDATE public.farmers
        SET farm_address = COALESCE(location, '') || ' Farm'
        WHERE farm_address IS NULL;

        -- Add a comment to the column
        COMMENT ON COLUMN public.farmers.farm_address IS 'Physical address of the farm';
    END IF;
END $$;

-- Refresh the schema cache to make PostgREST recognize the new column
NOTIFY pgrst, 'reload schema';
