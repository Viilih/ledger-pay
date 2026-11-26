CREATE UNIQUE INDEX
    uq_accounts_system_currency
    ON accounts (currency)
    WHERE type = 'SYSTEM';