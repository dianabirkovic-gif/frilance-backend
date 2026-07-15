package com.frilanceos.backend.clients;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class CooperationDurationFormatterTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 14);

    @Test
    void archivedClientAlwaysReadsDone() {
        assertThat(CooperationDurationFormatter.format(TODAY.minusMonths(3), ClientStatus.ARCHIVED, TODAY))
                .isEqualTo("завершено");
    }

    @Test
    void missingStartDateIsUnknown() {
        assertThat(CooperationDurationFormatter.format(null, ClientStatus.NEW, TODAY)).isEqualTo("—");
    }

    @Test
    void fewDaysUsesDayPluralForm() {
        assertThat(CooperationDurationFormatter.format(TODAY.minusDays(2), ClientStatus.NEW, TODAY))
                .isEqualTo("2 дні");
    }

    @Test
    void atLeastThirtyDaysSwitchesToMonths() {
        assertThat(CooperationDurationFormatter.format(TODAY.minusMonths(3), ClientStatus.ACTIVE, TODAY))
                .isEqualTo("3 міс");
        assertThat(CooperationDurationFormatter.format(TODAY.minusMonths(8), ClientStatus.ACTIVE, TODAY))
                .isEqualTo("8 міс");
    }
}
