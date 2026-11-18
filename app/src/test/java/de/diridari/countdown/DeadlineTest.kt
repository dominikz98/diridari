package de.diridari.countdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DeadlineTest {

    @Test
    fun easterSunday() {
        assertEquals(LocalDate.of(2024, 3, 31), BankCalendar.easterSunday(2024))
        assertEquals(LocalDate.of(2025, 4, 20), BankCalendar.easterSunday(2025))
        assertEquals(LocalDate.of(2026, 4, 5), BankCalendar.easterSunday(2026))
        assertEquals(LocalDate.of(2027, 3, 28), BankCalendar.easterSunday(2027))
    }

    @Test
    fun bankWorkingDays() {
        assertFalse(BankCalendar.isBankWorkingDay(LocalDate.of(2026, 6, 4)))   // Fronleichnam
        assertFalse(BankCalendar.isBankWorkingDay(LocalDate.of(2026, 12, 24))) // Heiligabend
        assertFalse(BankCalendar.isBankWorkingDay(LocalDate.of(2026, 11, 21))) // Samstag
        assertTrue(BankCalendar.isBankWorkingDay(LocalDate.of(2026, 11, 18)))  // Buß- und Bettag
    }

    @Test
    fun novemberExample() {
        assertEquals(LocalDate.of(2026, 11, 25), Deadline.forMonth(YearMonth.of(2026, 11)))
        assertEquals(LocalDate.of(2025, 11, 25), Deadline.forMonth(YearMonth.of(2025, 11)))
    }

    @Test
    fun holidaysAroundMonthEnd() {
        // 1.5.2026 ist Feiertag, zählt aber nicht zwischen den Tagen: 28., 29., 30.4.
        assertEquals(LocalDate.of(2026, 4, 27), Deadline.forMonth(YearMonth.of(2026, 4)))
        // Dezember 2025: 24., 25., 26. und 31.12. geschlossen -> 23., 29., 30.12. dazwischen
        assertEquals(LocalDate.of(2025, 12, 22), Deadline.forMonth(YearMonth.of(2025, 12)))
        assertEquals(LocalDate.of(2026, 12, 23), Deadline.forMonth(YearMonth.of(2026, 12)))
    }

    @Test
    fun nextRollsOverAfterDeadline() {
        assertEquals(7L, Deadline.daysUntilNext(LocalDate.of(2026, 11, 18)))
        assertEquals(0L, Deadline.daysUntilNext(LocalDate.of(2026, 11, 25)))
        assertEquals(LocalDate.of(2026, 12, 23), Deadline.next(LocalDate.of(2026, 11, 26)))
    }
}
