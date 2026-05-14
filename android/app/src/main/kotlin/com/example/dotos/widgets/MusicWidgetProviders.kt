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
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import com.example.dotos.R
import com.example.dotos.widgets.WidgetTheme.KEY_ARTIST
import com.example.dotos.widgets.WidgetTheme.KEY_PLAYING
import com.example.dotos.widgets.WidgetTheme.KEY_PROGRESS
import com.example.dotos.widgets.WidgetTheme.KEY_TRACK
import com.example.dotos.widgets.WidgetTheme.MUSIC_PREFS_NAME

// ─────────────────────────────────────────────────────────────────────────────
// Vinyl widget
// ─────────────────────────────────────────────────────────────────────────────
class MusicVinylWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) updateAppWidget(context, appWidgetManager, id, 0f)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_VINYL_SPIN") {
            animateSpin(context.applicationContext, goAsync())
        }
    }

    companion object {
        private var animHandler: Handler? = null

        private fun animateSpin(context: Context, pendingResult: PendingResult) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val prefs = context.getSharedPreferences("vinyl_widget", Context.MODE_PRIVATE)
            val currentRot = prefs.getFloat("rotation", 0f)

            val frameCount = 24
            val startTime = System.currentTimeMillis()
            val duration = 600L

            animHandler = Handler(Looper.getMainLooper())

            fun drawFrame(frame: Int) {
                if (frame > frameCount) {
                    prefs.edit().putFloat("rotation", currentRot + 90f).apply()
                    pendingResult.finish()
                    animHandler = null
                    return
                }
                val elapsed = System.currentTimeMillis() - startTime
                val t = (elapsed.toFloat() / duration).coerceAtMost(1f)
                val ease = 1f - (1f - t) * (1f - t) * (1f - t)
                val rot = currentRot + (90f * ease)

                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, MusicVinylWidgetProvider::class.java)
                )
                for (id in ids) updateAppWidget(context, appWidgetManager, id, rot)
                animHandler?.postDelayed({ drawFrame(frame + 1) }, 16L)
            }
            drawFrame(0)
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                            appWidgetId: Int, rotation: Float) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val palette = WidgetTheme.palette(context)
            views.setImageViewBitmap(R.id.widget_image, renderVinyl(context, palette, rotation))

            val intent = Intent(context, MusicVinylWidgetProvider::class.java).apply {
                action = "ACTION_VINYL_SPIN"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderVinyl(context: Context, palette: WidgetTheme.Palette, rotation: Float): Bitmap {
            val prefs = context.getSharedPreferences(MUSIC_PREFS_NAME, Context.MODE_PRIVATE)
            val trackName = prefs.getString(KEY_TRACK, "") ?: ""
            val artistName = prefs.getString(KEY_ARTIST, "") ?: ""
            val isPlaying = prefs.getBoolean(KEY_PLAYING, false)

            val size = 400
            val bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            canvas.drawColor(palette.background)

            // Dot grid
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(28,
                Color.red(palette.grid), Color.green(palette.grid), Color.blue(palette.grid))
            for (x in 16 until size step 26) {
                for (y in 16 until size step 26) {
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 1.2f, paint)
                }
            }

            val cx = size / 2f; val cy = size / 2f; val radius = 130f

            // Track info top-left
            paint.style = Paint.Style.FILL
            paint.color = palette.label
            paint.textSize = 14f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL)
            paint.letterSpacing = 0.12f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(if (trackName.isNotEmpty()) "NOW PLAYING" else "VINYL", 24f, 40f, paint)

            if (trackName.isNotEmpty()) {
                paint.color = palette.primary
                paint.textSize = 18f
                paint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
                canvas.drawText(trackName.take(18), 24f, 64f, paint)
                paint.color = palette.secondary
                paint.textSize = 12f
                paint.typeface = Typeface.create("monospace", Typeface.NORMAL)
                canvas.drawText(artistName.take(22), 24f, 82f, paint)
            }

            // Playing dot
            if (isPlaying) {
                paint.color = palette.accent
                canvas.drawCircle(size - 24f, 24f, 7f, paint)
            }

            canvas.save()
            canvas.translate(cx, cy)
            canvas.rotate(rotation)

            // Outer vinyl ring
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            paint.color = palette.primary
            canvas.drawCircle(0f, 0f, radius, paint)

            // Grooves
            paint.strokeWidth = 1.5f
            for (r in (radius - 20f).toInt() downTo 20 step 18) {
                paint.color = Color.argb(55,
                    Color.red(palette.border), Color.green(palette.border), Color.blue(palette.border))
                canvas.drawCircle(0f, 0f, r.toFloat(), paint)
            }

            // Label area
            paint.style = Paint.Style.FILL
            paint.color = palette.background
            val labelRadius = 55f
            canvas.drawCircle(0f, 0f, labelRadius, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            paint.color = palette.border
            canvas.drawCircle(0f, 0f, labelRadius, paint)

            // Label text
            paint.style = Paint.Style.FILL
            paint.textSize = 12f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL)
            paint.letterSpacing = 0.1f
            paint.textAlign = Paint.Align.CENTER
            paint.color = palette.label
            canvas.drawText("SIDE A", 0f, -10f, paint)
            paint.textSize = 10f
            paint.color = palette.muted
            canvas.drawText("33\u2153 RPM", 0f, 12f, paint)

            // Spindle
            paint.style = Paint.Style.FILL
            paint.color = palette.primary
            canvas.drawCircle(0f, 0f, 10f, paint)
            paint.color = palette.background
            canvas.drawCircle(0f, 0f, 5f, paint)

            // Accent orbit dot
            paint.color = palette.accent
            canvas.drawCircle(0f, -(radius - 18f), 10f, paint)
            paint.color = Color.argb(55,
                Color.red(palette.accent), Color.green(palette.accent), Color.blue(palette.accent))
            canvas.drawCircle(0f, -(radius - 36f), 6f, paint)

            canvas.restore()
            return bm
        }
    }
}
