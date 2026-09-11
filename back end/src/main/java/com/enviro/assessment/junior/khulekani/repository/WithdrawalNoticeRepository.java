package com.enviro.assessment.junior.khulekani.repository;

import com.enviro.assessment.junior.khulekani.model.WithdrawalNotice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalNoticeRepository extends JpaRepository<WithdrawalNotice, Long> {
    List<WithdrawalNotice> findByPortfolioInvestorIdOrderByRequestDateDesc(Long investorId);
}
