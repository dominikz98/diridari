package de.diridari.countdown

import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

enum class CountdownType { SALARY, DATE }

enum class CountdownShape(val label: String) {
    CIRCLE("Kreis"),
    TRIANGLE("Dreieck"),
    SQUARE("Viereck"),
    PAUL("Paul"), // Herz
}

enum class CountdownTextSize(val scale: Float) {
    SMALL(0.8f),
    MEDIUM(1f),
    LARGE(1.25f),
}

/** Farben zur Auswahl pro Countdown; die ersten vier sind die Standardfarben der Formen. */
object CountdownColors {
    val PALETTE = listOf(
        0xFF1565C0.toInt(), // Blau
        0xFF2E7D32.toInt(), // Grün
        0xFFEF6C00.toInt(), // Orange
        0xFFD81B60.toInt(), // Pink
        0xFF6A1B9A.toInt(), // Lila
        0xFFC62828.toInt(), // Rot
        0xFF00838F.toInt(), // Türkis
        0xFF424242.toInt(), // Grau
    )
}

/**
 * Ein Countdown, wie er in einem Widget angezeigt wird.
 *
 * - [CountdownType.SALARY]: Stichtag nach der Gehalt-Regel ([Deadline]), läuft jeden Monat neu.
 * - [CountdownType.DATE]: festes [date]; der Ladering läuft von [createdAt] bis [date].
 */
data class Countdown(
    val name: String,
    val type: CountdownType,
    val shape: CountdownShape,
    val date: LocalDate? = null,
    val createdAt: LocalDate = LocalDate.now(),
    /** Füll- und Ringfarbe; `null` = Standardfarbe der Form. */
    val color: Int? = null,
    val textSize: CountdownTextSize = CountdownTextSize.MEDIUM,
) {
    fun target(today: LocalDate): LocalDate = when (type) {
        CountdownType.SALARY -> Deadline.next(today)
        CountdownType.DATE -> date ?: today
    }

    /** Tage bis zum Ziel; negativ, wenn ein festes Datum vorbei ist. */
    fun daysLeft(today: LocalDate): Long = ChronoUnit.DAYS.between(today, target(today))

    /** Anteil der verbleibenden Zeit (1 = gerade gestartet, 0 = Ziel erreicht). */
    fun remainingFraction(today: LocalDate): Float {
        val target = target(today)
        val start = when (type) {
            CountdownType.SALARY -> Deadline.forMonth(YearMonth.from(target).minusMonths(1))
            CountdownType.DATE -> createdAt
        }
        val total = ChronoUnit.DAYS.between(start, target)
        val left = ChronoUnit.DAYS.between(today, target)
        if (left <= 0) return 0f
        if (total <= 0) return 1f
        return (left.toFloat() / total).coerceIn(0f, 1f)
    }

    companion object {
        const val SALARY_NAME = "Gehalt"
        val DEFAULT = Countdown(SALARY_NAME, CountdownType.SALARY, CountdownShape.CIRCLE)
    }
}
