package site.easy.to.build.crm.service.depenses;

import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depenses;

import java.util.List;

public interface DepensesService {
    public Depenses save(Depenses depenses);
    public Depenses findById(Integer id);
    public List<Depenses> findAll();
    public List<Depenses> findDepensesByCustomerId(Customer customer);
    public void deleteAll();
}
