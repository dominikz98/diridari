package de.diridari.countdown

import android.content.Context
import org.json.JSONObject
import java.time.LocalDate

/** Speichert pro Widget-ID einen [Countdown] in den SharedPreferences. */
object CountdownStore {

    private const val PREFS = "countdowns"

    fun load(context: Context, widgetId: Int): Countdown? = read(context, "w_$widgetId")

    fun save(context: Context, widgetId: Int, countdown: Countdown) = write(context, "w_$widgetId", countdown)

    fun delete(context: Context, widgetId: Int) = prefs(context).edit().remove("w_$widgetId").apply()

    /** Countdown merken, bis der Launcher das angepinnte Widget bestätigt. */
    fun savePending(context: Context, key: String, countdown: Countdown) = write(context, "p_$key", countdown)

    fun claimPending(context: Context, key: String, widgetId: Int) {
        val countdown = read(context, "p_$key") ?: return
        save(context, widgetId, countdown)
        prefs(context).edit().remove("p_$key").apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun read(context: Context, key: String): Countdown? {
        val raw = prefs(context).getString(key, null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            Countdown(
                name = json.getString("name"),
                type = CountdownType.valueOf(json.getString("type")),
                shape = CountdownShape.valueOf(json.getString("shape")),
                date = json.optString("date").takeIf { it.isNotEmpty() }?.let(LocalDate::parse),
                createdAt = LocalDate.parse(json.getString("createdAt")),
            )
        }.getOrNull()
    }

    private fun write(context: Context, key: String, countdown: Countdown) {
        val json = JSONObject()
            .put("name", countdown.name)
            .put("type", countdown.type.name)
            .put("shape", countdown.shape.name)
            .put("date", countdown.date?.toString() ?: "")
            .put("createdAt", countdown.createdAt.toString())
        prefs(context).edit().putString(key, json.toString()).apply()
    }
}
