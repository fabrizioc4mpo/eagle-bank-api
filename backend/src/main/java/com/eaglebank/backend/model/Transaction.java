package com.eaglebank.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    private String id; // format: tan-xxxxx

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency; // e.g. GBP

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String reference;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number", nullable = false)
    private BankAccount account;
}
