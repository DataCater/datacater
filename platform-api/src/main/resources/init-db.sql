create table deployments
(
    id        uuid not null
        primary key,
    createdat timestamp,
    name      varchar(255),
    spec      jsonb,
    updatedat timestamp,
    tenant_id text
);

create table pipeline
(
    id        uuid not null
        primary key,
    createdat timestamp,
    metadata  jsonb,
    name      varchar(255),
    spec      jsonb,
    updatedat timestamp,
    tenant_id text
);

create table streams
(
    id        uuid not null
        primary key,
    createdat timestamp,
    name      varchar(255),
    spec      jsonb,
    updatedat timestamp,
    tenant_id text
);

alter table deployments enable row level security;
create policy rls_policy on  deployments
    using (tenant_id = current_setting('datacater.tenant'::text));

alter table pipeline enable row level security;
create policy rls_policy on pipeline
    using (tenant_id = current_setting('datacater.tenant'::text));

alter table streams enable row level security;
create policy rls_policy on streams
    using (tenant_id = current_setting('datacater.tenant'::text));

create role datacater
    with login createrole password 'datacater';

GRANT ALL ON pipeline, deployments, streams TO datacater;
