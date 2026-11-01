package com.ledgerpay.ledgerpay.repository;

import com.ledgerpay.ledgerpay.model.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, Long> {
    List<LedgerEntryEntity> findAllByTransaction_Id(Long transactionId);
}
