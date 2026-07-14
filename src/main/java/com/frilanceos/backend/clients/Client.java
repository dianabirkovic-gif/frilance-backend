package com.frilanceos.backend.clients;

import com.frilanceos.backend.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * FR-05. Minimal fields needed by the dashboard vertical slice (name,
 * niche, assignee, status); full client-card fields (tariff plan,
 * cooperation start date, service cost) land with the Clients module.
 */
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

    protected Client() {
    }

    public Client(UUID ownerId, String name, String niche, UUID assigneeId, ClientStatus status) {
        super(ownerId);
        this.name = name;
        this.niche = niche;
        this.assigneeId = assigneeId;
        this.status = status;
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
}