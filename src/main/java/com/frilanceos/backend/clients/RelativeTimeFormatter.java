package com.frilanceos.backend.clients;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

/**
 * Ukrainian relative-time labels for the client card's "Активність"
 * column/activity feed ("Щойно", "X хв тому", ...). Pure function, no Spring —
 * unit-tested directly with a fixed {@code now}.
 */
public final class RelativeTimeFormatter {

    private RelativeTimeFormatter() {
    }

    public static String format(Instant occurredAt, Instant now) {
        Duration elapsed = Duration.between(occurredAt, now);
        long minutes = elapsed.toMinutes();

        if (minutes < 5) {
            return "Щойно";
        }
        if (minutes < 60) {
            return minutes + " " + pluralize(minutes, "хвилину", "хвилини", "хвилин") + " тому";
        }
        long hours = elapsed.toHours();
        if (hours < 24 && isSameCalendarDay(occurredAt, now)) {
            return hours + " " + pluralize(hours, "годину", "години", "годин") + " тому";
        }

        long calendarDaysBetween = ChronoUnit.DAYS.between(
                occurredAt.atZone(ZoneOffset.UTC).toLocalDate(), now.atZone(ZoneOffset.UTC).toLocalDate());
        if (calendarDaysBetween == 1) {
            return "Вчора";
        }
        long days = elapsed.toDays();
        if (days < 30) {
            return days + " " + pluralize(days, "день", "дні", "днів") + " тому";
        }
        long months = days / 30;
        return months + " міс тому";
    }

    private static boolean isSameCalendarDay(Instant a, Instant b) {
        return a.atZone(ZoneOffset.UTC).toLocalDate().equals(b.atZone(ZoneOffset.UTC).toLocalDate());
    }

    /** Ukrainian plural rules: 1 -> singular, 2-4 -> few, 0/5+/11-14 -> many. */
    private static String pluralize(long count, String singular, String few, String many) {
        long mod100 = count % 100;
        long mod10 = count % 10;
        if (mod100 >= 11 && mod100 <= 14) {
            return many;
        }
        if (mod10 == 1) {
            return singular;
        }
        if (mod10 >= 2 && mod10 <= 4) {
            return few;
        }
        return many;
    }
}
