package de.diridari.countdown

import android.graphics.Bitmap
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

/** Rendert alle Formen als PNG nach app/build/previews, zum Anschauen ohne Handy. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ShapeRendererPreviewTest {

    @Test
    fun renderPreviews() {
        val dir = File("build/previews").apply { mkdirs() }
        val today = LocalDate.of(2026, 11, 18)
        CountdownShape.entries.forEach { shape ->
            val name = if (shape == CountdownShape.PAUL) "Paul" else "Gehalt"
            val countdown = Countdown(name, CountdownType.SALARY, shape)
            listOf(110 to 330, 50 to 150).forEach { (dp, px) ->
                val bitmap = ShapeRenderer.render(countdown, today, px, px, dp.toFloat())
                File(dir, "${shape.name.lowercase()}_$dp.png").outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
        }
        // Eigene Farbe und Textgrößen
        CountdownTextSize.entries.forEach { size ->
            val countdown = Countdown("Urlaub", CountdownType.SALARY, CountdownShape.CIRCLE,
                color = CountdownColors.PALETTE[4], textSize = size)
            val bitmap = ShapeRenderer.render(countdown, today, 330, 330, 110f)
            File(dir, "color_size_${size.name.lowercase()}.png").outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
