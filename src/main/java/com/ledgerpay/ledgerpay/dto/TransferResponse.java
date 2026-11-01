package com.ledgerpay.ledgerpay.dto;

import com.ledgerpay.ledgerpay.model.TransferStatus;

import java.util.UUID;

public record TransferResponse(Long sourceAccountId,
                               Long destinationAccountId,
                               String currency,
                               Long amount,
                               UUID idempotencyKey,
                               TransferStatus status
                               ) {
}
