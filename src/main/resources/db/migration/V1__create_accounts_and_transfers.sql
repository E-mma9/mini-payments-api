-- Initial schema. Never edit this file after it has run against a database:
-- Flyway stores a checksum per script. Add a new V2__... script for every change.

create table accounts (
    id         uuid                        primary key,
    owner      varchar(255)                not null,
    balance    numeric(19, 2)              not null check (balance >= 0),
    created_at timestamp(6) with time zone not null,
    version    bigint                      not null
);

create table transfers (
    id              uuid                        primary key,
    from_account_id uuid                        not null references accounts (id),
    to_account_id   uuid                        not null references accounts (id),
    amount          numeric(19, 2)              not null check (amount > 0),
    created_at      timestamp(6) with time zone not null
);

-- Speeds up GET /api/accounts/{id}/transfers, which filters on either column.
create index idx_transfers_from_account on transfers (from_account_id);
create index idx_transfers_to_account on transfers (to_account_id);
