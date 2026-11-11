
package ru.stanislav.NauJava3.entity;

import jakarta.persistence.*;
import ru.stanislav.NauJava3.entity.enums.BudgetPeriod;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "limit_amount", nullable = false,
            precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetPeriod period;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "alert_percent", nullable = false)
    private int alertPercent = 80;

    protected Budget() {
    }

    public Budget(AppUser user, Category category, String name,
                  BigDecimal limitAmount, BudgetPeriod period,
                  LocalDate startDate, LocalDate endDate,
                  int alertPercent) {
        this.user = user;
        this.category = category;
        this.name = name;
        this.limitAmount = limitAmount;
        this.period = period;
        this.startDate = startDate;
        this.endDate = endDate;
        this.alertPercent = alertPercent;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }
}
