package com.frilanceos.backend.auth;

/**
 * Roles from SRS FR-02/FR-03. {@code SMM} covers smm1-smm4 (a role, not an
 * individual login) and {@code TARGETOLOGIST} covers the target role. Access
 * rules per role live in {@link RoleAccessPolicy}, mirroring the SRS
 * ROLE_ACCESS table so authorization stays declarative in one place.
 */
public enum Role {
    OWNER,
    PROJECT_MANAGER,
    SMM,
    TARGETOLOGIST,
    FREELANCER
}