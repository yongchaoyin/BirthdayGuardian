package com.birthday.guardian.util;

import com.birthday.guardian.entity.BirthdayRole;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class BirthdayDateUtil {

    private BirthdayDateUtil() {
    }

    public static LocalDate resolveUpcomingBirthday(BirthdayRole role, LocalDate referenceDate) {
        LocalDate candidate;
        if (role.getCalendarType() == 1) {
            candidate = LocalDate.of(referenceDate.getYear(), role.getBirthDate().getMonth(), role.getBirthDate().getDayOfMonth());
        } else {
            String[] parts = role.getLunarBirthDate().split("-");
            int lunarMonth = Integer.parseInt(parts[1]);
            int lunarDay = Integer.parseInt(parts[2]);
            candidate = LunarUtil.lunarToSolar(referenceDate.getYear(), lunarMonth, lunarDay);
        }

        if (!candidate.isBefore(referenceDate)) {
            return candidate;
        }

        if (role.getCalendarType() == 1) {
            return candidate.plusYears(1);
        }

        String[] parts = role.getLunarBirthDate().split("-");
        int lunarMonth = Integer.parseInt(parts[1]);
        int lunarDay = Integer.parseInt(parts[2]);
        return LunarUtil.lunarToSolar(referenceDate.getYear() + 1, lunarMonth, lunarDay);
    }

    public static long daysUntil(BirthdayRole role, LocalDate referenceDate) {
        LocalDate upcoming = resolveUpcomingBirthday(role, referenceDate);
        return ChronoUnit.DAYS.between(referenceDate, upcoming);
    }
}
