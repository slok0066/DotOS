package com.example.dotos.widgets

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.provider.CalendarContract
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.dotos.MainActivity
import com.example.dotos.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarAgendaWidgetProvider : AppWidgetProvider() {

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
            val bitmap = renderAgenda(context, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderAgenda(context: Context, appWidgetId: Int): Bitmap {
            // 4x2 Wide widget - 800x400
            val width = 800
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            // Palette
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "calendar")

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

            // Header
            val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            val dateStr = dateFormat.format(Date()).uppercase()
            
            paint.apply {
                color = palette.label
                textSize = 22f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.12f
            }
            canvas.drawText("TODAY'S AGENDA", 48f, 60f, paint)
            
            paint.textSize = 16f
            canvas.drawText(dateStr, 48f, 90f, paint)

            // Get today's events
            val events = getTodayEvents(context)
            
            if (events.isEmpty()) {
                // No events message
                paint.apply {
                    color = palette.muted
                    textSize = 24f
                    textAlign = Paint.Align.CENTER
                    letterSpacing = 0.08f
                }
                canvas.drawText("NO EVENTS TODAY", width / 2f, height / 2f, paint)
            } else {
                // Draw timeline
                val timelineX = 48f
                val startY = 140f
                val eventSpacing = 60f
                
                // Vertical timeline line
                paint.apply {
                    color = palette.grid
                    strokeWidth = 2f
                }
                canvas.drawLine(timelineX, startY, timelineX, startY + events.size * eventSpacing - 20f, paint)
                
                // Draw events
                for ((index, event) in events.withIndex()) {
                    val y = startY + index * eventSpacing
                    
                    // Timeline dot
                    paint.apply {
                        color = palette.accent
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(timelineX, y, 6f, paint)
                    
                    // Time
                    paint.apply {
                        color = palette.label
                        textSize = 18f
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.NORMAL)
                        textAlign = Paint.Align.LEFT
                        letterSpacing = 0.05f
                    }
                    canvas.drawText(event.time, timelineX + 24f, y + 6f, paint)
                    
                    // Event title
                    paint.apply {
                        color = palette.primary
                        textSize = 20f
                    }
                    // Truncate if too long
                    var title = event.title
                    val maxWidth = width - timelineX - 200f
                    while (paint.measureText(title) > maxWidth && title.length > 3) {
                        title = title.substring(0, title.length - 1)
                    }
                    if (title != event.title) title += "..."
                    canvas.drawText(title, timelineX + 180f, y + 6f, paint)
                }
            }

            return bitmap
        }
        
        private fun getTodayEvents(context: Context): List<AgendaEvent> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) 
                != PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }
            
            try {
                val events = mutableListOf<AgendaEvent>()
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfDay = calendar.timeInMillis
                
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val endOfDay = calendar.timeInMillis
                
                val projection = arrayOf(
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART
                )
                
                val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
                val selectionArgs = arrayOf(startOfDay.toString(), endOfDay.toString())
                val sortOrder = "${CalendarContract.Events.DTSTART} ASC"
                
                val cursor: Cursor? = context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )
                
                cursor?.use {
                    while (it.moveToNext() && events.size < 5) {
                        val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
                        val startIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
                        
                        if (titleIndex >= 0 && startIndex >= 0) {
                            val title = it.getString(titleIndex) ?: "Untitled"
                            val startMillis = it.getLong(startIndex)
                            
                            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                            val timeStr = timeFormat.format(Date(startMillis))
                            
                            events.add(AgendaEvent(title, timeStr))
                        }
                    }
                }
                
                return events
            } catch (e: Exception) {
                return emptyList()
            }
        }
    }
    
    data class AgendaEvent(val title: String, val time: String)
}
