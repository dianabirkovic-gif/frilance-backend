package com.frilanceos.backend.clients.dto;

import com.frilanceos.backend.clients.ClientStage;
import com.frilanceos.backend.clients.ClientStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** FR-05's client-creation fields. {@code assigneeId} only applies in agency mode. */
public record CreateClientRequest(
        @NotBlank String name,
        String niche,
        String tariffPlan,
        LocalDate cooperationStartDate,
        UUID assigneeId,
        BigDecimal serviceCost,
        @NotNull ClientStatus status,
        String contactName,
        String contactRole,
        ClientStage stage
) {
}
