package com.example.dotos.widgets

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.dotos.MainActivity
import com.example.dotos.R
import java.util.*

class CalendarMonthWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val bitmap = renderMonthCalendar(context)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderMonthCalendar(context: Context): Bitmap {
            // 2x2 Square widget - 400x400
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

                val palette = WidgetTheme.palette(context)

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

            val calendar = Calendar.getInstance()
            val today = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())?.uppercase()
            
            // Month label at top
            paint.apply {
                color = palette.label
                textSize = 24f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.15f
            }
            canvas.drawText(month ?: "MONTH", size / 2f, 60f, paint)

            // Week day labels
            val days = arrayOf("M", "T", "W", "T", "F", "S", "S")
            val startX = 40f
            val startY = 100f
            val cellSize = 45f
            
            paint.apply {
                color = palette.muted
                textSize = 16f
                textAlign = Paint.Align.CENTER
            }
            
            for (i in days.indices) {
                canvas.drawText(days[i], startX + i * cellSize, startY, paint)
            }

            // Get first day of month
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Adjust to Monday start
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            // Draw day numbers in grid
            paint.apply {
                color = Color.WHITE
                textSize = 18f
                textAlign = Paint.Align.CENTER
            }

            var day = 1
            for (week in 0 until 6) {
                for (dayOfWeek in 0 until 7) {
                    if (week == 0 && dayOfWeek < firstDayOfWeek) continue
                    if (day > daysInMonth) break
                    
                    val x = startX + dayOfWeek * cellSize
                    val y = startY + 40f + week * cellSize
                    
                    // Highlight today with red border
                    if (day == today) {
                        paint.style = Paint.Style.STROKE
                            paint.color = palette.accent
                        paint.strokeWidth = 2f
                        canvas.drawRoundRect(
                            x - 18f, y - 22f, x + 18f, y + 8f,
                            4f, 4f, paint
                        )
                        paint.style = Paint.Style.FILL
                        paint.color = palette.primary
                    }
                    
                    canvas.drawText(day.toString(), x, y, paint)
                    day++
                }
            }

            return bitmap
        }
    }
}
