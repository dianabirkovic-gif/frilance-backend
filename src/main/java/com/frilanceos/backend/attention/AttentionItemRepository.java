package com.frilanceos.backend.attention;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttentionItemRepository extends JpaRepository<AttentionItem, UUID> {

    List<AttentionItem> findByOwnerIdOrderBySeverityAsc(UUID ownerId);
}