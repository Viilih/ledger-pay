package com.ledgerpay.ledgerpay.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "ledger_entries")
@Getter
@NoArgsConstructor
public class LedgerEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "direction", nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private LedgerEntryDirection direction;


    // amount in cents
    @Column(nullable = false, updatable = false)
    private Long amount;

    @Column(nullable = false, length = 3, updatable = false)
    private String currency;


    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false)
    private TransactionEntity transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private AccountEntity account;

    @Builder
    public LedgerEntryEntity(
            TransactionEntity transaction,
            AccountEntity account,
            LedgerEntryDirection direction,
            Long amount,
            String currency
    ){
        this.transaction = transaction;
        this.account = account;
        this.direction = direction;
        this.amount = amount;
        this.currency = currency;
    }
}
