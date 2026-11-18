package de.diridari.countdown

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Übersicht der Countdowns und der nächsten Gehalt-Stichtage. */
class MainActivity : Activity() {

    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy", Locale.GERMAN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.add).setOnClickListener {
            startActivity(Intent(this, ConfigActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        CountdownWidgetProvider.updateAll(this)
        showCountdowns()
        showUpcomingSalaryDates()
    }

    private fun showCountdowns() {
        val today = LocalDate.now()
        val list = findViewById<LinearLayout>(R.id.list)
        list.removeAllViews()
        val ids = CountdownWidgetProvider.widgetIds(this)
        findViewById<TextView>(R.id.empty).visibility = if (ids.isEmpty()) View.VISIBLE else View.GONE

        val density = resources.displayMetrics.density
        val iconPx = (56 * density).toInt()
        ids.map { it to CountdownWidgetProvider.countdownFor(this, it) }
            .sortedBy { (_, c) -> c.target(today) }
            .forEach { (id, countdown) ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, (8 * density).toInt(), 0, (8 * density).toInt())
                    setOnClickListener {
                        startActivity(
                            Intent(this@MainActivity, ConfigActivity::class.java)
                                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                        )
                    }
                }
                row.addView(ImageView(this).apply {
                    setImageBitmap(ShapeRenderer.render(countdown, today, iconPx, iconPx, 56f))
                }, LinearLayout.LayoutParams(iconPx, iconPx))
                row.addView(TextView(this).apply {
                    text = getString(
                        R.string.row, countdown.name,
                        CountdownWidgetProvider.describe(this@MainActivity, countdown, today),
                        countdown.target(today).format(dateFormat)
                    )
                    textSize = 15f
                    setPadding((16 * density).toInt(), 0, 0, 0)
                })
                list.addView(row)
            }
    }

    private fun showUpcomingSalaryDates() {
        val start = YearMonth.from(Deadline.next(LocalDate.now()))
        findViewById<TextView>(R.id.upcoming).text = (0L until 12L).joinToString("\n") {
            Deadline.forMonth(start.plusMonths(it)).format(dateFormat)
        }
    }
}
