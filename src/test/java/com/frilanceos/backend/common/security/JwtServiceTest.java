package com.frilanceos.backend.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.frilanceos.backend.auth.Role;
import com.frilanceos.backend.auth.UserAccount;
import com.frilanceos.backend.auth.WorkMode;
import io.jsonwebtoken.JwtException;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "01234567890123456789012345678901".getBytes());

    private final JwtService jwtService = new JwtService(SECRET, 60);

    /**
     * {@code UserAccount.id} is only populated by Hibernate's UUID generator
     * once the entity is actually persisted (see {@code TenantScopedEntity}) —
     * in production that always happens via {@code AuthService} before a
     * token is ever issued. These unit tests never touch a repository, so we
     * set the id the same way JPA would, rather than pretending an
     * unpersisted entity already has one.
     */
    private static UserAccount withId(UserAccount user) {
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    @Test
    void issuedTokenParsesBackToTheIssuingUsersIdentity() {
        UserAccount owner = withId(new UserAccount("diana@example.com", "hash", "Діана М.",
                Role.OWNER, WorkMode.AGENCY, null));

        String token = jwtService.issueAccessToken(owner);
        JwtService.ParsedToken parsed = jwtService.parse(token);

        assertThat(parsed.userId()).isEqualTo(owner.getId());
        assertThat(parsed.tenantId()).isEqualTo(owner.tenantId());
        assertThat(parsed.role()).isEqualTo(Role.OWNER);
    }

    @Test
    void agencyTeamMemberTokenCarriesTheOwnersTenantIdNotTheirOwn() {
        UUID ownerId = UUID.randomUUID();
        UserAccount smm = withId(new UserAccount("oksana@example.com", "hash", "Оксана",
                Role.SMM, WorkMode.AGENCY, ownerId));

        JwtService.ParsedToken parsed = jwtService.parse(jwtService.issueAccessToken(smm));

        assertThat(parsed.tenantId()).isEqualTo(ownerId);
        assertThat(parsed.userId()).isEqualTo(smm.getId());
    }

    @Test
    void tamperedTokenIsRejected() {
        UserAccount owner = withId(new UserAccount("diana@example.com", "hash", "Діана М.",
                Role.OWNER, WorkMode.AGENCY, null));
        String token = jwtService.issueAccessToken(owner);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> jwtService.parse(tampered)).isInstanceOf(JwtException.class);
    }
}
