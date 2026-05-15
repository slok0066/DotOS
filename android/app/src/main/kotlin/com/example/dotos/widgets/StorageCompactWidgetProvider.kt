package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import android.os.StatFs
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R

class StorageCompactWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) { WidgetTheme.removeWidgetTheme(context, id) }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderCompactStorage(context, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderCompactStorage(context: Context, appWidgetId: Int): Bitmap {
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "storage")
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 48f, 48f, paint)

            paint.color = palette.grid
            paint.alpha = 102
            paint.style = Paint.Style.FILL
            val spacing = 32f
            val dotRadius = 3f
            for (x in 0 until (size / spacing.toInt() + 1)) {
                for (y in 0 until (size / spacing.toInt() + 1)) {
                    canvas.drawCircle(x * spacing, y * spacing, dotRadius, paint)
                }
            }
            paint.alpha = 255

            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            val usedBytes = totalBytes - availableBytes

            val totalGB = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val usedGB = usedBytes / (1024.0 * 1024.0 * 1024.0)
            val usedPercent = ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

            val storageColor = when {
                usedPercent >= 90 -> palette.accent
                usedPercent >= 70 -> palette.secondary
                else -> palette.primary
            }

            val centerX = size / 2f
            val centerY = size / 2f + 20f
            val radius = 100f

            paint.apply {
                color = palette.border
                style = Paint.Style.STROKE
                strokeWidth = 16f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawCircle(centerX, centerY, radius, paint)

            paint.color = storageColor
            val sweepAngle = (usedPercent / 100f) * 360f
            canvas.drawArc(
                centerX - radius, centerY - radius,
                centerX + radius, centerY + radius,
                -90f, sweepAngle, false, paint
            )

            paint.apply {
                color = storageColor
                textSize = 56f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                style = Paint.Style.FILL
                letterSpacing = -0.05f
            }
            canvas.drawText("$usedPercent%", centerX, centerY + 16f, paint)

            paint.apply {
                color = palette.label
                textSize = 18f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                letterSpacing = 0.08f
            }
            val storageText = String.format("%.1f / %.1f GB", usedGB, totalGB)
            canvas.drawText(storageText, centerX, 70f, paint)

            paint.apply {
                color = palette.muted
                textSize = 16f
                letterSpacing = 0.12f
            }
            canvas.drawText("STORAGE", centerX, size - 50f, paint)

            return bitmap
        }
    }
}
