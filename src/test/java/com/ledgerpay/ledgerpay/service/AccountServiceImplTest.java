package com.ledgerpay.ledgerpay.service;

import com.ledgerpay.ledgerpay.dto.CreateAccountRequest;
import com.ledgerpay.ledgerpay.dto.UpdateAccountRequest;
import com.ledgerpay.ledgerpay.exception.AccountAlreadyExistException;
import com.ledgerpay.ledgerpay.exception.AccountNotFoundWithIdException;
import com.ledgerpay.ledgerpay.exception.InvalidAccountStatusTransitionException;
import com.ledgerpay.ledgerpay.model.AccountEntity;
import com.ledgerpay.ledgerpay.model.AccountStatus;
import com.ledgerpay.ledgerpay.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {
    @Mock
    private AccountRepository accountRepository;
    private AccountService accountService;



    @BeforeEach
    void setUp(){
        accountService = new AccountServiceImpl(accountRepository);
    }


    @Test
    void shouldCreateAccountWithActiveStatus(){
        var request = new CreateAccountRequest("123456789012", "BRL");

        when(accountRepository.existsByAccountNumber("123456789012")).thenReturn(false);

        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity account = invocation.getArgument(0);
            account.setId(1L);
            return account;
        });
        var response = accountService.createAccount(request);

        var captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(captor.capture());

        AccountEntity persistedAccount = captor.getValue();

        assertEquals(request.accountNumber(), persistedAccount.getAccountNumber());
        assertEquals("BRL", persistedAccount.getCurrency());
        assertEquals(AccountStatus.ACTIVE, persistedAccount.getStatus());

        assertEquals(1L, response.id());
        assertEquals(request.accountNumber(), response.accountNumber());
        assertEquals(AccountStatus.ACTIVE, response.status());

    }


    @Test
    void shouldRejectStatusUpdateWhenAccountDoesNotExist(){
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new UpdateAccountRequest(AccountStatus.CLOSED);

        assertThrows(AccountNotFoundWithIdException.class, () -> accountService.updateAccountStatus(99L, request));

        verify(accountRepository, never()).save(any());
    }


    @Test
    void shouldRejectReopeningClosedAccount(){
        var account = AccountEntity.builder()
                .id(1L)
                .status(AccountStatus.CLOSED)
                .build();


        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        var request = new UpdateAccountRequest(AccountStatus.ACTIVE);

        assertThrows(InvalidAccountStatusTransitionException.class,
                () -> accountService.updateAccountStatus(1L, request));

        assertEquals(AccountStatus.CLOSED, account.getStatus());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldCloseActiveAccountThroughStatusUpdate(){
        var account = AccountEntity.builder()
                .id(1L)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));


        when(accountRepository.save(any(AccountEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = accountService.updateAccountStatus(1L, new UpdateAccountRequest(AccountStatus.CLOSED));

        assertEquals(AccountStatus.CLOSED, response.status());
        assertEquals(AccountStatus.CLOSED, account.getStatus());
        verify(accountRepository).save(account);
    }


    @Test
    void shouldCloseAccountWhenDeleted() {
        var account = AccountEntity.builder()
                .id(1L)
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        accountService.deleteAccount(1L);

        assertEquals(AccountStatus.CLOSED, account.getStatus());
        verify(accountRepository).save(account);
    }

    @Test
    void shouldRejectDuplicateAccount(){
        var request = new CreateAccountRequest("123456", "BRL");

        when(accountRepository.existsByAccountNumber("123456")).thenReturn(true);

        assertThrows(AccountAlreadyExistException.class, () -> accountService.createAccount(request));

        verify(accountRepository, never()).save(any());
    }

}
