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
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R
import java.util.*

class ClockBinaryWidgetProvider : AppWidgetProvider() {

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
        private const val ACTION_UPDATE = "com.example.dotos.ACTION_UPDATE_CLOCK_BINARY"
        
        private fun scheduleNextUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockBinaryWidgetProvider::class.java).apply {
                action = ACTION_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Update every minute (no seconds display)
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
            val intent = Intent(context, ClockBinaryWidgetProvider::class.java).apply {
                action = ACTION_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderBinaryClock(context)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderBinaryClock(context: Context): Bitmap {
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
            canvas.drawText("BINARY", 48f, 64f, paint)

            // Get current time
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            // Binary display - only HH:MM (4 columns)
            val startX = 120f
            val startY = 120f
            val dotSize = 20f
            val dotSpacing = 28f
            val columnSpacing = 80f

            // Draw binary representation for HH:MM only
            drawBinaryColumn(canvas, paint, startX, startY, hour / 10, "H", dotSize, dotSpacing, palette)
            drawBinaryColumn(canvas, paint, startX + columnSpacing, startY, hour % 10, "H", dotSize, dotSpacing, palette)
            
            drawBinaryColumn(canvas, paint, startX + columnSpacing * 2.5f, startY, minute / 10, "M", dotSize, dotSpacing, palette)
            drawBinaryColumn(canvas, paint, startX + columnSpacing * 3.5f, startY, minute % 10, "M", dotSize, dotSpacing, palette)

            // Digital time (right side)
            val timeStr = String.format("%02d:%02d", hour, minute)
            paint.apply {
                color = palette.primary
                textSize = 72f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                letterSpacing = -0.05f
            }
            canvas.drawText(timeStr, width - 48f, height / 2f + 20f, paint)

            // Labels
            paint.apply {
                color = palette.muted
                textSize = 20f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("DECIMAL", width - 48f, height / 2f + 60f, paint)

            return bitmap
        }

        private fun drawBinaryColumn(
            canvas: Canvas,
            paint: Paint,
            x: Float,
            y: Float,
            value: Int,
            label: String,
            dotSize: Float,
            spacing: Float,
            palette: WidgetTheme.Palette
        ) {
            // Draw 4 bits (0-9 needs max 4 bits)
            for (bit in 3 downTo 0) {
                val isOn = (value and (1 shl bit)) != 0
                paint.apply {
                    color = if (isOn) palette.primary else palette.border
                    style = Paint.Style.FILL
                }
                val dotY = y + (3 - bit) * spacing
                canvas.drawCircle(x, dotY, dotSize / 2, paint)
            }

            // Draw decimal value below
            paint.apply {
                color = palette.label
                textSize = 24f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(value.toString(), x, y + 4 * spacing + 30f, paint)

            // Draw label
            paint.apply {
                color = palette.muted
                textSize = 18f
            }
            canvas.drawText(label, x, y + 4 * spacing + 55f, paint)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, ClockBinaryWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}
