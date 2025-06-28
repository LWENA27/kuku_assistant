# 🚀 Supabase Setup Guide for Kuku Assistant

This guide will help you set up Supabase using CLI and configure the database for the Kuku Assistant project.

## 📋 Prerequisites

- Node.js (version 16 or higher)
- npm or yarn
- A Supabase account (sign up at https://supabase.com)

## 🛠️ Step 1: Install Supabase CLI

Run the following command to install the Supabase CLI globally:

```bash
npm install -g supabase
```

Or using yarn:
```bash
yarn global add supabase
```

## 🔐 Step 2: Login to Supabase

```bash
supabase login
```

This will open your browser to authenticate with your Supabase account.

## 🏗️ Step 3: Initialize Local Development

```bash
cd /home/lwena/StudioProjects/Kuku_assistant-master
supabase init
```

## 🗄️ Step 4: Start Local Development Server

```bash
supabase start
```

This will start local Supabase services including PostgreSQL database, API server, and Auth server.

## 📊 Step 5: Create Database Schema

Create the following SQL file to set up your database tables:

### Create the SQL migration file:

```bash
supabase migration new initial_schema
```

Then copy the following schema into the generated migration file:

```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create farmers table
CREATE TABLE IF NOT EXISTS farmers (
    farmer_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    phone_number TEXT,
    farm_location TEXT,
    farm_size TEXT,
    bird_count INTEGER,
    registered_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create vets table
CREATE TABLE IF NOT EXISTS vets (
    vet_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    phone_number TEXT,
    specialty TEXT,
    experience_years INTEGER,
    is_available BOOLEAN DEFAULT TRUE,
    availability_hours TEXT,
    profile_image_url TEXT,
    qualifications TEXT,
    bio TEXT,
    location TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create disease_info table
CREATE TABLE IF NOT EXISTS disease_info (
    disease_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    causes TEXT,
    symptoms TEXT,
    treatment TEXT,
    prevention TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create symptoms_reports table
CREATE TABLE IF NOT EXISTS symptoms_reports (
    report_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID REFERENCES farmers(farmer_id) ON DELETE CASCADE,
    symptom_reported TEXT NOT NULL,
    reported_at TIMESTAMP DEFAULT NOW(),
    severity TEXT CHECK (severity IN ('Low', 'Medium', 'High', 'Critical')),
    affected_chickens INTEGER,
    total_chickens INTEGER,
    additional_notes TEXT,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'reviewed', 'resolved')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create consultations table
CREATE TABLE IF NOT EXISTS consultations (
    consultation_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID REFERENCES farmers(farmer_id) ON DELETE CASCADE,
    vet_id UUID REFERENCES vets(vet_id) ON DELETE SET NULL,
    question TEXT NOT NULL,
    answer TEXT,
    asked_at TIMESTAMP DEFAULT NOW(),
    answered_at TIMESTAMP,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'answered', 'closed')),
    priority TEXT DEFAULT 'medium' CHECK (priority IN ('low', 'medium', 'high', 'urgent')),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create reminder table
CREATE TABLE IF NOT EXISTS reminder (
    reminder_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vet_id UUID REFERENCES vets(vet_id) ON DELETE CASCADE,
    farmer_id UUID REFERENCES farmers(farmer_id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    reminder_date TIMESTAMP NOT NULL,
    is_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Create indexes for better performance
CREATE INDEX idx_farmers_user_id ON farmers(user_id);
CREATE INDEX idx_farmers_email ON farmers(email);
CREATE INDEX idx_vets_user_id ON vets(user_id);
CREATE INDEX idx_vets_email ON vets(email);
CREATE INDEX idx_vets_available ON vets(is_available);
CREATE INDEX idx_symptoms_reports_farmer_id ON symptoms_reports(farmer_id);
CREATE INDEX idx_symptoms_reports_status ON symptoms_reports(status);
CREATE INDEX idx_consultations_farmer_id ON consultations(farmer_id);
CREATE INDEX idx_consultations_vet_id ON consultations(vet_id);
CREATE INDEX idx_consultations_status ON consultations(status);
CREATE INDEX idx_reminder_vet_id ON reminder(vet_id);
CREATE INDEX idx_reminder_farmer_id ON reminder(farmer_id);
CREATE INDEX idx_reminder_sent ON reminder(is_sent);

-- Enable Row Level Security (RLS)
ALTER TABLE farmers ENABLE ROW LEVEL SECURITY;
ALTER TABLE vets ENABLE ROW LEVEL SECURITY;
ALTER TABLE disease_info ENABLE ROW LEVEL SECURITY;
ALTER TABLE symptoms_reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE consultations ENABLE ROW LEVEL SECURITY;
ALTER TABLE reminder ENABLE ROW LEVEL SECURITY;

-- Create RLS policies

-- Farmers table policies
CREATE POLICY "Users can view own farmer profile" ON farmers
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own farmer profile" ON farmers
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own farmer profile" ON farmers
    FOR UPDATE USING (auth.uid() = user_id);

-- Vets table policies
CREATE POLICY "Everyone can view available vets" ON vets
    FOR SELECT USING (is_available = true OR auth.uid() = user_id);

CREATE POLICY "Users can insert own vet profile" ON vets
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own vet profile" ON vets
    FOR UPDATE USING (auth.uid() = user_id);

-- Disease info policies (public read access)
CREATE POLICY "Everyone can view disease info" ON disease_info
    FOR SELECT USING (true);

-- Symptoms reports policies
CREATE POLICY "Farmers can view own reports" ON symptoms_reports
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM farmers 
            WHERE farmers.farmer_id = symptoms_reports.farmer_id 
            AND farmers.user_id = auth.uid()
        )
    );

CREATE POLICY "Vets can view all reports" ON symptoms_reports
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM vets 
            WHERE vets.user_id = auth.uid()
        )
    );

CREATE POLICY "Farmers can insert own reports" ON symptoms_reports
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM farmers 
            WHERE farmers.farmer_id = symptoms_reports.farmer_id 
            AND farmers.user_id = auth.uid()
        )
    );

-- Consultations policies
CREATE POLICY "Farmers can view own consultations" ON consultations
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM farmers 
            WHERE farmers.farmer_id = consultations.farmer_id 
            AND farmers.user_id = auth.uid()
        )
    );

CREATE POLICY "Vets can view assigned consultations" ON consultations
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM vets 
            WHERE vets.vet_id = consultations.vet_id 
            AND vets.user_id = auth.uid()
        ) OR 
        EXISTS (
            SELECT 1 FROM vets 
            WHERE vets.user_id = auth.uid()
        )
    );

CREATE POLICY "Farmers can create consultations" ON consultations
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM farmers 
            WHERE farmers.farmer_id = consultations.farmer_id 
            AND farmers.user_id = auth.uid()
        )
    );

CREATE POLICY "Vets can update consultations" ON consultations
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM vets 
            WHERE vets.vet_id = consultations.vet_id 
            AND vets.user_id = auth.uid()
        )
    );

-- Reminder policies
CREATE POLICY "Vets can manage own reminders" ON reminder
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM vets 
            WHERE vets.vet_id = reminder.vet_id 
            AND vets.user_id = auth.uid()
        )
    );

CREATE POLICY "Farmers can view reminders sent to them" ON reminder
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM farmers 
            WHERE farmers.farmer_id = reminder.farmer_id 
            AND farmers.user_id = auth.uid()
        )
    );

-- Create trigger function for updating updated_at
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at
CREATE TRIGGER set_timestamp_farmers
    BEFORE UPDATE ON farmers
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_vets
    BEFORE UPDATE ON vets
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_disease_info
    BEFORE UPDATE ON disease_info
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_symptoms_reports
    BEFORE UPDATE ON symptoms_reports
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_consultations
    BEFORE UPDATE ON consultations
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

CREATE TRIGGER set_timestamp_reminder
    BEFORE UPDATE ON reminder
    FOR EACH ROW
    EXECUTE PROCEDURE trigger_set_timestamp();

-- Insert sample disease information
INSERT INTO disease_info (name, causes, symptoms, treatment, prevention, description) VALUES
('Fowl Typhoid', 
 'Salmonella Gallinarum bacteria', 
 'Homa, haraka nyekundu, kupungua kwa chakula, kuhara', 
 'Antibiotics (tetracycline, ampicillin), isolation, supportive care', 
 'Vaccination, proper hygiene, quarantine new birds', 
 'Serious bacterial infection affecting chickens of all ages'),
('Newcastle Disease', 
 'Newcastle disease virus (NDV)', 
 'Respiratory distress, nervous signs, decreased egg production', 
 'No specific treatment, supportive care, vaccination', 
 'Regular vaccination, biosecurity measures', 
 'Viral disease affecting poultry respiratory and nervous systems'),
('Infectious Bronchitis', 
 'Infectious bronchitis virus', 
 'Coughing, sneezing, nasal discharge, decreased egg production', 
 'Supportive care, antibiotics for secondary infections', 
 'Vaccination, proper ventilation, biosecurity', 
 'Viral respiratory disease in chickens');
```

## 🔧 Step 6: Apply Database Migration

```bash
supabase db push
```

## 🔗 Step 7: Link to Remote Supabase Project (Optional)

If you want to use a remote Supabase project instead of local development:

```bash
supabase link --project-ref YOUR_PROJECT_ID
```

## 📱 Step 8: Update Android App Configuration

Update your `SupabaseConfig.java` file with the correct values:

For local development:
```java
public static final String SUPABASE_URL = "http://localhost:54321";
public static final String SUPABASE_ANON_KEY = "YOUR_LOCAL_ANON_KEY";
```

For production:
```java
public static final String SUPABASE_URL = "https://YOUR_PROJECT_ID.supabase.co";
public static final String SUPABASE_ANON_KEY = "YOUR_PRODUCTION_ANON_KEY";
```

## 🧪 Step 9: Test the Setup

1. Start the local Supabase services:
   ```bash
   supabase start
   ```

2. Open the Supabase Studio at http://localhost:54323

3. Verify that all tables are created correctly

4. Test the Android app connection

## 🔍 Step 10: Debugging Common Issues

### Issue: RLS Policies Too Restrictive
If you get access denied errors, temporarily disable RLS for testing:
```sql
ALTER TABLE table_name DISABLE ROW LEVEL SECURITY;
```

### Issue: Authentication Problems
Make sure your app is using the correct API keys and URLs.

### Issue: CORS Errors
For local development, CORS should be handled automatically by Supabase.

## 📚 Additional Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase CLI Reference](https://supabase.com/docs/reference/cli)
- [Row Level Security Guide](https://supabase.com/docs/guides/auth/row-level-security)

## 🚀 Quick Start Commands

```bash
# Install Supabase CLI
npm install -g supabase

# Login
supabase login

# Initialize project
cd /home/lwena/StudioProjects/Kuku_assistant-master
supabase init

# Start local development
supabase start

# Create and apply migration
supabase migration new initial_schema
# (Add the SQL schema above to the migration file)
supabase db push

# Get local development URLs and keys
supabase status
```

## 🎯 Next Steps

1. Follow this guide step by step
2. Test the database connection from your Android app
3. Verify that authentication works
4. Test the main app features (registration, login, reports, consultations)
5. Deploy to production when ready

Remember to update your Android app's `SupabaseConfig.java` with the correct URL and API key after setup!
