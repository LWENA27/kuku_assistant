-- Create tables for Kuku Assistant application
-- Migration: 20250704001_kuku_assistant_schema

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create farmers table
DROP TABLE IF EXISTS public.farmers CASCADE;
CREATE TABLE public.farmers (
    farmer_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    phone_number TEXT,
    location TEXT,
    farm_name TEXT,
    farm_size TEXT,
    bird_count INT,
    bird_type TEXT,
    profile_image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT unique_farmer_user_id UNIQUE (user_id)
);

-- Create vets table
DROP TABLE IF EXISTS public.vets CASCADE;
CREATE TABLE public.vets (
    vet_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT NOT NULL,
    phone_number TEXT,
    qualification TEXT,
    license_number TEXT,
    specialization TEXT,
    experience_years INT,
    available BOOLEAN DEFAULT true,
    profile_image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT unique_vet_user_id UNIQUE (user_id)
);

-- Create consultations table
DROP TABLE IF EXISTS public.consultations CASCADE;
CREATE TABLE public.consultations (
    consultation_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID NOT NULL REFERENCES public.farmers(farmer_id) ON DELETE CASCADE,
    vet_id UUID REFERENCES public.vets(vet_id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    description TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING',
    urgency TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Create consultation messages table
DROP TABLE IF EXISTS public.consultation_messages CASCADE;
CREATE TABLE public.consultation_messages (
    message_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    consultation_id UUID NOT NULL REFERENCES public.consultations(consultation_id) ON DELETE CASCADE,
    sender_id UUID NOT NULL,
    sender_type TEXT NOT NULL,
    message TEXT NOT NULL,
    attachments JSONB,
    read BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Create symptoms reports table
DROP TABLE IF EXISTS public.symptoms_reports CASCADE;
CREATE TABLE public.symptoms_reports (
    report_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farmer_id UUID NOT NULL REFERENCES public.farmers(farmer_id) ON DELETE CASCADE,
    symptoms TEXT[] NOT NULL,
    description TEXT,
    bird_type TEXT,
    bird_age TEXT,
    bird_count INT,
    date_observed DATE NOT NULL,
    images JSONB,
    status TEXT DEFAULT 'OPEN',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- Enable RLS on all tables
ALTER TABLE public.farmers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.vets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.consultations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.consultation_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.symptoms_reports ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
-- Farmers policies
CREATE POLICY "Farmers can read their own records"
    ON public.farmers FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Farmers can update their own records"
    ON public.farmers FOR UPDATE
    USING (auth.uid() = user_id);

-- Vets policies
CREATE POLICY "Vets can read their own records"
    ON public.vets FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "Vets can update their own records"
    ON public.vets FOR UPDATE
    USING (auth.uid() = user_id);

-- Create indexes
CREATE INDEX farmer_user_id_idx ON public.farmers(user_id);
CREATE INDEX vet_user_id_idx ON public.vets(user_id);
CREATE INDEX consultation_farmer_idx ON public.consultations(farmer_id);
CREATE INDEX consultation_vet_idx ON public.consultations(vet_id);
CREATE INDEX consultation_msg_idx ON public.consultation_messages(consultation_id);
CREATE INDEX symptom_report_farmer_idx ON public.symptoms_reports(farmer_id);
