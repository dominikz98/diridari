package de.diridari.countdown

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Stichtag eines Monats: der Bankarbeitstag, nach dem noch genau [BANK_DAYS_BEFORE]
 * Bankarbeitstage bis zum 1. des Folgemonats liegen.
 *
 * Beispiel November 2026 (1.12. ist ein Dienstag): dazwischen liegen 26.11., 27.11. und 30.11.,
 * also ist der Stichtag Mittwoch, der 25.11.
 */
object Deadline {

    const val BANK_DAYS_BEFORE = 3

    fun forMonth(month: YearMonth): LocalDate {
        var date = month.plusMonths(1).atDay(1)
        var bankDaysBetween = 0
        while (true) {
            date = date.minusDays(1)
            if (!BankCalendar.isBankWorkingDay(date)) continue
            if (bankDaysBetween == BANK_DAYS_BEFORE) return date
            bankDaysBetween++
        }
    }

    /** Nächster Stichtag ab [today] (inklusive heute). */
    fun next(today: LocalDate): LocalDate {
        val thisMonth = forMonth(YearMonth.from(today))
        return if (!today.isAfter(thisMonth)) thisMonth else forMonth(YearMonth.from(today).plusMonths(1))
    }

    fun daysUntilNext(today: LocalDate): Long = ChronoUnit.DAYS.between(today, next(today))
}
