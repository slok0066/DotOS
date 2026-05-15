package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.widget.RemoteViews
import com.example.dotos.R

class TapCounterDialWidgetProvider : AppWidgetProvider() {

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
            
            val bitmap = renderDialWidget(context, count, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, TapCounterDialWidgetProvider::class.java).apply {
                action = "ACTION_INCREMENT"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderDialWidget(context: Context, count: Int, appWidgetId: Int): Bitmap {
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            // Background
                val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "tap_counter")
                paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 48f, 48f, paint)

            // Outer Circle
            paint.color = Color.WHITE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - 60f, paint)

            // Progress Arc (using count % 100 for visual)
                paint.color = palette.accent
            val rect = RectF(60f, 60f, size - 60f, size - 60f)
            val sweep = (count % 100) * 3.6f
            canvas.drawArc(rect, -90f, sweep, false, paint)

            // Count text
            val countStr = count.toString()
            val dotSize = 10f
            val dotSpacing = 2f
            val textWidth = DotMatrixRenderer.measureDotMatrixText(countStr, dotSize, dotSpacing)
            
            DotMatrixRenderer.drawDotMatrixText(
                canvas,
                countStr,
                (size - textWidth) / 2f,
                size / 2f - 50f,
                dotSize,
                dotSpacing,
                palette.primary
            )

            // Label
            paint.apply {
                    color = palette.muted
                textSize = 24f
                style = Paint.Style.FILL
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.15f
            }
            canvas.drawText("TAPS", size / 2f, size / 2f + 85f, paint)

            return bitmap
        }
    }
}
