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

class StorageDetailedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderDetailedStorage(context)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderDetailedStorage(context: Context): Bitmap {
            // 4x2 Wide widget - 800x400
            val width = 800
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            val palette = WidgetTheme.palette(context)

            // Background
            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 48f, 48f, paint)

            // Dot Grid Pattern
            paint.color = palette.grid
            paint.alpha = 102
            paint.style = Paint.Style.FILL
            val spacing = 32f
            val dotRadius = 3f
            for (x in 0 until (width / spacing.toInt() + 1)) {
                for (y in 0 until (height / spacing.toInt() + 1)) {
                    canvas.drawCircle(x * spacing, y * spacing, dotRadius, paint)
                }
            }
            paint.alpha = 255

            // Get storage info
            val stat = StatFs(Environment.getDataDirectory().path)
            val totalBytes = stat.blockCountLong * stat.blockSizeLong
            val availableBytes = stat.availableBlocksLong * stat.blockSizeLong
            val usedBytes = totalBytes - availableBytes
            
            val totalGB = totalBytes / (1024.0 * 1024.0 * 1024.0)
            val usedGB = usedBytes / (1024.0 * 1024.0 * 1024.0)
            val availableGB = availableBytes / (1024.0 * 1024.0 * 1024.0)
            val usedPercent = ((usedBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

            // Header
            paint.apply {
                color = palette.label
                textSize = 22f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.12f
            }
            canvas.drawText("STORAGE ANALYSIS", 48f, 60f, paint)

            // Total storage info
            paint.apply {
                color = palette.muted
                textSize = 16f
            }
            canvas.drawText(String.format("TOTAL: %.1f GB", totalGB), 48f, 90f, paint)

            // Category breakdown (placeholder percentages)
            val categories = listOf(
                StorageCategory("APPS", 35, palette.primary),
                StorageCategory("PHOTOS", 25, palette.secondary),
                StorageCategory("VIDEOS", 20, palette.label),
                StorageCategory("DOCUMENTS", 10, palette.muted),
                StorageCategory("OTHER", 5, palette.border),
                StorageCategory("FREE", 100 - usedPercent, palette.grid)
            )

            val startY = 140f
            val barHeight = 30f
            val barSpacing = 8f
            val maxBarWidth = width - 320f

            for ((index, category) in categories.withIndex()) {
                val y = startY + index * (barHeight + barSpacing)
                
                // Category label
                paint.apply {
                    color = palette.label
                    textSize = 16f
                    textAlign = Paint.Align.LEFT
                    letterSpacing = 0.08f
                }
                canvas.drawText(category.name, 48f, y + 20f, paint)

                // Bar
                val barWidth = (category.percent / 100f) * maxBarWidth
                paint.apply {
                    color = category.color
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    200f, y,
                    200f + barWidth, y + barHeight,
                    4f, 4f, paint
                )

                // Percentage and size
                val categoryGB = (category.percent / 100f) * totalGB
                val sizeText = if (category.name == "FREE") {
                    String.format("%.1f GB", availableGB)
                } else {
                    String.format("%.1f GB", categoryGB)
                }
                
                paint.apply {
                    color = palette.primary
                    textSize = 14f
                    textAlign = Paint.Align.RIGHT
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                }
                canvas.drawText("${category.percent}%", width - 120f, y + 20f, paint)
                
                paint.apply {
                    color = palette.label
                    textSize = 12f
                }
                canvas.drawText(sizeText, width - 48f, y + 20f, paint)
            }

            return bitmap
        }
    }
    
    data class StorageCategory(val name: String, val percent: Int, val color: Int)
}
