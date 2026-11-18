package de.diridari.countdown

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Typeface
import java.time.LocalDate
import kotlin.math.min

/**
 * Zeichnet einen Countdown als Bitmap: die Form, darum einen Ladering entlang der Kontur
 * (Länge = verbleibender Anteil, Start oben, im Uhrzeigersinn) und die Tage in der Mitte.
 */
object ShapeRenderer {

    /** Geometrie einer Form im Einheitsquadrat (0..1). */
    private class Geometry(
        val centerX: Float,
        val centerY: Float,
        /** Abstand vom Zentrum zur Kontur, für den gleichmäßigen Abstand zwischen Ring und Füllung. */
        val inradius: Float,
        /** Verfügbare Textbreite. */
        val textWidth: Float,
        val color: Int,
        /** Vertikale Textmitte, falls abweichend vom Zentrum. */
        val textY: Float = centerY,
    )

    private val geometries = mapOf(
        CountdownShape.CIRCLE to Geometry(0.5f, 0.5f, 0.5f, 0.62f, 0xFF1565C0.toInt()),
        CountdownShape.SQUARE to Geometry(0.5f, 0.5f, 0.5f, 0.72f, 0xFFEF6C00.toInt()),
        CountdownShape.TRIANGLE to Geometry(0.5f, 0.69f, 0.31f, 0.36f, 0xFF2E7D32.toInt()),
        CountdownShape.PAUL to Geometry(0.5f, 0.58f, 0.4f, 0.5f, 0xFFD81B60.toInt(), textY = 0.47f),
    )

    fun color(shape: CountdownShape): Int = geometries.getValue(shape).color

    /**
     * @param sizeDp kleinere Seite des Widgets in dp; darunter fällt der Name weg.
     */
    fun render(countdown: Countdown, today: LocalDate, widthPx: Int, heightPx: Int, sizeDp: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val geometry = geometries.getValue(countdown.shape)

        val size = min(widthPx, heightPx).toFloat()
        val left = (widthPx - size) / 2f
        val top = (heightPx - size) / 2f
        val stroke = size * 0.075f
        val gap = size * 0.035f

        // Ring-Kontur: die Form, so eingerückt, dass der Strich ins Bitmap passt.
        val ringRect = RectF(left, top, left + size, top + size).apply { inset(stroke / 2f, stroke / 2f) }
        val ringPath = unitPath(countdown.shape).apply {
            transform(Matrix().apply { setRectToRect(RectF(0f, 0f, 1f, 1f), ringRect, Matrix.ScaleToFit.FILL) })
        }

        // Füllung: dieselbe Form, um das Zentrum verkleinert, damit überall etwa gleich viel Abstand bleibt.
        val cx = ringRect.left + geometry.centerX * ringRect.width()
        val cy = ringRect.top + geometry.centerY * ringRect.height()
        val inradiusPx = geometry.inradius * ringRect.width()
        val scale = ((inradiusPx - stroke / 2f - gap) / inradiusPx).coerceAtLeast(0.1f)
        val fillPath = Path(ringPath).apply { transform(Matrix().apply { setScale(scale, scale, cx, cy) }) }

        val corner = CornerPathEffect(size * 0.05f)
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = geometry.color
            style = Paint.Style.FILL
            pathEffect = corner
        }
        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(geometry.color, 0x40)
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            pathEffect = corner
        }
        val progressPaint = Paint(trackPaint).apply { color = geometry.color }

        canvas.drawPath(fillPath, fillPaint)
        canvas.drawPath(ringPath, trackPaint)

        val fraction = countdown.remainingFraction(today)
        if (fraction > 0f) {
            val measure = PathMeasure(ringPath, false)
            val segment = Path()
            measure.getSegment(0f, measure.length * fraction, segment, true)
            canvas.drawPath(segment, progressPaint)
        }

        val textY = ringRect.top + geometry.textY * ringRect.height()
        drawText(canvas, countdown, today, geometry, cx, textY, ringRect.width(), sizeDp)
        return bitmap
    }

    private fun drawText(
        canvas: Canvas, countdown: Countdown, today: LocalDate, geometry: Geometry,
        cx: Float, cy: Float, shapeSize: Float, sizeDp: Float,
    ) {
        val days = countdown.daysLeft(today)
        val main = when {
            days > 0 -> days.toString()
            days == 0L -> "Heute"
            else -> "Vorbei"
        }
        val maxWidth = geometry.textWidth * shapeSize
        val showName = sizeDp >= 72f && countdown.name.isNotBlank()

        val mainPaint = textPaint(Typeface.BOLD, shapeSize * (if (days > 0) 0.32f else 0.17f) * textScale(countdown.shape), maxWidth, main)
        val namePaint = textPaint(Typeface.NORMAL, shapeSize * 0.1f * textScale(countdown.shape), maxWidth, countdown.name)

        // Höhe der Großbuchstaben/Ziffern ≈ 0,72 × Schriftgröße; Block aus Zahl und Name vertikal zentrieren.
        val mainHeight = mainPaint.textSize * 0.72f
        val nameHeight = if (showName) namePaint.textSize * 1.15f else 0f
        val blockTop = cy - (mainHeight + nameHeight) / 2f

        canvas.drawText(main, cx, blockTop + mainHeight, mainPaint)
        if (showName) {
            canvas.drawText(countdown.name, cx, blockTop + mainHeight + nameHeight, namePaint)
        }
    }

    private fun textScale(shape: CountdownShape) = if (shape == CountdownShape.TRIANGLE) 0.8f else 1f

    private fun textPaint(style: Int, size: Float, maxWidth: Float, text: String) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, style)
            textSize = size
            val width = measureText(text)
            if (width > maxWidth && width > 0f) textSize = size * maxWidth / width
        }

    private fun withAlpha(color: Int, alpha: Int) = (color and 0x00FFFFFF) or (alpha shl 24)

    /** Kontur im Einheitsquadrat, Start oben in der Mitte (bzw. an der Spitze), im Uhrzeigersinn. */
    private fun unitPath(shape: CountdownShape): Path = Path().apply {
        when (shape) {
            CountdownShape.CIRCLE -> {
                val oval = RectF(0f, 0f, 1f, 1f)
                arcTo(oval, -90f, 180f, true)
                arcTo(oval, 90f, 180f, false)
                close()
            }

            CountdownShape.SQUARE -> {
                val r = 0.16f
                moveTo(0.5f, 0f)
                lineTo(1f - r, 0f)
                arcTo(RectF(1f - 2 * r, 0f, 1f, 2 * r), -90f, 90f, false)
                lineTo(1f, 1f - r)
                arcTo(RectF(1f - 2 * r, 1f - 2 * r, 1f, 1f), 0f, 90f, false)
                lineTo(r, 1f)
                arcTo(RectF(0f, 1f - 2 * r, 2 * r, 1f), 90f, 90f, false)
                lineTo(0f, r)
                arcTo(RectF(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
                close()
            }

            CountdownShape.TRIANGLE -> {
                moveTo(0.5f, 0f)
                lineTo(1f, 1f)
                lineTo(0f, 1f)
                close()
            }

            CountdownShape.PAUL -> {
                moveTo(0.5f, 0.27f)
                cubicTo(0.5f, 0.08f, 0.98f, 0.02f, 0.98f, 0.37f)
                cubicTo(0.98f, 0.62f, 0.66f, 0.78f, 0.5f, 0.97f)
                cubicTo(0.34f, 0.78f, 0.02f, 0.62f, 0.02f, 0.37f)
                cubicTo(0.02f, 0.02f, 0.5f, 0.08f, 0.5f, 0.27f)
                close()
            }
        }
    }
}
