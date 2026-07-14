package com.frilanceos.backend.agency;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyConfigRepository extends JpaRepository<AgencyConfig, UUID> {
}