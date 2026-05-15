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
import android.net.Uri
import android.provider.CalendarContract
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.example.dotos.MainActivity
import com.example.dotos.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarWidgetProvider : AppWidgetProvider() {

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
            val bitmap = renderCalendar(context, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderCalendar(context: Context, appWidgetId: Int): Bitmap {
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "calendar")
            val width = 800
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            // Background
            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 48f, 48f, paint)

            // Dot Grid Pattern - INCREASED VISIBILITY
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
            paint.style = Paint.Style.FILL

            // Label "CALENDAR"
            paint.apply {
                color = palette.label
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
            }
            canvas.drawText("CALENDAR", 48f, 64f, paint)

            // Get calendar data
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val nextEvent = getNextCalendarEvent(context)
            
            // Day number in DOT-MATRIX STYLE (left side, upper portion)
            DotMatrixRenderer.drawDotMatrixText(
                canvas = canvas,
                text = day.toString(),
                x = 48f,
                y = 120f,  // Positioned higher to avoid overlap
                dotSize = 14f,
                dotSpacing = 5f,
                color = palette.primary
            )

            // Month and day name (bottom left, well below day number)
            val dateFormat = SimpleDateFormat("MMM, EEE", Locale.getDefault())
            val dateStr = dateFormat.format(Date()).uppercase()
            
            paint.apply {
                color = palette.label
                textSize = 22f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.05f
            }
            canvas.drawText(dateStr, 48f, height - 48f, paint)

            // Event info (right side)
            if (nextEvent != null) {
                paint.apply {
                    color = palette.label
                    textSize = 20f
                    letterSpacing = 0.08f
                }
                canvas.drawText("NEXT EVENT", width / 2f + 20f, 120f, paint)
                
                paint.apply {
                    color = palette.primary
                    textSize = 28f
                }
                // Truncate event title if too long
                val maxWidth = width / 2f - 80f
                var eventTitle = nextEvent.title
                while (paint.measureText(eventTitle) > maxWidth && eventTitle.length > 3) {
                    eventTitle = eventTitle.substring(0, eventTitle.length - 1)
                }
                if (eventTitle != nextEvent.title) eventTitle += "..."
                canvas.drawText(eventTitle, width / 2f + 20f, 170f, paint)
                
                paint.apply {
                    color = palette.label
                    textSize = 22f
                }
                canvas.drawText(nextEvent.time, width / 2f + 20f, 210f, paint)
            } else {
                paint.apply {
                    color = palette.muted
                    textSize = 24f
                    letterSpacing = 0.08f
                }
                canvas.drawText("NO UPCOMING", width / 2f + 20f, height / 2f - 10f, paint)
                canvas.drawText("EVENTS", width / 2f + 20f, height / 2f + 30f, paint)
            }

            return bitmap
        }
        
        private fun getNextCalendarEvent(context: Context): CalendarEvent? {
            // Check permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) 
                != PackageManager.PERMISSION_GRANTED) {
                return null
            }
            
            try {
                val now = System.currentTimeMillis()
                val endTime = now + (7 * 24 * 60 * 60 * 1000) // Next 7 days
                
                val projection = arrayOf(
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND
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
                    if (it.moveToFirst()) {
                        val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
                        val startIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
                        
                        if (titleIndex >= 0 && startIndex >= 0) {
                            val title = it.getString(titleIndex) ?: "Untitled Event"
                            val startMillis = it.getLong(startIndex)
                            
                            val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            val timeStr = timeFormat.format(Date(startMillis))
                            
                            return CalendarEvent(title, timeStr)
                        }
                    }
                }
            } catch (e: Exception) {
                // Permission denied or error reading calendar
                return null
            }
            
            return null
        }
    }
    
    data class CalendarEvent(val title: String, val time: String)
}
