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

class TapCounterMatrixWidgetProvider : AppWidgetProvider() {

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

            val appWidgetManager = AppWidgetManager.getInstance(context)
            updateAllInstances(context, appWidgetManager)
        }
    }

    private fun updateAllInstances(context: Context, appWidgetManager: AppWidgetManager) {
        val providers = arrayOf(
            TapCounterWidgetProvider::class.java,
            TapCounterDialWidgetProvider::class.java,
            TapCounterMatrixWidgetProvider::class.java
        )
        for (provider in providers) {
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, provider))
            for (id in ids) {
                when (provider) {
                    TapCounterWidgetProvider::class.java -> TapCounterWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                    TapCounterDialWidgetProvider::class.java -> TapCounterDialWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                    TapCounterMatrixWidgetProvider::class.java -> updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            val prefs = context.getSharedPreferences("tap_counter", Context.MODE_PRIVATE)
            val count = prefs.getInt("count", 0)
            
            val bitmap = renderMatrixWidget(context, count)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, TapCounterMatrixWidgetProvider::class.java).apply {
                action = "ACTION_INCREMENT"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderMatrixWidget(context: Context, count: Int): Bitmap {
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            val palette = WidgetTheme.palette(context)

            // Background
            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 48f, 48f, paint)

            // Large Matrix Display
            val countStr = count.toString().padStart(4, '0')
            val dotSize = 14f
            val dotSpacing = 4f
            val textWidth = DotMatrixRenderer.measureDotMatrixText(countStr, dotSize, dotSpacing)
            
            DotMatrixRenderer.drawDotMatrixText(
                canvas,
                countStr,
                (size - textWidth) / 2f,
                size / 2f - 30f,
                dotSize,
                dotSpacing,
                palette.primary
            )

            // Indicators at bottom
            val indicatorY = size - 80f
            val spacing = 20f
            for (i in 0 until 4) {
                paint.color = if (i == 3) palette.accent else palette.grid
                canvas.drawCircle(size / 2f - (1.5f * spacing) + (i * spacing), indicatorY, 4f, paint)
            }

            // Small label
            paint.apply {
                color = palette.label
                textSize = 20f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("COUNTER MATRIX", size / 2f, indicatorY + 40f, paint)

            return bitmap
        }
    }
}
