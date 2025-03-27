package site.easy.to.build.crm.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depenses;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.settings.BudgetAlerte;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.customer.CustomerServiceImpl;
import site.easy.to.build.crm.service.depenses.DepensesService;
import site.easy.to.build.crm.service.settings.BudgetAlerteService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/employee/depenses")
public class DepensesController {

    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;
    private final DepensesService depensesService;
    private final CustomerService customerService;
    private final BudgetAlerteService budgetAlerteService;
    private final HttpSession httpSession;
    private final CustomerServiceImpl customerServiceImpl;

    @Autowired
    public DepensesController(AuthenticationUtils authenticationUtils, UserService userService, DepensesService depensesService, CustomerService customerService, BudgetAlerteService budgetAlerteService, HttpSession httpSession, CustomerServiceImpl customerServiceImpl) {
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
        this.depensesService = depensesService;
        this.customerService = customerService;
        this.budgetAlerteService = budgetAlerteService;
        this.httpSession = httpSession;
        this.customerServiceImpl = customerServiceImpl;
    }


    @GetMapping("/customer/{id}")
    public String getByCustomer(@PathVariable("id") int id, Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        Customer customer = customerService.findByCustomerId(id);
        List<Depenses> depenses = depensesService.findDepensesByCustomerId(customer);
        model.addAttribute("depenses", depenses);
        return "depense/depenses-list";
    }

    @GetMapping("/add/{id}")
    public String addForm(Model model,@PathVariable("id")int id) {
        Customer customer = customerService.findByCustomerId(id);
        model.addAttribute("customer",customer);
        model.addAttribute("depense",new Depenses());
        return "depense/create";
    }

    @PostMapping("/create")
    public String addDepense(@ModelAttribute("depense")@Validated Depenses depenses,
                             BindingResult bindingResult,
                             Authentication authentication, RedirectAttributes redirectAttributes,
                             Model model) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User manager = userService.findById(userId);
        if(manager.isInactiveUser()) {
            return "error/account-inactive";
        }

        if(bindingResult.hasErrors()) {
            return "redirect:/employee/depenses/customer/"+depenses.getCustomerId().getCustomerId();
        }

//        double montant = depenses.checkBudget();
        Customer customer = depenses.getCustomerId();
//        Budget budget = depenses.getBudget();
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        Date datedebut = new Date();
//        Date dateFin = new Date();
//        try {
//            datedebut = dateFormat.parse(budget.getCreatedAt());
//            dateFin = dateFormat.parse(budget.getFinishedAt());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        BudgetAlerte budgetAlerte = budgetAlerteService.getClosestAlerte(datedebut,dateFin);
        BudgetAlerte budgetAlerte = budgetAlerteService.findLastAlerte();
        double montantBudget = customer.getSommeBudget();
        double montantDepense = customer.getSommeDepenses()+depenses.getMontant();
        double montantAlerte = montantBudget*(budgetAlerte.getValeur().doubleValue()/100);
        if (montantDepense>=montantAlerte){
            redirectAttributes.addFlashAttribute("alerte","Le taux d'alerte a ete ateint veuiller consulter le budget");
        }
        if (montantDepense>montantBudget) {
            httpSession.setAttribute("depenses",depenses);
            model.addAttribute("depenses",depenses);
            return "depense/confirmation";
        }

        depensesService.save(depenses);

        return "redirect:/employee/depenses/list";
    }

    @PostMapping("/confirmer")
    public String confirmation(){
        Depenses depenses = (Depenses) httpSession.getAttribute("depenses");
        depensesService.save(depenses);
        return "redirect:/employee/depenses/list";
    }

    @GetMapping("/list")
    public String getAllCustomers(Model model){
        List<Customer> customers;
        try {
            customers = customerService.findAll();
        } catch (Exception e){
            return "error/500";
        }
        model.addAttribute("customers",customers);
        return "depense/all-customers-depense";
    }
}
