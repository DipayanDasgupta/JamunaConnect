package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.BudgetPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetPortfolioRepository extends JpaRepository<BudgetPortfolio, Long> {

    List<BudgetPortfolio> findAllByOrderByDisplayOrderAscIdAsc();

    Optional<BudgetPortfolio> findByCodeIgnoreCase(String code);
}
