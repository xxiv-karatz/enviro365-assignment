package com.enviro.assessment.junior.khulekani.dto;

import com.enviro.assessment.junior.khulekani.model.WithdrawalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class WithdrawalRequest {
    @NotNull(message = "investorId is required")
    @Positive(message = "investorId must be greater than zero")
    private Long investorId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "type is required")
    private WithdrawalType type;

    public WithdrawalRequest() {
    }

    public Long getInvestorId() {
        return investorId;
    }

    public void setInvestorId(Long investorId) {
        this.investorId = investorId;
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
}
