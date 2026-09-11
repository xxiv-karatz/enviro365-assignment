package com.enviro.assessment.junior.khulekani.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.enviro.assessment.junior.khulekani.dto.PortfolioResponse;
import com.enviro.assessment.junior.khulekani.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.khulekani.model.Investor;
import com.enviro.assessment.junior.khulekani.model.Portfolio;
import com.enviro.assessment.junior.khulekani.model.Product;
import com.enviro.assessment.junior.khulekani.repository.PortfolioRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void returnsPortfolioWithProductsForExistingInvestor() {
        Investor investor = new Investor("Amara Ndlovu", 42, "amara@example.com");
        investor.setId(1L);
        Portfolio portfolio = new Portfolio(investor, new BigDecimal("125000.00"));
        investor.setPortfolio(portfolio);
        portfolio.addProduct(new Product("Green Energy Fund", "EQUITY", new BigDecimal("50000.00")));
        when(portfolioRepository.findByInvestorId(1L)).thenReturn(Optional.of(portfolio));

        PortfolioResponse response = portfolioService.getPortfolio(1L);

        assertEquals("Amara Ndlovu", response.getInvestorName());
        assertEquals(42, response.getAge());
        assertEquals(new BigDecimal("125000.00"), response.getBalance());
        assertEquals(1, response.getProducts().size());
        assertEquals("Green Energy Fund", response.getProducts().get(0).getName());
    }

    @Test
    void throwsResourceNotFoundWhenInvestorHasNoPortfolio() {
        when(portfolioRepository.findByInvestorId(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> portfolioService.getPortfolio(99L));

        assertEquals("No portfolio found for investor 99", exception.getMessage());
    }
}
