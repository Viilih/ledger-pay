ALTER TABLE ledger_transactions ADD COLUMN
    type VARCHAR(20) not null default  'TRANSFER'
        check( type in ('TRANSFER', 'DEPOSIT'));

