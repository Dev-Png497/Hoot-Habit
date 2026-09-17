package com.hoothabit.app.ui.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.hoothabit.app.domain.MilestoneCopy
import com.hoothabit.app.ui.theme.HootColors

enum class ShareCardFormat(val width: Int, val height: Int, val label: String) {
    SQUARE(1080, 1080, "Square"),
    STORY(1080, 1920, "Story"),
    PORTRAIT(1080, 1350, "Portrait")
}

enum class ShareCardStyle(val label: String) {
    COMPANION("Companion"),
    MINIMAL("Minimal"),
    STATS("Stats")
}

data class ShareCardContent(
    val copy: MilestoneCopy,
    val habitName: String?,
    val bestStreak: Int?,
    val completionPercent: Int?,
    val timeInvestedLabel: String?,
    val showBranding: Boolean
)

/**
 * Renders a milestone share card straight onto a Bitmap with android.graphics —
 * deliberately avoiding Compose-capture-to-bitmap, which is fragile across
 * Compose versions. Quiet, tasteful, no watermark spam (spec #60-64).
 */
object ShareCardRenderer {

    fun render(
        format: ShareCardFormat,
        style: ShareCardStyle,
        content: ShareCardContent,
        colors: HootColors
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(format.width, format.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(colors.background.toArgb())

        when (style) {
            ShareCardStyle.MINIMAL -> drawMinimal(canvas, format, content, colors)
            ShareCardStyle.STATS -> drawStats(canvas, format, content, colors)
            ShareCardStyle.COMPANION -> drawCompanion(canvas, format, content, colors)
        }

        return bitmap
    }

    private fun androidx.compose.ui.graphics.Color.toArgb(): Int =
        Color.argb((alpha * 255).toInt(), (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())

    private fun textPaint(textColor: androidx.compose.ui.graphics.Color, size: Float, bold: Boolean = false, center: Boolean = true): Paint {
        val argb = textColor.toArgb()
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = argb
            textSize = size
            typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
            if (center) textAlign = Paint.Align.CENTER
        }
    }

    private fun drawBranding(canvas: Canvas, format: ShareCardFormat, colors: HootColors, show: Boolean) {
        if (!show) return
        val paint = textPaint(colors.textTertiary, format.width * 0.028f)
        canvas.drawText("HOOT HABIT", format.width / 2f, format.height - format.height * 0.05f, paint)
        val tagline = textPaint(colors.textTertiary, format.width * 0.024f)
        canvas.drawText("Built one day at a time.", format.width / 2f, format.height - format.height * 0.08f, tagline)
    }

    private fun drawMinimal(canvas: Canvas, format: ShareCardFormat, content: ShareCardContent, colors: HootColors) {
        val cx = format.width / 2f
        val numberPaint = textPaint(colors.accent, format.width * 0.24f, bold = true)
        val words = content.copy.headline.split(" ")
        val numberPart = words.firstOrNull { it.any(Char::isDigit) } ?: content.copy.headline
        val labelPart = content.copy.headline.removePrefix(numberPart).trim().ifEmpty { "DAYS" }

        canvas.drawText(numberPart, cx, format.height * 0.44f, numberPaint)
        canvas.drawText(labelPart, cx, format.height * 0.52f, textPaint(colors.textPrimary, format.width * 0.06f, bold = true))

        content.habitName?.let {
            canvas.drawText(it.uppercase(), cx, format.height * 0.60f, textPaint(colors.textSecondary, format.width * 0.03f))
        }

        drawBranding(canvas, format, colors, content.showBranding)
    }

    private fun drawStats(canvas: Canvas, format: ShareCardFormat, content: ShareCardContent, colors: HootColors) {
        val cx = format.width / 2f
        var y = format.height * 0.22f

        canvas.drawText(content.copy.headline, cx, y, textPaint(colors.textPrimary, format.width * 0.09f, bold = true))
        y += format.height * 0.045f
        canvas.drawText(content.copy.subtitle, cx, y, textPaint(colors.textSecondary, format.width * 0.032f))

        y += format.height * 0.12f
        content.bestStreak?.let {
            canvas.drawText("🔥 Best streak · $it", cx, y, textPaint(colors.textPrimary, format.width * 0.042f))
            y += format.height * 0.07f
        }
        content.completionPercent?.let {
            canvas.drawText("✓ Completion · $it%", cx, y, textPaint(colors.textPrimary, format.width * 0.042f))
            y += format.height * 0.07f
        }
        content.timeInvestedLabel?.let {
            canvas.drawText("⏱ Time · $it", cx, y, textPaint(colors.textPrimary, format.width * 0.042f))
        }

        drawBranding(canvas, format, colors, content.showBranding)
    }

    private fun drawCompanion(canvas: Canvas, format: ShareCardFormat, content: ShareCardContent, colors: HootColors) {
        val cx = format.width / 2f
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.surfaceElevated.toArgb() }
        val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.textPrimary.toArgb() }
        val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.accent.toArgb() }

        val ownY = format.height * 0.22f
        val bodyR = format.width * 0.16f
        canvas.drawOval(cx - bodyR, ownY - bodyR * 0.9f, cx + bodyR, ownY + bodyR * 0.9f, bodyPaint)
        val eyeDx = bodyR * 0.42f
        val eyeR = bodyR * 0.28f
        canvas.drawCircle(cx - eyeDx, ownY, eyeR, eyePaint)
        canvas.drawCircle(cx + eyeDx, ownY, eyeR, eyePaint)
        canvas.drawCircle(cx - eyeDx, ownY, eyeR * 0.4f, pupilPaint)
        canvas.drawCircle(cx + eyeDx, ownY, eyeR * 0.4f, pupilPaint)

        val numberPaint = textPaint(colors.accent, format.width * 0.14f, bold = true)
        canvas.drawText(content.copy.headline, cx, format.height * 0.52f, numberPaint)
        canvas.drawText(content.copy.subtitle, cx, format.height * 0.58f, textPaint(colors.textSecondary, format.width * 0.032f))

        var y = format.height * 0.68f
        val statParts = buildList {
            content.bestStreak?.let { add("🔥 $it-day best streak") }
            content.completionPercent?.let { add("$it% completion") }
            content.timeInvestedLabel?.let { add("$it invested") }
        }
        statParts.forEach {
            canvas.drawText(it, cx, y, textPaint(colors.textPrimary, format.width * 0.032f))
            y += format.height * 0.045f
        }

        drawBranding(canvas, format, colors, content.showBranding)
    }
}
