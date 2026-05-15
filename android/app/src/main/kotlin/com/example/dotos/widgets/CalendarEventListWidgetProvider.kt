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
import android.graphics.Typeface
import android.provider.CalendarContract
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.dotos.MainActivity
import com.example.dotos.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarEventListWidgetProvider : AppWidgetProvider() {

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
            val bitmap = renderEventList(context, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderEventList(context: Context, appWidgetId: Int): Bitmap {
            val width = 800
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

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

            // Label
            paint.apply {
                color = palette.label
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
            }
            canvas.drawText("UPCOMING", 48f, 64f, paint)

            // Get events
            val events = getUpcomingEvents(context, 4)
            
            if (events.isEmpty()) {
                // No events
                paint.apply {
                    color = palette.muted
                    textSize = 24f
                }
                canvas.drawText("NO UPCOMING", 48f, height / 2f - 10f, paint)
                canvas.drawText("EVENTS", 48f, height / 2f + 30f, paint)
            } else {
                // Draw events
                var y = 120f
                val lineHeight = 70f
                
                for ((index, event) in events.withIndex()) {
                    if (index >= 4) break // Max 4 events
                    
                    // Time dot
                    paint.apply {
                        color = palette.accent
                        style = Paint.Style.FILL
                    }
                    canvas.drawCircle(48f, y + 10f, 6f, paint)
                    
                    // Vertical line (except last)
                    if (index < events.size - 1 && index < 3) {
                        paint.apply {
                            color = palette.grid
                            strokeWidth = 2f
                        }
                        canvas.drawLine(48f, y + 16f, 48f, y + lineHeight, paint)
                    }
                    
                    // Time
                    paint.apply {
                        color = palette.label
                        textSize = 20f
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                        textAlign = Paint.Align.LEFT
                        letterSpacing = 0.08f
                        style = Paint.Style.FILL
                    }
                    canvas.drawText(event.time, 72f, y + 15f, paint)
                    
                    // Event title
                    paint.apply {
                        color = palette.primary
                        textSize = 24f
                    }
                    var title = event.title
                    val maxWidth = width - 120f
                    while (paint.measureText(title) > maxWidth && title.length > 3) {
                        title = title.substring(0, title.length - 1)
                    }
                    if (title != event.title) title += "..."
                    canvas.drawText(title, 72f, y + 45f, paint)
                    
                    y += lineHeight
                }
            }

            return bitmap
        }

        private fun getUpcomingEvents(context: Context, limit: Int): List<CalendarEvent> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) 
                != PackageManager.PERMISSION_GRANTED) {
                return emptyList()
            }
            
            val events = mutableListOf<CalendarEvent>()
            
            try {
                val now = System.currentTimeMillis()
                val endTime = now + (7 * 24 * 60 * 60 * 1000) // Next 7 days
                
                val projection = arrayOf(
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART
                )
                
                val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
                val selectionArgs = arrayOf(now.toString(), endTime.toString())
                val sortOrder = "${CalendarContract.Events.DTSTART} ASC"
                
                val cursor: Cursor? = context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )
                
                cursor?.use {
                    while (it.moveToNext() && events.size < limit) {
                        val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
                        val startIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
                        
                        if (titleIndex >= 0 && startIndex >= 0) {
                            val title = it.getString(titleIndex) ?: "Untitled"
                            val startMillis = it.getLong(startIndex)
                            
                            val timeFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                            val timeStr = timeFormat.format(Date(startMillis)).uppercase()
                            
                            events.add(CalendarEvent(title.uppercase(), timeStr))
                        }
                    }
                }
            } catch (e: Exception) {
                // Permission denied or error
            }
            
            return events
        }
    }
    
    data class CalendarEvent(val title: String, val time: String)
}
