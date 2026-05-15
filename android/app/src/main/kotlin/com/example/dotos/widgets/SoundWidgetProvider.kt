package com.example.dotos.widgets

import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.widget.RemoteViews
import com.example.dotos.R

private const val ACTION_TOGGLE_SOUND = "com.example.dotos.widgets.ACTION_TOGGLE_SOUND"
private const val WIDGET_SIZE = 400
private const val CORNER_RADIUS = 48f

private fun drawBase(canvas: Canvas, paint: Paint, palette: WidgetTheme.Palette) {
    paint.color = palette.background
    canvas.drawRoundRect(0f, 0f, WIDGET_SIZE.toFloat(), WIDGET_SIZE.toFloat(), CORNER_RADIUS, CORNER_RADIUS, paint)

    paint.color = palette.grid
    paint.alpha = 80
    paint.style = Paint.Style.FILL
    val spacing = 32f
    val dotRadius = 2.5f
    for (x in 0 until (WIDGET_SIZE / spacing.toInt() + 1)) {
        for (y in 0 until (WIDGET_SIZE / spacing.toInt() + 1)) {
            canvas.drawCircle(x * spacing, y * spacing, dotRadius, paint)
        }
    }
    paint.alpha = 255
}

private fun drawLabel(canvas: Canvas, paint: Paint, palette: WidgetTheme.Palette, text: String) {
    paint.apply {
        color = palette.label
        textSize = 24f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textAlign = Paint.Align.LEFT
        letterSpacing = 0.08f
    }
    canvas.drawText(text, 36f, 56f, paint)
}

private fun modeLabel(mode: Int): String {
    return when (mode) {
        AudioManager.RINGER_MODE_VIBRATE -> "VIBRATE"
        AudioManager.RINGER_MODE_SILENT -> "SILENT"
        else -> "RING"
    }
}

private fun modeIndex(mode: Int): Int {
    return when (mode) {
        AudioManager.RINGER_MODE_VIBRATE -> 1
        AudioManager.RINGER_MODE_SILENT -> 2
        else -> 0
    }
}

private fun hasNotificationPolicyAccess(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return true
    }
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return notificationManager.isNotificationPolicyAccessGranted
}

private fun openDndSettings(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return
    }
    val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun handleToggle(context: Context) {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val currentMode = audioManager.ringerMode
    val nextMode = when (currentMode) {
        AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
        AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
        else -> AudioManager.RINGER_MODE_NORMAL
    }

    if (nextMode == AudioManager.RINGER_MODE_SILENT && !hasNotificationPolicyAccess(context)) {
        openDndSettings(context)
        updateAllSoundWidgets(context)
        return
    }

    audioManager.ringerMode = nextMode
    updateAllSoundWidgets(context)
}

private fun updateAllSoundWidgets(context: Context) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    SoundWidgetProvider.updateAll(context, appWidgetManager)
    SoundSegmentsWidgetProvider.updateAll(context, appWidgetManager)
    SoundDialWidgetProvider.updateAll(context, appWidgetManager)
}

class SoundWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_TOGGLE_SOUND -> handleToggle(context)
            AudioManager.RINGER_MODE_CHANGED_ACTION -> updateAllInstances(context)
        }
    }

    private fun updateAllInstances(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAll(context, appWidgetManager)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) { WidgetTheme.removeWidgetTheme(context, id) }
    }

    companion object {
        fun updateAll(context: Context, appWidgetManager: AppWidgetManager) {
            val component = ComponentName(context, SoundWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(component)
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentMode = audioManager.ringerMode
            val hasDndAccess = hasNotificationPolicyAccess(context)
            val bitmap = renderClassicWidget(context, currentMode, hasDndAccess, appWidgetId)

            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, SoundWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_SOUND
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

class SoundSegmentsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_SOUND -> handleToggle(context)
            AudioManager.RINGER_MODE_CHANGED_ACTION -> updateAllInstances(context)
        }
    }

    private fun updateAllInstances(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAll(context, appWidgetManager)
    }

    companion object {
        fun updateAll(context: Context, appWidgetManager: AppWidgetManager) {
            val component = ComponentName(context, SoundSegmentsWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(component)
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentMode = audioManager.ringerMode
            val hasDndAccess = hasNotificationPolicyAccess(context)
            val bitmap = renderSegmentsWidget(context, currentMode, hasDndAccess, appWidgetId)

            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, SoundSegmentsWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_SOUND
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

class SoundDialWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_TOGGLE_SOUND -> handleToggle(context)
            AudioManager.RINGER_MODE_CHANGED_ACTION -> updateAllInstances(context)
        }
    }

    private fun updateAllInstances(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateAll(context, appWidgetManager)
    }

    companion object {
        fun updateAll(context: Context, appWidgetManager: AppWidgetManager) {
            val component = ComponentName(context, SoundDialWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(component)
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentMode = audioManager.ringerMode
            val hasDndAccess = hasNotificationPolicyAccess(context)
            val bitmap = renderDialWidget(context, currentMode, hasDndAccess, appWidgetId)

            views.setImageViewBitmap(R.id.widget_image, bitmap)

            val intent = Intent(context, SoundDialWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_SOUND
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

private fun renderClassicWidget(context: Context, mode: Int, hasDndAccess: Boolean, appWidgetId: Int): Bitmap {
    val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "sound")
    val bitmap = Bitmap.createBitmap(WIDGET_SIZE, WIDGET_SIZE, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { isAntiAlias = true }

    drawBase(canvas, paint, palette)
    drawLabel(canvas, paint, palette, "SOUND")

    paint.apply {
        color = palette.primary
        textSize = 56f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
        letterSpacing = -0.02f
    }
    canvas.drawText(modeLabel(mode), 36f, 160f, paint)

    paint.apply {
        color = if (hasDndAccess) palette.muted else palette.accent
        textSize = 18f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textAlign = Paint.Align.LEFT
        letterSpacing = 0.08f
    }
    val hint = if (hasDndAccess) "TAP TO TOGGLE" else "ALLOW DND"
    canvas.drawText(hint, 36f, 220f, paint)

    return bitmap
}

private fun renderSegmentsWidget(context: Context, mode: Int, hasDndAccess: Boolean, appWidgetId: Int): Bitmap {
    val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "sound")
    val bitmap = Bitmap.createBitmap(WIDGET_SIZE, WIDGET_SIZE, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { isAntiAlias = true }

    drawBase(canvas, paint, palette)
    drawLabel(canvas, paint, palette, "MODE")

    val barWidth = 320f
    val barHeight = 48f
    val gap = 8f
    val left = (WIDGET_SIZE - barWidth) / 2f
    val top = 120f
    val segmentWidth = (barWidth - gap * 2) / 3f
    val labels = listOf("RING", "VIB", "SIL")
    val activeIndex = modeIndex(mode)

    for (i in labels.indices) {
        val segmentLeft = left + i * (segmentWidth + gap)
        val rect = RectF(segmentLeft, top, segmentLeft + segmentWidth, top + barHeight)
        val isActive = i == activeIndex

        if (isActive) {
            paint.style = Paint.Style.FILL
            paint.color = palette.primary
            canvas.drawRoundRect(rect, 8f, 8f, paint)
        }

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = palette.border
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.apply {
            color = if (isActive) palette.background else palette.label
            textSize = 16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText(labels[i], rect.centerX(), rect.centerY() + 6f, paint)
    }

    paint.apply {
        color = if (hasDndAccess) palette.muted else palette.accent
        textSize = 18f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textAlign = Paint.Align.LEFT
        letterSpacing = 0.08f
    }
    val hint = if (hasDndAccess) "TAP TO TOGGLE" else "ALLOW DND"
    canvas.drawText(hint, 36f, 220f, paint)

    return bitmap
}

private fun renderDialWidget(context: Context, mode: Int, hasDndAccess: Boolean, appWidgetId: Int): Bitmap {
    val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "sound")
    val bitmap = Bitmap.createBitmap(WIDGET_SIZE, WIDGET_SIZE, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply { isAntiAlias = true }

    drawBase(canvas, paint, palette)
    drawLabel(canvas, paint, palette, "SOUND")

    val centerX = WIDGET_SIZE / 2f
    val centerY = WIDGET_SIZE / 2f + 10f
    val radius = 110f

    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 8f
    paint.color = palette.border
    canvas.drawCircle(centerX, centerY, radius, paint)

    val activeIndex = modeIndex(mode)
    val angles = listOf(-90f, 30f, 150f)
    for (i in angles.indices) {
        val angle = Math.toRadians(angles[i].toDouble())
        val inner = radius - 14f
        val outer = radius + 10f
        val startX = centerX + (inner * Math.cos(angle)).toFloat()
        val startY = centerY + (inner * Math.sin(angle)).toFloat()
        val endX = centerX + (outer * Math.cos(angle)).toFloat()
        val endY = centerY + (outer * Math.sin(angle)).toFloat()

        paint.color = if (i == activeIndex) palette.primary else palette.border
        paint.strokeWidth = if (i == activeIndex) 10f else 6f
        canvas.drawLine(startX, startY, endX, endY, paint)
    }

    paint.style = Paint.Style.FILL
    paint.apply {
        color = palette.primary
        textSize = 44f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
        letterSpacing = -0.02f
    }
    canvas.drawText(modeLabel(mode), centerX, centerY + 16f, paint)

    paint.apply {
        color = if (hasDndAccess) palette.muted else palette.accent
        textSize = 18f
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.08f
    }
    val hint = if (hasDndAccess) "TAP TO TOGGLE" else "ALLOW DND"
    canvas.drawText(hint, centerX, WIDGET_SIZE - 48f, paint)

    return bitmap
}
