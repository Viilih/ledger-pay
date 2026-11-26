package com.ledgerpay.ledgerpay.dto;

import java.util.UUID;

public record DepositRequest(Long accountId,
                             String currency,
                             Long amount,
                             UUID idempotencyKey,
                             String description) {
}
