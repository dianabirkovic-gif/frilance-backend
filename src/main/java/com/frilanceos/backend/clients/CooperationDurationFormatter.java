package com.frilanceos.backend.clients;

import java.time.LocalDate;
import java.time.Period;

/**
 * "У співпраці" label on the client card. Pure function, no Spring —
 * unit-tested directly.
 */
public final class CooperationDurationFormatter {

    private CooperationDurationFormatter() {
    }

    public static String format(LocalDate cooperationStartDate, ClientStatus status, LocalDate today) {
        if (status == ClientStatus.ARCHIVED) {
            return "завершено";
        }
        if (cooperationStartDate == null) {
            return "—";
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(cooperationStartDate, today);
        if (days < 30) {
            return days + " " + pluralizeDay(days);
        }

        Period period = Period.between(cooperationStartDate, today);
        int months = Math.max(1, period.getYears() * 12 + period.getMonths());
        return months + " міс";
    }

    private static String pluralizeDay(long count) {
        long mod100 = count % 100;
        long mod10 = count % 10;
        if (mod100 >= 11 && mod100 <= 14) {
            return "днів";
        }
        if (mod10 == 1) {
            return "день";
        }
        if (mod10 >= 2 && mod10 <= 4) {
            return "дні";
        }
        return "днів";
    }
}
