create table if not exists sample_user (
  id bigserial primary key,
  email varchar(255) not null unique,
  name varchar(100) not null,
  created_at timestamptz not null default now()
);