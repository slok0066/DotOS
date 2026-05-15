package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.BatteryManager
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R

class BatteryMinimalWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        BatteryWidgetUpdater.schedule(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        BatteryWidgetUpdater.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        BatteryWidgetUpdater.cancelIfNoBatteryWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == Intent.ACTION_BATTERY_CHANGED ||
            intent.action == Intent.ACTION_POWER_CONNECTED ||
            intent.action == Intent.ACTION_POWER_DISCONNECTED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, BatteryMinimalWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
            BatteryWidgetUpdater.schedule(context)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) { WidgetTheme.removeWidgetTheme(context, id) }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderMinimalBattery(context, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderMinimalBattery(context: Context, appWidgetId: Int): Bitmap {
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "battery")
            // 2x2 Square widget - 400x400
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            // Background
            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 48f, 48f, paint)

            // Dot Grid Pattern
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

            // Get battery info
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                            status == BatteryManager.BATTERY_STATUS_FULL

            // Determine color
            val batteryColor = when {
                isCharging -> palette.success
                level <= 20 -> palette.accent
                level <= 50 -> palette.secondary
                else -> palette.primary
            }

            // Huge percentage (centered)
            paint.apply {
                color = batteryColor
                textSize = 160f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                letterSpacing = -0.08f
            }
            canvas.drawText("$level", size / 2f, size / 2f + 50f, paint)

            // Percent symbol
            paint.apply {
                color = palette.muted
                textSize = 60f
            }
            canvas.drawText("%", size / 2f, size / 2f + 120f, paint)

            // Tiny charging indicator at top
            if (isCharging) {
                paint.apply {
                    color = palette.success
                    textSize = 16f
                    letterSpacing = 0.15f
                }
                canvas.drawText("CHARGING", size / 2f, 60f, paint)
                
                // Lightning bolt
                paint.textSize = 24f
                canvas.drawText("⚡", size / 2f, size - 50f, paint)
            }

            return bitmap
        }
    }
}
