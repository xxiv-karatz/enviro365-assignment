package com.enviro.assessment.junior.khulekani.model;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ProductTableNameTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void productEntityPersistsWithAValidTableName() {
        Investor investor = new Investor("Test Investor", 40, "test@example.com");
        Portfolio portfolio = new Portfolio(investor, new BigDecimal("1000.00"));
        investor.setPortfolio(portfolio);

        Product product = new Product("Growth Fund", "EQUITY", new BigDecimal("250.00"));
        portfolio.addProduct(product);

        entityManager.persist(investor);
        entityManager.persist(portfolio);
        entityManager.flush();
        entityManager.clear();
    }
}
