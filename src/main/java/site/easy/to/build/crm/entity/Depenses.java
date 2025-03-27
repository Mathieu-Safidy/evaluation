package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "depenses")
public class Depenses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "depenses_id")
    private int depensesId;

    @Column(name = "montant",precision = 15,scale = 2)
    @NotNull(message = "Amount is required")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a valid number with up to 2 decimal places")
    @DecimalMin(value = "0.00", inclusive = true, message = "Amount must be greater than or equal to 0.00")
    @DecimalMax(value = "999999999.99", inclusive = true, message = "Amount must be less than or equal to 9999999.99")
    private double montant;

    @Column(name = "created_at")
    @NotBlank(message = "Start Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Invalid datetime format. Expected format: yyyy-MM-dd HH:mm:ss")
    private String createdAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customerId;

    @Column(name = "libele")
    private String libele;

//    @OneToMany(mappedBy = "depenses" , cascade = CascadeType.ALL)
//    private List<Budget> budgets;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "budget_id")
    private Budget budget;

    public Depenses() {
    }

    public Depenses(double montant, String createdAt) {
        this.montant = montant;
        this.createdAt = createdAt;
    }

    public int getDepensesId() {
        return depensesId;
    }

    public void setDepensesId(int depensesId) {
        this.depensesId = depensesId;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Customer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Customer customerId) {
        this.customerId = customerId;
    }

    public String getLibele() {
        return libele;
    }

    public void setLibele(String libele) {
        this.libele = libele;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public double checkBudget() {
        double montant = this.getMontant() + this.getBudget().getSommeDepenses();
        return montant;
    }
}
