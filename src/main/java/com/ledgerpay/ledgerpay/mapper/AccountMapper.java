package com.ledgerpay.ledgerpay.mapper;

import com.ledgerpay.ledgerpay.dto.AccountResponse;
import com.ledgerpay.ledgerpay.model.AccountEntity;

public class AccountMapper {
    private AccountMapper(){

    }

    public static AccountResponse toResponse(AccountEntity account){
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getStatus(),
                account.getCurrency(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
