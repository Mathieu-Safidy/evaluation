package site.easy.to.build.crm.service.budget;

import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;

import java.util.List;

public interface BudgetService {
    public Budget save(Budget budget);
    public Budget findByBudgetId(Integer id);
    public void deleteAll();
    public List<Budget> findByCustomerId(Customer customer);
}
