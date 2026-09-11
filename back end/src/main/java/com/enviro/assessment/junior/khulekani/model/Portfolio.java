package com.enviro.assessment.junior.khulekani.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "investor_id", nullable = false, unique = true)
    private Investor investor;

    private BigDecimal balance;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WithdrawalNotice> withdrawalNotices = new ArrayList<>();

    public Portfolio() {
    }

    public Portfolio(Investor investor, BigDecimal balance) {
        this.investor = investor;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Investor getInvestor() {
        return investor;
    }

    public void setInvestor(Investor investor) {
        this.investor = investor;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<WithdrawalNotice> getWithdrawalNotices() {
        return withdrawalNotices;
    }

    public void setWithdrawalNotices(List<WithdrawalNotice> withdrawalNotices) {
        this.withdrawalNotices = withdrawalNotices;
    }

    public void addProduct(Product product) {
        products.add(product);
        product.setPortfolio(this);
    }

    public void addWithdrawalNotice(WithdrawalNotice notice) {
        withdrawalNotices.add(notice);
        notice.setPortfolio(this);
    }
}
