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
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Environment
import android.os.StatFs
import android.widget.RemoteViews
import com.example.dotos.MainActivity
import com.example.dotos.R

class StorageCircularWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateStorageWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateStorageWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        
        val stat = StatFs(Environment.getDataDirectory().path)
        val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
        val bytesTotal = stat.blockSizeLong * stat.blockCountLong
        val gbUsed = (bytesTotal - bytesAvailable) / (1024.0 * 1024.0 * 1024.0)
        val gbTotal = bytesTotal / (1024.0 * 1024.0 * 1024.0)
        val usagePercent = ((bytesTotal - bytesAvailable).toFloat() / bytesTotal.toFloat())
        
        val bitmap = renderCircularStorage(context, gbUsed, gbTotal, usagePercent)
        views.setImageViewBitmap(R.id.widget_image, bitmap)
        
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, appWidgetId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        private fun renderCircularStorage(context: Context, gbUsed: Double, gbTotal: Double, usagePercent: Float): Bitmap {
            val palette = WidgetTheme.palette(context)
            val width = 800
            val height = 400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            paint.color = palette.background
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 48f, 48f, paint)

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

            paint.apply {
                color = palette.label
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.LEFT
                letterSpacing = 0.08f
            }
            canvas.drawText("STORAGE", 48f, 64f, paint)

            val centerX = width - 200f
            val centerY = height / 2f
            val outerRadius = 120f
            val innerRadius = 90f
            val strokeWidth = outerRadius - innerRadius

            paint.apply {
                color = palette.border
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
            }
            canvas.drawCircle(centerX, centerY, (outerRadius + innerRadius) / 2, paint)

            val sweepAngle = usagePercent * 360f
            paint.apply {
                color = when {
                    usagePercent >= 0.9f -> palette.accent
                    usagePercent >= 0.7f -> palette.secondary
                    else -> palette.primary
                }
            }
            val rect = RectF(
                centerX - (outerRadius + innerRadius) / 2,
                centerY - (outerRadius + innerRadius) / 2,
                centerX + (outerRadius + innerRadius) / 2,
                centerY + (outerRadius + innerRadius) / 2
            )
            canvas.drawArc(rect, -90f, sweepAngle, false, paint)

            DotMatrixRenderer.drawDotMatrixText(
                canvas = canvas,
                text = "%.0f".format(gbUsed),
                x = centerX - 40f,
                y = centerY - 30f,
                dotSize = 10f,
                dotSpacing = 3f,
                color = palette.primary
            )

            paint.apply {
                color = palette.label
                textSize = 20f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                style = Paint.Style.FILL
            }
            canvas.drawText("GB USED", centerX, centerY + 30f, paint)

            paint.textSize = 18f
            canvas.drawText("OF %.0f GB".format(gbTotal), centerX, centerY + 55f, paint)

            paint.apply {
                color = palette.primary
                textSize = 72f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
                letterSpacing = -0.05f
            }
            canvas.drawText("%.0f%%".format(usagePercent * 100), 48f, height / 2f + 20f, paint)

            paint.apply {
                color = palette.label
                textSize = 24f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            }
            canvas.drawText("USED", 48f, height / 2f + 55f, paint)

            return bitmap
        }
    }
}
