#!/bin/bash

# =============================================================================
# PROJECT CLEANUP SCRIPT
# Removes duplicated, unnecessary, and temporary files from Kuku Assistant
# =============================================================================

echo "🧹 Starting project cleanup..."

# Get the project root directory
PROJECT_ROOT="/home/lwena/StudioProjects/Kuku_assistant-master"
cd "$PROJECT_ROOT"

# =============================================================================
# 1. REMOVE BUILD ARTIFACTS AND CACHE FILES
# =============================================================================
echo "📦 Removing build artifacts and cache files..."

# Android build files
rm -rf app/build/
rm -rf build/
rm -rf .gradle/

# IDE cache files
rm -rf .idea/caches/
rm -rf .idea/modules/
rm -rf .idea/libraries/
rm -rf .idea/workspace.xml
rm -rf .idea/tasks.xml
rm -rf .idea/.name
rm -rf .idea/compiler.xml
rm -rf .idea/copyright/
rm -rf .idea/encodings.xml
rm -rf .idea/scopes/
rm -rf .idea/vcs.xml

# Temporary files
rm -f *.tmp
rm -f *.temp
rm -f *.log
rm -f debug_logs.txt

echo "✅ Build artifacts cleaned"

# =============================================================================
# 2. REMOVE DUPLICATE AND OUTDATED DOCUMENTATION
# =============================================================================
echo "📚 Removing duplicate documentation files..."

# Remove redundant fix documentation (keep only the most recent/relevant ones)
rm -f CRITICAL_FIX_DASHBOARDMANAGER.md
rm -f DATABASE_FIX_GUIDE.md
rm -f DATABASE_FIX_INSTRUCTIONS.md
rm -f FIX_SUMMARY.md
rm -f ISSUE_RESOLUTION_SUMMARY.md
rm -f NAVIGATION_FIXES.md
rm -f NAVIGATION_FIXES_SUMMARY.md
rm -f PROFILE_COMPLETION_FIX.md
rm -f QUICK_FIX_SUMMARY.md
rm -f TEMPORARY_CODE_FIX.md
rm -f VET_LOGOUT_FIX.md
rm -f VET_NAVIGATION_FIX.md
rm -f VSCODE_SETUP.md

# Keep only: AI_DEVELOPMENT_PROMPT.md, APP_TESTING_GUIDE.md, FINAL_FIX_SUMMARY.md, DEVICE_COMMANDS.md

echo "✅ Duplicate documentation removed"

# =============================================================================
# 3. REMOVE DUPLICATE SQL FILES
# =============================================================================
echo "🗄️ Removing duplicate SQL files..."

# Remove redundant SQL files (keep only the most comprehensive ones)
rm -f database_fix.sql
rm -f supabase_rls_fix.sql
rm -f verify_database_fix.sql
rm -f supabase_verification.sql
rm -f supabase_cache_refresh.sql

# Keep only: supabase_immediate_fix.sql (the most comprehensive one)

echo "✅ Duplicate SQL files removed"

# =============================================================================
# 4. REMOVE SHELL SCRIPTS (TEMPORARY TESTING FILES)
# =============================================================================
echo "🔧 Removing temporary shell scripts..."

rm -f build_and_test.sh
rm -f debug_screen_blinking.sh
rm -f test_vet_fix.sh

echo "✅ Temporary scripts removed"

# =============================================================================
# 5. REMOVE LAYOUT BACKUP (OLD LAYOUTS)
# =============================================================================
echo "🎨 Removing old layout backup..."

# These are backup layouts from before the UI redesign
rm -rf layout_backup_20250619/

echo "✅ Old layout backup removed"

# =============================================================================
# 6. REMOVE SYSTEM DESIGN FILES (NOT NEEDED IN CODE REPO)
# =============================================================================
echo "🎨 Removing design files..."

rm -f "system Design.pptx"
rm -f ".~lock.system Design.pptx#"

echo "✅ Design files removed"

# =============================================================================
# 7. CLEAN UP ANDROID SPECIFIC TEMPORARY FILES
# =============================================================================
echo "📱 Cleaning Android temporary files..."

# Remove local properties if it contains absolute paths (will be regenerated)
if [ -f "local.properties" ]; then
    echo "ℹ️  Note: local.properties kept (contains SDK paths)"
fi

# Clean gradle wrapper if needed
if [ -d "gradle/wrapper" ]; then
    # Keep gradle wrapper
    echo "ℹ️  Note: Gradle wrapper kept (needed for builds)"
fi

echo "✅ Android cleanup completed"

# =============================================================================
# 8. FINAL CLEANUP - EMPTY DIRECTORIES
# =============================================================================
echo "📁 Removing empty directories..."

find . -type d -empty -delete 2>/dev/null

echo "✅ Empty directories removed"

# =============================================================================
# 9. GENERATE CLEANUP SUMMARY
# =============================================================================
echo ""
echo "🎉 PROJECT CLEANUP COMPLETED!"
echo ""
echo "📊 CLEANUP SUMMARY:"
echo "==================="
echo "✅ Removed build artifacts and cache files"
echo "✅ Removed 12+ duplicate documentation files"
echo "✅ Removed 5+ duplicate SQL files"
echo "✅ Removed temporary shell scripts"
echo "✅ Removed old layout backup directory"
echo "✅ Removed design files"
echo "✅ Cleaned Android temporary files"
echo "✅ Removed empty directories"
echo ""
echo "📁 REMAINING IMPORTANT FILES:"
echo "=============================="
echo "📄 AI_DEVELOPMENT_PROMPT.md - Development guidelines"
echo "📄 APP_TESTING_GUIDE.md - Testing instructions"
echo "📄 FINAL_FIX_SUMMARY.md - Latest fix summary"
echo "📄 DEVICE_COMMANDS.md - Device management commands"
echo "📄 supabase_immediate_fix.sql - Database configuration"
echo ""
echo "⚠️  NOTE: You may need to run './gradlew clean' after this cleanup"
echo "⚠️  NOTE: Android Studio may need to re-index the project"
echo ""
echo "🚀 Your project is now clean and ready for development!"
