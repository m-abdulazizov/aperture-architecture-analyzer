alter table app_users
    alter column password_hash type varchar(255);

alter table app_users
    add column if not exists role varchar(50) not null default 'USER';
