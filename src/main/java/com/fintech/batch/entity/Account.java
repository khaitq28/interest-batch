package com.fintech.batch.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private double annualInterestRate;

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal accruedInterest;

    private LocalDateTime lastInterestCalculatedAt;
    private LocalDateTime createdAt;

    public Account() {}
}
