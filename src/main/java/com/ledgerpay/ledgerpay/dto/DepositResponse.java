package com.ledgerpay.ledgerpay.dto;

import com.ledgerpay.ledgerpay.model.TransferStatus;

import java.util.UUID;

public record DepositResponse(Long accountId,
                              String currency,
                              Long amount,
                              UUID idempotencyKey,
                              TransferStatus status) {
}
