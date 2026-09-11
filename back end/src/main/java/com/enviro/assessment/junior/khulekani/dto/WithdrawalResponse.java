package com.enviro.assessment.junior.khulekani.dto;

import com.enviro.assessment.junior.khulekani.model.WithdrawalNotice;
import com.enviro.assessment.junior.khulekani.model.WithdrawalStatus;
import com.enviro.assessment.junior.khulekani.model.WithdrawalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WithdrawalResponse {
    private Long id;
    private Long investorId;
    private BigDecimal amount;
    private WithdrawalType type;
    private WithdrawalStatus status;
    private LocalDateTime requestDate;
    private String reason;

    public static WithdrawalResponse from(WithdrawalNotice notice) {
        WithdrawalResponse response = new WithdrawalResponse();
        response.id = notice.getId();
        response.investorId = notice.getPortfolio().getInvestor().getId();
        response.amount = notice.getAmount();
        response.type = notice.getType();
        response.status = notice.getStatus();
        response.requestDate = notice.getRequestDate();
        response.reason = notice.getReason();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public WithdrawalType getType() {
        return type;
    }

    public WithdrawalStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public String getReason() {
        return reason;
    }
}
