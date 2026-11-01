package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.TransferRequest;
import com.ledgerpay.ledgerpay.dto.TransferResponse;
import com.ledgerpay.ledgerpay.exception.InvalidAccountStatusTransitionException;
import com.ledgerpay.ledgerpay.exception.InvalidTransactionException;
import com.ledgerpay.ledgerpay.mapper.TransactionMapper;
import com.ledgerpay.ledgerpay.model.LedgerEntryDirection;
import com.ledgerpay.ledgerpay.model.LedgerEntryEntity;
import com.ledgerpay.ledgerpay.model.TransactionEntity;
import com.ledgerpay.ledgerpay.repository.LedgerEntryRepository;
import com.ledgerpay.ledgerpay.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository  transactionRepository;
    private final LedgerEntryRepository  ledgerEntryRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
    LedgerEntryRepository ledgerEntryRepository
    ){
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

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
                throw new InvalidAccountStatusTransitionException(
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
        return null;
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
