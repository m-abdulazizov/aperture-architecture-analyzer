create table if not exists app_users (
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(128) not null,
    api_token varchar(100) not null,
    created_at timestamp not null
);

create unique index if not exists uq_app_users_email on app_users(lower(email));
create unique index if not exists uq_app_users_api_token on app_users(api_token);
