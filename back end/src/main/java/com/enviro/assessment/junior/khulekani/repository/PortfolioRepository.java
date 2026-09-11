package com.enviro.assessment.junior.khulekani.repository;

import com.enviro.assessment.junior.khulekani.model.Portfolio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByInvestorId(Long investorId);
}
