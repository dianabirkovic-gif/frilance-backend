package com.frilanceos.backend.auth;

import java.util.EnumMap;
import java.util.Map;

/**
 * Mirrors the SRS ROLE_ACCESS table (FR-03). Kept as one small lookup so
 * "can this role see all clients / finances / delete records" never has to
 * be re-decided ad hoc in a controller or service.
 */
public final class RoleAccessPolicy {

    public record Access(boolean allClients, boolean financeAccess, boolean allContent, boolean canDelete) {
    }

    private static final Map<Role, Access> POLICY = new EnumMap<>(Role.class);

    static {
        POLICY.put(Role.OWNER, new Access(true, true, true, true));
        POLICY.put(Role.PROJECT_MANAGER, new Access(true, false, true, false));
        POLICY.put(Role.SMM, new Access(false, false, false, false));
        POLICY.put(Role.TARGETOLOGIST, new Access(false, false, false, false));
        POLICY.put(Role.FREELANCER, new Access(true, true, true, true));
    }

    private RoleAccessPolicy() {
    }

    public static Access forRole(Role role) {
        Access access = POLICY.get(role);
        if (access == null) {
            throw new IllegalArgumentException("No access policy defined for role " + role);
        }
        return access;
    }
}