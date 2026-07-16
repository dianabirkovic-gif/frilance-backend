package com.frilanceos.backend.clients.dto;

import com.frilanceos.backend.clients.ClientStatus;
import jakarta.validation.constraints.NotNull;

/** Backs the client card's quick-action status changes (e.g. "Архівувати"). */
public record UpdateClientStatusRequest(@NotNull ClientStatus status) {
}
