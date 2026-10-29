package com.ledgerpay.ledgerpay.controller;

import com.ledgerpay.ledgerpay.dto.AccountResponse;
import com.ledgerpay.ledgerpay.dto.CreateAccountRequest;
import com.ledgerpay.ledgerpay.dto.UpdateAccountRequest;
import com.ledgerpay.ledgerpay.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request){
        return accountService.createAccount(request);
    }


    @PatchMapping("/{id}/status")
    public AccountResponse updateAccountStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountRequest request) {
        return accountService.updateAccountStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@PathVariable Long id){
         accountService.deleteAccount(id);
    }
}
