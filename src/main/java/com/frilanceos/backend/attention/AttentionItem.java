package com.frilanceos.backend.attention;

import com.frilanceos.backend.common.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Backs the dashboard's "Потребують уваги" panel. This is a placeholder
 * table seeded directly for now. In a later pass this should become a
 * computed view over clients/finance/targets (silent lead, unpaid invoice,
 * content not ready, budget near exhaustion) instead of a table that other
 * modules write to directly — do not build new features against this entity
 * without re-reading that plan.
 */
@Entity
@Table(name = "attention_item")
public class AttentionItem extends TenantScopedEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttentionSeverity severity;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String subtitle;

    /** Freeform display value shown at the end of the row (e.g. "5 днів", "₴12 000", "90%"). */
    @Column(name = "meta_label", nullable = false)
    private String metaLabel;

    @Column(name = "meta_is_danger", nullable = false)
    private boolean metaIsDanger;

    protected AttentionItem() {
    }

    public AttentionItem(UUID ownerId, AttentionSeverity severity, String title, String subtitle,
                          String metaLabel, boolean metaIsDanger) {
        super(ownerId);
        this.severity = severity;
        this.title = title;
        this.subtitle = subtitle;
        this.metaLabel = metaLabel;
        this.metaIsDanger = metaIsDanger;
    }

    public AttentionSeverity getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getMetaLabel() {
        return metaLabel;
    }

    public boolean isMetaIsDanger() {
        return metaIsDanger;
    }
}