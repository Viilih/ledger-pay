INSERT INTO accounts (account_number, status, currency, type, created_at, updated_at)
VALUES
    ('SYS-FUND-BRL', 'ACTIVE', 'BRL', 'SYSTEM', now(), now()),
    ('SYS-FUND-USD', 'ACTIVE', 'USD', 'SYSTEM', now(), now())
ON CONFLICT (account_number) DO NOTHING;
