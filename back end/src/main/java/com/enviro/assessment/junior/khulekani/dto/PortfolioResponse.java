package com.enviro.assessment.junior.khulekani.dto;

import com.enviro.assessment.junior.khulekani.model.Portfolio;
import com.enviro.assessment.junior.khulekani.model.Product;
import java.math.BigDecimal;
import java.util.List;

public class PortfolioResponse {
    private Long portfolioId;
    private Long investorId;
    private String investorName;
    private int investorAge;
    private BigDecimal balance;
    private List<ProductResponse> products;

    public PortfolioResponse() {
    }

    public static PortfolioResponse from(Portfolio portfolio) {
        PortfolioResponse response = new PortfolioResponse();
        response.portfolioId = portfolio.getId();
        response.investorId = portfolio.getInvestor().getId();
        response.investorName = portfolio.getInvestor().getName();
        response.investorAge = portfolio.getInvestor().getAge();
        response.balance = portfolio.getBalance();
        response.products = portfolio.getProducts().stream().map(ProductResponse::from).toList();
        return response;
    }

    public Long getPortfolioId() {
        return portfolioId;
    }

    public Long getInvestorId() {
        return investorId;
    }

    public String getInvestorName() {
        return investorName;
    }

    public int getAge() {
        return investorAge;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<ProductResponse> getProducts() {
        return products;
    }

    public static class ProductResponse {
        private Long id;
        private String name;
        private String type;
        private BigDecimal value;

        public static ProductResponse from(Product product) {
            ProductResponse response = new ProductResponse();
            response.id = product.getId();
            response.name = product.getName();
            response.type = product.getType();
            response.value = product.getValue();
            return response;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public BigDecimal getValue() {
            return value;
        }
    }
}
