package com.frilanceos.backend.ledger;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventLogEntryRepository extends JpaRepository<EventLogEntry, UUID> {

    List<EventLogEntry> findByOwnerIdOrderByOccurredAtDesc(UUID ownerId, Pageable pageable);
}