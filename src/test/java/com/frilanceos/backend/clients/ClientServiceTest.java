package com.frilanceos.backend.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.frilanceos.backend.auth.Role;
import com.frilanceos.backend.auth.UserAccountRepository;
import com.frilanceos.backend.clients.dto.ClientResponse.ClientListItemDto;
import com.frilanceos.backend.common.exception.ApiException;
import com.frilanceos.backend.common.security.SecurityUser;
import com.frilanceos.backend.common.tenant.TenantContext;
import com.frilanceos.backend.contentplan.PostRepository;
import com.frilanceos.backend.ledger.EventLogEntryRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private EventLogEntryRepository eventLogEntryRepository;

    private ClientService clientService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID smmUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientRepository, userAccountRepository, postRepository,
                eventLogEntryRepository);
        TenantContext.set(tenantId, tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    /**
     * {@code Client.id} is only populated by Hibernate's UUID generator once
     * persisted (see {@code TenantScopedEntity}); these unit tests never
     * touch a repository, so set it the same way JPA would.
     */
    private Client sampleClient(UUID assigneeId) {
        Client client = new Client(tenantId, "MUSE'23", "Beauty", assigneeId, ClientStatus.ACTIVE, "Преміум",
                LocalDate.now().minusMonths(2), BigDecimal.valueOf(32000), "Наталія Вовк", "Засновниця",
                ClientStage.REPORT);
        setEntityId(client, UUID.randomUUID());
        return client;
    }

    @Test
    void ownerSeesAllClientsForTheTenant() {
        SecurityUser owner = new SecurityUser(UUID.randomUUID(), tenantId, Role.OWNER);
        when(clientRepository.findByOwnerId(tenantId)).thenReturn(List.of(sampleClient(null)));
        when(postRepository.findFirstByOwnerIdAndClientIdAndScheduledDateGreaterThanEqualOrderByScheduledDateAsc(
                any(), any(), any())).thenReturn(Optional.empty());
        when(eventLogEntryRepository.findByOwnerIdAndClientIdOrderByOccurredAtDesc(any(), any(), any()))
                .thenReturn(List.of());

        List<ClientListItemDto> result = clientService.listClients(owner);

        assertThat(result).hasSize(1);
        verify(clientRepository).findByOwnerId(tenantId);
        verify(clientRepository, never()).findByOwnerIdAndAssigneeId(any(), any());
    }

    @Test
    void smmOnlySeesClientsAssignedToThem() {
        SecurityUser smm = new SecurityUser(smmUserId, tenantId, Role.SMM);
        when(clientRepository.findByOwnerIdAndAssigneeId(tenantId, smmUserId))
                .thenReturn(List.of(sampleClient(smmUserId)));
        when(postRepository.findFirstByOwnerIdAndClientIdAndScheduledDateGreaterThanEqualOrderByScheduledDateAsc(
                any(), any(), any())).thenReturn(Optional.empty());
        when(eventLogEntryRepository.findByOwnerIdAndClientIdOrderByOccurredAtDesc(any(), any(), any()))
                .thenReturn(List.of());

        List<ClientListItemDto> result = clientService.listClients(smm);

        assertThat(result).hasSize(1);
        verify(clientRepository).findByOwnerIdAndAssigneeId(tenantId, smmUserId);
        verify(clientRepository, never()).findByOwnerId(any());
    }

    @Test
    void smmCannotDeleteAClient() {
        SecurityUser smm = new SecurityUser(smmUserId, tenantId, Role.SMM);

        assertThatThrownBy(() -> clientService.deleteClient(smm, UUID.randomUUID()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus().value()).isEqualTo(403));
    }

    @Test
    void ownerCanDeleteAClient() {
        SecurityUser owner = new SecurityUser(UUID.randomUUID(), tenantId, Role.OWNER);
        Client client = sampleClient(null);
        UUID clientId = UUID.randomUUID();
        setEntityId(client, clientId);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        clientService.deleteClient(owner, clientId);

        verify(clientRepository).delete(client);
    }

    private void setEntityId(Client client, UUID id) {
        ReflectionTestUtils.setField(client, "id", id);
    }
}
