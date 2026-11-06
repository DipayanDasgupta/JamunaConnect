package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetItemRepository extends JpaRepository<BudgetItem, Long> {

    List<BudgetItem> findByPortfolioIdOrderByDisplayOrderAscIdAsc(Long portfolioId);

    void deleteByPortfolioId(Long portfolioId);
}
