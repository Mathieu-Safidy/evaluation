package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Integer> {
    public Budget findByBudgetId(Integer id);
    public void deleteAll();
    public List<Budget> findBudgetByCustomerId(Customer customer);
}

