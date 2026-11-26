ALTER TABLE accounts ADD COLUMN
    type VARCHAR(20) not null default  'USER'
        check( type in ('USER', 'SYSTEM'));