package com.ledgerpay.ledgerpay.dto;

import com.ledgerpay.ledgerpay.model.AccountStatus;

import java.time.LocalDateTime;

public record AccountResponse(Long id, String accountNumber, AccountStatus status,
                              LocalDateTime createdAt,
                              LocalDateTime updatedAt) {
}
