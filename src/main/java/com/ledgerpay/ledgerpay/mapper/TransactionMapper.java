package com.ledgerpay.ledgerpay.mapper;

import com.ledgerpay.ledgerpay.dto.TransferResponse;
import com.ledgerpay.ledgerpay.model.LedgerEntryEntity;
import com.ledgerpay.ledgerpay.model.TransactionEntity;
import com.ledgerpay.ledgerpay.model.TransferStatus;

public class TransactionMapper {
    public static TransferResponse toResponse(TransactionEntity transaction,
                                              LedgerEntryEntity debit,
                                              LedgerEntryEntity credit){
        return new TransferResponse(
                debit.getAccount().getId(),
                credit.getAccount().getId(),
                debit.getCurrency(),
                debit.getAmount(),
                transaction.getIdempotencyKey(),
                TransferStatus.COMPLETED
        );
    }
}
