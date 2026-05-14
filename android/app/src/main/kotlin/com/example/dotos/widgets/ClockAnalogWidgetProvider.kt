package com.example.dotos.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class ClockAnalogWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleNextUpdate(context)
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdates(context)
    }

    companion object {
        private const val ACTION_UPDATE = "com.example.dotos.ACTION_UPDATE_CLOCK_ANALOG"
        
        private fun scheduleNextUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockAnalogWidgetProvider::class.java).apply {
                action = ACTION_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        
        private fun cancelUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockAnalogWidgetProvider::class.java).apply {
                action = ACTION_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderAnalogClock(context)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderAnalogClock(context: Context): Bitmap {
            val palette = WidgetTheme.palette(context)
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

            // Label
            paint.apply {
                color = palette.label
                textSize = 28f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
            }
            canvas.drawText("ANALOG", 48f, 64f, paint)

            // Clock circle center and radius
            val centerX = width / 2f + 100f
            val centerY = height / 2f
            val radius = 140f

            // Outer circle
            paint.apply {
                color = palette.border
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawCircle(centerX, centerY, radius, paint)

            // Hour markers (12 dots)
            paint.style = Paint.Style.FILL
            paint.color = palette.primary
            for (i in 0 until 12) {
                val angle = (i * 30 - 90) * PI / 180
                val markerRadius = if (i % 3 == 0) 6f else 4f
                val markerX = centerX + (radius - 20f) * cos(angle).toFloat()
                val markerY = centerY + (radius - 20f) * sin(angle).toFloat()
                canvas.drawCircle(markerX, markerY, markerRadius, paint)
            }

            // Get current time
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)

            // Hour hand
            val hourAngle = ((hour + minute / 60f) * 30 - 90) * PI / 180
            paint.apply {
                color = palette.primary
                strokeWidth = 6f
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawLine(
                centerX,
                centerY,
                centerX + (radius * 0.5f * cos(hourAngle)).toFloat(),
                centerY + (radius * 0.5f * sin(hourAngle)).toFloat(),
                paint
            )

            // Minute hand
            val minuteAngle = (minute * 6 - 90) * PI / 180
            paint.strokeWidth = 4f
            canvas.drawLine(
                centerX,
                centerY,
                centerX + (radius * 0.75f * cos(minuteAngle)).toFloat(),
                centerY + (radius * 0.75f * sin(minuteAngle)).toFloat(),
                paint
            )

            // Center dot
            paint.style = Paint.Style.FILL
            canvas.drawCircle(centerX, centerY, 8f, paint)

            // Digital time (left side)
            val timeStr = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), minute)
            paint.apply {
                color = palette.primary
                textSize = 72f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                textAlign = Paint.Align.LEFT
                letterSpacing = -0.05f
            }
            canvas.drawText(timeStr, 48f, height / 2f + 20f, paint)

            return bitmap
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, ClockAnalogWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}
