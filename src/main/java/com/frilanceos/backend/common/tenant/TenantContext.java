package com.frilanceos.backend.common.tenant;

import java.util.UUID;

/**
 * Holds the current request's tenant id for the lifetime of the thread.
 *
 * <p>Tenant id is the isolation key required by NFR-04: for a freelancer it is
 * their own user id, for an agency member (owner, PM, SMM, targetolog) it is
 * the agency owner's user id. {@link com.frilanceos.backend.common.security.JwtAuthenticationFilter}
 * populates this from the JWT on every authenticated request and clears it
 * once the request completes — never trust a tenant id supplied by the client
 * in a request body or query param.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID tenantId, UUID userId) {
        CURRENT_TENANT.set(tenantId);
        CURRENT_USER.set(userId);
    }

    public static UUID currentTenantId() {
        UUID tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException(
                    "No tenant bound to the current thread — this endpoint must run behind JwtAuthenticationFilter");
        }
        return tenantId;
    }

    public static UUID currentUserId() {
        UUID userId = CURRENT_USER.get();
        if (userId == null) {
            throw new IllegalStateException(
                    "No user bound to the current thread — this endpoint must run behind JwtAuthenticationFilter");
        }
        return userId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
    }
}