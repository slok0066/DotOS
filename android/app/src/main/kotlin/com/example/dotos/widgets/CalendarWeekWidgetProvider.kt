package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
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
import java.text.SimpleDateFormat
import java.util.*

class CalendarWeekWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) { WidgetTheme.removeWidgetTheme(context, id) }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderWeekView(context, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderWeekView(context: Context, appWidgetId: Int): Bitmap {
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "calendar")
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
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
            }
            canvas.drawText("WEEK VIEW", 48f, 64f, paint)

            // Get current week
            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_WEEK)
            
            // Set to Monday
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            
            // Draw 7 days
            val startX = 48f
            val startY = 120f
            val dayWidth = 100f
            val daySpacing = 8f

            for (i in 0 until 7) {
                val x = startX + i * (dayWidth + daySpacing)
                val isToday = (i + Calendar.MONDAY) == today
                
                drawDayColumn(canvas, paint, x, startY, calendar, isToday, palette)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            // Month and year at bottom
            calendar.add(Calendar.DAY_OF_MONTH, -7) // Reset
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val monthYear = monthFormat.format(calendar.time).uppercase()
            
            paint.apply {
                color = palette.label
                textSize = 24f
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText(monthYear, 48f, height - 48f, paint)

            return bitmap
        }

        private fun drawDayColumn(
            canvas: Canvas,
            paint: Paint,
            x: Float,
            y: Float,
            calendar: Calendar,
            isToday: Boolean,
            palette: WidgetTheme.Palette
        ) {
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(calendar.time).uppercase()
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

            // Day name
            paint.apply {
                color = if (isToday) palette.primary else palette.muted
                textSize = 20f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.08f
            }
            canvas.drawText(dayOfWeek, x + 50f, y, paint)

            // Day number (larger if today)
            if (isToday) {
                // Highlight box
                paint.apply {
                    color = palette.accent
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                canvas.drawRoundRect(x + 10f, y + 20f, x + 90f, y + 120f, 8f, 8f, paint)
                
                paint.style = Paint.Style.FILL
            }

            paint.apply {
                color = if (isToday) palette.primary else palette.label
                textSize = if (isToday) 72f else 56f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(dayOfMonth.toString(), x + 50f, y + 90f, paint)
        }
    }
}
