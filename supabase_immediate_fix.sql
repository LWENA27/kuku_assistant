-- ====================================================================
-- IMMEDIATE FIX FOR VETS TABLE ACCESS
-- Since the column exists, this is likely an RLS or API access issue
-- ====================================================================

-- 1. CHECK CURRENT RLS POLICIES
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check
FROM pg_policies 
WHERE tablename = 'vets';

-- 2. TEMPORARILY DISABLE RLS TO TEST (BE CAREFUL - ONLY FOR TESTING)
-- ALTER TABLE public.vets DISABLE ROW LEVEL SECURITY;

-- 3. OR CREATE A PERMISSIVE POLICY FOR ALL AUTHENTICATED USERS
DROP POLICY IF EXISTS "Enable read access for authenticated users" ON public.vets;
CREATE POLICY "Enable read access for authenticated users" ON public.vets
    FOR SELECT USING (auth.role() = 'authenticated');

-- 4. ENSURE TABLE PERMISSIONS
GRANT SELECT ON public.vets TO authenticated;
GRANT SELECT ON public.vets TO anon;

-- 5. TEST THE API QUERY FORMAT YOUR APP USES
-- This mimics what your Android app is doing
SELECT *
FROM public.vets 
WHERE user_id = '0a5f0b0a-be72-4e8a-aef5-66b71480553f'  -- Replace with actual user_id
LIMIT 1;

-- 6. REFRESH POSTGREST SCHEMA CACHE
NOTIFY pgrst, 'reload schema';
