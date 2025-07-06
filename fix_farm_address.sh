#!/bin/bash

# Fix farm_address column issue
# This script applies the immediate fix and ensures migrations are up to date

echo "🔍 Checking Supabase connection..."

# Check if supabase CLI is available
if ! command -v supabase &> /dev/null; then
    echo "❌ Supabase CLI not found. Please install it first:"
    echo "npm install -g supabase"
    exit 1
fi

echo "✅ Supabase CLI found"

# Navigate to project directory
cd "$(dirname "$0")"

echo "🔄 Applying database migrations..."

# Reset and apply all migrations
supabase db reset --debug

echo "🔧 Applying immediate fix..."

# Apply the immediate fix
supabase db psql -c "
DO \$\$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'farmers'
        AND column_name = 'farm_address'
    ) THEN
        ALTER TABLE public.farmers ADD COLUMN farm_address TEXT;

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
END \$\$;
"

echo "✅ Checking farmers table structure..."

# Verify the fix
supabase db psql -c "
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public'
AND table_name = 'farmers'
ORDER BY ordinal_position;
"

echo "🎉 Farm address column fix complete!"
echo ""
echo "📱 Now rebuild and restart your Android app:"
echo "cd /home/lwena/StudioProjects/Kuku_assistant-master"
echo "./gradlew clean && ./gradlew build"
echo ""
echo "Or if using Android Studio:"
echo "Build → Clean Project → Build → Rebuild Project"
