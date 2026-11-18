package de.diridari.countdown

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay

/**
 * Bankarbeitstage in Bayern.
 *
 * Ein Bankarbeitstag ist Montag bis Freitag, außer an gesetzlichen Feiertagen in Bayern
 * und an den Tagen, an denen Banken zusätzlich geschlossen haben (24.12. und 31.12.).
 *
 * Mariä Himmelfahrt (15.08.) ist nur in Gemeinden mit überwiegend katholischer Bevölkerung
 * Feiertag (z.B. München); er wird hier als Feiertag gezählt.
 * Buß- und Bettag und das Augsburger Friedensfest zählen nicht.
 */
object BankCalendar {

    private val fixedClosedDays = setOf(
        MonthDay.of(1, 1),   // Neujahr
        MonthDay.of(1, 6),   // Heilige Drei Könige
        MonthDay.of(5, 1),   // Tag der Arbeit
        MonthDay.of(8, 15),  // Mariä Himmelfahrt
        MonthDay.of(10, 3),  // Tag der Deutschen Einheit
        MonthDay.of(11, 1),  // Allerheiligen
        MonthDay.of(12, 24), // Heiligabend (Banken geschlossen)
        MonthDay.of(12, 25), // 1. Weihnachtstag
        MonthDay.of(12, 26), // 2. Weihnachtstag
        MonthDay.of(12, 31), // Silvester (Banken geschlossen)
    )

    /** Offsets in Tagen relativ zum Ostersonntag. */
    private val easterOffsets = listOf(
        -2L, // Karfreitag
        1L,  // Ostermontag
        39L, // Christi Himmelfahrt
        50L, // Pfingstmontag
        60L, // Fronleichnam
    )

    fun isBankWorkingDay(date: LocalDate): Boolean {
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) return false
        if (MonthDay.from(date) in fixedClosedDays) return false
        val easter = easterSunday(date.year)
        return easterOffsets.none { easter.plusDays(it) == date }
    }

    /** Ostersonntag nach der Gauß'schen Osterformel (gregorianischer Kalender). */
    fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }
}
