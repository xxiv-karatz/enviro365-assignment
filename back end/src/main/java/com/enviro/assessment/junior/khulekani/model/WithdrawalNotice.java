package com.enviro.assessment.junior.khulekani.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class WithdrawalNotice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private WithdrawalType type;

    @Enumerated(EnumType.STRING)
    private WithdrawalStatus status;

    private LocalDateTime requestDate;
    private String reason;

    public WithdrawalNotice() {
    }

    public WithdrawalNotice(Portfolio portfolio, BigDecimal amount, WithdrawalType type,
                            WithdrawalStatus status, LocalDateTime requestDate, String reason) {
        this.portfolio = portfolio;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.requestDate = requestDate;
        this.reason = reason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public WithdrawalType getType() {
        return type;
    }

    public void setType(WithdrawalType type) {
        this.type = type;
    }

    public WithdrawalStatus getStatus() {
        return status;
    }

    public void setStatus(WithdrawalStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
