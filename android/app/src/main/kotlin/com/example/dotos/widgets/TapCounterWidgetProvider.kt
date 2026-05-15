package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R

class TapCounterWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "ACTION_INCREMENT") {
            val prefs = context.getSharedPreferences("tap_counter", Context.MODE_PRIVATE)
            val count = prefs.getInt("count", 0) + 1
            prefs.edit().putInt("count", count).apply()
            WidgetUpdateBroadcaster.refreshTapCounters(context)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) { WidgetTheme.removeWidgetTheme(context, id) }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            val prefs = context.getSharedPreferences("tap_counter", Context.MODE_PRIVATE)
            val count = prefs.getInt("count", 0)
            
            val bitmap = renderTapWidget(context, count, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            // Setup click intent
            val intent = Intent(context, TapCounterWidgetProvider::class.java).apply {
                action = "ACTION_INCREMENT"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderTapWidget(context: Context, count: Int, appWidgetId: Int): Bitmap {
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "tap_counter")

            // Background
            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 48f, 48f, paint)

            // Dot Grid Pattern
            paint.color = palette.grid
            paint.alpha = 80
            paint.style = Paint.Style.FILL
            val spacing = 32f
            val dotRadius = 2.5f
            for (x in 0 until (size / spacing.toInt() + 1)) {
                for (y in 0 until (size / spacing.toInt() + 1)) {
                    canvas.drawCircle(x * spacing, y * spacing, dotRadius, paint)
                }
            }
            paint.alpha = 255

            // Draw Count using DotMatrixRenderer
            val countStr = count.toString()
            val dotSize = 12f
            val dotSpacing = 3f
            val textWidth = DotMatrixRenderer.measureDotMatrixText(countStr, dotSize, dotSpacing)
            
            DotMatrixRenderer.drawDotMatrixText(
                canvas,
                countStr,
                (size - textWidth) / 2f,
                size / 2f - 60f,
                dotSize,
                dotSpacing,
                palette.primary
            )

            // "TAPS" label
            paint.apply {
                color = palette.muted
                textSize = 32f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.2f
            }
            canvas.drawText("TAPS", size / 2f, size / 2f + 100f, paint)

            return bitmap
        }
    }
}
