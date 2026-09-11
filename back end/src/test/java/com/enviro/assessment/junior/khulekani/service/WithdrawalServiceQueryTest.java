package com.enviro.assessment.junior.khulekani.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.enviro.assessment.junior.khulekani.dto.WithdrawalResponse;
import com.enviro.assessment.junior.khulekani.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.khulekani.model.Investor;
import com.enviro.assessment.junior.khulekani.model.Portfolio;
import com.enviro.assessment.junior.khulekani.model.WithdrawalNotice;
import com.enviro.assessment.junior.khulekani.model.WithdrawalStatus;
import com.enviro.assessment.junior.khulekani.model.WithdrawalType;
import com.enviro.assessment.junior.khulekani.repository.InvestorRepository;
import com.enviro.assessment.junior.khulekani.repository.PortfolioRepository;
import com.enviro.assessment.junior.khulekani.repository.WithdrawalNoticeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceQueryTest {
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
        investor.setId(1L);
        portfolio = new Portfolio(investor, new BigDecimal("1000.00"));
        portfolio.setId(2L);
    }

    @Test
    void returnsHistoryAfterConfirmingInvestorExists() {
        WithdrawalNotice notice = notice(WithdrawalType.STANDARD, LocalDateTime.of(2026, 1, 10, 12, 0));
        when(investorRepository.existsById(1L)).thenReturn(true);
        when(withdrawalNoticeRepository.findByPortfolioInvestorIdOrderByRequestDateDesc(1L))
                .thenReturn(List.of(notice));

        List<WithdrawalResponse> history = withdrawalService.getHistory(1L);

        assertEquals(1, history.size());
        assertEquals(WithdrawalType.STANDARD, history.get(0).getType());
        verify(investorRepository).existsById(1L);
    }

    @Test
    void rejectsHistoryForUnknownInvestor() {
        when(investorRepository.existsById(99L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> withdrawalService.getHistory(99L));

        assertEquals("Investor 99 was not found", exception.getMessage());
    }

    @Test
    void filtersExportByTypeAndInclusiveDateRange() {
        WithdrawalNotice included = notice(WithdrawalType.STANDARD,
                LocalDateTime.of(2026, 2, 1, 12, 0));
        WithdrawalNotice wrongType = notice(WithdrawalType.RETIREMENT,
                LocalDateTime.of(2026, 2, 1, 12, 0));
        WithdrawalNotice outsideRange = notice(WithdrawalType.STANDARD,
                LocalDateTime.of(2026, 3, 1, 12, 0));
        when(investorRepository.existsById(1L)).thenReturn(true);
        when(withdrawalNoticeRepository.findByPortfolioInvestorIdOrderByRequestDateDesc(1L))
                .thenReturn(List.of(included, wrongType, outsideRange));

        List<WithdrawalResponse> result = withdrawalService.findForExport(
                1L, WithdrawalType.STANDARD, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        assertEquals(1, result.size());
        assertEquals(WithdrawalType.STANDARD, result.get(0).getType());
        assertEquals(LocalDateTime.of(2026, 2, 1, 12, 0), result.get(0).getRequestDate());
    }

    private WithdrawalNotice notice(WithdrawalType type, LocalDateTime requestDate) {
        return new WithdrawalNotice(portfolio, new BigDecimal("100.00"), type,
                WithdrawalStatus.APPROVED, requestDate, "Withdrawal approved");
    }
}