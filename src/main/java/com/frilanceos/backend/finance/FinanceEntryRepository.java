package com.frilanceos.backend.finance;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinanceEntryRepository extends JpaRepository<FinanceEntry, UUID> {

    List<FinanceEntry> findByOwnerIdAndEntryDateBetween(UUID ownerId, LocalDate from, LocalDate to);
}
