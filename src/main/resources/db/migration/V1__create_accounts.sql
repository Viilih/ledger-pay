create table accounts(
    id bigserial PRIMARY KEY not null,
    accountNumber varchar(12) unique not null,
    status text not null  check( status in ('ACTIVE', 'BLOCKED', 'CLOSED')),
    currency varchar(3) not null,
    createdAt timestamp not null default now(),
    updatedAt timestamp not null
)
