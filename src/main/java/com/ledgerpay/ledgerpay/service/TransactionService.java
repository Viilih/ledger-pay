package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.TransferRequest;
import com.ledgerpay.ledgerpay.dto.TransferResponse;

public interface TransactionService {

    TransferResponse transfer(TransferRequest request);

}
