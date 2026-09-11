package com.enviro.assessment.junior.khulekani.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enviro.assessment.junior.khulekani.dto.WithdrawalRequest;
import com.enviro.assessment.junior.khulekani.dto.WithdrawalResponse;
import com.enviro.assessment.junior.khulekani.exception.BusinessRuleViolationException;
import com.enviro.assessment.junior.khulekani.model.Investor;
import com.enviro.assessment.junior.khulekani.model.Portfolio;
import com.enviro.assessment.junior.khulekani.model.WithdrawalNotice;
import com.enviro.assessment.junior.khulekani.model.WithdrawalStatus;
import com.enviro.assessment.junior.khulekani.model.WithdrawalType;
import com.enviro.assessment.junior.khulekani.repository.InvestorRepository;
import com.enviro.assessment.junior.khulekani.repository.PortfolioRepository;
import com.enviro.assessment.junior.khulekani.repository.WithdrawalNoticeRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {
    @Mock
    private InvestorRepository investorRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private WithdrawalNoticeRepository withdrawalNoticeRepository;

    @InjectMocks
    private WithdrawalService withdrawalService;

    private Portfolio portfolio;

    @BeforeEach
    void setUp() {
        Investor investor = new Investor("Test Investor", 70, "test@example.com");
        portfolio = new Portfolio(investor, new BigDecimal("1000.00"));
        investor.setPortfolio(portfolio);
        when(portfolioRepository.findByInvestorId(1L)).thenReturn(Optional.of(portfolio));
        when(withdrawalNoticeRepository.save(any(WithdrawalNotice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void approvesWithdrawalAndDeductsBalance() {
        WithdrawalResponse response = withdrawalService.submitWithdrawal(request("250.00", WithdrawalType.STANDARD));

        assertEquals(new BigDecimal("750.00"), portfolio.getBalance());
        assertEquals(WithdrawalStatus.APPROVED, response.getStatus());
        verify(portfolioRepository).save(portfolio);
        verify(withdrawalNoticeRepository).save(any(WithdrawalNotice.class));
    }

    @Test
    void rejectsAmountAboveBalance() {
        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> withdrawalService.submitWithdrawal(request("1000.01", WithdrawalType.STANDARD)));

        assertEquals("Withdrawal amount must not exceed the portfolio balance", exception.getMessage());
        assertEquals(new BigDecimal("1000.00"), portfolio.getBalance());
        assertRejectedNotice();
    }

    @Test
    void rejectsAmountAboveNinetyPercentOfBalance() {
        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> withdrawalService.submitWithdrawal(request("900.01", WithdrawalType.STANDARD)));

        assertEquals("Withdrawal amount must not exceed 90% of the portfolio balance", exception.getMessage());
        assertRejectedNotice();
    }

    @Test
    void rejectsRetirementWithdrawalForInvestorAge65OrUnder() {
        portfolio.getInvestor().setAge(65);

        BusinessRuleViolationException exception = assertThrows(BusinessRuleViolationException.class,
                () -> withdrawalService.submitWithdrawal(request("100.00", WithdrawalType.RETIREMENT)));

        assertEquals("Retirement withdrawals are only allowed for investors over 65", exception.getMessage());
        assertRejectedNotice();
    }

    private WithdrawalRequest request(String amount, WithdrawalType type) {
        WithdrawalRequest request = new WithdrawalRequest();
        request.setInvestorId(1L);
        request.setAmount(new BigDecimal(amount));
        request.setType(type);
        return request;
    }

    private void assertRejectedNotice() {
        ArgumentCaptor<WithdrawalNotice> captor = ArgumentCaptor.forClass(WithdrawalNotice.class);
        verify(withdrawalNoticeRepository).save(captor.capture());
        assertEquals(WithdrawalStatus.REJECTED, captor.getValue().getStatus());
    }
}
