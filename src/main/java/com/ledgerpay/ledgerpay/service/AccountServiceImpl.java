package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.AccountResponse;
import com.ledgerpay.ledgerpay.dto.CreateAccountRequest;
import com.ledgerpay.ledgerpay.dto.UpdateAccountRequest;
import com.ledgerpay.ledgerpay.exception.AccountAlreadyExistException;
import com.ledgerpay.ledgerpay.exception.AccountNotFoundWithIdException;
import com.ledgerpay.ledgerpay.exception.InvalidAccountStatusTransitionException;
import com.ledgerpay.ledgerpay.mapper.AccountMapper;
import com.ledgerpay.ledgerpay.model.AccountEntity;
import com.ledgerpay.ledgerpay.model.AccountStatus;
import com.ledgerpay.ledgerpay.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository repository;

    public AccountServiceImpl(AccountRepository repository){
        this.repository = repository;
    }
    public AccountResponse createAccount(CreateAccountRequest createAccountDto) {


        boolean accountAlreadyExists = repository.existsByAccountNumber(createAccountDto.accountNumber());

        if (accountAlreadyExists){
            throw new AccountAlreadyExistException(
                    String.format("Account already exists with number %s", createAccountDto.accountNumber()));
        }


        // Convert  DTO to Entity
        AccountEntity account = AccountEntity.builder()
                .accountNumber(createAccountDto.accountNumber())
                .currency(createAccountDto.currency())
                .status(AccountStatus.ACTIVE)
                .build();

        AccountEntity savedAccount = repository.save(account);

        return AccountMapper.toResponse(savedAccount);
    }

    public AccountResponse updateAccountStatus(Long id, UpdateAccountRequest updateAccountStatusDto) {
       AccountEntity account = repository.findById(id)
               .orElseThrow(() -> new AccountNotFoundWithIdException(String.
                       format("Account not found with id %s", id)));

       if (account.getStatus() == AccountStatus.CLOSED
               && updateAccountStatusDto.status() != AccountStatus.CLOSED){
           throw new InvalidAccountStatusTransitionException("Invalid status");
       }

       account.setStatus(updateAccountStatusDto.status());
       AccountEntity savedAccount = repository.save(account);

       return AccountMapper.toResponse(savedAccount);
    }

    public void deleteAccount(Long id){
        AccountEntity account = repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundWithIdException(String.
                        format("Account not found with id %s", id)));

        account.setStatus(AccountStatus.CLOSED);
        AccountEntity savedAccount = repository.save(account);
    }
}
