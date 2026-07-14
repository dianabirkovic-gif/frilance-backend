package com.frilanceos.backend.auth.dto;

import com.frilanceos.backend.auth.WorkMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** FR-01: freelancer self-registration. Agency team member onboarding (FR-02) is a separate future flow. */
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "must be at least 6 characters") String password,
        @NotBlank String fullName,
        @NotNull WorkMode workMode
) {
}