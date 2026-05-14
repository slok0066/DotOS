package com.example.dotos.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R

class BatteryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateBatteryWidget(context, appWidgetManager, appWidgetId)
        }
        BatteryWidgetUpdater.schedule(context)
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        BatteryWidgetUpdater.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        BatteryWidgetUpdater.cancelIfNoBatteryWidgets(context)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Update widget when battery changes
        if (intent.action == Intent.ACTION_BATTERY_CHANGED || 
            intent.action == Intent.ACTION_POWER_CONNECTED ||
            intent.action == Intent.ACTION_POWER_DISCONNECTED) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, BatteryWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            onUpdate(context, appWidgetManager, appWidgetIds)
            BatteryWidgetUpdater.schedule(context)
        }
    }

    private fun updateBatteryWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        
        // Get real battery data
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 0
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPct = (level * 100 / scale.toFloat()).toInt()
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                        status == BatteryManager.BATTERY_STATUS_FULL
        
        val bitmap = NothingRenderer.renderBatteryWidget(context, batteryPct, isCharging)
        views.setImageViewBitmap(R.id.widget_image, bitmap)
        
        // Add click intent
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

class StorageWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateStorageWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateStorageWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        
        // Get real storage data
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
        val bytesTotal = stat.blockSizeLong * stat.blockCountLong
        val gbUsed = (bytesTotal - bytesAvailable) / (1024.0 * 1024.0 * 1024.0)
        val gbTotal = bytesTotal / (1024.0 * 1024.0 * 1024.0)
        val usagePercent = ((bytesTotal - bytesAvailable).toFloat() / bytesTotal.toFloat())
        
        val bitmap = NothingRenderer.renderStorageWidget(context, gbUsed, gbTotal, usagePercent)
        views.setImageViewBitmap(R.id.widget_image, bitmap)
        
        // Add click intent
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

object NothingRenderer {
    private const val WIDTH = 800
    private const val HEIGHT = 400
    private const val CORNER_RADIUS = 48f
    private const val PADDING = 48f
    
    private fun drawBackground(canvas: Canvas, paint: Paint, palette: WidgetTheme.Palette) {
        paint.color = palette.background
        canvas.drawRoundRect(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), CORNER_RADIUS, CORNER_RADIUS, paint)
        
        // Dot grid pattern - INCREASED VISIBILITY
        paint.color = palette.grid
        paint.alpha = 102  // Increased from 40 to 102 (40% of 255)
        paint.style = Paint.Style.FILL
        val spacing = 32f
        val dotRadius = 3f  // Increased from 2f to 3f
        for (x in 0 until (WIDTH / spacing.toInt() + 1)) {
            for (y in 0 until (HEIGHT / spacing.toInt() + 1)) {
                canvas.drawCircle(x * spacing, y * spacing, dotRadius, paint)
            }
        }
        paint.alpha = 255
        paint.style = Paint.Style.FILL
    }
    
    private fun drawLabel(canvas: Canvas, paint: Paint, palette: WidgetTheme.Palette, label: String) {
        paint.apply {
            color = palette.label
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.08f
        }
        canvas.drawText(label, PADDING, 64f, paint)
    }
    
    // BATTERY WIDGET - Matches Flutter BatteryCard
    fun renderBatteryWidget(context: Context, batteryPct: Int, isCharging: Boolean): Bitmap {
        val palette = WidgetTheme.palette(context)
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true }
        
        drawBackground(canvas, paint, palette)
        drawLabel(canvas, paint, palette, "BATTERY")
        
        // Battery percentage in DOT-MATRIX STYLE
        DotMatrixRenderer.drawDotMatrixText(
            canvas = canvas,
            text = "$batteryPct%",
            x = PADDING,
            y = HEIGHT / 2f - 40f,
            dotSize = 12f,
            dotSpacing = 4f,
            color = palette.primary
        )
        
        // Charging status label
        paint.apply {
            color = palette.label
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.08f
        }
        val statusText = if (isCharging) "CHARGING" else "DISCHARGING"
        canvas.drawText(statusText, PADDING, HEIGHT - 48f, paint)
        
        // Battery icon (right side) - matches Flutter design
        val iconX = WIDTH - 100f
        val iconY = HEIGHT / 2f
        paint.apply {
            color = if (isCharging) palette.success else palette.label
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        
        // Battery body
        canvas.drawRoundRect(iconX - 30f, iconY - 40f, iconX + 30f, iconY + 40f, 8f, 8f, paint)
        // Battery tip
        canvas.drawRect(iconX + 30f, iconY - 10f, iconX + 35f, iconY + 10f, paint)
        
        // Fill level
        if (batteryPct > 0) {
            paint.style = Paint.Style.FILL
            val fillHeight = (batteryPct / 100f) * 70f
            canvas.drawRect(
                iconX - 24f, 
                iconY + 34f - fillHeight, 
                iconX + 24f, 
                iconY + 34f, 
                paint
            )
        }
        
        return bitmap
    }
    
    // STORAGE WIDGET - Matches Flutter StorageCard
    fun renderStorageWidget(context: Context, gbUsed: Double, gbTotal: Double, usagePercent: Float): Bitmap {
        val palette = WidgetTheme.palette(context)
        val bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply { isAntiAlias = true }
        
        drawBackground(canvas, paint, palette)
        
        // Label and total (top row)
        paint.apply {
            color = palette.label
            textSize = 28f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.08f
        }
        canvas.drawText("STORAGE", PADDING, 64f, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 24f
        canvas.drawText("%.0f GB".format(gbTotal), WIDTH - PADDING, 64f, paint)
        
        // Used storage in DOT-MATRIX STYLE
        val usedText = "%.1f GB".format(gbUsed)
        DotMatrixRenderer.drawDotMatrixText(
            canvas = canvas,
            text = usedText,
            x = PADDING,
            y = HEIGHT / 2f - 30f,
            dotSize = 10f,
            dotSpacing = 3f,
            color = palette.primary
        )
        
        // "USED" label below the number
        paint.apply {
            color = palette.label
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.08f
        }
        canvas.drawText("USED", PADDING, HEIGHT / 2f + 40f, paint)
        
        // Progress bar (bottom)
        val barY = HEIGHT - 80f
        val barHeight = 16f
        val barWidth = WIDTH - (PADDING * 2)
        
        // Background bar
        paint.apply {
            color = palette.border
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(PADDING, barY, PADDING + barWidth, barY + barHeight, 4f, 4f, paint)
        
        // Filled bar
        paint.color = palette.primary
        val fillWidth = barWidth * usagePercent
        canvas.drawRoundRect(PADDING, barY, PADDING + fillWidth, barY + barHeight, 4f, 4f, paint)
        
        return bitmap
    }
    
}
