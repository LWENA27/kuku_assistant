-- Add missing farm_address column to farmers table
-- Migration: 20250706001_add_farm_address

-- Add farm_address column if it doesn't exist
ALTER TABLE public.farmers
ADD COLUMN IF NOT EXISTS farm_address TEXT;

-- Update any existing records to have default values
UPDATE public.farmers
SET farm_address = location || ' Farm'
WHERE farm_address IS NULL AND location IS NOT NULL;

-- Add farm_name column if missing for better compatibility
ALTER TABLE public.farmers
ADD COLUMN IF NOT EXISTS farm_name TEXT;
