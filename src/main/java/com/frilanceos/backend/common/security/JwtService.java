package com.frilanceos.backend.common.security;

import com.frilanceos.backend.auth.Role;
import com.frilanceos.backend.auth.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Issues and parses the single access token this backend uses. There is no
 * refresh-token rotation yet — see the "Known simplifications" section of
 * this repo's CLAUDE.md before shipping this beyond a local prototype.
 */
@Component
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TENANT_ID = "tenantId";

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret.getBytes()));
        this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
    }

    public String issueAccessToken(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TENANT_ID, user.tenantId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    public ParsedToken parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return new ParsedToken(
                UUID.fromString(claims.getSubject()),
                UUID.fromString(claims.get(CLAIM_TENANT_ID, String.class)),
                Role.valueOf(claims.get(CLAIM_ROLE, String.class)));
    }

    public record ParsedToken(UUID userId, UUID tenantId, Role role) {
    }
}