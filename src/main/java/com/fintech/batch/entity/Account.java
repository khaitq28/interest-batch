package com.fintech.batch.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public Long getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getBalance() { return balance; }
    public double getAnnualInterestRate() { return annualInterestRate; }
    public BigDecimal getAccruedInterest() { return accruedInterest; }
    public LocalDateTime getLastInterestCalculatedAt() { return lastInterestCalculatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setAnnualInterestRate(double annualInterestRate) { this.annualInterestRate = annualInterestRate; }
    public void setAccruedInterest(BigDecimal accruedInterest) { this.accruedInterest = accruedInterest; }
    public void setLastInterestCalculatedAt(LocalDateTime lastInterestCalculatedAt) { this.lastInterestCalculatedAt = lastInterestCalculatedAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
