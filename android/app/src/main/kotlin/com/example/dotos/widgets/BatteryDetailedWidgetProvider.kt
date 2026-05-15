package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
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

class BatteryDetailedWidgetProvider : AppWidgetProvider() {

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
                android.content.ComponentName(context, BatteryDetailedWidgetProvider::class.java)
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
            val bitmap = renderDetailedBattery(context, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderDetailedBattery(context: Context, appWidgetId: Int): Bitmap {
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "battery")
            // 4x2 Wide widget - 800x400
            val width = 800
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

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

            // Get battery info
            val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
            val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
            val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
            
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                            status == BatteryManager.BATTERY_STATUS_FULL
            
            val voltageV = voltage / 1000.0
            val tempC = temperature / 10.0
            
            val healthStr = when (health) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "GOOD"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEAT"
                BatteryManager.BATTERY_HEALTH_DEAD -> "DEAD"
                BatteryManager.BATTERY_HEALTH_COLD -> "COLD"
                else -> "UNKNOWN"
            }

            // Header
            paint.apply {
                color = palette.label
                textSize = 22f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.12f
            }
            canvas.drawText("BATTERY STATS", 48f, 60f, paint)

            // Large percentage on left
            val batteryColor = when {
                isCharging -> palette.success
                level <= 20 -> palette.accent
                level <= 50 -> palette.secondary
                else -> palette.primary
            }
            
            paint.apply {
                color = batteryColor
                textSize = 96f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                textAlign = Paint.Align.LEFT
                letterSpacing = -0.05f
            }
            canvas.drawText("$level%", 48f, height / 2f + 30f, paint)

            // Status below percentage
            paint.apply {
                color = if (isCharging) palette.success else palette.label
                textSize = 18f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                letterSpacing = 0.08f
            }
            canvas.drawText(if (isCharging) "CHARGING" else "DISCHARGING", 48f, height / 2f + 60f, paint)

            // Stats grid on right
            val statsX = 380f
            val statsY = 120f
            val lineHeight = 50f

            // Voltage
            drawStatRow(canvas, paint, "VOLTAGE", String.format("%.2f V", voltageV), statsX, statsY, palette)
            
            // Temperature
            drawStatRow(canvas, paint, "TEMP", String.format("%.1f °C", tempC), statsX, statsY + lineHeight, palette)
            
            // Health
            drawStatRow(canvas, paint, "HEALTH", healthStr, statsX, statsY + lineHeight * 2, palette)
            
            // Time remaining estimate (placeholder)
            val hoursRemaining = if (isCharging) {
                ((100 - level) / 20.0).toInt()
            } else {
                (level / 10.0).toInt()
            }
            val timeStr = if (isCharging) "$hoursRemaining H TO FULL" else "$hoursRemaining H REMAINING"
            drawStatRow(canvas, paint, "TIME", timeStr, statsX, statsY + lineHeight * 3, palette)

            return bitmap
        }
        
        private fun drawStatRow(canvas: Canvas, paint: Paint, label: String, value: String, x: Float, y: Float, palette: WidgetTheme.Palette) {
            // Label
            paint.apply {
                color = palette.muted
                textSize = 16f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.12f
            }
            canvas.drawText(label, x, y, paint)
            
            // Value
            paint.apply {
                color = palette.primary
                textSize = 22f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
            }
            canvas.drawText(value, x, y + 26f, paint)
        }
    }
}
