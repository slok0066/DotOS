package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Environment
import android.os.StatFs
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R
import kotlin.math.roundToInt

class StorageAnalysisWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderAnalysisStorage(context)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderAnalysisStorage(context: Context): Bitmap {
            val palette = WidgetTheme.palette(context)
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 48f, 48f, paint)

            paint.color = palette.grid
            paint.alpha = 96
            val spacing = 32f
            for (x in 0..(size / spacing).toInt()) {
                for (y in 0..(size / spacing).toInt()) {
                    canvas.drawCircle(x * spacing, y * spacing, 3f, paint)
                }
            }
            paint.alpha = 255

            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            val usedPercent = ((totalBytes - availableBytes).toDouble() / totalBytes.toDouble() * 100.0)
                .roundToInt()
                .coerceIn(0, 100)

            val apps = (usedPercent * 0.45).roundToInt().coerceAtLeast(1)
            val media = (usedPercent * 0.35).roundToInt().coerceAtLeast(1)
            val free = (100 - usedPercent).coerceAtLeast(0)

            paint.apply {
                color = palette.label
                textSize = 18f
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.1f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
            }
            canvas.drawText("ANALYSIS", 34f, 56f, paint)

            drawBar(canvas, paint, palette, "APPS", apps / 100f, 34f, 112f, 332f, palette.primary)
            drawBar(canvas, paint, palette, "MEDIA", media / 100f, 34f, 170f, 332f, palette.secondary)
            drawBar(canvas, paint, palette, "FREE", free / 100f, 34f, 228f, 332f, palette.muted)

            paint.apply {
                color = palette.primary
                textSize = 42f
                textAlign = Paint.Align.LEFT
                letterSpacing = -0.04f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            }
            canvas.drawText("$usedPercent%", 34f, 330f, paint)

            paint.apply {
                color = palette.label
                textSize = 16f
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
            }
            canvas.drawText("USED TODAY", 34f, 356f, paint)

            return bitmap
        }

        private fun drawBar(
            canvas: Canvas,
            paint: Paint,
            palette: WidgetTheme.Palette,
            label: String,
            value: Float,
            x: Float,
            y: Float,
            width: Float,
            color: Int,
        ) {
            paint.apply {
                textSize = 14f
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                style = Paint.Style.FILL
                this.color = palette.label
            }
            canvas.drawText(label, x, y - 10f, paint)

            paint.color = palette.border
            canvas.drawRoundRect(x, y, x + width, y + 10f, 5f, 5f, paint)

            paint.color = color
            canvas.drawRoundRect(x, y, x + (width * value.coerceIn(0f, 1f)), y + 10f, 5f, 5f, paint)
        }
    }
}
