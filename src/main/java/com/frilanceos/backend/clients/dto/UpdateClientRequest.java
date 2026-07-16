package com.frilanceos.backend.clients.dto;

import com.frilanceos.backend.clients.ClientStage;
import com.frilanceos.backend.clients.ClientStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateClientRequest(
        @NotBlank String name,
        String niche,
        String tariffPlan,
        LocalDate cooperationStartDate,
        UUID assigneeId,
        BigDecimal serviceCost,
        @NotNull ClientStatus status,
        @NotBlank String contactName,
        String contactRole,
        @NotBlank String contactPhone,
        @NotBlank @Email String contactEmail,
        @NotNull ClientStage stage
) {
}
