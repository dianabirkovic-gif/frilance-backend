package com.frilanceos.backend.auth;

import com.frilanceos.backend.auth.dto.AuthResponse;
import com.frilanceos.backend.auth.dto.LoginRequest;
import com.frilanceos.backend.auth.dto.RegisterRequest;
import com.frilanceos.backend.common.exception.ApiException;
import com.frilanceos.backend.common.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implements FR-01 (freelancer self-registration/login). Agency-role login
 * (FR-02: owner email/password, PM email/password, SMM/targetolog shared team
 * password + anonymous session) is intentionally not implemented yet — see
 * the {@code agency} package and this repo's CLAUDE.md.
 */
@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("Email is already registered");
        }

        Role role = switch (request.workMode()) {
            case FREELANCER -> Role.FREELANCER;
            case AGENCY -> Role.OWNER;
        };

        UserAccount user = new UserAccount(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                role,
                request.workMode(),
                null);
        userAccountRepository.save(user);

        return new AuthResponse(jwtService.issueAccessToken(user), user.getFullName(), user.getRole());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Invalid email or password");
        }

        return new AuthResponse(jwtService.issueAccessToken(user), user.getFullName(), user.getRole());
    }
}