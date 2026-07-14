package com.frilanceos.backend.auth.dto;

import com.frilanceos.backend.auth.Role;

public record AuthResponse(String accessToken, String fullName, Role role) {
}