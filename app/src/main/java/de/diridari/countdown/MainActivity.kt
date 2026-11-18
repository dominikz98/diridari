package de.diridari.countdown

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Zeigt den nächsten Stichtag und die Stichtage der kommenden zwölf Monate. */
class MainActivity : Activity() {

    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        val today = LocalDate.now()
        val days = Deadline.daysUntilNext(today)

        findViewById<TextView>(R.id.summary).text = getString(
            R.string.summary, days, Deadline.next(today).format(dateFormat)
        )

        val start = YearMonth.from(Deadline.next(today))
        findViewById<TextView>(R.id.upcoming).text = (0L until 12L).joinToString("\n") {
            Deadline.forMonth(start.plusMonths(it)).format(dateFormat)
        }

        CountdownWidgetProvider.updateAll(this)
    }
}
