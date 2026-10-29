package com.ledgerpay.ledgerpay.dto;

import com.ledgerpay.ledgerpay.model.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank(message = "Account number is required")
        @Size(max = 12, message = "Account number must have at most 12 characters")
        String accountNumber,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "Currency must have 3 uppercase letters")
        String currency
) {}
