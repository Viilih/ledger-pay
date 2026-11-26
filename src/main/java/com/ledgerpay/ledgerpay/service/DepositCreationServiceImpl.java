package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.DepositRequest;
import com.ledgerpay.ledgerpay.dto.DepositResponse;
import com.ledgerpay.ledgerpay.exception.InvalidTransactionException;
import com.ledgerpay.ledgerpay.mapper.TransactionMapper;
import com.ledgerpay.ledgerpay.model.*;
import com.ledgerpay.ledgerpay.repository.AccountRepository;
import com.ledgerpay.ledgerpay.repository.LedgerEntryRepository;
import com.ledgerpay.ledgerpay.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositCreationServiceImpl {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountRepository accountRepository;

    public DepositCreationServiceImpl(
            TransactionRepository transactionRepository,
            LedgerEntryRepository ledgerEntryRepository,
            AccountRepository accountRepository
    ){
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public DepositResponse create(DepositRequest request){

        AccountEntity userAccount = accountRepository.findByIdForUpdate(request.accountId())
                .orElseThrow(() -> new InvalidTransactionException("Account not found")
                );
        if (userAccount.getType() != AccountType.USER){
            throw new InvalidTransactionException("The type of the account is invalid");
        }

        if (userAccount.getStatus() != AccountStatus.ACTIVE){
            throw new InvalidTransactionException("The account must be active");
        }

        if (!userAccount.getCurrency().equals(request.currency())){
            throw new InvalidTransactionException("The currency supported by the account is different from the request");
        }

        AccountEntity systemAccount = accountRepository.findByTypeAndCurrency(AccountType.SYSTEM, request.currency())
                .orElseThrow(() -> new InvalidTransactionException("System account not found"));

        TransactionEntity transaction = transactionRepository.save(
                TransactionEntity.builder()
                        .idempotencyKey(request.idempotencyKey())
                        .description(request.description())
                        .type(TransactionType.DEPOSIT)
                        .build()
        );

        LedgerEntryEntity debit = ledgerEntryRepository.save(
                LedgerEntryEntity.builder()
                        .transaction(transaction)
                        .account(systemAccount)
                        .direction(LedgerEntryDirection.DEBIT)
                        .amount(request.amount())
                        .currency(request.currency())
                        .build()
        );

        LedgerEntryEntity credit = ledgerEntryRepository.save(
                LedgerEntryEntity.builder()
                        .transaction(transaction)
                        .account(userAccount)
                        .direction(LedgerEntryDirection.CREDIT)
                        .amount(request.amount())
                        .currency(request.currency())
                        .build()
        );

        return TransactionMapper.toDepositResponse(transaction, credit);
    }
}
