package edu.iitm.jamunaconnect.repository;

import edu.iitm.jamunaconnect.domain.BudgetSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetSettingRepository extends JpaRepository<BudgetSetting, Integer> {
}
