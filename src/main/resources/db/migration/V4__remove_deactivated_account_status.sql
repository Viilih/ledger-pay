UPDATE accounts SET status = 'CLOSED' WHERE status = 'DEACTIVATED';

ALTER TABLE accounts DROP CONSTRAINT accounts_status_check;

ALTER TABLE accounts ADD CONSTRAINT accounts_status_check
    CHECK (status IN ('ACTIVE', 'BLOCKED', 'CLOSED'));
