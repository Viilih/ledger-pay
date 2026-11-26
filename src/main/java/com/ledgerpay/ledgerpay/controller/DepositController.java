package com.ledgerpay.ledgerpay.controller;

import com.ledgerpay.ledgerpay.dto.DepositRequest;
import com.ledgerpay.ledgerpay.dto.DepositResponse;
import com.ledgerpay.ledgerpay.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deposits")
public class DepositController {

    private final TransactionService transactionService;

    public DepositController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public DepositResponse deposit(@Valid @RequestBody DepositRequest request) {
        return transactionService.deposit(request);
    }
}
