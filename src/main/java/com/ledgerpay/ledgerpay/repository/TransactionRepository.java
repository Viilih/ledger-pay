package com.ledgerpay.ledgerpay.repository;

import com.ledgerpay.ledgerpay.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    Optional<TransactionEntity> findByIdempotencyKey(UUID idempotencyKey);
}
