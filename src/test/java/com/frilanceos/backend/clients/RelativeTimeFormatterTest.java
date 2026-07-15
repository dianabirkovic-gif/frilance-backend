package com.frilanceos.backend.clients;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class RelativeTimeFormatterTest {

    private static final Instant NOW = Instant.parse("2026-07-14T12:00:00Z");

    @Test
    void justUnderFiveMinutesIsJustNow() {
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofMinutes(4)), NOW)).isEqualTo("Щойно");
    }

    @Test
    void minutesUsePluralForm() {
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofMinutes(22)), NOW)).isEqualTo("22 хвилини тому");
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofMinutes(21)), NOW)).isEqualTo("21 хвилину тому");
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofMinutes(45)), NOW)).isEqualTo("45 хвилин тому");
    }

    @Test
    void sameCalendarDayHoursAgo() {
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofHours(2)), NOW)).isEqualTo("2 години тому");
    }

    @Test
    void previousCalendarDayIsYesterday() {
        Instant lateLastNight = NOW.minus(Duration.ofHours(15));
        assertThat(RelativeTimeFormatter.format(lateLastNight, NOW)).isEqualTo("Вчора");
    }

    @Test
    void daysUsePluralForm() {
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofDays(3)), NOW)).isEqualTo("3 дні тому");
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofDays(5)), NOW)).isEqualTo("5 днів тому");
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofDays(11)), NOW)).isEqualTo("11 днів тому");
    }

    @Test
    void monthsAgo() {
        assertThat(RelativeTimeFormatter.format(NOW.minus(Duration.ofDays(62)), NOW)).isEqualTo("2 міс тому");
    }
}
