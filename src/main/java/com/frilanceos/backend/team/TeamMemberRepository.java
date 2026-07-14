package com.frilanceos.backend.team;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    List<TeamMember> findByOwnerIdOrderByLoadPercentDesc(UUID ownerId);
}