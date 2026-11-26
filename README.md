# ledger-pay

## Roadmap / Missing items

- [ ] POST /deposits — deposit endpoint that writes a ledger entry (idempotent)
- [ ] GET /accounts/{id}/balance — current balance query
- [ ] GET /accounts/{id}/statement — paginated statement of the account's ledger entries
- [ ] Idempotency replay validation — check that the payload (accounts/amount) matches the original key; return 409/422 on mismatch
- [ ] Lock timeout — configure lock_timeout on transactions that use SELECT FOR UPDATE and map the error to a clean HTTP response (503/409)
- [ ] Complete-flow integration tests — create accounts → deposit → transfer → check balance and statement
- [ ] Concurrency tests — double-spend (balance never goes negative), deadlock prevention (A → B and B → A at the same time), and concurrent idempotency
- [ ] Deploy / Docker — docker compose up working in one command + online deploy
- [ ] Documentation — README.md in English (architecture, technical decisions, curl examples, how to run tests, Out of Scope mentioning Auth) and update/remove docs/ledger.md
