package com.ledgerpay.ledgerpay.repository;

import com.ledgerpay.ledgerpay.model.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, Long> {
    List<LedgerEntryEntity> findAllByTransaction_Id(Long transactionId);


    @Query("""
        select coalesce(sum(
            case when e.direction = 'CREDIT'
                then e.amount else -e.amount
             end
            ), 0)
        from LedgerEntryEntity e
            where e.account.id = :accountId
    """)
    Long sumBalanceByAccountId(@Param("accountId") Long accountId);
}
