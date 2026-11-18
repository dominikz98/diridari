package de.diridari.countdown

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MIDNIGHT,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> updateAll(context)
            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { manager.updateAppWidget(it, buildViews(context)) }
        scheduleMidnightUpdate(context)
    }

    override fun onDisabled(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(midnightIntent(context))
    }

    companion object {
        private const val ACTION_MIDNIGHT = "de.diridari.countdown.MIDNIGHT"
        private val dateFormat = DateTimeFormatter.ofPattern("EE, dd.MM.yyyy", Locale.GERMAN)

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CountdownWidgetProvider::class.java))
            if (ids.isNotEmpty()) {
                ids.forEach { manager.updateAppWidget(it, buildViews(context)) }
                scheduleMidnightUpdate(context)
            }
        }

        private fun buildViews(context: Context): RemoteViews {
            val today = LocalDate.now()
            val target = Deadline.next(today)
            val days = Deadline.daysUntilNext(today)

            return RemoteViews(context.packageName, R.layout.widget_countdown).apply {
                setTextViewText(R.id.days, if (days == 0L) context.getString(R.string.today) else days.toString())
                setTextViewText(
                    R.id.unit,
                    when (days) {
                        0L -> ""
                        1L -> context.getString(R.string.day)
                        else -> context.getString(R.string.days)
                    }
                )
                setTextViewText(R.id.date, target.format(dateFormat))
                setOnClickPendingIntent(
                    R.id.root,
                    PendingIntent.getActivity(
                        context, 0, Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }
        }

        /** Inexakter Alarm kurz nach Mitternacht; braucht keine Exact-Alarm-Berechtigung. */
        private fun scheduleMidnightUpdate(context: Context) {
            val zone = ZoneId.systemDefault()
            val nextMidnight = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).plusMinutes(1)
            context.getSystemService(AlarmManager::class.java).set(
                AlarmManager.RTC,
                nextMidnight.toInstant().toEpochMilli(),
                midnightIntent(context)
            )
        }

        private fun midnightIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context, 0,
                Intent(context, CountdownWidgetProvider::class.java).setAction(ACTION_MIDNIGHT),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
    }
}
