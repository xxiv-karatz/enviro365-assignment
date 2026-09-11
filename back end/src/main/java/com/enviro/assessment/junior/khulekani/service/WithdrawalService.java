package com.enviro.assessment.junior.khulekani.service;

import com.enviro.assessment.junior.khulekani.dto.WithdrawalRequest;
import com.enviro.assessment.junior.khulekani.dto.WithdrawalResponse;
import com.enviro.assessment.junior.khulekani.exception.BusinessRuleViolationException;
import com.enviro.assessment.junior.khulekani.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.khulekani.model.Portfolio;
import com.enviro.assessment.junior.khulekani.model.WithdrawalNotice;
import com.enviro.assessment.junior.khulekani.model.WithdrawalStatus;
import com.enviro.assessment.junior.khulekani.repository.InvestorRepository;
import com.enviro.assessment.junior.khulekani.repository.PortfolioRepository;
import com.enviro.assessment.junior.khulekani.repository.WithdrawalNoticeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WithdrawalService {
    private static final BigDecimal MAX_WITHDRAWAL_RATE = new BigDecimal("0.90");

    private final InvestorRepository investorRepository;
    private final PortfolioRepository portfolioRepository;
    private final WithdrawalNoticeRepository withdrawalNoticeRepository;

    public WithdrawalService(InvestorRepository investorRepository,
                             PortfolioRepository portfolioRepository,
                             WithdrawalNoticeRepository withdrawalNoticeRepository) {
        this.investorRepository = investorRepository;
        this.portfolioRepository = portfolioRepository;
        this.withdrawalNoticeRepository = withdrawalNoticeRepository;
    }

    @Transactional(noRollbackFor = BusinessRuleViolationException.class)
    public WithdrawalResponse submitWithdrawal(WithdrawalRequest request) {
        Portfolio portfolio = portfolioRepository.findByInvestorId(request.getInvestorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No portfolio found for investor " + request.getInvestorId()));

        String rejectionReason = findRejectionReason(portfolio, request);
        if (rejectionReason != null) {
            WithdrawalNotice rejection = createNotice(portfolio, request, WithdrawalStatus.REJECTED,
                    rejectionReason);
            withdrawalNoticeRepository.save(rejection);
            throw new BusinessRuleViolationException(rejectionReason,
                    WithdrawalResponse.from(rejection));
        }

        portfolio.setBalance(portfolio.getBalance().subtract(request.getAmount()));
        portfolioRepository.save(portfolio);
        WithdrawalNotice approval = createNotice(portfolio, request, WithdrawalStatus.APPROVED,
                "Withdrawal approved");
        withdrawalNoticeRepository.save(approval);
        return WithdrawalResponse.from(approval);
    }

    public List<WithdrawalResponse> getHistory(Long investorId) {
        ensureInvestorExists(investorId);
        return withdrawalNoticeRepository.findByPortfolioInvestorIdOrderByRequestDateDesc(investorId)
                .stream().map(WithdrawalResponse::from).toList();
    }

    // Filtering is applied in-memory rather than via a derived repository query because
    // type/from/to are all optional and can be combined in any way. Expressing every
    // combination as its own derived query method (or reaching for JPA Specifications)
    // is more machinery than this dataset size warrants for a junior-level assessment.
    public List<WithdrawalResponse> findForExport(Long investorId, com.enviro.assessment.junior.khulekani.model.WithdrawalType type,
                                                  LocalDate from, LocalDate to) {
        ensureInvestorExists(investorId);
        LocalDateTime fromDateTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateTime = to == null ? null : to.atTime(LocalTime.MAX);
        return withdrawalNoticeRepository.findByPortfolioInvestorIdOrderByRequestDateDesc(investorId)
                .stream()
                .filter(notice -> type == null || notice.getType() == type)
                .filter(notice -> fromDateTime == null || !notice.getRequestDate().isBefore(fromDateTime))
                .filter(notice -> toDateTime == null || !notice.getRequestDate().isAfter(toDateTime))
                .map(WithdrawalResponse::from)
                .toList();
    }

    private String findRejectionReason(Portfolio portfolio, WithdrawalRequest request) {
        if (request.getType() == com.enviro.assessment.junior.khulekani.model.WithdrawalType.RETIREMENT
                && portfolio.getInvestor().getAge() <= 65) {
            return "Retirement withdrawals are only allowed for investors over 65";
        }
        if (request.getAmount().compareTo(portfolio.getBalance()) > 0) {
            return "Withdrawal amount must not exceed the portfolio balance";
        }
        BigDecimal maximumAllowed = portfolio.getBalance().multiply(MAX_WITHDRAWAL_RATE);
        if (request.getAmount().compareTo(maximumAllowed) > 0) {
            return "Withdrawal amount must not exceed 90% of the portfolio balance";
        }
        return null;
    }

    private WithdrawalNotice createNotice(Portfolio portfolio, WithdrawalRequest request,
                                          WithdrawalStatus status, String reason) {
        WithdrawalNotice notice = new WithdrawalNotice();
        notice.setPortfolio(portfolio);
        notice.setAmount(request.getAmount());
        notice.setType(request.getType());
        notice.setStatus(status);
        notice.setRequestDate(LocalDateTime.now());
        notice.setReason(reason);
        return notice;
    }

    private void ensureInvestorExists(Long investorId) {
        if (!investorRepository.existsById(investorId)) {
            throw new ResourceNotFoundException("Investor " + investorId + " was not found");
        }
    }
}
