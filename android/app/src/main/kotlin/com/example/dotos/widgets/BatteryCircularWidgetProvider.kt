package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.BatteryManager
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R

class BatteryCircularWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateBatteryWidget(context, appWidgetManager, appWidgetId)
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
            val thisWidget = ComponentName(context, BatteryCircularWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
            BatteryWidgetUpdater.schedule(context)
        }
    }

    private fun updateBatteryWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = (level * 100 / scale.toFloat()).toInt()
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val bitmap = renderCircularBattery(context, batteryPct, isCharging, appWidgetId)
        views.setImageViewBitmap(R.id.widget_image, bitmap)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) { WidgetTheme.removeWidgetTheme(context, id) }
    }

    companion object {
        private fun renderCircularBattery(context: Context, batteryPct: Int, isCharging: Boolean, appWidgetId: Int): Bitmap {
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "battery")
            val width = 800
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 48f, 48f, paint)

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

            paint.apply {
                color = palette.label
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
            }
            canvas.drawText("BATTERY", 48f, 64f, paint)

            val centerX = width - 200f
            val centerY = height / 2f
            val radius = 120f
            val strokeWidth = 16f

            paint.apply {
                color = palette.border
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawCircle(centerX, centerY, radius, paint)

            val sweepAngle = (batteryPct / 100f) * 360f
            paint.apply {
                color = when {
                    batteryPct <= 20 -> palette.accent
                    batteryPct <= 50 -> palette.secondary
                    else -> if (isCharging) palette.success else palette.primary
                }
            }
            val rect = RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
            )
            canvas.drawArc(rect, -90f, sweepAngle, false, paint)

                        val textToDraw = "$batteryPct%"
            val dotSize = 5f
            val dotSpacing = 1.5f
            val textWidth = DotMatrixRenderer.measureDotMatrixText(textToDraw, dotSize, dotSpacing)
            DotMatrixRenderer.drawDotMatrixText(
                canvas = canvas,
                text = textToDraw,
                x = centerX - (textWidth / 2f),
                y = if (isCharging) centerY - 45f else centerY - 22f,
                dotSize = dotSize,
                dotSpacing = dotSpacing,
                color = palette.primary
            )

            if (isCharging) {
                paint.apply {
                    color = palette.success
                    style = Paint.Style.FILL
                }
                val boltPath = android.graphics.Path().apply {
                    moveTo(centerX - 12f, centerY + 25f)
                    lineTo(centerX + 3f, centerY + 25f)
                    lineTo(centerX - 7f, centerY + 40f)
                    lineTo(centerX + 12f, centerY + 40f)
                    lineTo(centerX - 7f, centerY + 65f)
                    lineTo(centerX - 17f, centerY + 40f)
                    lineTo(centerX - 7f, centerY + 40f)
                    close()
                }
                canvas.drawPath(boltPath, paint)
            }

            paint.apply {
                color = palette.primary
                textSize = 72f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
                letterSpacing = -0.05f
                style = Paint.Style.FILL
            }
            canvas.drawText("$batteryPct%", 48f, height / 2f + 20f, paint)

            paint.apply {
                color = palette.label
                textSize = 24f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            val statusText = if (isCharging) "CHARGING" else "REMAINING"
            canvas.drawText(statusText, 48f, height / 2f + 55f, paint)

            return bitmap
        }
    }
}
