create table ledger_transactions (
    id bigserial PRIMARY KEY not null,
    idempotency_key uuid unique not null,
    description text,
    created_at timestamptz not null default now()
);


create table ledger_entries (
   id bigserial PRIMARY KEY not null,
   transaction_id bigint not null,
   account_id bigint not null,
   direction text not null check (direction in
                         ('DEBIT', 'CREDIT')),
   amount bigint not null check ( amount > 0 ),
   currency varchar(3) not null,
   created_at timestamptz not null default now(),


   constraint fk_transaction_id
                            foreign key (transaction_id)
                            references ledger_transactions(id),

   constraint fk_account_id
                            foreign key (account_id)
                            references accounts(id)
);