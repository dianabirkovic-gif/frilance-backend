package com.frilanceos.backend.finance;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyGoalRepository extends JpaRepository<MonthlyGoal, UUID> {

    Optional<MonthlyGoal> findByOwnerIdAndGoalMonth(UUID ownerId, String goalMonth);
}
