package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.DepositRequest;
import com.ledgerpay.ledgerpay.dto.DepositResponse;
import com.ledgerpay.ledgerpay.dto.TransferRequest;
import com.ledgerpay.ledgerpay.dto.TransferResponse;

public interface TransactionService {

    TransferResponse transfer(TransferRequest request);

    DepositResponse  deposit(DepositRequest request);
}
