package com.frilanceos.backend.auth;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Deliberately not a repository over a {@code TenantScopedEntity}: user
 * accounts are looked up by email (login) or id (JWT resolution), never by
 * tenant, so the usual "filter by ownerId" rule does not apply here.
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);
}