package com.frilanceos.backend.clients;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findByOwnerId(UUID ownerId);

    /** SMM/Targetolog's restricted view — see {@code RoleAccessPolicy.allClients}. */
    List<Client> findByOwnerIdAndAssigneeId(UUID ownerId, UUID assigneeId);

    /** Backs the dashboard's "Потребують уваги" panel — see DashboardService.buildAttentionItems. */
    List<Client> findByOwnerIdAndStatusOrderByNameAsc(UUID ownerId, ClientStatus status);
}