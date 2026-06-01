create table if not exists projects (
    id uuid primary key,
    name varchar(255) not null,
    description varchar(1000),
    original_file_name varchar(500),
    stored_file_path varchar(1000),
    extracted_path varchar(1000),
    status varchar(50) not null,
    failure_reason text,
    created_at timestamp not null,
    updated_at timestamp
);

create index if not exists idx_projects_status on projects(status);

create unique index if not exists uq_projects_name_active
    on projects(lower(name))
    where status <> 'DELETED';

create table if not exists scan_results (
    id uuid primary key,
    project_id uuid not null references projects(id) on delete cascade,
    total_score integer not null,
    architecture_score integer not null,
    security_score integer not null,
    persistence_score integer not null,
    maintainability_score integer not null,
    testing_score integer not null,
    total_issues integer not null,
    critical_issues integer not null,
    high_issues integer not null,
    medium_issues integer not null,
    low_issues integer not null,
    info_issues integer not null,
    started_at timestamp,
    finished_at timestamp,
    created_at timestamp not null
);

create index if not exists idx_scan_results_project_id on scan_results(project_id);
create index if not exists idx_scan_results_created_at on scan_results(created_at);

create table if not exists scan_issues (
    id uuid primary key,
    scan_result_id uuid not null references scan_results(id) on delete cascade,
    category varchar(100) not null,
    severity varchar(50) not null,
    rule_code varchar(150) not null,
    title varchar(500) not null,
    description text not null,
    recommendation text,
    file_path varchar(1000),
    line_number integer,
    created_at timestamp not null
);

create index if not exists idx_scan_issues_scan_result_id on scan_issues(scan_result_id);
create index if not exists idx_scan_issues_category on scan_issues(category);
create index if not exists idx_scan_issues_severity on scan_issues(severity);
create index if not exists idx_scan_issues_rule_code on scan_issues(rule_code);

create table if not exists scan_jobs (
    id uuid primary key,
    project_id uuid not null references projects(id) on delete cascade,
    scan_result_id uuid references scan_results(id) on delete set null,
    status varchar(50) not null,
    failure_reason text,
    created_at timestamp not null,
    started_at timestamp,
    finished_at timestamp
);

create index if not exists idx_scan_jobs_project_id on scan_jobs(project_id);
create index if not exists idx_scan_jobs_status on scan_jobs(status);
create index if not exists idx_scan_jobs_created_at on scan_jobs(created_at);
