alter table scan_jobs add column if not exists stage varchar(50);
alter table scan_jobs add column if not exists progress_percent integer not null default 0;

create table if not exists rule_configurations (
    id uuid primary key,
    project_id uuid,
    rule_code varchar(150) not null,
    enabled boolean not null,
    severity_override varchar(50),
    created_at timestamp not null,
    updated_at timestamp
);

create unique index if not exists uq_rule_config_global_rule
    on rule_configurations(rule_code)
    where project_id is null;

create unique index if not exists uq_rule_config_project_rule
    on rule_configurations(project_id, rule_code)
    where project_id is not null;

create index if not exists idx_rule_config_rule_code on rule_configurations(rule_code);
