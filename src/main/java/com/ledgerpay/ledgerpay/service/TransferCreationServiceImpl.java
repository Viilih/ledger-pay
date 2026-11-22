package com.ledgerpay.ledgerpay.service;



import com.ledgerpay.ledgerpay.dto.TransferRequest;
import com.ledgerpay.ledgerpay.dto.TransferResponse;
import com.ledgerpay.ledgerpay.exception.InvalidTransactionException;
import com.ledgerpay.ledgerpay.mapper.TransactionMapper;
import com.ledgerpay.ledgerpay.model.AccountEntity;
import com.ledgerpay.ledgerpay.model.AccountStatus;
import com.ledgerpay.ledgerpay.model.LedgerEntryDirection;
import com.ledgerpay.ledgerpay.model.LedgerEntryEntity;
import com.ledgerpay.ledgerpay.model.TransactionEntity;
import com.ledgerpay.ledgerpay.repository.AccountRepository;
import com.ledgerpay.ledgerpay.repository.LedgerEntryRepository;
import com.ledgerpay.ledgerpay.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TransferCreationServiceImpl {
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;

    public TransferCreationServiceImpl(
            TransactionRepository transactionRepository,
            LedgerEntryRepository ledgerEntryRepository,
            AccountRepository accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRepository = accountRepository;
    }

    // Must be a Spring bean method so @Transactional opens a real transaction.
    // Calling this from the same class would skip the proxy.
    @Transactional
    public TransferResponse create(TransferRequest request) {
        Long firstId = Math.min(request.sourceAccountId(), request.destinationAccountId());
        Long secondId = Math.max(request.sourceAccountId(), request.destinationAccountId());

        AccountEntity first = accountRepository.findByIdForUpdate(firstId).orElseThrow(
                () -> new InvalidTransactionException("Account not found.")
        );
        AccountEntity second = accountRepository.findByIdForUpdate(secondId).orElseThrow(
                () -> new InvalidTransactionException("Account not found.")
        );

        AccountEntity source;
        AccountEntity destination;
        if (first.getId().equals(request.sourceAccountId())) {
            source = first;
            destination = second;
        } else {
            source = second;
            destination = first;
        }

        validateAccounts(source, destination, request.currency());

        Long balance = ledgerEntryRepository.sumBalanceByAccountId(source.getId());
        if (balance < request.amount()) {
            throw new InvalidTransactionException(
                    "Source account does not have enough balance"
            );
        }

        TransactionEntity transaction = transactionRepository.save(
                TransactionEntity.builder()
                        .idempotencyKey(request.idempotencyKey())
                        .description(request.description())
                        .build()
        );

        LedgerEntryEntity debit = ledgerEntryRepository.save(
                LedgerEntryEntity.builder()
                        .transaction(transaction)
                        .account(source)
                        .direction(LedgerEntryDirection.DEBIT)
                        .amount(request.amount())
                        .currency(request.currency())
                        .build()
        );

        LedgerEntryEntity credit = ledgerEntryRepository.save(
                LedgerEntryEntity.builder()
                        .transaction(transaction)
                        .account(destination)
                        .direction(LedgerEntryDirection.CREDIT)
                        .amount(request.amount())
                        .currency(request.currency())
                        .build()
        );

        return TransactionMapper.toResponse(transaction, debit, credit);
    }

    private void validateAccounts(AccountEntity source, AccountEntity destination, String currency) {
        if (source.getStatus() != AccountStatus.ACTIVE
                || destination.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransactionException("Both accounts must be active.");
        }
        if (currency == null
                || !currency.equals(source.getCurrency())
                || !currency.equals(destination.getCurrency())) {
            throw new InvalidTransactionException(
                    "Both accounts must use the requested currency."
            );
        }
    }

}
