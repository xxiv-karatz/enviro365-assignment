package com.enviro.assessment.junior.khulekani.repository;

import com.enviro.assessment.junior.khulekani.model.Investor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestorRepository extends JpaRepository<Investor, Long> {
}
