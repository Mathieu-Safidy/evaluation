package site.easy.to.build.crm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.oauth2.sdk.util.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.entity.settings.BudgetAlerte;
import site.easy.to.build.crm.service.budget.BudgetServiceImpl;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;
import site.easy.to.build.crm.service.depenses.DepensesServiceImpl;
import site.easy.to.build.crm.service.lead.LeadServiceImpl;
import site.easy.to.build.crm.service.settings.BudgetAlerteService;
import site.easy.to.build.crm.service.ticket.TicketServiceImpl;
import org.springframework.web.bind.annotation.RestController;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.entity.model.Modelmport;

import javax.swing.text.html.Option;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api")
public class LoginApiController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private LeadServiceImpl leadServiceImpl;
    @Autowired
    private TicketServiceImpl ticketServiceImpl;
    @Autowired
    private CustomerServiceImpl customerServiceImpl;
    @Autowired
    private BudgetServiceImpl budgetServiceImpl;
    @Autowired
    private DepensesServiceImpl depensesServiceImpl;

    private BudgetAlerteService budgetAlerteService;
    @Autowired
    private UserServiceImpl userServiceImpl;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public LoginApiController(BudgetAlerteService budgetAlerteService) {
        this.budgetAlerteService = budgetAlerteService;
    }

    @PostMapping("/connect")
    public ResponseEntity<?> login(@RequestParam("username") String username,@RequestParam("password") String password ) throws Exception {
        List<User> user = userServiceImpl.findByUsername(username);
        if (!user.isEmpty()) {
            User use = user.get(0);
            String sessionToken = UUID.randomUUID().toString();
            use.setToken(sessionToken);
            boolean matches = passwordEncoder.matches(password, use.getPassword());
            if (matches) {
                return ResponseEntity.ok(user);
            }
        }
        return null;
    }

    @GetMapping("/show")
    public ResponseEntity<?> show() {
        return ResponseEntity.ok().body("{\"token\":\"access\"}");
    }

    @GetMapping("/getData")
    public ResponseEntity<?> getData() throws Exception {
        try {
        Map<String,Object> data = new HashMap<>();
        data.put("taux_alerte",budgetAlerteService.findLastAlerte());
        data.put("leads",leadServiceImpl.findAll());
        data.put("tickets",ticketServiceImpl.findAll());
        data.put("customer",customerServiceImpl.findAll());
        data.put("budgets",budgetServiceImpl.findAll());
        data.put("depenses",depensesServiceImpl.findAll());
            // Création et configuration de ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String json = objectMapper.writeValueAsString(data);

//            System.out.println(json);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erreur lors de la conversion en JSON");
        }
    }

    @GetMapping("tickets/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try{
            Ticket ticket1 = ticketServiceImpl.findByTicketId(id);
            if (ticket1 == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket non trouvé");
            }
            Depenses depenses = ticket1.getDepenses();
            ticketServiceImpl.delete(ticket1);
            depensesServiceImpl.delete(depenses);
            return ResponseEntity.ok().body("Ticket suprimer avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la supression du ticket");
        }
    }
    @GetMapping("lead/delete/{id}")
    public ResponseEntity<?> deleteLead(@PathVariable Integer id) {
        try{
            Lead lead = leadServiceImpl.findByLeadId(id);
            if (lead == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lead non trouvé");
            }
            Depenses depenses = lead.getDepenses();
            leadServiceImpl.delete(lead);
            if(depenses != null){
                depensesServiceImpl.delete(depenses);
            }
            return ResponseEntity.ok().body("Lead suprimer avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la supression du lead");
        }
    }

    @PostMapping("tickets/edit/{id}")
    public ResponseEntity<?> editTicket(@PathVariable Integer id , @RequestBody JsonNode ticket) {
        try {
            Ticket ticket1 = ticketServiceImpl.findByTicketId(id);
            if (ticket1 == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket non trouvé");
            }

            if (!ticket.has("montant")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Champ 'montant' manquant");
            }

            double montant = ticket.get("montant").asDouble();
            Depenses depenses = ticket1.getDepenses();

            if (depenses == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le ticket n'a pas de dépense associée");
            }
            System.out.println("Montant : "+montant+" ; id : "+id);
            depenses.setMontant(montant);
            depensesServiceImpl.update(depenses);

            return ResponseEntity.ok().body("Ticket mis à jour avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du ticket");
        }
    }
    @PostMapping("lead/edit/{id}")
    public ResponseEntity<?> editLead(@PathVariable Integer id , @RequestBody JsonNode ticket) {
        try {
            Lead lead = leadServiceImpl.findByLeadId(id);
            if (lead == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Lead non trouvé");
            }

            if (!ticket.has("montant")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Champ 'montant' manquant");
            }

            double montant = ticket.get("montant").asDouble();
            Depenses depenses = lead.getDepenses();

            if (depenses == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le ticket n'a pas de dépense associée");
            }
            System.out.println("Montant : " + montant + " ; id : " + id);
            depenses.setMontant(montant);
            depensesServiceImpl.update(depenses);

            return ResponseEntity.ok().body("Ticket mis à jour avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du ticket");
        }
    }
    @PostMapping("taux/edit/{id}")
    public ResponseEntity<?> editTaux(@PathVariable Integer id , @RequestBody JsonNode ticket) {
        try {
            Optional<BudgetAlerte> budgetAlerte = budgetAlerteService.findById(id);
            if (budgetAlerte.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Taux non trouvé");
            }

            if (!ticket.has("valeur")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Champ 'valeur' manquant");
            }

            double valeur = ticket.get("valeur").asDouble();
            BudgetAlerte budgets = budgetAlerte.get();
            budgets.setValeur(BigDecimal.valueOf(valeur));

            budgetAlerteService.save(budgets);

            return ResponseEntity.ok().body("Taux mis à jour avec succès");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du taux");
        }
    }

    @PostMapping("/duplicate")
    public ResponseEntity<?> importData(@RequestBody Modelmport modelmport){
        Customer customer = modelmport.getCustomer();
        List<Lead> leadList = modelmport.getLead();
        List<Ticket> ticketList = modelmport.getTicketList();
        int idcustomer = customer.getCustomerId();
        customer.setCustomerId(null);
        List<Budget> budgets = customer.getBudgets();
        customer.setBudgets(null);
        customer = customerServiceImpl.save(customer);
        System.out.println(leadList.size()+" - "+ticketList.size()+" ancien id "+idcustomer+" - "+customer.getCustomerId());
        for (Budget budget : budgets) {
            budget.setCustomerId(customer);
            budget.setBudgetId(null);
            budgetServiceImpl.save(budget);
        }
        for (Lead lead : leadList) {
            lead.setCustomer(customer);
            lead.setLeadId(null);
            leadServiceImpl.save(lead);
        }

        for (Ticket ticket : ticketList) {
            ticket.setCustomer(customer);
            ticket.setTicketId(null);
            ticketServiceImpl.save(ticket);
        }

        return ResponseEntity.ok(customer);
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
//        try {
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(username, password)
//            );
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//
//            String sessionToken = UUID.randomUUID().toString();
//
//            return ResponseEntity.ok().body("{\"token\":\"" + sessionToken + "\"}");
//
//        } catch (AuthenticationException e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
//        }
//    }

}
