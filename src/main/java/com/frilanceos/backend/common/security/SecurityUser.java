package com.frilanceos.backend.common.security;

import com.frilanceos.backend.auth.Role;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * The authenticated principal placed in the {@code SecurityContext} for every
 * request that carries a valid access token. {@code tenantId} is exposed here
 * only for convenience in tests/logging — controllers and services should
 * read the tenant via {@code TenantContext}, not by casting the principal.
 */
public class SecurityUser extends User {

    private final UUID userId;
    private final UUID tenantId;
    private final Role role;

    public SecurityUser(UUID userId, UUID tenantId, Role role) {
        super(userId.toString(), "", authorities(role));
        this.userId = userId;
        this.tenantId = tenantId;
        this.role = role;
    }

    private static List<GrantedAuthority> authorities(Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public Role getRole() {
        return role;
    }
}