package com.enviro.assessment.junior.khulekani.service;

import com.enviro.assessment.junior.khulekani.dto.PortfolioResponse;
import com.enviro.assessment.junior.khulekani.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.khulekani.repository.PortfolioRepository;
import org.springframework.stereotype.Service;

@Service
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public PortfolioResponse getPortfolio(Long investorId) {
        return portfolioRepository.findByInvestorId(investorId)
                .map(PortfolioResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No portfolio found for investor " + investorId));
    }
}
