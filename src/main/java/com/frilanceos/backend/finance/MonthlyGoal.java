package com.frilanceos.backend.finance;

import com.frilanceos.backend.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

/** Backs the "progress toward goal" bars in FR-18 (revenue goal) and the tax reserve stat card. */
@Entity
@Table(name = "monthly_goal")
public class MonthlyGoal extends TenantScopedEntity {

    @Column(name = "goal_month", nullable = false)
    private String goalMonth;

    @Column(name = "revenue_goal", nullable = false, precision = 14, scale = 2)
    private BigDecimal revenueGoal;

    @Column(name = "tax_reserve_goal", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxReserveGoal;

    protected MonthlyGoal() {
    }

    public MonthlyGoal(UUID ownerId, YearMonth goalMonth, BigDecimal revenueGoal, BigDecimal taxReserveGoal) {
        super(ownerId);
        this.goalMonth = goalMonth.toString();
        this.revenueGoal = revenueGoal;
        this.taxReserveGoal = taxReserveGoal;
    }

    public YearMonth getGoalMonth() {
        return YearMonth.parse(goalMonth);
    }

    public BigDecimal getRevenueGoal() {
        return revenueGoal;
    }

    public BigDecimal getTaxReserveGoal() {
        return taxReserveGoal;
    }
}
