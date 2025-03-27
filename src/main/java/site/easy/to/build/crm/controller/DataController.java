package site.easy.to.build.crm.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;
import site.easy.to.build.crm.exception.DataException;
import site.easy.to.build.crm.repository.BudgetRepository;
import site.easy.to.build.crm.repository.DepensesRepository;
import site.easy.to.build.crm.repository.FileRepository;
import site.easy.to.build.crm.repository.GoogleDriveFileRepository;
import site.easy.to.build.crm.service.budget.BudgetServiceImpl;
import site.easy.to.build.crm.service.data.DataServiceImpl;
import site.easy.to.build.crm.service.depenses.DepensesService;
import site.easy.to.build.crm.service.depenses.DepensesServiceImpl;


@Controller
@RequestMapping("/employee/data")
public class DataController {

    @Autowired
    FileRepository fileRepository;

    @Autowired
    GoogleDriveFileRepository googleDriveFileRepository;

    @Autowired
    DepensesServiceImpl depensesService;

    @Autowired
    private BudgetServiceImpl budgetServiceImpl;
    @Autowired
    private DataServiceImpl dataServiceImpl;


    @PostMapping("/reset")
    public String reset(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            dataServiceImpl.restore(file);
            redirectAttributes.addFlashAttribute("success","Restoration de la base avec success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error","Erreur lors de la restoration de la base");
        }
        return "redirect:/employee/data/form";
    }

    @GetMapping("/form")
    public String gotForm() {
        return "data/confirmation";
    }

    @PostMapping("/upload")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication,
                              HttpSession session) {
        if (files.length < 3) {
            redirectAttributes.addFlashAttribute("error", "Veuillez télécharger 3 fichiers !");
            return "redirect:/employee/data/form ";
        }


        MultipartFile importLead = files[0];
        MultipartFile importCustomer = files[1];
        MultipartFile importBudget = files[2];
        try {
            dataServiceImpl.multipleImport(importCustomer,importLead,importBudget,authentication);
            redirectAttributes.addFlashAttribute("success", "Importation réussie !");

        } catch (DataException ex) {
            List<String> errors = ex.getErrorMessages();
            redirectAttributes.addFlashAttribute("errorList1", errors);
//            redirectAttributes.addFlashAttribute("error", "Erreurs d'importation : " + String.join(", ", errors));

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Une erreur inattendue est survenue !");
        }
        return "redirect:/employee/data/form";
    }

}
