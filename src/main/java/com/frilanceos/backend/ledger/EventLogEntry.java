package com.frilanceos.backend.ledger;

import com.frilanceos.backend.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * The dashboard's "Реєстр подій" (event ledger) — an append-only feed of
 * things that happened across modules. Today this is written to directly by
 * seed data; once other modules exist, each should append here (invoice
 * paid, post published, client added, budget spent) rather than the
 * dashboard reconstructing history from other tables at read time.
 */
@Entity
@Table(name = "event_log_entry")
public class EventLogEntry extends TenantScopedEntity {

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "actor_initials", nullable = false)
    private String actorInitials;

    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventTag tag;

    /** Null when the event has no monetary amount (e.g. a published post). */
    @Column(precision = 14, scale = 2)
    private BigDecimal amount;

    protected EventLogEntry() {
    }

    public EventLogEntry(UUID ownerId, Instant occurredAt, String actorInitials, String actorName,
                          String description, EventTag tag, BigDecimal amount) {
        super(ownerId);
        this.occurredAt = occurredAt;
        this.actorInitials = actorInitials;
        this.actorName = actorName;
        this.description = description;
        this.tag = tag;
        this.amount = amount;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getActorInitials() {
        return actorInitials;
    }

    public String getActorName() {
        return actorName;
    }

    public String getDescription() {
        return description;
    }

    public EventTag getTag() {
        return tag;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}