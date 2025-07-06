-- Enable RLS and policies for chat
alter table public.consultation_messages enable row level security;
create policy "Allow users to send messages" on public.consultation_messages
  for insert using (auth.role() = 'authenticated');
create policy "Allow users to view messages" on public.consultation_messages
  for select using (auth.role() = 'authenticated');

-- Enable RLS and policies for problems
alter table public.problems enable row level security;
create policy "Allow farmers to submit problems" on public.problems
  for insert using (auth.role() = 'authenticated');
create policy "Allow vets to view and solve problems" on public.problems
  for select, update using (auth.role() in ('authenticated', 'service_role'));
