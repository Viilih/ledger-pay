package com.ledgerpay.ledgerpay.repository;

import com.ledgerpay.ledgerpay.model.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    boolean existsByAccountNumber(String accountNumber);

}
