package site.easy.to.build.crm.service.budget;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.repository.BudgetRepository;

import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService{

    @Autowired
    BudgetRepository budgetRepository;

    @Override
    public Budget save(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public Budget findByBudgetId(Integer id) {
        return budgetRepository.findByBudgetId(id);
    }

    @Override
    public void deleteAll() {
        budgetRepository.deleteAll();
    }

    public List<Budget> findAll() {return budgetRepository.findAll();}

    @Override
    public List<Budget> findByCustomerId(Customer customer) {
        return budgetRepository.findBudgetByCustomerId(customer);
    }

    private static final int BATCH_SIZE = 1000; // Nombre d’insertions par lot

    @Transactional
    public int saveAllBatch(List<Budget> depenses) {
        int totalInserted = 0;

        for (int i = 0; i < depenses.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, depenses.size());
            List<Budget> batch = depenses.subList(i, end);

            budgetRepository.saveAll(batch);
            budgetRepository.flush();

            totalInserted += batch.size();
        }

        return totalInserted;
    }

}
