package com.enviro.assessment.junior.khulekani.controller;

import com.enviro.assessment.junior.khulekani.dto.PortfolioResponse;
import com.enviro.assessment.junior.khulekani.service.PortfolioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investors")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/{investorId}/portfolio")
    public PortfolioResponse getPortfolio(@PathVariable Long investorId) {
        return portfolioService.getPortfolio(investorId);
    }
}
