package com.frilanceos.backend.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import com.frilanceos.backend.auth.WorkMode;
import com.frilanceos.backend.auth.dto.AuthResponse;
import com.frilanceos.backend.auth.dto.RegisterRequest;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Exercises the whole stack (auth -> JWT -> tenant-scoped dashboard read)
 * against a real Postgres container. Requires a local Docker daemon; skip
 * with -DexcludedGroups if none is available (see this repo's README).
 */
@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class DashboardOverviewIntegrationTest {

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

    @Test
    void newFreelancerSeesAnEmptyButWellFormedOverview() {
        RegisterRequest register = new RegisterRequest(
                "freelancer-" + UUID.randomUUID() + "@example.com", "password123", "Тестовий Фрілансер",
                WorkMode.FREELANCER);
        AuthResponse auth = restTemplate.postForObject(url("/api/v1/auth/register"), register, AuthResponse.class);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(auth.accessToken());

        ResponseEntity<DashboardOverviewResponse> response = restTemplate.exchange(
                url("/api/v1/dashboard/overview"), org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers), DashboardOverviewResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().attentionItems()).isEmpty();
        assertThat(response.getBody().financeSummary().rows()).hasSize(3);
    }

    @Test
    void unauthenticatedRequestIsRejected() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/v1/dashboard/overview"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}