package com.enviro.assessment.junior.khulekani.exception;

import com.enviro.assessment.junior.khulekani.dto.WithdrawalResponse;

public class BusinessRuleViolationException extends RuntimeException {
    private final WithdrawalResponse rejection;

    public BusinessRuleViolationException(String message, WithdrawalResponse rejection) {
        super(message);
        this.rejection = rejection;
    }

    public WithdrawalResponse getRejection() {
        return rejection;
    }
}
