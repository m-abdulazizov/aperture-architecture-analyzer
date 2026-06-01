alter table scan_issues
    add column if not exists fingerprint varchar(128);

update scan_issues
set fingerprint = 'legacy-' || cast(id as varchar)
where fingerprint is null;

alter table scan_issues
    alter column fingerprint set not null;

create index if not exists idx_scan_issues_fingerprint on scan_issues (fingerprint);

create table if not exists suppressed_issues (
    id uuid primary key,
    project_id uuid not null references projects(id),
    fingerprint varchar(128) not null,
    rule_code varchar(150) not null,
    file_path varchar(1000),
    reason varchar(1000) not null,
    created_at timestamp not null
);

create unique index if not exists uk_suppressed_issues_project_fingerprint
    on suppressed_issues (project_id, fingerprint);

create index if not exists idx_suppressed_issues_project_id on suppressed_issues (project_id);
create index if not exists idx_suppressed_issues_fingerprint on suppressed_issues (fingerprint);
create index if not exists idx_suppressed_issues_rule_code on suppressed_issues (rule_code);
