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
import android.graphics.Typeface
import android.os.BatteryManager
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R

class BatteryBarsWidgetProvider : AppWidgetProvider() {
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
            val thisWidget = ComponentName(context, BatteryBarsWidgetProvider::class.java)
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
        
        val bitmap = renderBatteryBars(context, batteryPct, isCharging)
        views.setImageViewBitmap(R.id.widget_image, bitmap)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        private fun renderBatteryBars(context: Context, batteryPct: Int, isCharging: Boolean): Bitmap {
            val palette = WidgetTheme.palette(context)
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

            paint.apply {
                color = palette.primary
                textSize = 96f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                letterSpacing = -0.05f
            }
            canvas.drawText("$batteryPct%", 48f, 180f, paint)

            paint.apply {
                color = palette.label
                textSize = 24f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            val statusText = if (isCharging) "CHARGING" else "DISCHARGING"
            canvas.drawText(statusText, 48f, 215f, paint)

            val barsStartX = 400f
            val barsStartY = 100f
            val barWidth = 350f
            val barHeight = 20f
            val barSpacing = 6f
            val totalBars = 10
            val filledBars = (batteryPct / 10f).toInt()

            for (i in 0 until totalBars) {
                val y = barsStartY + i * (barHeight + barSpacing)
                val isFilled = i < filledBars

                paint.apply {
                    color = when {
                        !isFilled -> palette.border
                        batteryPct <= 20 -> palette.accent
                        batteryPct <= 50 -> palette.secondary
                        else -> if (isCharging) palette.success else palette.primary
                    }
                    style = Paint.Style.FILL
                }

                canvas.drawRoundRect(
                    barsStartX,
                    y,
                    barsStartX + barWidth,
                    y + barHeight,
                    4f, 4f, paint
                )

                if (isFilled) {
                    paint.apply {
                        color = palette.background
                        textSize = 14f
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(
                        "${(i + 1) * 10}",
                        barsStartX + barWidth / 2,
                        y + 15f,
                        paint
                    )
                }
            }

            if (isCharging) {
                paint.apply {
                    color = palette.success
                    textSize = 20f
                    typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                    textAlign = Paint.Align.RIGHT
                }
                canvas.drawText("⚡ CHARGING", width - 48f, height - 48f, paint)
            }

            return bitmap
        }
    }
}
