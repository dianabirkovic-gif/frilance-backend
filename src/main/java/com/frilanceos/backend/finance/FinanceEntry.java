package com.frilanceos.backend.finance;

import com.frilanceos.backend.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * FR-09. Minimal shape for the dashboard's finance strip (income / team
 * expenses / tax reserve for the current month); currency conversion,
 * payroll templates (FR-10) and CSV export (FR-19) are out of scope here.
 */
@Entity
@Table(name = "finance_entry")
public class FinanceEntry extends TenantScopedEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinanceEntryType type;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    protected FinanceEntry() {
    }

    public FinanceEntry(UUID ownerId, FinanceEntryType type, BigDecimal amount, String currency,
                         LocalDate entryDate) {
        super(ownerId);
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.entryDate = entryDate;
    }

    public FinanceEntryType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }
}
