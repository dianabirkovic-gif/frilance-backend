package com.frilanceos.backend.clients.dto;

import java.math.BigDecimal;
import java.util.List;

/** DTOs behind the Clients screen (clients.html) — one aggregated shape per endpoint. */
public final class ClientResponse {

    private ClientResponse() {
    }

    public record ClientListItemDto(
            String id,
            String name,
            String niche,
            String assigneeName,
            String assigneeInitials,
            String status,
            String nextPostLabel,
            BigDecimal monthlyRevenue,
            String lastActivityLabel
    ) {
    }

    public record ClientDetailDto(
            String id,
            String name,
            String niche,
            String assigneeName,
            String status,
            BigDecimal monthlyRevenue,
            String cooperationDurationLabel,
            String contactName,
            String contactRole,
            String contactPhone,
            String contactEmail,
            String stage,
            List<ActivityEntryDto> activity
    ) {
    }

    public record ActivityEntryDto(
            String timeLabel,
            String actorInitials,
            String actorName,
            String description
    ) {
    }
}
