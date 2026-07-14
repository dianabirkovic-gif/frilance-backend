package com.frilanceos.backend.clients;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {

    List<Client> findByOwnerId(UUID ownerId);
}