package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.Budget;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.Depenses;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.settings.BudgetAlerte;
import site.easy.to.build.crm.service.budget.BudgetService;
import site.easy.to.build.crm.service.budget.BudgetServiceImpl;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.depenses.DepensesService;
import site.easy.to.build.crm.service.settings.BudgetAlerteService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/employee/budget")
public class BudgetController {

    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;
    private final CustomerService customerService;
    private final BudgetServiceImpl budgetServiceImpl;
    private final BudgetAlerteService budgetAlerteService;

    @Autowired
    public BudgetController(AuthenticationUtils authenticationUtils, UserService userService, CustomerService customerService, BudgetServiceImpl budgetServiceImpl, BudgetAlerteService budgetAlerteService) {
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
        this.customerService = customerService;
        this.budgetServiceImpl = budgetServiceImpl;
        this.budgetAlerteService = budgetAlerteService;
    }


    @GetMapping("/customer/{id}")
    public String getByCustomer(@PathVariable("id") int id, Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        Customer customer = customerService.findByCustomerId(id);
        List<Budget> budget = customer.getBudgets();
        model.addAttribute("budgets", budget);
        return "budget/budget-list";
    }

    @GetMapping("/add/{id}")
    public String addForm(Model model,@PathVariable("id")int id) {
        Customer customer = customerService.findByCustomerId(id);
        model.addAttribute("customer",customer);
//        List<Budget>
        model.addAttribute("budget",new Budget());
        return "budget/create";
    }

    @PostMapping("/create")
    public String addBudget(@ModelAttribute("budget")@Validated Budget budget,
                            BindingResult bindingResult,
                            Authentication authentication, RedirectAttributes redirectAttributes) {
        System.out.println("Atooo");
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User manager = userService.findById(userId);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date datedebut = new Date();
        Date dateFin = new Date();
        try {
            datedebut = dateFormat.parse(budget.getCreatedAt());
            dateFin = dateFormat.parse(budget.getFinishedAt());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        BudgetAlerte budgetAlerte = budgetAlerteService.getClosestAlerte(datedebut,dateFin);

        if(manager.isInactiveUser()) {
            return "error/account-inactive";
        }

        if(bindingResult.hasErrors()) {
            return "redirect:/employee/budget/add/"+budget.getCustomerId().getCustomerId();
        }

        if (budgetAlerte == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Le taux d'alerte est inexistant");
            return "redirect:/employee/budget/add/"+budget.getCustomerId().getCustomerId();
        }

        budget.setIdAlerte(budgetAlerte);

        budgetServiceImpl.save(budget);

        return "redirect:/employee/budget/list";
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
        return "budget/all-customers-budget";
    }

    public List<String> importCSV(MultipartFile file) {
        List<String> erreurs = new ArrayList<>();
        List<Budget> budgetValides = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String ligne;
            boolean isFirstLine = true;
            int ligneNum = 1; // Pour afficher où est l'erreur

            while ((ligne = reader.readLine()) != null) {
                // Ignorer la première ligne (en-tête)
                if (isFirstLine) {
                    isFirstLine = false;
                    ligneNum++;
                    continue;
                }

                // Diviser les valeurs en colonnes
                String[] valeurs = ligne.split(",");

                if (valeurs.length < 5) {
                    erreurs.add("Ligne " + ligneNum + " : Nombre de colonnes incorrect");
                    ligneNum++;
                    continue;
                }

                String libele = valeurs[0].trim();
                String montantStr = valeurs[1].trim();
                String dateCreate = valeurs[2].trim();
                String customerId = valeurs[3].trim();
                String finishedAt = valeurs[4].trim();


                // Vérification des champs vides
                if (libele.isEmpty() || montantStr.isEmpty() || dateCreate.isEmpty() || customerId.isEmpty() || finishedAt.isEmpty()) {
                    erreurs.add("Ligne " + ligneNum + " : Un champ est vide");
                    ligneNum++;
                    continue;
                }

                try {
                    int customer = Integer.parseInt(customerId);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    Date datedebut = new Date();
                    Date dateFin = new Date();
                    try {
                        datedebut = dateFormat.parse(dateCreate);
                        dateFin = dateFormat.parse(finishedAt);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    BudgetAlerte budgetAlerte = budgetAlerteService.getClosestAlerte(datedebut,dateFin);

                    double montant = Double.parseDouble(montantStr);
                    Customer customer1 = customerService.findByCustomerId(customer);
                    Budget budget = new Budget(montant,dateCreate,finishedAt,libele,customer1,budgetAlerte);

                    budgetValides.add(budget);
                } catch (NumberFormatException e) {
                    erreurs.add("Ligne " + ligneNum + " : Montant invalide ('" + montantStr + "')");
                }

                ligneNum++;
            }

            // Si aucune erreur, on sauvegarde en base
            if (erreurs.isEmpty() && !budgetValides.isEmpty()) {
                budgetServiceImpl.saveAllBatch(budgetValides);
            }

        } catch (Exception e) {
            erreurs.add("Erreur générale lors de l'importation : " + e.getMessage());
        }

        return erreurs;
    }
}
