package com.frilanceos.backend.contentplan;

import com.frilanceos.backend.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** FR-08. Minimal fields for the dashboard's "this week" content-plan strip. */
@Entity
@Table(name = "post")
public class Post extends TenantScopedEntity {

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String title;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status;

    protected Post() {
    }

    public Post(UUID ownerId, UUID clientId, String clientName, String title, LocalDate scheduledDate,
                PostStatus status) {
        super(ownerId);
        this.clientId = clientId;
        this.clientName = clientName;
        this.title = title;
        this.scheduledDate = scheduledDate;
        this.status = status;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public PostStatus getStatus() {
        return status;
    }
}