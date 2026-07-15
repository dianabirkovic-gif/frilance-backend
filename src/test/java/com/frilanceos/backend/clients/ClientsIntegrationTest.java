package com.frilanceos.backend.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.frilanceos.backend.auth.Role;
import com.frilanceos.backend.auth.UserAccount;
import com.frilanceos.backend.auth.UserAccountRepository;
import com.frilanceos.backend.auth.WorkMode;
import com.frilanceos.backend.auth.dto.AuthResponse;
import com.frilanceos.backend.auth.dto.RegisterRequest;
import com.frilanceos.backend.clients.dto.ClientResponse.ClientListItemDto;
import com.frilanceos.backend.common.security.JwtService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Exercises FR-05's list endpoint against a real Postgres container: tenant
 * isolation (NFR-04) and the "SMM sees only their own clients" RBAC rule.
 * Requires a local Docker daemon; see this repo's README if none is
 * available.
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ClientsIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private AuthResponse registerAgencyOwner() {
        RegisterRequest register = new RegisterRequest(
                "owner-" + UUID.randomUUID() + "@example.com", "password123", "Тестова Власниця", WorkMode.AGENCY);
        return restTemplate.postForObject(url("/api/v1/auth/register"), register, AuthResponse.class);
    }

    private HttpHeaders bearer(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    @Test
    void ownerSeesOnlyTheirOwnTenantsClients() {
        AuthResponse ownerA = registerAgencyOwner();
        UUID ownerAId = jwtService.parse(ownerA.accessToken()).userId();
        clientRepository.save(new Client(ownerAId, "Клієнт A", "Ніша", null, ClientStatus.ACTIVE,
                null, null, null, null, null, ClientStage.BRIEF));

        AuthResponse ownerB = registerAgencyOwner();

        ResponseEntity<ClientListItemDto[]> responseA = restTemplate.exchange(
                url("/api/v1/clients"), HttpMethod.GET, new HttpEntity<>(bearer(ownerA.accessToken())),
                ClientListItemDto[].class);
        ResponseEntity<ClientListItemDto[]> responseB = restTemplate.exchange(
                url("/api/v1/clients"), HttpMethod.GET, new HttpEntity<>(bearer(ownerB.accessToken())),
                ClientListItemDto[].class);

        assertThat(responseA.getBody()).extracting(ClientListItemDto::name).contains("Клієнт A");
        // Tenant isolation (NFR-04): owner B must never see owner A's client.
        assertThat(responseB.getBody()).isEmpty();
    }

    @Test
    void smmSeesOnlyClientsAssignedToThem() {
        AuthResponse owner = registerAgencyOwner();
        UUID ownerId = jwtService.parse(owner.accessToken()).userId();

        UserAccount smm = new UserAccount("smm-" + UUID.randomUUID() + "@example.com",
                passwordEncoder.encode("password123"), "Тестова СММ", Role.SMM, WorkMode.AGENCY, ownerId);
        userAccountRepository.save(smm);
        String smmToken = jwtService.issueAccessToken(smm);

        clientRepository.save(new Client(ownerId, "Клієнт СММ", "Ніша", smm.getId(), ClientStatus.ACTIVE,
                null, null, null, null, null, ClientStage.BRIEF));
        clientRepository.save(new Client(ownerId, "Клієнт іншого", "Ніша", null, ClientStatus.ACTIVE,
                null, null, null, null, null, ClientStage.BRIEF));

        ResponseEntity<ClientListItemDto[]> response = restTemplate.exchange(
                url("/api/v1/clients"), HttpMethod.GET, new HttpEntity<>(bearer(smmToken)),
                ClientListItemDto[].class);

        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].name()).isEqualTo("Клієнт СММ");
    }

    @Test
    void unauthenticatedRequestIsRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/v1/clients"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}
