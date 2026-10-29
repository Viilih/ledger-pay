package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.AccountResponse;
import com.ledgerpay.ledgerpay.dto.CreateAccountRequest;
import com.ledgerpay.ledgerpay.dto.UpdateAccountRequest;
import com.ledgerpay.ledgerpay.model.AccountStatus;

public interface AccountService {
    public AccountResponse createAccount(CreateAccountRequest createAccountDto);

    public AccountResponse updateAccountStatus(Long id, UpdateAccountRequest updateAccountStatusDto);

    public void deleteAccount(Long id);
}
