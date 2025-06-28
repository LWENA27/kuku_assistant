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
