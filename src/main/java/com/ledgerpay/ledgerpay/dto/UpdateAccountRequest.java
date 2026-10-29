package com.ledgerpay.ledgerpay.dto;

import com.ledgerpay.ledgerpay.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountRequest(
        @NotNull(message = "Status is required")
        AccountStatus status
) {}