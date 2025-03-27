package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import site.easy.to.build.crm.customValidations.contract.StartDateBeforeEndDate;
import site.easy.to.build.crm.entity.settings.BudgetAlerte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "budget")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private Integer budgetId;

    @Column(name = "montant",precision = 15,scale = 2)
    @NotNull(message = "Amount is required")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a valid number with up to 2 decimal places")
    @DecimalMin(value = "0.00", inclusive = true, message = "Amount must be greater than or equal to 0.00")
    @DecimalMax(value = "999999999.99", inclusive = true, message = "Amount must be less than or equal to 9999999.99")
    private double montant;
    
    @Column(name = "taux_alerte")
    private double tauxAlerte;

    @Column(name = "created_at")
    @NotBlank(message = "Start Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Invalid date format. Expected format: yyyy-MM-dd")
    private String createdAt;

    @Column(name = "finished_at")
    @NotBlank(message = "Start Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Invalid date format. Expected format: yyyy-MM-dd")
    private String finishedAt;

    @Column(name = "libele")
    private String libele;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customerId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_alerte")
    private BudgetAlerte idAlerte;

    public List<Depenses> getDepenses() {
        return depenses;
    }

    public void setDepenses(List<Depenses> depenses) {
        this.depenses = depenses;
    }

    @OneToMany(mappedBy = "budget" , cascade = CascadeType.ALL)
    private List<Depenses> depenses;

    public BudgetAlerte getIdAlerte() {
        return idAlerte;
    }

    public void setIdAlerte(BudgetAlerte idAlerte) {
        this.idAlerte = idAlerte;
    }


    public Budget(@NotBlank(message = "Amount can't be void") double montant, double tauxAlerte) {
        this.montant = montant;
        this.tauxAlerte = tauxAlerte;
    }

    public Budget() {
    }

    public Budget(double montant, String createdAt, String finishedAt, String libele, Customer customerId, BudgetAlerte idAlerte) {
        this.montant = montant;
        this.createdAt = createdAt;
        this.finishedAt = finishedAt;
        this.libele = libele;
        this.customerId = customerId;
        this.idAlerte = idAlerte;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Integer budgetId) {
        this.budgetId = budgetId;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public double getTauxAlerte() {
        return tauxAlerte;
    }

    public void setTauxAlerte(double tauxAlerte) {
        this.tauxAlerte = tauxAlerte;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getLibele() {
        return libele;
    }

    public void setLibele(String libele) {
        this.libele = libele;
    }

    public Customer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Customer customerId) {
        this.customerId = customerId;
    }

    public double getSommeDepenses() {
        double somme = 0;
        for (Depenses depenses:this.depenses) {
            somme += depenses.getMontant();
        }
        return somme;
    }
}
