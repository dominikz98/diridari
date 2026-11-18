package de.diridari.countdown

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CountdownTest {

    private val salary = Countdown("Gehalt", CountdownType.SALARY, CountdownShape.CIRCLE)

    @Test
    fun salaryRingRunsFromPreviousDeadline() {
        // 27.10.2026 -> 25.11.2026 = 29 Tage
        val today = LocalDate.of(2026, 11, 18)
        assertEquals(7L, salary.daysLeft(today))
        assertEquals(7f / 29f, salary.remainingFraction(today), 1e-6f)
        assertEquals(0f, salary.remainingFraction(LocalDate.of(2026, 11, 25)), 0f)
        // Tag nach dem Stichtag: Ring fast voll für den nächsten Monat
        assertEquals(27f / 28f, salary.remainingFraction(LocalDate.of(2026, 11, 26)), 1e-6f)
    }

    @Test
    fun dateCountdown() {
        val trip = Countdown(
            "Urlaub", CountdownType.DATE, CountdownShape.PAUL,
            date = LocalDate.of(2026, 12, 18), createdAt = LocalDate.of(2026, 11, 18)
        )
        assertEquals(1f, trip.remainingFraction(LocalDate.of(2026, 11, 18)), 0f)
        assertEquals(0.5f, trip.remainingFraction(LocalDate.of(2026, 12, 3)), 1e-6f)
        assertEquals(0L, trip.daysLeft(LocalDate.of(2026, 12, 18)))
        assertEquals(-2L, trip.daysLeft(LocalDate.of(2026, 12, 20)))
        assertEquals(0f, trip.remainingFraction(LocalDate.of(2026, 12, 20)), 0f)
    }
}
