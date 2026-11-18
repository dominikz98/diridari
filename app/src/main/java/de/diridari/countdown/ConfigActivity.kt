package de.diridari.countdown

import android.app.Activity
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Anlegen und Bearbeiten eines Countdowns.
 *
 * - Mit Widget-ID (vom Launcher beim Platzieren oder per Tipp aufs Widget): bearbeitet dieses Widget.
 * - Ohne Widget-ID (aus der App): legt einen neuen Countdown an und pinnt ein Widget auf den Homescreen.
 */
class ConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var original: Countdown? = null

    private var type = CountdownType.SALARY
    private var shape = CountdownShape.CIRCLE
    private var date: LocalDate? = null

    private lateinit var nameView: EditText
    private lateinit var pickDate: Button
    private lateinit var preview: ImageView

    private val dateFormat = DateTimeFormatter.ofPattern("EE, dd.MM.yyyy", Locale.GERMAN)

    private val shapeIds = mapOf(
        CountdownShape.CIRCLE to R.id.shapeCircle,
        CountdownShape.TRIANGLE to R.id.shapeTriangle,
        CountdownShape.SQUARE to R.id.shapeSquare,
        CountdownShape.PAUL to R.id.shapePaul,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_config)

        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        original = if (isWidget()) CountdownStore.load(this, widgetId) else null
        val start = original ?: Countdown.DEFAULT
        type = start.type
        shape = start.shape
        date = start.date

        nameView = findViewById(R.id.name)
        pickDate = findViewById(R.id.pickDate)
        preview = findViewById(R.id.preview)

        nameView.setText(start.name)
        nameView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) = refresh()
        })

        findViewById<RadioGroup>(R.id.typeGroup).apply {
            check(if (type == CountdownType.SALARY) R.id.typeSalary else R.id.typeDate)
            setOnCheckedChangeListener { _, checked ->
                type = if (checked == R.id.typeSalary) CountdownType.SALARY else CountdownType.DATE
                val name = nameView.text.toString().trim()
                if (type == CountdownType.SALARY && name.isEmpty()) nameView.setText(Countdown.SALARY_NAME)
                if (type == CountdownType.DATE && name == Countdown.SALARY_NAME) nameView.setText("")
                if (type == CountdownType.DATE && date == null) showDatePicker()
                refresh()
            }
        }

        findViewById<RadioGroup>(R.id.shapeGroup).apply {
            check(shapeIds.getValue(shape))
            setOnCheckedChangeListener { _, checked ->
                shape = shapeIds.entries.first { it.value == checked }.key
                refresh()
            }
        }

        pickDate.setOnClickListener { showDatePicker() }
        findViewById<Button>(R.id.save).setOnClickListener { save() }
        refresh()
    }

    private fun isWidget() = widgetId != AppWidgetManager.INVALID_APPWIDGET_ID

    private fun showDatePicker() {
        val initial = date ?: LocalDate.now().plusDays(7)
        DatePickerDialog(this, { _, y, m, d ->
            date = LocalDate.of(y, m + 1, d)
            refresh()
        }, initial.year, initial.monthValue - 1, initial.dayOfMonth).apply {
            datePicker.minDate = LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.show()
    }

    private fun current(): Countdown {
        val name = nameView.text.toString().trim()
            .ifEmpty { if (type == CountdownType.SALARY) Countdown.SALARY_NAME else "" }
        val countdownDate = if (type == CountdownType.DATE) date else null
        val prev = original
        // Ladering beginnt neu, wenn sich das Ziel ändert.
        val createdAt = if (prev != null && prev.type == type && prev.date == countdownDate) prev.createdAt else LocalDate.now()
        return Countdown(name, type, shape, countdownDate, createdAt)
    }

    private fun refresh() {
        pickDate.isEnabled = type == CountdownType.DATE
        pickDate.text = if (type == CountdownType.DATE && date != null) date!!.format(dateFormat) else getString(R.string.pick_date)

        val px = (160 * resources.displayMetrics.density).toInt()
        preview.setImageBitmap(ShapeRenderer.render(current(), LocalDate.now(), px, px, 160f))
    }

    private fun save() {
        if (type == CountdownType.DATE && date == null) {
            Toast.makeText(this, R.string.error_no_date, Toast.LENGTH_SHORT).show()
            return
        }
        val countdown = current()

        if (isWidget()) {
            CountdownStore.save(this, widgetId, countdown)
            CountdownWidgetProvider.update(this, AppWidgetManager.getInstance(this), widgetId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
            finish()
            return
        }

        val manager = AppWidgetManager.getInstance(this)
        if (!manager.isRequestPinAppWidgetSupported) {
            Toast.makeText(this, R.string.error_no_pin, Toast.LENGTH_LONG).show()
            return
        }
        val key = System.currentTimeMillis().toString()
        CountdownStore.savePending(this, key, countdown)
        val callback = PendingIntent.getBroadcast(
            this, key.hashCode(),
            Intent(this, CountdownWidgetProvider::class.java)
                .setAction(CountdownWidgetProvider.ACTION_PINNED)
                .putExtra(CountdownWidgetProvider.EXTRA_PENDING_KEY, key),
            // Mutable, weil das System die Widget-ID ergänzt.
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        manager.requestPinAppWidget(ComponentName(this, CountdownWidgetProvider::class.java), null, callback)
        finish()
    }
}
