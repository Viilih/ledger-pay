package com.ledgerpay.ledgerpay.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_transactions")
@Getter
@NoArgsConstructor
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key",  nullable = false, unique = true, updatable = false)
    private UUID idempotencyKey;

    @Column(name = "description")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "created_at", nullable = false)
    @Setter(AccessLevel.NONE)
    @CreationTimestamp
    private Instant createdAt;

    @Builder
    public TransactionEntity(UUID idempotencyKey, String description, TransactionType type) {
        this.idempotencyKey = idempotencyKey;
        this.description = description;
        this.type = type;
    }
}
