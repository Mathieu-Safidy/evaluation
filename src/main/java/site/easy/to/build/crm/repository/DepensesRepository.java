package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depenses;

import java.util.List;

@Repository
public interface DepensesRepository extends JpaRepository<Depenses,Integer> {
    public List<Depenses> findAll();
    public Depenses findByDepensesId(Integer depensesId);
    public List<Depenses> findDepensesByCustomerId(Customer customerId);
    List<Depenses> findByDepensesIdNotIn(List<Integer> depenseIds);
}
