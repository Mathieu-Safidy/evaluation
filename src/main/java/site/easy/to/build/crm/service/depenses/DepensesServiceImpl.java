package site.easy.to.build.crm.service.depenses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depenses;
import site.easy.to.build.crm.repository.DepensesRepository;
import site.easy.to.build.crm.repository.LeadRepository;
import site.easy.to.build.crm.repository.TicketRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DepensesServiceImpl implements DepensesService {

    private final DepensesRepository depensesRepository;
    private final LeadRepository leadRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public DepensesServiceImpl(DepensesRepository depensesRepository, LeadRepository leadRepository, TicketRepository ticketRepository) {
        this.depensesRepository = depensesRepository;
        this.leadRepository = leadRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Depenses save(Depenses depenses) {
        return depensesRepository.save(depenses);
    }

    @Override
    public Depenses findById(Integer id) {
        return depensesRepository.findByDepensesId(id);
    }

    @Override
    public List<Depenses> findAll() {
        return depensesRepository.findAll();
    }

    @Override
    public List<Depenses> findDepensesByCustomerId(Customer customer) {
        return depensesRepository.findDepensesByCustomerId(customer);
    }

    public void delete(Depenses depenses) {
        depensesRepository.delete(depenses);
    }

    @Override
    public void deleteAll() {
        depensesRepository.deleteAll();
    }

    public Depenses update(Depenses depenses) {
        return depensesRepository.save(depenses);
    }

    public List<Depenses> getDepensesNotInLeadOrTicket() {
        // Récupérer les IDs des dépenses associées à des leads
        List<Integer> depenseIdsInLead = leadRepository.findAll().stream()
                .filter(lead -> lead.getDepenses() != null) // Éviter NullPointerException
                .map(lead -> lead.getDepenses().getDepensesId())
                .collect(Collectors.toList());

        // Récupérer les IDs des dépenses associées à des tickets
        List<Integer> depenseIdsInTicket = ticketRepository.findAll().stream()
                .filter(ticket -> ticket.getDepenses() != null) // Éviter NullPointerException
                .map(ticket -> ticket.getDepenses().getDepensesId())
                .collect(Collectors.toList());

        // Fusionner les deux listes d'IDs
        Set<Integer> usedDepenseIds = new HashSet<>();
        usedDepenseIds.addAll(depenseIdsInLead);
        usedDepenseIds.addAll(depenseIdsInTicket);

        // Log pour vérifier les valeurs récupérées
        System.out.println("IDs de dépenses dans Lead: " + depenseIdsInLead);
        System.out.println("IDs de dépenses dans Ticket: " + depenseIdsInTicket);
        System.out.println("IDs combinés à exclure: " + usedDepenseIds);

        // Si aucune dépense n'est utilisée, retourner toutes les dépenses
        if (usedDepenseIds.isEmpty()) {
            System.out.println("Aucune dépense associée à Lead ou Ticket, on retourne toutes les dépenses.");
            return depensesRepository.findAll();
        }

        // Récupérer les dépenses qui ne sont associées ni à Lead ni à Ticket
        return depensesRepository.findByDepensesIdNotIn(new ArrayList<>(usedDepenseIds));
    }
}
