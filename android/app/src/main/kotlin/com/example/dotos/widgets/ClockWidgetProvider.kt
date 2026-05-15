package com.example.dotos.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R
import java.text.SimpleDateFormat
import java.util.*

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        
        // Schedule next update at the start of next minute
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

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) { WidgetTheme.removeWidgetTheme(context, id) }
    }

    companion object {
        private const val ACTION_UPDATE = "com.example.dotos.ACTION_UPDATE_CLOCK"
        
        private fun scheduleNextUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockWidgetProvider::class.java).apply {
                action = ACTION_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Calculate time until next minute
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                add(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            // Use setExactAndAllowWhileIdle for precise updates
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        
        private fun cancelUpdates(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockWidgetProvider::class.java).apply {
                action = ACTION_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
        
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderClock(context, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderClock(context: Context, appWidgetId: Int): Bitmap {
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "clock")
            val width = 800
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            // 1. Background
            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 48f, 48f, paint)

            // 2. Dot Grid Pattern (Signature Nothing Look) - INCREASED VISIBILITY
            paint.color = palette.grid
            paint.alpha = 102  // Increased from 40 to 102 (40% of 255)
            paint.style = Paint.Style.FILL
            val spacing = 32f
            val dotRadius = 3f  // Increased from 2f to 3f
            for (x in 0 until (width / spacing.toInt() + 1)) {
                for (y in 0 until (height / spacing.toInt() + 1)) {
                    canvas.drawCircle(x * spacing, y * spacing, dotRadius, paint)
                }
            }
            paint.alpha = 255
            paint.style = Paint.Style.FILL

            // 3. Label "TIME" (Match Flutter)
            paint.apply {
                color = palette.label
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
            }
            canvas.drawText("TIME", 48f, 64f, paint)

            // 4. Time in DOT-MATRIX STYLE (Nothing Phone aesthetic)
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = sdf.format(Date())
            
            // Render time with dot-matrix style
            DotMatrixRenderer.drawDotMatrixText(
                canvas = canvas,
                text = time,
                x = 48f,
                y = height / 2f - 40f,
                dotSize = 12f,  // Size of each dot
                dotSpacing = 4f,  // Space between dots
                color = palette.primary
            )
            
            // 5. Date (Match Flutter)
            val dateSdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            val dateStr = dateSdf.format(Date()).uppercase()
            
            paint.apply {
                color = palette.secondary
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                letterSpacing = 0.05f
            }
            canvas.drawText(dateStr, 48f, height - 48f, paint)

            // 6. Clock Icon (Right side - match Flutter)
            val iconX = width - 80f
            val iconY = height / 2f
            paint.apply {
                color = palette.label
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            // Clock circle
            canvas.drawCircle(iconX, iconY, 32f, paint)
            // Hour hand (pointing up-right)
            canvas.drawLine(iconX, iconY, iconX, iconY - 18f, paint)
            // Minute hand (pointing right)
            canvas.drawLine(iconX, iconY, iconX + 18f, iconY, paint)

            return bitmap
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, ClockWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
}
