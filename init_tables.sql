-- Chat table for real-time messaging
create table if not exists public.consultation_messages (
  id uuid primary key default gen_random_uuid(),
  consultation_id text not null,
  sender_id text not null,
  sender_role text not null,
  sender_username text not null,
  message text not null,
  created_at timestamp with time zone default timezone('utc'::text, now())
);

-- Problem reporting table
create table if not exists public.problems (
  id uuid primary key default gen_random_uuid(),
  farmer_id text not null,
  symptoms text not null,
  status text default 'open',
  created_at timestamp with time zone default timezone('utc'::text, now()),
  vet_id text,
  solution text
);
