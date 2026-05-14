package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.widget.RemoteViews
import com.example.dotos.R
import com.example.dotos.widgets.WidgetTheme.KEY_ARTIST
import com.example.dotos.widgets.WidgetTheme.KEY_PLAYING
import com.example.dotos.widgets.WidgetTheme.KEY_PROGRESS
import com.example.dotos.widgets.WidgetTheme.KEY_TRACK
import com.example.dotos.widgets.WidgetTheme.MUSIC_PREFS_NAME

class MusicPlayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_MUSIC_PLAYER_TOGGLE") {
            MusicNotificationListener.handleMediaControl(context, "TOGGLE")
        }
    }

    companion object {
        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_music_player)
            val palette = WidgetTheme.palette(context)
            views.setImageViewBitmap(R.id.music_player_image, render(context, palette))
            views.setOnClickPendingIntent(
                R.id.music_player_image,
                PendingIntent.getBroadcast(
                    context,
                    id + 2500,
                    Intent(context, MusicPlayerWidgetProvider::class.java).apply {
                        action = "ACTION_MUSIC_PLAYER_TOGGLE"
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            mgr.updateAppWidget(id, views)
        }

        fun refreshWidget(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            for (id in mgr.getAppWidgetIds(ComponentName(context, MusicPlayerWidgetProvider::class.java))) {
                updateWidget(context, mgr, id)
            }
        }

        fun render(context: Context, palette: WidgetTheme.Palette): Bitmap {
            val prefs = context.getSharedPreferences(MUSIC_PREFS_NAME, Context.MODE_PRIVATE)
            val isPlaying = prefs.getBoolean(KEY_PLAYING, false)
            val track = prefs.getString(KEY_TRACK, "") ?: ""
            val artist = prefs.getString(KEY_ARTIST, "") ?: ""
            val progress = prefs.getFloat(KEY_PROGRESS, 0f).coerceIn(0f, 1f)

            val size = 400
            val bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            canvas.drawColor(palette.background)

            paint.style = Paint.Style.FILL
            paint.color = Color.argb(
                24,
                Color.red(palette.grid),
                Color.green(palette.grid),
                Color.blue(palette.grid),
            )
            for (x in 14 until size step 24) {
                for (y in 14 until size step 24) {
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 1f, paint)
                }
            }

            val cx = size / 2f
            val cy = 156f
            val ringRadius = 78f
            val ringBounds = RectF(cx - ringRadius, cy - ringRadius, cx + ringRadius, cy + ringRadius)

            paint.style = Paint.Style.STROKE
            paint.strokeCap = Paint.Cap.ROUND
            paint.strokeWidth = 8f
            paint.color = palette.border
            canvas.drawArc(ringBounds, 0f, 360f, false, paint)

            paint.color = palette.accent
            canvas.drawArc(ringBounds, -90f, 360f * progress, false, paint)

            paint.style = Paint.Style.FILL
            paint.color = palette.background
            canvas.drawCircle(cx, cy, 54f, paint)

            paint.color = if (isPlaying) palette.accent else palette.label
            paint.textSize = 24f
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            canvas.drawText("\u266B", cx, cy + 9f, paint)

            if (isPlaying) {
                paint.color = palette.accent
                canvas.drawCircle(cx + 66f, cy - 66f, 8f, paint)
            }

            paint.textAlign = Paint.Align.CENTER
            paint.color = palette.label
            paint.textSize = 11f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL)
            canvas.drawText(if (track.isNotEmpty() || isPlaying) "NOW PLAYING" else "NOTHING PLAYING", cx, 300f, paint)

            paint.color = palette.primary
            paint.textSize = if (track.length > 12) 24f else 30f
            paint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            paint.letterSpacing = -0.02f
            canvas.drawText(if (track.isNotEmpty()) track.take(18) else "NO TRACK", cx, 330f, paint)

            paint.color = palette.secondary
            paint.textSize = 13f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL)
            paint.letterSpacing = 0.04f
            if (artist.isNotEmpty()) {
                canvas.drawText(artist.take(24), cx, 352f, paint)
            }

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = palette.border
            canvas.drawLine(36f, 372f, size - 36f, 372f, paint)

            paint.color = if (isPlaying) palette.accent else palette.muted
            canvas.drawLine(36f, 372f, 36f + (size - 72f) * progress, 372f, paint)

            if (progress > 0f) {
                paint.style = Paint.Style.FILL
                canvas.drawCircle(36f + (size - 72f) * progress, 372f, 5f, paint)
            }

            return bm
        }
    }
}