-- Add user_type handling and fix auth metadata
-- Migration: 20250704002_user_type_fix

-- Function to ensure user_type is set in auth.users metadata
CREATE OR REPLACE FUNCTION public.handle_user_type()
RETURNS TRIGGER AS $$
DECLARE
    user_type text;
BEGIN
    -- Get the user type based on existing records
    SELECT
        CASE
            WHEN EXISTS (SELECT 1 FROM public.farmers WHERE user_id = NEW.id) THEN 'farmer'
            WHEN EXISTS (SELECT 1 FROM public.vets WHERE user_id = NEW.id) THEN 'vet'
            ELSE COALESCE(NEW.raw_user_meta_data->>'user_type', 'unassigned')
        END INTO user_type;

    -- Update user metadata with user_type
    NEW.raw_user_meta_data =
        COALESCE(NEW.raw_user_meta_data, '{}'::jsonb) ||
        jsonb_build_object('user_type', user_type);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Drop existing trigger if it exists
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;

-- Create trigger for new user creation
CREATE TRIGGER on_auth_user_created
    BEFORE INSERT OR UPDATE ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_user_type();

-- Function to update user type when farmer/vet profile is created
CREATE OR REPLACE FUNCTION public.update_user_type()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'farmers' THEN
        UPDATE auth.users
        SET raw_user_meta_data =
            COALESCE(raw_user_meta_data, '{}'::jsonb) ||
            jsonb_build_object('user_type', 'farmer')
        WHERE id = NEW.user_id;
    ELSIF TG_TABLE_NAME = 'vets' THEN
        UPDATE auth.users
        SET raw_user_meta_data =
            COALESCE(raw_user_meta_data, '{}'::jsonb) ||
            jsonb_build_object('user_type', 'vet')
        WHERE id = NEW.user_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create triggers for farmers and vets tables
DROP TRIGGER IF EXISTS on_farmer_created ON public.farmers;
CREATE TRIGGER on_farmer_created
    AFTER INSERT ON public.farmers
    FOR EACH ROW
    EXECUTE FUNCTION public.update_user_type();

DROP TRIGGER IF EXISTS on_vet_created ON public.vets;
CREATE TRIGGER on_vet_created
    AFTER INSERT ON public.vets
    FOR EACH ROW
    EXECUTE FUNCTION public.update_user_type();

-- Fix existing users without user_type
UPDATE auth.users
SET raw_user_meta_data =
    COALESCE(raw_user_meta_data, '{}'::jsonb) ||
    jsonb_build_object('user_type',
        CASE
            WHEN EXISTS (SELECT 1 FROM public.farmers WHERE user_id = auth.users.id) THEN 'farmer'
            WHEN EXISTS (SELECT 1 FROM public.vets WHERE user_id = auth.users.id) THEN 'vet'
            ELSE 'unassigned'
        END
    )
WHERE raw_user_meta_data->>'user_type' IS NULL;

-- Add RLS policy to allow users to read their own metadata
CREATE POLICY "Allow users to read their own metadata"
ON auth.users FOR SELECT
TO authenticated
USING (auth.uid() = id);

-- Create helper function to get user type
CREATE OR REPLACE FUNCTION public.get_user_type(user_id uuid)
RETURNS TEXT AS $$
BEGIN
    RETURN (
        SELECT raw_user_meta_data->>'user_type'
        FROM auth.users
        WHERE id = user_id
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
