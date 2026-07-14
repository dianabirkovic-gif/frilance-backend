package com.frilanceos.backend.team;

import com.frilanceos.backend.auth.Role;
import com.frilanceos.backend.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Read-model row for the dashboard's "team workload" panel. {@code loadPercent}
 * and {@code clientCount} are stored directly rather than derived from
 * {@code Client.assigneeId} counts — computing them live is future work for
 * the Team module (see FR-03/team package-info).
 */
@Entity
@Table(name = "team_member")
public class TeamMember extends TenantScopedEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "load_percent", nullable = false)
    private int loadPercent;

    @Column(name = "client_count", nullable = false)
    private int clientCount;

    protected TeamMember() {
    }

    public TeamMember(UUID ownerId, String name, Role role, int loadPercent, int clientCount) {
        super(ownerId);
        this.name = name;
        this.role = role;
        this.loadPercent = loadPercent;
        this.clientCount = clientCount;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public int getLoadPercent() {
        return loadPercent;
    }

    public int getClientCount() {
        return clientCount;
    }
}