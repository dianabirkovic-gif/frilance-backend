package com.frilanceos.backend.contentplan;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByOwnerIdAndScheduledDateBetweenOrderByScheduledDateAsc(
            UUID ownerId, LocalDate from, LocalDate to);
}