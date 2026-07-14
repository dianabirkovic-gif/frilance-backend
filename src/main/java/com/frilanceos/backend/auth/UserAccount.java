package com.frilanceos.backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A login identity. Not a {@link com.frilanceos.backend.common.tenant.TenantScopedEntity}
 * itself — it defines the tenant boundary for everything else rather than
 * living inside one.
 */
@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false)
    private WorkMode workMode;

    /**
     * Null for an OWNER or FREELANCER (they are their own tenant); set to the
     * owning agency's owner id for PROJECT_MANAGER / SMM / TARGETOLOGIST.
     */
    @Column(name = "agency_owner_id")
    private UUID agencyOwnerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UserAccount() {
    }

    public UserAccount(String email, String passwordHash, String fullName, Role role, WorkMode workMode,
                        UUID agencyOwnerId) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.workMode = workMode;
        this.agencyOwnerId = agencyOwnerId;
        this.createdAt = Instant.now();
    }

    /** The tenant/isolation key described in {@code TenantContext} — see its Javadoc. */
    public UUID tenantId() {
        return agencyOwnerId != null ? agencyOwnerId : id;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public UUID getAgencyOwnerId() {
        return agencyOwnerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}