package com.frilanceos.backend.clients;

import com.frilanceos.backend.auth.RoleAccessPolicy;
import com.frilanceos.backend.auth.UserAccount;
import com.frilanceos.backend.auth.UserAccountRepository;
import com.frilanceos.backend.clients.dto.ClientResponse.ActivityEntryDto;
import com.frilanceos.backend.clients.dto.ClientResponse.ClientDetailDto;
import com.frilanceos.backend.clients.dto.ClientResponse.ClientListItemDto;
import com.frilanceos.backend.clients.dto.CreateClientRequest;
import com.frilanceos.backend.clients.dto.UpdateClientRequest;
import com.frilanceos.backend.clients.dto.UpdateClientStatusRequest;
import com.frilanceos.backend.common.exception.ApiException;
import com.frilanceos.backend.common.security.SecurityUser;
import com.frilanceos.backend.common.tenant.TenantContext;
import com.frilanceos.backend.contentplan.Post;
import com.frilanceos.backend.contentplan.PostRepository;
import com.frilanceos.backend.ledger.EventLogEntry;
import com.frilanceos.backend.ledger.EventLogEntryRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FR-05: client list/card. Every query is scoped to
 * {@code TenantContext.currentTenantId()} — see this repo's CLAUDE.md
 * "Tenant isolation" section before adding a query here that doesn't filter
 * by owner id.
 */
@Service
public class ClientService {

    private static final Map<DayOfWeek, String> WEEKDAY_LABELS = Map.of(
            DayOfWeek.MONDAY, "Пн",
            DayOfWeek.TUESDAY, "Вт",
            DayOfWeek.WEDNESDAY, "Ср",
            DayOfWeek.THURSDAY, "Чт",
            DayOfWeek.FRIDAY, "Пт",
            DayOfWeek.SATURDAY, "Сб",
            DayOfWeek.SUNDAY, "Нд");

    private final ClientRepository clientRepository;
    private final UserAccountRepository userAccountRepository;
    private final PostRepository postRepository;
    private final EventLogEntryRepository eventLogEntryRepository;

    public ClientService(ClientRepository clientRepository, UserAccountRepository userAccountRepository,
                          PostRepository postRepository, EventLogEntryRepository eventLogEntryRepository) {
        this.clientRepository = clientRepository;
        this.userAccountRepository = userAccountRepository;
        this.postRepository = postRepository;
        this.eventLogEntryRepository = eventLogEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientListItemDto> listClients(SecurityUser currentUser) {
        UUID tenantId = TenantContext.currentTenantId();
        LocalDate today = LocalDate.now();

        return visibleClients(currentUser, tenantId).stream()
                .map(client -> toListItem(client, today))
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientDetailDto getClient(SecurityUser currentUser, UUID clientId) {
        Client client = findVisibleOrThrow(currentUser, clientId);
        return toDetail(client, LocalDate.now());
    }

    @Transactional
    public ClientDetailDto createClient(SecurityUser currentUser, CreateClientRequest request) {
        UUID tenantId = TenantContext.currentTenantId();
        Client client = new Client(
                tenantId, request.name(), request.niche(), request.assigneeId(), request.status(),
                request.tariffPlan(), request.cooperationStartDate(), request.serviceCost(),
                request.contactName(), request.contactRole(), request.contactPhone(), request.contactEmail(),
                request.stage() != null ? request.stage() : ClientStage.BRIEF);
        clientRepository.save(client);
        return toDetail(client, LocalDate.now());
    }

    @Transactional
    public ClientDetailDto updateClient(SecurityUser currentUser, UUID clientId, UpdateClientRequest request) {
        Client client = findVisibleOrThrow(currentUser, clientId);
        client.update(
                request.name(), request.niche(), request.assigneeId(), request.status(), request.tariffPlan(),
                request.cooperationStartDate(), request.serviceCost(), request.contactName(),
                request.contactRole(), request.contactPhone(), request.contactEmail(), request.stage());
        return toDetail(client, LocalDate.now());
    }

    @Transactional
    public ClientDetailDto updateClientStatus(SecurityUser currentUser, UUID clientId,
                                               UpdateClientStatusRequest request) {
        Client client = findVisibleOrThrow(currentUser, clientId);
        client.updateStatus(request.status());
        return toDetail(client, LocalDate.now());
    }

    @Transactional
    public void deleteClient(SecurityUser currentUser, UUID clientId) {
        if (!RoleAccessPolicy.forRole(currentUser.getRole()).canDelete()) {
            throw ApiException.forbidden("Role is not allowed to delete clients");
        }
        Client client = findVisibleOrThrow(currentUser, clientId);
        clientRepository.delete(client);
    }

    private List<Client> visibleClients(SecurityUser currentUser, UUID tenantId) {
        if (RoleAccessPolicy.forRole(currentUser.getRole()).allClients()) {
            return clientRepository.findByOwnerId(tenantId);
        }
        // FR-05 acceptance: "SMM-спеціаліст бачить у списку лише прикріплених до нього клієнтів."
        return clientRepository.findByOwnerIdAndAssigneeId(tenantId, currentUser.getUserId());
    }

    private Client findVisibleOrThrow(SecurityUser currentUser, UUID clientId) {
        UUID tenantId = TenantContext.currentTenantId();
        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getOwnerId().equals(tenantId))
                .orElseThrow(() -> ApiException.notFound("Client not found"));

        boolean visible = RoleAccessPolicy.forRole(currentUser.getRole()).allClients()
                || currentUser.getUserId().equals(client.getAssigneeId());
        if (!visible) {
            // 404, not 403 — don't reveal that a client outside this role's assignment exists.
            throw ApiException.notFound("Client not found");
        }
        return client;
    }

    private ClientListItemDto toListItem(Client client, LocalDate today) {
        UserAccount assignee = resolveAssignee(client.getAssigneeId());
        return new ClientListItemDto(
                client.getId().toString(),
                client.getName(),
                client.getNiche(),
                assignee != null ? assignee.getFullName() : null,
                assignee != null ? initials(assignee.getFullName()) : null,
                client.getStatus().name(),
                nextPostLabel(client, today),
                client.getServiceCost(),
                lastActivityLabel(client));
    }

    private ClientDetailDto toDetail(Client client, LocalDate today) {
        UserAccount assignee = resolveAssignee(client.getAssigneeId());
        List<ActivityEntryDto> activity = eventLogEntryRepository
                .findByOwnerIdAndClientIdOrderByOccurredAtDesc(client.getOwnerId(), client.getId(), PageRequest.of(0, 10))
                .stream()
                .map(entry -> new ActivityEntryDto(
                        RelativeTimeFormatter.format(entry.getOccurredAt(), Instant.now()),
                        entry.getActorInitials(),
                        entry.getActorName(),
                        entry.getDescription()))
                .toList();

        return new ClientDetailDto(
                client.getId().toString(),
                client.getName(),
                client.getNiche(),
                assignee != null ? assignee.getFullName() : null,
                client.getStatus().name(),
                client.getServiceCost(),
                CooperationDurationFormatter.format(client.getCooperationStartDate(), client.getStatus(), today),
                client.getContactName(),
                client.getContactRole(),
                client.getContactPhone(),
                client.getContactEmail(),
                client.getStage().name(),
                activity);
    }

    private UserAccount resolveAssignee(UUID assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return userAccountRepository.findById(assigneeId).orElse(null);
    }

    private String nextPostLabel(Client client, LocalDate today) {
        Optional<Post> nextPost = postRepository
                .findFirstByOwnerIdAndClientIdAndScheduledDateGreaterThanEqualOrderByScheduledDateAsc(
                        client.getOwnerId(), client.getId(), today);
        return nextPost
                .map(post -> WEEKDAY_LABELS.getOrDefault(post.getScheduledDate().getDayOfWeek(), "")
                        + ", " + post.getTitle())
                .orElse("Не заплановано");
    }

    private String lastActivityLabel(Client client) {
        List<EventLogEntry> latest = eventLogEntryRepository
                .findByOwnerIdAndClientIdOrderByOccurredAtDesc(client.getOwnerId(), client.getId(), PageRequest.of(0, 1));
        if (latest.isEmpty()) {
            return "—";
        }
        return RelativeTimeFormatter.format(latest.get(0).getOccurredAt(), Instant.now());
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
            }
            if (result.length() >= 2) {
                break;
            }
        }
        return result.toString();
    }
}
