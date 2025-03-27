package site.easy.to.build.crm.service.settings;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.settings.BudgetAlerte;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.settings.BudgetAlerteRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetAlerteService {
    @Autowired
    private BudgetAlerteRepository budgetAlerteRepository;

    public BudgetAlerte getClosestAlerte(Date startDate, Date endDate) {
        // 1. Cherche les alertes entre startDate et endDate
        List<BudgetAlerte> alertes = budgetAlerteRepository.findAlerteBetweenDates(startDate, endDate);

        if (!alertes.isEmpty()) {
            // Si on trouve des alertes entre les dates, on retourne la plus récente
            return alertes.get(0); // Retourne la plus récente alerte
        }

        // 2. Si aucune alerte n'est trouvée entre startDate et endDate, cherche la plus proche de startDate
        alertes = budgetAlerteRepository.findClosestToStartDate(startDate);
        return alertes.isEmpty() ? null : alertes.get(0); // Retourne la plus proche si elle existe
    }

    public BudgetAlerte getClosestAlerte(Date dateNow) {
        List<BudgetAlerte> alertes = budgetAlerteRepository.findClosestToStartDate(dateNow);
        return alertes.isEmpty() ? null : alertes.get(0);
    }

    public void save(BudgetAlerte alerte) {
        budgetAlerteRepository.save(alerte);
    }

    public List<BudgetAlerte> getAll() {
        return budgetAlerteRepository.findAll();
    }

    public BudgetAlerte findLastAlerte() {
        BudgetAlerte alerte = budgetAlerteRepository.findFirstByOrderByIdDesc();
        if (alerte.getId() != null) {
            Hibernate.initialize(alerte);  // Force l'initialisation de l'objet
        }
        return alerte;
    }

    public Optional<BudgetAlerte> findById(int id) {return budgetAlerteRepository.findById(id);}

}
