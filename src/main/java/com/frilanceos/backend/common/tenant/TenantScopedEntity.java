package com.frilanceos.backend.common.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.Objects;
import java.util.UUID;

/**
 * Base class for every entity that must be isolated per NFR-04.
 *
 * <p>{@code ownerId} is the tenant key described in {@link TenantContext}: an
 * agency owner's user id for agency data, or a freelancer's own user id.
 * Every repository method that queries a {@link TenantScopedEntity} subtype
 * MUST filter by {@code ownerId = TenantContext.currentTenantId()} — see the
 * repository conventions in this backend's CLAUDE.md before adding a new
 * finder method.
 */
@MappedSuperclass
public abstract class TenantScopedEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    protected TenantScopedEntity() {
    }

    protected TenantScopedEntity(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantScopedEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}