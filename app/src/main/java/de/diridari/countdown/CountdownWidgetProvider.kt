package de.diridari.countdown

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.min
import kotlin.math.roundToInt

class CountdownWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PINNED -> {
                val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                val key = intent.getStringExtra(EXTRA_PENDING_KEY)
                if (id != AppWidgetManager.INVALID_APPWIDGET_ID && key != null) {
                    CountdownStore.claimPending(context, key, id)
                    update(context, AppWidgetManager.getInstance(context), id)
                }
            }

            ACTION_MIDNIGHT,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED -> updateAll(context)

            else -> super.onReceive(context, intent)
        }
    }

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { update(context, manager, it) }
        scheduleMidnightUpdate(context)
    }

    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) {
        update(context, manager, id)
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        ids.forEach { CountdownStore.delete(context, it) }
    }

    override fun onDisabled(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(midnightIntent(context))
    }

    companion object {
        const val ACTION_PINNED = "de.diridari.countdown.PINNED"
        const val EXTRA_PENDING_KEY = "pending_key"
        private const val ACTION_MIDNIGHT = "de.diridari.countdown.MIDNIGHT"
        private const val MAX_BITMAP_PX = 480

        fun widgetIds(context: Context): IntArray =
            AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, CountdownWidgetProvider::class.java))

        fun countdownFor(context: Context, id: Int): Countdown = CountdownStore.load(context, id) ?: Countdown.DEFAULT

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = widgetIds(context)
            if (ids.isNotEmpty()) {
                ids.forEach { update(context, manager, it) }
                scheduleMidnightUpdate(context)
            }
        }

        fun update(context: Context, manager: AppWidgetManager, id: Int) {
            val countdown = countdownFor(context, id)
            val today = LocalDate.now()

            // Hochformat: minWidth × maxHeight
            val options = manager.getAppWidgetOptions(id)
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH).takeIf { it > 0 } ?: 110
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT).takeIf { it > 0 } ?: 110
            val density = context.resources.displayMetrics.density
            val scale = min(density, MAX_BITMAP_PX / maxOf(widthDp, heightDp).toFloat())
            val bitmap = ShapeRenderer.render(
                countdown, today,
                (widthDp * scale).roundToInt(), (heightDp * scale).roundToInt(),
                min(widthDp, heightDp).toFloat()
            )

            val views = RemoteViews(context.packageName, R.layout.widget_countdown).apply {
                setImageViewBitmap(R.id.image, bitmap)
                setContentDescription(R.id.image, describe(context, countdown, today))
                setOnClickPendingIntent(
                    R.id.root,
                    PendingIntent.getActivity(
                        context, id,
                        Intent(context, ConfigActivity::class.java)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }
            manager.updateAppWidget(id, views)
        }

        fun describe(context: Context, countdown: Countdown, today: LocalDate): String {
            val days = countdown.daysLeft(today)
            return when {
                days > 1 -> context.getString(R.string.days_left, countdown.name, days)
                days == 1L -> context.getString(R.string.one_day_left, countdown.name)
                days == 0L -> context.getString(R.string.today_reached, countdown.name)
                else -> context.getString(R.string.past, countdown.name)
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
