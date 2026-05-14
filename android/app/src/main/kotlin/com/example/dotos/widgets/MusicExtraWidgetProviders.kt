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
import android.view.KeyEvent
import android.widget.RemoteViews
import com.example.dotos.R
import com.example.dotos.widgets.WidgetTheme.KEY_ARTIST
import com.example.dotos.widgets.WidgetTheme.KEY_PLAYING
import com.example.dotos.widgets.WidgetTheme.KEY_PROGRESS
import com.example.dotos.widgets.WidgetTheme.KEY_TRACK
import com.example.dotos.widgets.WidgetTheme.MUSIC_PREFS_NAME

// ─── music_03: Compact 2×2 ────────────────────────────────────────────────
class MusicCompactWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_MUSIC_COMPACT_TOGGLE") {
            MusicNotificationListener.handleMediaControl(context, "TOGGLE")
        }
    }
    companion object {
        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val palette = WidgetTheme.palette(context)
            views.setImageViewBitmap(R.id.widget_image, render(context, palette))
            views.setOnClickPendingIntent(R.id.widget_image, PendingIntent.getBroadcast(context, id + 3000,
                Intent(context, MusicCompactWidgetProvider::class.java).apply { action = "ACTION_MUSIC_COMPACT_TOGGLE" },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            mgr.updateAppWidget(id, views)
        }
        fun refreshWidget(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            for (id in mgr.getAppWidgetIds(ComponentName(context, MusicCompactWidgetProvider::class.java))) updateWidget(context, mgr, id)
        }
        fun render(context: Context, palette: WidgetTheme.Palette): Bitmap {
            val prefs = context.getSharedPreferences(MUSIC_PREFS_NAME, Context.MODE_PRIVATE)
            val isPlaying = prefs.getBoolean(KEY_PLAYING, false)
            val track = prefs.getString(KEY_TRACK, "") ?: ""
            val artist = prefs.getString(KEY_ARTIST, "") ?: ""
            val progress = prefs.getFloat(KEY_PROGRESS, 0f)
            val w = 400; val h = 400
            val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm); val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            canvas.drawColor(palette.background)
            // Dot grid
            paint.color = Color.argb(25, Color.red(palette.grid), Color.green(palette.grid), Color.blue(palette.grid))
            for (x in 14 until w step 24) for (y in 14 until h step 24) canvas.drawCircle(x.toFloat(), y.toFloat(), 1f, paint)
            // Vinyl disc
            val cx = w / 2f; val cy = 155f; val r = 108f
            paint.style = Paint.Style.FILL; paint.color = palette.grid; canvas.drawCircle(cx, cy, r, paint)
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 2f; paint.color = palette.border; canvas.drawCircle(cx, cy, r, paint)
            paint.strokeWidth = 0.8f; paint.color = Color.argb(45, Color.red(palette.label), Color.green(palette.label), Color.blue(palette.label))
            for (gr in (r - 16f).toInt() downTo 36 step 16) canvas.drawCircle(cx, cy, gr.toFloat(), paint)
            paint.style = Paint.Style.FILL; paint.color = palette.background; canvas.drawCircle(cx, cy, 30f, paint)
            paint.color = if (isPlaying) palette.accent else palette.label
            paint.textSize = 26f; paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            canvas.drawText("\u266B", cx, cy + 10f, paint)
            if (isPlaying) { paint.color = palette.accent; canvas.drawCircle(cx + r - 5f, cy - r + 5f, 9f, paint) }
            // Track info
            paint.textAlign = Paint.Align.CENTER; paint.color = palette.label; paint.textSize = 11f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL); paint.letterSpacing = 0.14f
            canvas.drawText(if (track.isNotEmpty() || isPlaying) "NOW PLAYING" else "NOTHING PLAYING", cx, 294f, paint)
            paint.color = palette.primary; paint.textSize = if (track.length > 12) 26f else 32f
            paint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); paint.letterSpacing = -0.02f
            if (track.isNotEmpty()) canvas.drawText(track.take(16), cx, 328f, paint)
            paint.color = palette.secondary; paint.textSize = 14f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL); paint.letterSpacing = 0.05f
            if (artist.isNotEmpty()) canvas.drawText(artist.take(20), cx, 350f, paint)
            // Progress bar
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f; paint.strokeCap = Paint.Cap.ROUND
            paint.color = palette.border; canvas.drawLine(32f, 376f, w - 32f, 376f, paint)
            val fw = (w - 64f) * progress.coerceIn(0f, 1f)
            paint.color = palette.accent; canvas.drawLine(32f, 376f, 32f + fw, 376f, paint)
            if (progress > 0f) { paint.style = Paint.Style.FILL; canvas.drawCircle(32f + fw, 376f, 6f, paint) }
            return bm
        }
    }
}

// ─── music_04: Minimal Ticker 4×1 ────────────────────────────────────────
class MusicTickerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_MUSIC_TICKER_TOGGLE") {
            MusicNotificationListener.handleMediaControl(context, "TOGGLE")
        }
    }
    companion object {
        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val palette = WidgetTheme.palette(context)
            views.setImageViewBitmap(R.id.widget_image, render(context, palette))
            views.setOnClickPendingIntent(R.id.widget_image, PendingIntent.getBroadcast(context, id + 4000,
                Intent(context, MusicTickerWidgetProvider::class.java).apply { action = "ACTION_MUSIC_TICKER_TOGGLE" },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            mgr.updateAppWidget(id, views)
        }
        fun refreshWidget(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            for (id in mgr.getAppWidgetIds(ComponentName(context, MusicTickerWidgetProvider::class.java))) updateWidget(context, mgr, id)
        }
        fun render(context: Context, palette: WidgetTheme.Palette): Bitmap {
            val prefs = context.getSharedPreferences(MUSIC_PREFS_NAME, Context.MODE_PRIVATE)
            val isPlaying = prefs.getBoolean(KEY_PLAYING, false)
            val track = prefs.getString(KEY_TRACK, "") ?: ""
            val artist = prefs.getString(KEY_ARTIST, "") ?: ""
            val progress = prefs.getFloat(KEY_PROGRESS, 0f)
            val w = 800; val h = 160
            val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm); val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            canvas.drawColor(palette.background)
            paint.style = Paint.Style.FILL; paint.color = palette.accent
            canvas.drawRect(0f, 18f, 4f, h - 18f, paint)
            paint.color = if (isPlaying) palette.accent else palette.label
            paint.textSize = 34f; paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            canvas.drawText("\u266B", 44f, h / 2f + 12f, paint)
            paint.textAlign = Paint.Align.LEFT
            paint.color = palette.primary; paint.textSize = 30f
            paint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); paint.letterSpacing = -0.02f
            canvas.drawText(if (track.isNotEmpty()) track.take(24) else "NOTHING PLAYING", 80f, 62f, paint)
            paint.color = palette.secondary; paint.textSize = 16f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL); paint.letterSpacing = 0.04f
            if (artist.isNotEmpty()) canvas.drawText(artist.take(32), 80f, 88f, paint)
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f; paint.strokeCap = Paint.Cap.ROUND
            paint.color = palette.border; canvas.drawLine(80f, 116f, w - 110f, 116f, paint)
            val fw = (w - 190f) * progress.coerceIn(0f, 1f)
            paint.color = if (isPlaying) palette.accent else palette.muted; canvas.drawLine(80f, 116f, 80f + fw, 116f, paint)
            val ppx = w - 52f; val ppy = h / 2f - 8f
            paint.style = Paint.Style.FILL; paint.color = palette.primary; canvas.drawCircle(ppx, ppy, 26f, paint)
            paint.color = palette.background
            if (isPlaying) {
                canvas.drawRect(ppx - 9f, ppy - 11f, ppx - 3f, ppy + 11f, paint)
                canvas.drawRect(ppx + 3f, ppy - 11f, ppx + 9f, ppy + 11f, paint)
            } else {
                val pp = android.graphics.Path(); pp.moveTo(ppx - 7f, ppy - 11f); pp.lineTo(ppx + 11f, ppy); pp.lineTo(ppx - 7f, ppy + 11f); pp.close()
                canvas.drawPath(pp, paint)
            }
            return bm
        }
    }
}

// ─── music_05: EQ Wave 2×2 ───────────────────────────────────────────────
class MusicWaveWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) updateWidget(context, appWidgetManager, id)
    }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_MUSIC_WAVE_TOGGLE") {
            MusicNotificationListener.handleMediaControl(context, "TOGGLE")
        }
    }
    companion object {
        private var animHandler: android.os.Handler? = null
        private var isAnimating = false

        fun updateWidget(context: Context, mgr: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val palette = WidgetTheme.palette(context)
            views.setImageViewBitmap(R.id.widget_image, render(context, palette))
            views.setOnClickPendingIntent(R.id.widget_image, PendingIntent.getBroadcast(context, id + 5000,
                Intent(context, MusicWaveWidgetProvider::class.java).apply { action = "ACTION_MUSIC_WAVE_TOGGLE" },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            mgr.updateAppWidget(id, views)
        }
        
        fun refreshWidget(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            for (id in mgr.getAppWidgetIds(ComponentName(context, MusicWaveWidgetProvider::class.java))) {
                updateWidget(context, mgr, id)
            }
            
            // Manage animation loop
            val prefs = context.getSharedPreferences(MUSIC_PREFS_NAME, Context.MODE_PRIVATE)
            val isPlaying = prefs.getBoolean(KEY_PLAYING, false)
            if (isPlaying && !isAnimating) {
                isAnimating = true
                if (animHandler == null) animHandler = android.os.Handler(android.os.Looper.getMainLooper())
                val runnable = object : Runnable {
                    override fun run() {
                        if (!isAnimating) return
                        if (context.getSharedPreferences(MUSIC_PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_PLAYING, false)) {
                            val am = AppWidgetManager.getInstance(context)
                            for (id in am.getAppWidgetIds(ComponentName(context, MusicWaveWidgetProvider::class.java))) {
                                updateWidget(context, am, id)
                            }
                            animHandler?.postDelayed(this, 100L) // 10 FPS
                        } else {
                            isAnimating = false
                        }
                    }
                }
                animHandler?.postDelayed(runnable, 100L)
            } else if (!isPlaying) {
                isAnimating = false
            }
        }
        fun render(context: Context, palette: WidgetTheme.Palette): Bitmap {
            val prefs = context.getSharedPreferences(MUSIC_PREFS_NAME, Context.MODE_PRIVATE)
            val isPlaying = prefs.getBoolean(KEY_PLAYING, false)
            val track = prefs.getString(KEY_TRACK, "") ?: ""
            val artist = prefs.getString(KEY_ARTIST, "") ?: ""
            val progress = prefs.getFloat(KEY_PROGRESS, 0f)
            val w = 400; val h = 400
            val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm); val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            canvas.drawColor(palette.background)
            paint.color = Color.argb(22, Color.red(palette.grid), Color.green(palette.grid), Color.blue(palette.grid))
            for (x in 14 until w step 24) for (y in 14 until h step 24) canvas.drawCircle(x.toFloat(), y.toFloat(), 1f, paint)
            paint.style = Paint.Style.FILL; paint.color = palette.label; paint.textSize = 11f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL); paint.letterSpacing = 0.14f; paint.textAlign = Paint.Align.LEFT
            canvas.drawText(if (track.isNotEmpty() || isPlaying) "NOW PLAYING" else "NOTHING PLAYING", 22f, 40f, paint)
            paint.color = palette.primary; paint.textSize = if (track.length > 12) 26f else 34f
            paint.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL); paint.letterSpacing = -0.02f
            if (track.isNotEmpty()) canvas.drawText(track.take(16), 22f, 82f, paint)
            paint.color = palette.secondary; paint.textSize = 13f
            paint.typeface = Typeface.create("monospace", Typeface.NORMAL); paint.letterSpacing = 0.04f
            if (artist.isNotEmpty()) canvas.drawText(artist.take(22), 22f, 104f, paint)
            // EQ bars
            val barH = floatArrayOf(0.3f,0.55f,0.75f,0.95f,0.8f,0.65f,0.9f,1f,0.88f,0.7f,0.6f,0.82f,0.94f,0.72f,0.58f,0.84f,0.68f,0.48f,0.38f,0.22f)
            val bw = 11f; val gap = 5f; val totalW = barH.size * bw + (barH.size - 1) * gap
            val sx = (w - totalW) / 2f; val baseY = 300f; val maxH = 150f
            paint.style = Paint.Style.FILL
            val time = System.currentTimeMillis()
            for (i in barH.indices) {
                val bx = sx + i * (bw + gap)
                var bh = barH[i]
                if (isPlaying) {
                    // Dynamic wobble based on time and index to simulate active EQ
                    val wobble = kotlin.math.sin((time + i * 200) / 150.0).toFloat() * 0.6f
                    bh = (bh + wobble).coerceIn(0.15f, 1.0f)
                }
                val barHeight = if (isPlaying) bh * maxH else 6f
                val alpha = if (isPlaying) 255 else 70
                paint.color = Color.argb(alpha, Color.red(palette.accent), Color.green(palette.accent), Color.blue(palette.accent))
                canvas.drawRoundRect(android.graphics.RectF(bx, baseY - barHeight, bx + bw, baseY), 3f, 3f, paint)
            }
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f; paint.strokeCap = Paint.Cap.ROUND
            paint.color = palette.border; canvas.drawLine(22f, 328f, w - 22f, 328f, paint)
            val fw = (w - 44f) * progress.coerceIn(0f, 1f)
            paint.color = if (isPlaying) palette.accent else palette.muted; canvas.drawLine(22f, 328f, 22f + fw, 328f, paint)
            val ppx = w / 2f; val ppy = 375f
            paint.style = Paint.Style.FILL; paint.color = palette.primary; canvas.drawCircle(ppx, ppy, 22f, paint)
            paint.color = palette.background
            if (isPlaying) {
                canvas.drawRect(ppx - 8f, ppy - 10f, ppx - 3f, ppy + 10f, paint)
                canvas.drawRect(ppx + 3f, ppy - 10f, ppx + 8f, ppy + 10f, paint)
            } else {
                val pp = android.graphics.Path(); pp.moveTo(ppx - 7f, ppy - 10f); pp.lineTo(ppx + 10f, ppy); pp.lineTo(ppx - 7f, ppy + 10f); pp.close()
                canvas.drawPath(pp, paint)
            }
            return bm
        }
    }
}
