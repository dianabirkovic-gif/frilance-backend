package com.frilanceos.backend.dashboard;

import com.frilanceos.backend.auth.RoleAccessPolicy;
import com.frilanceos.backend.auth.Role;
import com.frilanceos.backend.clients.Client;
import com.frilanceos.backend.clients.ClientRepository;
import com.frilanceos.backend.clients.ClientStatus;
import com.frilanceos.backend.clients.RelativeTimeFormatter;
import com.frilanceos.backend.common.security.SecurityUser;
import com.frilanceos.backend.common.tenant.TenantContext;
import com.frilanceos.backend.contentplan.PostRepository;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse.AttentionItemDto;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse.ContentPlanDayDto;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse.FinanceRowDto;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse.FinanceSummary;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse.LedgerEntryDto;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse.Revenue;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse.RevenuePoint;
import com.frilanceos.backend.dashboard.dto.DashboardOverviewResponse.TeamMemberDto;
import com.frilanceos.backend.finance.FinanceEntry;
import com.frilanceos.backend.finance.FinanceEntryRepository;
import com.frilanceos.backend.finance.FinanceEntryType;
import com.frilanceos.backend.finance.MonthlyGoal;
import com.frilanceos.backend.finance.MonthlyGoalRepository;
import com.frilanceos.backend.ledger.EventLogEntry;
import com.frilanceos.backend.ledger.EventLogEntryRepository;
import com.frilanceos.backend.team.TeamMemberRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates the read models behind the Overview screen (dashboard.html).
 * Every query is scoped to {@code TenantContext.currentTenantId()} — see
 * this repo's CLAUDE.md "Tenant isolation" section before adding a query
 * here that doesn't filter by owner id.
 */
@Service
public class DashboardService {

    private static final Locale UK = Locale.forLanguageTag("uk-UA");
    private static final Map<DayOfWeek, String> WEEKDAY_LABELS = Map.of(
            DayOfWeek.MONDAY, "Пн",
            DayOfWeek.TUESDAY, "Вт",
            DayOfWeek.WEDNESDAY, "Ср",
            DayOfWeek.THURSDAY, "Чт",
            DayOfWeek.FRIDAY, "Пт");

    private final ClientRepository clientRepository;
    private final PostRepository postRepository;
    private final FinanceEntryRepository financeEntryRepository;
    private final MonthlyGoalRepository monthlyGoalRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final EventLogEntryRepository eventLogEntryRepository;

    public DashboardService(ClientRepository clientRepository,
                             PostRepository postRepository,
                             FinanceEntryRepository financeEntryRepository,
                             MonthlyGoalRepository monthlyGoalRepository,
                             TeamMemberRepository teamMemberRepository,
                             EventLogEntryRepository eventLogEntryRepository) {
        this.clientRepository = clientRepository;
        this.postRepository = postRepository;
        this.financeEntryRepository = financeEntryRepository;
        this.monthlyGoalRepository = monthlyGoalRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.eventLogEntryRepository = eventLogEntryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverview(SecurityUser currentUser) {
        UUID tenantId = TenantContext.currentTenantId();
        LocalDate today = LocalDate.now();

        return new DashboardOverviewResponse(
                formatDateLabel(today),
                greeting(currentUser),
                buildRevenue(tenantId, today),
                buildAttentionItems(tenantId),
                buildContentPlanWeek(tenantId, today),
                buildFinanceSummary(tenantId, today, currentUser.getRole()),
                buildTeamWorkload(tenantId),
                buildEventLedger(tenantId));
    }

    private String formatDateLabel(LocalDate date) {
        String weekday = date.getDayOfWeek().getDisplayName(TextStyle.FULL, UK);
        String formatted = date.format(DateTimeFormatter.ofPattern("d MMMM", UK));
        return capitalize(weekday) + ", " + formatted;
    }

    private String capitalize(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String greeting(SecurityUser currentUser) {
        return "Гарного дня!";
    }

    private Revenue buildRevenue(UUID tenantId, LocalDate today) {
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        BigDecimal currentTotal = sumIncome(tenantId, currentMonth);
        BigDecimal previousTotal = sumIncome(tenantId, previousMonth);

        double deltaPercent = previousTotal.signum() == 0
                ? 0.0
                : currentTotal.subtract(previousTotal)
                        .divide(previousTotal, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();

        List<RevenuePoint> series = buildCumulativeSeries(tenantId, currentMonth);

        return new Revenue(currentTotal, "UAH", deltaPercent, series);
    }

    private BigDecimal sumIncome(UUID tenantId, YearMonth month) {
        return financeEntryRepository
                .findByOwnerIdAndEntryDateBetween(tenantId, month.atDay(1), month.atEndOfMonth())
                .stream()
                .filter(entry -> entry.getType() == FinanceEntryType.INCOME)
                .map(FinanceEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<RevenuePoint> buildCumulativeSeries(UUID tenantId, YearMonth month) {
        List<FinanceEntry> incomeEntries = financeEntryRepository
                .findByOwnerIdAndEntryDateBetween(tenantId, month.atDay(1), month.atEndOfMonth())
                .stream()
                .filter(entry -> entry.getType() == FinanceEntryType.INCOME)
                .sorted((a, b) -> a.getEntryDate().compareTo(b.getEntryDate()))
                .toList();

        List<RevenuePoint> series = new ArrayList<>();
        BigDecimal running = BigDecimal.ZERO;
        for (FinanceEntry entry : incomeEntries) {
            running = running.add(entry.getAmount());
            series.add(new RevenuePoint(entry.getEntryDate(), running));
        }
        return series;
    }

    /**
     * FR-05 replaces the old directly-seeded {@code attention_item} table:
     * "needs attention" is now a real signal computed from {@code Client.status}
     * rather than placeholder data (see backend CLAUDE.md "Known simplifications").
     * Every client's status here has equal weight, so severity doesn't
     * distinguish HIGH/MID the way the placeholder data used to — that
     * gradation would need the richer per-client signal (unpaid invoice,
     * silent lead, etc.) the same CLAUDE.md note describes as future work.
     */
    private List<AttentionItemDto> buildAttentionItems(UUID tenantId) {
        return clientRepository.findByOwnerIdAndStatusOrderByNameAsc(tenantId, ClientStatus.ATTENTION).stream()
                .limit(4)
                .map(client -> {
                    Optional<EventLogEntry> latestEvent = latestEvent(client);
                    return new AttentionItemDto(
                            "MID",
                            client.getName(),
                            latestEvent.map(EventLogEntry::getDescription).orElse("—"),
                            latestEvent.map(entry -> RelativeTimeFormatter.format(entry.getOccurredAt(), Instant.now()))
                                    .orElse("—"),
                            true);
                })
                .toList();
    }

    private Optional<EventLogEntry> latestEvent(Client client) {
        return eventLogEntryRepository
                .findByOwnerIdAndClientIdOrderByOccurredAtDesc(client.getOwnerId(), client.getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    private List<ContentPlanDayDto> buildContentPlanWeek(UUID tenantId, LocalDate today) {
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDate friday = monday.plusDays(4);

        return postRepository
                .findByOwnerIdAndScheduledDateBetweenOrderByScheduledDateAsc(tenantId, monday, friday)
                .stream()
                .map(post -> new ContentPlanDayDto(
                        WEEKDAY_LABELS.getOrDefault(post.getScheduledDate().getDayOfWeek(), ""),
                        post.getScheduledDate(),
                        post.getClientName() + " — " + post.getTitle(),
                        post.getStatus().name()))
                .toList();
    }

    private FinanceSummary buildFinanceSummary(UUID tenantId, LocalDate today, Role role) {
        if (!RoleAccessPolicy.forRole(role).financeAccess()) {
            // FR-09: SMM/Targetolog see only their own payout, never the agency-wide picture.
            return new FinanceSummary(List.of());
        }

        YearMonth month = YearMonth.from(today);
        List<FinanceEntry> entries = financeEntryRepository
                .findByOwnerIdAndEntryDateBetween(tenantId, month.atDay(1), month.atEndOfMonth());

        BigDecimal income = sumByType(entries, FinanceEntryType.INCOME);
        BigDecimal teamExpenses = sumByType(entries, FinanceEntryType.PAYOUT);
        BigDecimal taxReserve = sumByType(entries, FinanceEntryType.TAX);

        MonthlyGoal goal = monthlyGoalRepository.findByOwnerIdAndGoalMonth(tenantId, month.toString())
                .orElse(null);
        BigDecimal revenueGoal = goal != null ? goal.getRevenueGoal() : BigDecimal.ZERO;
        BigDecimal taxReserveGoal = goal != null ? goal.getTaxReserveGoal() : BigDecimal.ZERO;

        List<FinanceRowDto> rows = List.of(
                new FinanceRowDto("Дохід", income, percentOf(income, revenueGoal), "brand"),
                new FinanceRowDto("Витрати команди", teamExpenses, percentOf(teamExpenses, income), "info"),
                new FinanceRowDto("Резерв на податки", taxReserve, percentOf(taxReserve, taxReserveGoal), "gold"));

        return new FinanceSummary(rows);
    }

    private BigDecimal sumByType(List<FinanceEntry> entries, FinanceEntryType type) {
        return entries.stream()
                .filter(entry -> entry.getType() == type)
                .map(FinanceEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int percentOf(BigDecimal value, BigDecimal ofGoal) {
        if (ofGoal == null || ofGoal.signum() <= 0) {
            return 0;
        }
        int percent = value.divide(ofGoal, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
        return Math.min(100, Math.max(0, percent));
    }

    private List<TeamMemberDto> buildTeamWorkload(UUID tenantId) {
        return teamMemberRepository.findByOwnerIdOrderByLoadPercentDesc(tenantId).stream()
                .limit(4)
                .map(member -> new TeamMemberDto(
                        member.getName(), member.getRole().name(), member.getLoadPercent(), member.getClientCount()))
                .toList();
    }

    private List<LedgerEntryDto> buildEventLedger(UUID tenantId) {
        return eventLogEntryRepository
                .findByOwnerIdOrderByOccurredAtDesc(tenantId, PageRequest.of(0, 10))
                .stream()
                .map(entry -> new LedgerEntryDto(
                        entry.getOccurredAt().atZone(ZoneOffset.UTC).toLocalTime()
                                .format(DateTimeFormatter.ofPattern("HH:mm")),
                        entry.getActorInitials(),
                        entry.getActorName(),
                        entry.getDescription(),
                        entry.getTag().name(),
                        entry.getAmount(),
                        entry.getAmount() == null ? "NONE" : entry.getAmount().signum() >= 0 ? "IN" : "OUT"))
                .toList();
    }
}