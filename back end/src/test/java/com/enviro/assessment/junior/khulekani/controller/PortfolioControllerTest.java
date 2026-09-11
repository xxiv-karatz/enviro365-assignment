package com.enviro.assessment.junior.khulekani.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.enviro.assessment.junior.khulekani.dto.PortfolioResponse;
import com.enviro.assessment.junior.khulekani.exception.ResourceNotFoundException;
import com.enviro.assessment.junior.khulekani.model.Investor;
import com.enviro.assessment.junior.khulekani.model.Portfolio;
import com.enviro.assessment.junior.khulekani.model.Product;
import com.enviro.assessment.junior.khulekani.service.PortfolioService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PortfolioService portfolioService;

    @Test
    void getPortfolioReturnsPortfolioForKnownInvestor() throws Exception {
        when(portfolioService.getPortfolio(1L)).thenReturn(samplePortfolioResponse());

        mockMvc.perform(get("/api/investors/1/portfolio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investorName").value("Amara Ndlovu"))
                .andExpect(jsonPath("$.products.length()").value(1));
    }

    @Test
    void getPortfolioReturnsNotFoundForUnknownInvestor() throws Exception {
        when(portfolioService.getPortfolio(99L))
                .thenThrow(new ResourceNotFoundException("No portfolio found for investor 99"));

        mockMvc.perform(get("/api/investors/99/portfolio"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No portfolio found for investor 99"));
    }

    private PortfolioResponse samplePortfolioResponse() {
        Investor investor = new Investor("Amara Ndlovu", 42, "amara@example.com");
        investor.setId(1L);
        Portfolio portfolio = new Portfolio(investor, new BigDecimal("125000.00"));
        investor.setPortfolio(portfolio);
        portfolio.addProduct(new Product("Green Energy Fund", "EQUITY", new BigDecimal("50000.00")));
        return PortfolioResponse.from(portfolio);
    }
}
