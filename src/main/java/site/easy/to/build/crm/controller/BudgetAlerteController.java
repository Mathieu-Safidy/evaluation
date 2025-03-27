package site.easy.to.build.crm.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.settings.BudgetAlerte;
import site.easy.to.build.crm.service.settings.BudgetAlerteService;
import site.easy.to.build.crm.service.user.UserServiceImpl;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/employee/taux")
public class BudgetAlerteController {
    private final BudgetAlerteService budgetAlerteService;
    private final AuthenticationUtils authenticationUtils;
    private final UserServiceImpl userServiceImpl;

    public BudgetAlerteController(BudgetAlerteService budgetAlerteService, AuthenticationUtils authenticationUtils, UserServiceImpl userServiceImpl) {
        this.budgetAlerteService = budgetAlerteService;
        this.authenticationUtils = authenticationUtils;
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping("/set")
    public String set(Model model) {
        Date dateFin = new Date();
        BudgetAlerte budgetAlerte = (BudgetAlerte) budgetAlerteService.getClosestAlerte(dateFin);
        if (budgetAlerte == null) {
            budgetAlerte = new BudgetAlerte();
        }
        model.addAttribute("budgetAlert",budgetAlerte);
        return "budgetAlert/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("budgetAlert") @Validated BudgetAlerte budgetAlerte, BindingResult bindingResult, Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User manager = userServiceImpl.findById(userId);
        if(manager.isInactiveUser()) {
            return "error/account-inactive";
        }
        if (bindingResult.hasErrors()) {
            return "redirect:/employee/taux/set";
        }
        if(budgetAlerte.getLabel() == null){
            budgetAlerte.setLabel("Taux d'alerte");
        }
        budgetAlerteService.save(budgetAlerte);
        return "redirect:/employee/taux/all";
    }

    @GetMapping("/all")
    public String all(Model model) {
        List<BudgetAlerte> alertes = budgetAlerteService.getAll();
        model.addAttribute("alerts",alertes);
        return "budgetAlert/budget-list";
    }
}
