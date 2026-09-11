package com.enviro.assessment.junior.khulekani.config;

import com.enviro.assessment.junior.khulekani.model.Investor;
import com.enviro.assessment.junior.khulekani.model.Portfolio;
import com.enviro.assessment.junior.khulekani.model.Product;
import com.enviro.assessment.junior.khulekani.repository.InvestorRepository;
import com.enviro.assessment.junior.khulekani.repository.PortfolioRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedData(InvestorRepository investorRepository,
                               PortfolioRepository portfolioRepository) {
        return args -> {
            if (investorRepository.count() > 0) {
                return;
            }
            createInvestor("Amara Ndlovu", 42, "amara@example.com", "125000.00",
                    portfolioRepository, investorRepository,
                    new Product("Green Energy Fund", "EQUITY", new BigDecimal("50000.00")),
                    new Product("Sustainable Bonds", "BOND", new BigDecimal("40000.00")),
                    new Product("Water Security ETF", "ETF", new BigDecimal("35000.00")));
            createInvestor("Liam Jacobs", 65, "liam@example.com", "98000.00",
                    portfolioRepository, investorRepository,
                    new Product("Renewable Infrastructure", "EQUITY", new BigDecimal("42000.00")),
                    new Product("Impact Income Fund", "BOND", new BigDecimal("30000.00")),
                    new Product("Climate Technology ETF", "ETF", new BigDecimal("26000.00")));
            createInvestor("Nandi Mokoena", 72, "nandi@example.com", "210000.00",
                    portfolioRepository, investorRepository,
                    new Product("Solar Growth Fund", "EQUITY", new BigDecimal("90000.00")),
                    new Product("Eco Infrastructure Bonds", "BOND", new BigDecimal("70000.00")),
                    new Product("Clean Transport ETF", "ETF", new BigDecimal("50000.00")));
        };
    }

    private void createInvestor(String name, int age, String email, String balance,
                                PortfolioRepository portfolioRepository,
                                InvestorRepository investorRepository,
                                Product... products) {
        Investor investor = investorRepository.save(new Investor(name, age, email));
        Portfolio portfolio = new Portfolio(investor, new BigDecimal(balance));
        investor.setPortfolio(portfolio);
        for (Product product : products) {
            portfolio.addProduct(product);
        }
        portfolioRepository.save(portfolio);
    }
}
