package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.TransferRequest;
import com.ledgerpay.ledgerpay.dto.TransferResponse;
import com.ledgerpay.ledgerpay.exception.InvalidAccountStatusTransitionException;
import com.ledgerpay.ledgerpay.exception.InvalidTransactionException;
import com.ledgerpay.ledgerpay.mapper.TransactionMapper;
import com.ledgerpay.ledgerpay.model.*;
import com.ledgerpay.ledgerpay.repository.AccountRepository;
import com.ledgerpay.ledgerpay.repository.LedgerEntryRepository;
import com.ledgerpay.ledgerpay.repository.TransactionRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository  transactionRepository;
    private final LedgerEntryRepository  ledgerEntryRepository;
    private final AccountRepository      accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
    LedgerEntryRepository ledgerEntryRepository,
                                  AccountRepository accountRepository
    ){
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        // Search for idempotency key and return the current Transaction
        Optional<TransactionEntity> currentTransaction = transactionRepository.findByIdempotencyKey
                (request.idempotencyKey());

        if (currentTransaction.isPresent()){
            TransactionEntity transaction = currentTransaction.get();
            List<LedgerEntryEntity> entries = ledgerEntryRepository.findAllByTransaction_Id(transaction.getId());

            boolean validAmountOfMoney = isAmountOfMoneyTransferredValid(entries);
            boolean validCurrency = isCurrencyOfMoneyTransferredValid(entries, request.currency());

            if (!validAmountOfMoney || !validCurrency){
                throw new InvalidTransactionException(
                        "The transaction has inconsistent entries"
                );
            }

            LedgerEntryEntity debit = entries.stream().filter(entry ->
                    entry.getDirection() == LedgerEntryDirection.DEBIT)
                    .findFirst()
                    .orElseThrow(() -> new InvalidTransactionException("Debit entry not found"));

            LedgerEntryEntity credit = entries.stream().filter(entry ->
                    entry.getDirection() == LedgerEntryDirection.CREDIT)
                    .findFirst()
                    .orElseThrow(() -> new InvalidTransactionException("Credit entry not found"));

            return TransactionMapper.toResponse(transaction, debit, credit);
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



        // checar saldo da conta de origem

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

        if (balance < request.amount()){
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

    private void validateAccounts(
            AccountEntity source,
            AccountEntity destination,
            String currency
    ) {
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
    // 1 - receive request with source account, destiny account, idempotency key e amount

    // 2 - Buscar pela chave de idempotencia e retorno a transaçao existente

    // 3 - Validar contas origem e destino para verificar se estão ativas

    // 4 - Checar saldo da origem, caso não tenha o valor suficiente falha

    //  todos os créditos - todos os débitos que a conta origem recebeu

    // 5 - Criar Transaction, salvando a chave de idempotencia

    // 6 - Criar as entries de debito na origem e crédito na de destino,
    // com o mesmo transaction id, amout em centavos e a currency em cada entry

    // 7 - Atualizar o saldo
}
