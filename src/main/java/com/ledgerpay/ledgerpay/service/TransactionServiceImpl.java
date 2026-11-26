package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.DepositRequest;
import com.ledgerpay.ledgerpay.dto.DepositResponse;
import com.ledgerpay.ledgerpay.dto.TransferRequest;
import com.ledgerpay.ledgerpay.dto.TransferResponse;
import com.ledgerpay.ledgerpay.exception.InvalidTransactionException;
import com.ledgerpay.ledgerpay.mapper.TransactionMapper;
import com.ledgerpay.ledgerpay.model.*;
import com.ledgerpay.ledgerpay.repository.LedgerEntryRepository;
import com.ledgerpay.ledgerpay.repository.TransactionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository  transactionRepository;
    private final LedgerEntryRepository  ledgerEntryRepository;
    private final TransferCreationServiceImpl transferCreationService;
    private final DepositCreationServiceImpl depositCreationService;



    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  LedgerEntryRepository ledgerEntryRepository,
                                  TransferCreationServiceImpl transferCreationService,
                                  DepositCreationServiceImpl depositCreationService
    ){
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transferCreationService = transferCreationService;
        this.depositCreationService = depositCreationService;
    }

    public TransferResponse transfer(TransferRequest request) {

        if (request.idempotencyKey() == null) {
            throw new InvalidTransactionException("Idempotency key is required.");
        }

        Optional<TransferResponse> existing = findExisting(request);
        if (existing.isPresent()) {
            return existing.get();
        }

        if (!isAmountValid(request.amount())){
            throw new InvalidTransactionException(
                    "Transfer amount must be greater than zero."
            );
        }

        if (request.sourceAccountId() == null || request.destinationAccountId() == null) {
            throw new InvalidTransactionException("Source and destination accounts are required.");
        }

        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new InvalidTransactionException("Source and destination accounts must be different.");
        }

        // create() runs in its own transaction. A unique violation on
        // idempotency_key rolls that transaction back; this method then
        // reads the transfer the other request committed.
        try {
            return transferCreationService.create(request);
        } catch (DataIntegrityViolationException exception) {
            return findExisting(request).orElseThrow(() -> exception);
        }

    }


    public DepositResponse deposit(DepositRequest request){

        if (request.idempotencyKey() == null) {
            throw new InvalidTransactionException("Idempotency key is required.");
        }

        Optional<DepositResponse> existing = findExistingDeposit(request);
        if (existing.isPresent()) {
            return existing.get();
        }

        if (request.accountId() == null) {
            throw new InvalidTransactionException("Account is required.");
        }

        if (!isAmountValid(request.amount())) {
            throw new InvalidTransactionException(
                    "Deposit amount must be greater than zero."
            );
        }

        if (request.currency() == null) {
            throw new InvalidTransactionException("Currency is required.");
        }

        try {
            return depositCreationService.create(request);
        } catch (DataIntegrityViolationException exception) {
            return findExistingDeposit(request).orElseThrow(() -> exception);
        }
    }



     private Optional<TransferResponse> findExisting(TransferRequest request) {
        Optional<TransactionEntity> currentTransaction =
                transactionRepository.findByIdempotencyKey(request.idempotencyKey());

        if (currentTransaction.isEmpty()) {
            return Optional.empty();
        }

        TransactionEntity transaction = currentTransaction.get();
        if (transaction.getType() != TransactionType.TRANSFER) {
            throw new InvalidTransactionException("Idempotency key belongs to a different transaction type.");
        }

        List<LedgerEntryEntity> entries =
                ledgerEntryRepository.findAllByTransaction_Id(transaction.getId());

        boolean validAmountOfMoney = isAmountOfMoneyTransferredValid(entries);
        boolean validCurrency = isCurrencyOfMoneyTransferredValid(entries, request.currency());

        if (!validAmountOfMoney || !validCurrency) {
            throw new InvalidTransactionException("The transaction has inconsistent entries");
        }

        LedgerEntryEntity debit = entries.stream()
                .filter(entry -> entry.getDirection() == LedgerEntryDirection.DEBIT)
                .findFirst()
                .orElseThrow(() -> new InvalidTransactionException("Debit entry not found"));

        LedgerEntryEntity credit = entries.stream()
                .filter(entry -> entry.getDirection() == LedgerEntryDirection.CREDIT)
                .findFirst()
                .orElseThrow(() -> new InvalidTransactionException("Credit entry not found"));

        return Optional.of(TransactionMapper.toResponse(transaction, debit, credit));
    }

    private Optional<DepositResponse> findExistingDeposit(DepositRequest request) {
        Optional<TransactionEntity> currentTransaction =
                transactionRepository.findByIdempotencyKey(request.idempotencyKey());

        if (currentTransaction.isEmpty()) {
            return Optional.empty();
        }

        TransactionEntity transaction = currentTransaction.get();
        if (transaction.getType() != TransactionType.DEPOSIT) {
            throw new InvalidTransactionException("Idempotency key belongs to a different transaction type.");
        }

        List<LedgerEntryEntity> entries =
                ledgerEntryRepository.findAllByTransaction_Id(transaction.getId());

        boolean validAmountOfMoney = isAmountOfMoneyTransferredValid(entries);
        boolean validCurrency = isCurrencyOfMoneyTransferredValid(entries, request.currency());

        if (!validAmountOfMoney || !validCurrency) {
            throw new InvalidTransactionException("The transaction has inconsistent entries");
        }

        LedgerEntryEntity credit = entries.stream()
                .filter(entry -> entry.getDirection() == LedgerEntryDirection.CREDIT)
                .findFirst()
                .orElseThrow(() -> new InvalidTransactionException("Credit entry not found"));

        return Optional.of(TransactionMapper.toDepositResponse(transaction, credit));
    }

    private boolean isAmountOfMoneyTransferredValid(List<LedgerEntryEntity> entries){

        if (entries.size() != 2) {
            return false;
        }

        LedgerEntryEntity debit = null;
        LedgerEntryEntity credit = null;

        for (LedgerEntryEntity entry : entries){

            if (entry.getDirection() == LedgerEntryDirection.DEBIT) {
                debit = entry;
            } else if (entry.getDirection() == LedgerEntryDirection.CREDIT) {
                credit = entry;
            } else {
                return false;
            }
        }

        return debit != null
                && credit != null
                && debit.getAmount().equals(credit.getAmount());
    }

    private boolean isCurrencyOfMoneyTransferredValid(
            List<LedgerEntryEntity> entries,
            String expectedCurrency
    ) {
        if (entries.isEmpty() || expectedCurrency == null) {
            return false;
        }

        for (LedgerEntryEntity entry : entries) {
            if (!expectedCurrency.equals(entry.getCurrency())) {
                return false;
            }
        }

        return true;
    }

    private boolean isAmountValid(Long amount){
        return amount != null && amount > 0;
    }
}
