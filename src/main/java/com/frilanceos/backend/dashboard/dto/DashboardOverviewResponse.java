package com.frilanceos.backend.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** One aggregated payload for the whole Overview screen — see dashboard.html sections. */
public record DashboardOverviewResponse(
        String dateLabel,
        String greeting,
        Revenue revenue,
        List<AttentionItemDto> attentionItems,
        List<ContentPlanDayDto> contentPlanWeek,
        FinanceSummary financeSummary,
        List<TeamMemberDto> teamWorkload,
        List<LedgerEntryDto> eventLedger
) {

    public record Revenue(
            BigDecimal amount,
            String currency,
            double deltaPercent,
            List<RevenuePoint> series
    ) {
    }

    public record RevenuePoint(LocalDate date, BigDecimal cumulativeAmount) {
    }

    public record AttentionItemDto(
            String severity,
            String title,
            String subtitle,
            String metaLabel,
            boolean metaIsDanger
    ) {
    }

    public record ContentPlanDayDto(
            String dayLabel,
            LocalDate date,
            String clientLabel,
            String status
    ) {
    }

    /** Null when the current role has no finance access (RBAC, FR-03/FR-09). */
    public record FinanceSummary(
            List<FinanceRowDto> rows
    ) {
    }

    public record FinanceRowDto(
            String label,
            BigDecimal amount,
            int percentOfGoal,
            String colorRole
    ) {
    }

    public record TeamMemberDto(
            String name,
            String role,
            int loadPercent,
            int clientCount
    ) {
    }

    public record LedgerEntryDto(
            String time,
            String actorInitials,
            String actorName,
            String description,
            String tag,
            BigDecimal amount,
            String direction
    ) {
    }
}