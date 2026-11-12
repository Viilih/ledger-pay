package com.ledgerpay.ledgerpay.dto;

import java.util.UUID;

public record TransferRequest (
        Long sourceAccountId,
        Long destinationAccountId,
        String currency,
        Long amount,
        UUID idempotencyKey,
        String description
){}
