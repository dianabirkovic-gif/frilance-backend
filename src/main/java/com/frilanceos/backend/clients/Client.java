package com.frilanceos.backend.clients;

import com.frilanceos.backend.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** FR-05: client/project cards. */
@Entity
@Table(name = "client")
public class Client extends TenantScopedEntity {

    @Column(nullable = false)
    private String name;

    @Column
    private String niche;

    @Column(name = "assignee_id")
    private UUID assigneeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    @Column(name = "tariff_plan")
    private String tariffPlan;

    @Column(name = "cooperation_start_date")
    private LocalDate cooperationStartDate;

    @Column(name = "service_cost", precision = 14, scale = 2)
    private BigDecimal serviceCost;

    @Column(name = "contact_name")
    private String contactName;

    @Column(name = "contact_role")
    private String contactRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStage stage;

    protected Client() {
    }

    public Client(UUID ownerId, String name, String niche, UUID assigneeId, ClientStatus status,
                  String tariffPlan, LocalDate cooperationStartDate, BigDecimal serviceCost,
                  String contactName, String contactRole, ClientStage stage) {
        super(ownerId);
        this.name = name;
        this.niche = niche;
        this.assigneeId = assigneeId;
        this.status = status;
        this.tariffPlan = tariffPlan;
        this.cooperationStartDate = cooperationStartDate;
        this.serviceCost = serviceCost;
        this.contactName = contactName;
        this.contactRole = contactRole;
        this.stage = stage;
    }

    public String getName() {
        return name;
    }

    public String getNiche() {
        return niche;
    }

    public UUID getAssigneeId() {
        return assigneeId;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public String getTariffPlan() {
        return tariffPlan;
    }

    public LocalDate getCooperationStartDate() {
        return cooperationStartDate;
    }

    public BigDecimal getServiceCost() {
        return serviceCost;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactRole() {
        return contactRole;
    }

    public ClientStage getStage() {
        return stage;
    }

    public void update(String name, String niche, UUID assigneeId, ClientStatus status, String tariffPlan,
                        LocalDate cooperationStartDate, BigDecimal serviceCost, String contactName,
                        String contactRole, ClientStage stage) {
        this.name = name;
        this.niche = niche;
        this.assigneeId = assigneeId;
        this.status = status;
        this.tariffPlan = tariffPlan;
        this.cooperationStartDate = cooperationStartDate;
        this.serviceCost = serviceCost;
        this.contactName = contactName;
        this.contactRole = contactRole;
        this.stage = stage;
    }
}
