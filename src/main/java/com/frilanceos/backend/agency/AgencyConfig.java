package com.frilanceos.backend.agency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * One row per agency, keyed by the owner's user id (FR-02). Presence of a
 * row here is what makes a tenant an "agency" rather than a lone freelancer;
 * team invite/role-join flows (FR-02 for PM/SMM/targetolog) are not yet
 * implemented — see this repo's CLAUDE.md "Known simplifications".
 */
@Entity
@Table(name = "agency_config")
public class AgencyConfig {

    @Id
    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "agency_name", nullable = false)
    private String agencyName;

    protected AgencyConfig() {
    }

    public AgencyConfig(UUID ownerId, String agencyName) {
        this.ownerId = ownerId;
        this.agencyName = agencyName;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getAgencyName() {
        return agencyName;
    }
}