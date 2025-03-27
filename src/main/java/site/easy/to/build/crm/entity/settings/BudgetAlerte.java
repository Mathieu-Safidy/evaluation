package site.easy.to.build.crm.entity.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import site.easy.to.build.crm.entity.Budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "budget_alerte")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BudgetAlerte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerte", nullable = false)
    private Integer id;

    @Size(max = 50)
    @Column(name = "label", nullable = true, length = 50)
    private String label;

    @NotNull
    @Column(name = "valeur", nullable = false, precision = 5, scale = 2)
    private BigDecimal valeur;

    @NotNull
    @Column(name = "created_At", nullable = false)
    private LocalDate createdAt;

//    @JsonIgnore
//    @OneToMany(mappedBy = "idAlerte",fetch = FetchType.EAGER)
//    private List<Budget> budgets;

    public BudgetAlerte() {
    }

    public BudgetAlerte(String label, LocalDate createdAt, BigDecimal valeur) {
//        this.budgets = budgets;
        this.label = label;
        this.createdAt = createdAt;
        this.valeur = valeur;
    }

    public @Size(max = 50) String getLabel() {
        return label;
    }

    public void setLabel(@Size(max = 50) String label) {
        this.label = label;
    }

    public @NotNull BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(@NotNull BigDecimal valeur) {
        this.valeur = valeur;
    }

    public @NotNull LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NotNull LocalDate createdAt) {
        this.createdAt = createdAt;
    }

//    public List<Budget> getBudgets() {
//        return budgets;
//    }
//
//    public void setBudgets(List<Budget> budgets) {
//        this.budgets = budgets;
//    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}