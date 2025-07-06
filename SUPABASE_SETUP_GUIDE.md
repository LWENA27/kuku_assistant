# Supabase Setup Guide for Kuku Assistant

## 1. Install Supabase CLI

```bash
npm install -g supabase
```

## 2. Login to Supabase

```bash
supabase login
```

## 3. Initialize Supabase Project (if not done)

```bash
supabase init
```

## 4. Create the `problems` Table

```sql
-- Run this in the Supabase SQL editor or via CLI:
create table if not exists public.problems (
  id uuid primary key default gen_random_uuid(),
  farmer_id text not null,
  symptoms text not null,
  status text default 'open',
  created_at timestamp with time zone default timezone('utc'::text, now()),
  vet_id text,
  solution text
);
```

## 5. Enable Row Level Security (RLS) and Policies

```sql
-- Enable RLS
alter table public.problems enable row level security;

-- Allow insert for authenticated users
create policy "Allow farmers to submit problems" on public.problems
  for insert using (auth.role() = 'authenticated');

-- Allow vets/admins to select/update
create policy "Allow vets to view and solve problems" on public.problems
  for select, update using (auth.role() in ('authenticated', 'service_role'));
```

## 6. Get Supabase Project URL and Anon Key
- Go to your Supabase project dashboard → Project Settings → API
- Copy the `Project URL` and `anon` public API key
- Replace the placeholders in your Android code:
  - `SUPABASE_URL = "https://YOUR_SUPABASE_PROJECT_ID.supabase.co/rest/v1/problems"`
  - `SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"`

## 7. Test Submission from App
- When a farmer submits a report, it will POST to the `problems` table.
- Vets/admins can view and update these via Supabase dashboard or a custom admin interface.

## 8. (Optional) Expose REST API to Admin App
- Use Supabase's auto-generated REST API (PostgREST) for admin/vet dashboard.
- You can filter by `status = 'open'` to show unsolved problems.

## 9. Create the `consultation_messages` Table (for Chat)

```sql
-- Run this in the Supabase SQL editor or CLI:
create table if not exists public.consultation_messages (
  id uuid primary key default gen_random_uuid(),
  consultation_id text not null, -- group messages by consultation/session
  sender_id text not null,
  sender_role text not null, -- 'farmer' or 'vet'
  sender_username text not null,
  message text not null,
  created_at timestamp with time zone default timezone('utc'::text, now())
);
```

## 10. Enable RLS and Policies for Chat

```sql
alter table public.consultation_messages enable row level security;

-- Allow insert for authenticated users
create policy "Allow users to send messages" on public.consultation_messages
  for insert using (auth.role() = 'authenticated');

-- Allow select for authenticated users (filter by consultation_id in app)
create policy "Allow users to view messages" on public.consultation_messages
  for select using (auth.role() = 'authenticated');
```

## 11. Using the Chat API
- POST to `/rest/v1/consultation_messages` to send a message
- GET from `/rest/v1/consultation_messages?consultation_id=eq.SESSION_ID&order=created_at.asc` to fetch messages for a session
- Each message should include sender_username and sender_role for display

---

**This enables real-time chat between farmer and vet, with all messages stored in Supabase and visible to both parties.**
