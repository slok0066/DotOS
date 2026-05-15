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
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import com.example.dotos.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val gameAnimationHandler = Handler(Looper.getMainLooper())

private fun drawPixelDino(canvas: Canvas, x: Float, y: Float, paint: Paint, background: Int, leftUp: Boolean, rightUp: Boolean) {
    canvas.save()
    canvas.translate(x, y)
    canvas.scale(1.8f, 1.8f)
    canvas.drawRect(22f, 0f, 44f, 16f, paint)
    canvas.drawRect(22f, 16f, 32f, 32f, paint)
    val oldColor = paint.color
    paint.color = background
    canvas.drawRect(26f, 4f, 30f, 8f, paint)
    paint.color = oldColor
    canvas.drawRect(32f, 20f, 44f, 24f, paint)
    canvas.drawRect(10f, 16f, 22f, 40f, paint)
    canvas.drawRect(0f, 8f, 10f, 30f, paint)
    canvas.drawRect(22f, 24f, 28f, 28f, paint)
    if (!leftUp) canvas.drawRect(10f, 40f, 14f, 48f, paint)
    if (!rightUp) canvas.drawRect(18f, 40f, 22f, 48f, paint)
    canvas.restore()
}

private fun drawPixelCactus(canvas: Canvas, x: Float, y: Float, paint: Paint) {
    canvas.save()
    canvas.translate(x, y)
    canvas.scale(1.8f, 1.8f)
    canvas.drawRect(10f, 0f, 18f, 40f, paint)
    canvas.drawRect(0f, 10f, 10f, 16f, paint)
    canvas.drawRect(0f, 10f, 6f, 24f, paint)
    canvas.drawRect(18f, 16f, 28f, 22f, paint)
    canvas.drawRect(22f, 8f, 28f, 22f, paint)
    canvas.restore()
}

private fun easeOutCubic(progress: Float): Float {
    val clamped = progress.coerceIn(0f, 1f)
    val inverse = 1f - clamped
    return 1f - inverse * inverse * inverse
}

private fun easeOutQuart(progress: Float): Float {
    val clamped = progress.coerceIn(0f, 1f)
    val inverse = 1f - clamped
    return 1f - inverse * inverse * inverse * inverse
}

private fun drawDotGrid(canvas: Canvas, paint: Paint, width: Int, height: Int, palette: WidgetTheme.Palette) {
    paint.style = Paint.Style.FILL
    paint.color = Color.argb(40, Color.red(palette.grid), Color.green(palette.grid), Color.blue(palette.grid))
    for (x in 24 until width step 32) {
        for (y in 24 until height step 32) {
            canvas.drawCircle(x.toFloat(), y.toFloat(), 1.2f, paint)
        }
    }
}

private fun drawLabel(canvas: Canvas, paint: Paint, label: String, x: Float, y: Float, palette: WidgetTheme.Palette) {
    paint.style = Paint.Style.FILL
    paint.color = palette.label
    paint.textSize = 18f
    paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText(label, x, y, paint)
}

private fun drawDottedCircle(
    canvas: Canvas,
    paint: Paint,
    cx: Float,
    cy: Float,
    radius: Float,
    count: Int,
    dotRadius: Float,
    color: Int,
    rotation: Float = 0f,
) {
    paint.style = Paint.Style.FILL
    paint.color = color
    for (dot in 0 until count) {
        val angle = Math.toRadians((dot * 360f / count + rotation).toDouble())
        canvas.drawCircle(
            cx + radius * cos(angle).toFloat(),
            cy + radius * sin(angle).toFloat(),
            dotRadius,
            paint,
        )
    }
}

class SpinnerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_SPIN") {
            animateSpin(context.applicationContext, goAsync())
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) { WidgetTheme.removeWidgetTheme(context, id) }
    }

    companion object {
        private val SPINNER_PROVIDERS = arrayOf(SpinnerWidgetProvider::class.java)
        private val SEGMENTS = arrayOf("1", "2", "3", "4", "5", "6")

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null, null, 0f)
        }

        private fun animateSpin(context: Context, pendingResult: android.content.BroadcastReceiver.PendingResult) {
            val prefs = context.getSharedPreferences("spinner", Context.MODE_PRIVATE)
            val startRotation = prefs.getFloat("rotation", 0f)
            val targetSegment = Random.nextInt(SEGMENTS.size)
            val segmentAngle = 360f / SEGMENTS.size
            val finalRotation = startRotation + (3 + Random.nextInt(3)) * 360f + targetSegment * segmentAngle
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val frameCount = 24

            fun drawFrame(frame: Int) {
                val progress = frame / frameCount.toFloat()
                val eased = easeOutQuart(progress)
                val rotation = startRotation + (finalRotation - startRotation) * eased
                val energy = 1f - eased

                prefs.edit()
                    .putInt("segment", targetSegment)
                    .putFloat("rotation", rotation)
                    .apply()

                for (provider in SPINNER_PROVIDERS) {
                    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, provider))
                    for (id in ids) {
                        updateAppWidget(context, appWidgetManager, id, targetSegment, rotation, energy)
                    }
                }

                if (frame < frameCount) {
                    gameAnimationHandler.postDelayed({ drawFrame(frame + 1) }, 32L)
                } else {
                    pendingResult.finish()
                }
            }

            drawFrame(0)
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            animatedSegment: Int?,
            animatedRotation: Float?,
            energy: Float,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val prefs = context.getSharedPreferences("spinner", Context.MODE_PRIVATE)
            val segment = animatedSegment ?: prefs.getInt("segment", 0)
            val rotation = animatedRotation ?: prefs.getFloat("rotation", 0f)
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "games")
            val bitmap = renderSpinner(context, segment, rotation, energy, palette, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)
            val intent = Intent(context, SpinnerWidgetProvider::class.java).apply { action = "ACTION_SPIN" }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderSpinner(context: Context, segment: Int, rotation: Float, energy: Float, palette: WidgetTheme.Palette, appWidgetId: Int): Bitmap {
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            canvas.drawColor(palette.background)
            drawDotGrid(canvas, paint, size, size, palette)
            drawLabel(canvas, paint, "SPINNER", 24f, 42f, palette)

            val cx = size / 2f
            val cy = size / 2f - 8f
            val armRadius = 104f
            val bearingRadius = 43f
            val spin = rotation % 360f

            drawDottedCircle(canvas, paint, cx, cy, 154f, 48, 2.5f, palette.border, spin * 0.08f)
            drawDottedCircle(canvas, paint, cx, cy, 142f, 36, 3.2f, palette.primary, spin * 0.18f)

            for (trail in 4 downTo 1) {
                paint.color = Color.argb((energy * 50 / trail).toInt().coerceIn(0, 58), 255, 255, 255)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 7f / trail
                canvas.drawArc(
                    RectF(cx - 134f, cy - 134f, cx + 134f, cy + 134f),
                    spin - trail * 18f,
                    94f,
                    false,
                    paint,
                )
            }

            canvas.save()
            canvas.rotate(spin, cx, cy)
            paint.style = Paint.Style.FILL
            paint.color = palette.secondary
            for (i in 0 until 3) {
                val angle = Math.toRadians((i * 120f - 90f).toDouble())
                val bx = cx + armRadius * cos(angle).toFloat()
                val by = cy + armRadius * sin(angle).toFloat()
                paint.color = palette.secondary
                canvas.drawCircle(bx, by, bearingRadius, paint)
                paint.color = palette.background
                canvas.drawCircle(bx, by, 22f, paint)
                drawDottedCircle(canvas, paint, bx, by, bearingRadius, 18, 2.8f, palette.primary)
                drawDottedCircle(canvas, paint, bx, by, 22f, 14, 1.8f, palette.primary)
                paint.style = Paint.Style.FILL
            }

            paint.color = palette.secondary
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 36f
            paint.strokeCap = Paint.Cap.ROUND
            for (i in 0 until 3) {
                val angle = Math.toRadians((i * 120f - 90f).toDouble())
                canvas.drawLine(
                    cx,
                    cy,
                    cx + armRadius * 0.58f * cos(angle).toFloat(),
                    cy + armRadius * 0.58f * sin(angle).toFloat(),
                    paint,
                )
            }
            paint.strokeCap = Paint.Cap.BUTT

            paint.color = palette.background
            paint.style = Paint.Style.FILL
            canvas.drawCircle(cx, cy, 42f, paint)
            drawDottedCircle(canvas, paint, cx, cy, 42f, 24, 2.4f, palette.accent)
            canvas.restore()

            return bitmap
        }
    }
}

class BottleSpinWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_BOTTLE_SPIN") {
            animateBottle(context.applicationContext, goAsync())
        }
    }

    companion object {
        private val BOTTLE_PROVIDERS = arrayOf(BottleSpinWidgetProvider::class.java)

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null, null, 0f)
        }

        private fun animateBottle(context: Context, pendingResult: android.content.BroadcastReceiver.PendingResult) {
            val prefs = context.getSharedPreferences("bottle_spin", Context.MODE_PRIVATE)
            val startRotation = prefs.getFloat("rotation", 0f)
            val target = Random.nextInt(24)
            val finalRotation = startRotation + (4 + Random.nextInt(3)) * 360f + target * 15f
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val frameCount = 26

            fun drawFrame(frame: Int) {
                val progress = frame / frameCount.toFloat()
                val eased = easeOutQuart(progress)
                val rotation = startRotation + (finalRotation - startRotation) * eased
                val energy = 1f - eased

                prefs.edit()
                    .putFloat("rotation", rotation)
                    .putInt("target", target)
                    .apply()

                for (provider in BOTTLE_PROVIDERS) {
                    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, provider))
                    for (id in ids) {
                        updateAppWidget(context, appWidgetManager, id, rotation, target, energy)
                    }
                }

                if (frame < frameCount) {
                    gameAnimationHandler.postDelayed({ drawFrame(frame + 1) }, 34L)
                } else {
                    pendingResult.finish()
                }
            }

            drawFrame(0)
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            animatedRotation: Float?,
            animatedTarget: Int?,
            energy: Float,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val prefs = context.getSharedPreferences("bottle_spin", Context.MODE_PRIVATE)
            val rotation = animatedRotation ?: prefs.getFloat("rotation", 0f)
            val target = animatedTarget ?: prefs.getInt("target", 0)
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "games")
            val bitmap = renderBottle(context, rotation, target, energy, palette, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)
            val intent = Intent(context, BottleSpinWidgetProvider::class.java).apply { action = "ACTION_BOTTLE_SPIN" }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderBottle(context: Context, rotation: Float, target: Int, energy: Float, palette: WidgetTheme.Palette, appWidgetId: Int): Bitmap {
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            canvas.drawColor(palette.background)
            drawDotGrid(canvas, paint, size, size, palette)

            val cx = size / 2f
            val cy = size / 2f - 16f
            drawLabel(canvas, paint, "BOTTLE SPIN", 24f, 42f, palette)

            for (trail in 4 downTo 1) {
                paint.color = Color.argb((energy * 36 / trail).toInt().coerceIn(0, 46), 255, 255, 255)
                paint.strokeWidth = 7f / trail
                canvas.drawArc(
                    RectF(cx - 142f, cy - 142f, cx + 142f, cy + 142f),
                    rotation - trail * 12f,
                    54f,
                    false,
                    paint,
                )
            }

            canvas.save()
            canvas.rotate(rotation % 360f, cx, cy)
            canvas.scale(2.25f, 1f, cx, cy)
            drawBottleIcon(context, canvas, cx, cy, palette)
            canvas.restore()

            return bitmap
        }

        private fun drawBottleIcon(context: Context, canvas: Canvas, cx: Float, cy: Float, palette: WidgetTheme.Palette) {
            val drawable = context.getDrawable(R.drawable.ic_whiskey_bottle) ?: return
            drawable.mutate()
            drawable.setTint(palette.primary)
            drawable.setBounds(
                (cx - 62f).toInt(),
                (cy - 116f).toInt(),
                (cx + 62f).toInt(),
                (cy + 116f).toInt(),
            )
            drawable.draw(canvas)
        }
    }
}

class DiceRollWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_ROLL") {
            animateRoll(context.applicationContext, goAsync())
        }
    }

    companion object {
        private val DICE_PROVIDERS = arrayOf(DiceRollWidgetProvider::class.java)

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            updateAppWidget(context, appWidgetManager, appWidgetId, null, null, 0f, 0f)
        }

        private fun animateRoll(context: Context, pendingResult: android.content.BroadcastReceiver.PendingResult) {
            val prefs = context.getSharedPreferences("dice", Context.MODE_PRIVATE)
            val targetValue = Random.nextInt(1, 7)
            val rolls = prefs.getInt("rolls", 0) + 1
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val frameCount = 18

            fun drawFrame(frame: Int) {
                val progress = frame / frameCount.toFloat()
                val eased = easeOutCubic(progress)
                val value = if (frame == frameCount) targetValue else Random.nextInt(1, 7)
                val rotation = (1f - eased) * 540f + if (frame % 2 == 0) 10f else -10f
                val energy = 1f - eased

                prefs.edit()
                    .putInt("value", value)
                    .putInt("rolls", rolls)
                    .apply()

                for (provider in DICE_PROVIDERS) {
                    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, provider))
                    for (id in ids) {
                        updateAppWidget(context, appWidgetManager, id, value, rolls, rotation, energy)
                    }
                }

                if (frame < frameCount) {
                    gameAnimationHandler.postDelayed({ drawFrame(frame + 1) }, 42L)
                } else {
                    pendingResult.finish()
                }
            }

            drawFrame(0)
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            animatedValue: Int?,
            animatedRolls: Int?,
            rotation: Float,
            energy: Float,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val prefs = context.getSharedPreferences("dice", Context.MODE_PRIVATE)
            val value = animatedValue ?: prefs.getInt("value", 1)
            val rolls = animatedRolls ?: prefs.getInt("rolls", 0)
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "games")
            val bitmap = renderDice(context, value, rolls, rotation, energy, palette, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)
            val intent = Intent(context, DiceRollWidgetProvider::class.java).apply { action = "ACTION_ROLL" }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderDice(context: Context, value: Int, rolls: Int, rotation: Float, energy: Float, palette: WidgetTheme.Palette, appWidgetId: Int): Bitmap {
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            canvas.drawColor(palette.background)
            drawDotGrid(canvas, paint, size, size, palette)

            val cx = size / 2f
            val dieSize = 136f
            val left = cx - dieSize / 2f
            val bounce = (sin(((1f - energy) * Math.PI).toDouble()) * 34f).toFloat()
            val top = size / 2f - 80f - bounce
            val right = cx + dieSize / 2f
            val bottom = top + dieSize
            val centerY = top + dieSize / 2f

            for (trail in 1..3) {
                paint.color = Color.argb((energy * 34 / trail).toInt().coerceIn(0, 40), 255, 255, 255)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawRoundRect(
                    left - trail * 12f,
                    top + trail * 10f,
                    right - trail * 12f,
                    bottom + trail * 10f,
                    18f,
                    18f,
                    paint,
                )
            }

            canvas.save()
            canvas.rotate(rotation, cx, centerY)

            paint.color = palette.primary
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            val cornerRadius = 12f
            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, paint)

            paint.color = palette.primary
            paint.style = Paint.Style.FILL
            val dotRadius = 10f
            val dotOffset = dieSize * 0.28f
            val centerX = cx

            when (value) {
                1 -> canvas.drawCircle(centerX, centerY, dotRadius, paint)
                2 -> {
                    canvas.drawCircle(centerX - dotOffset, centerY - dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY + dotOffset, dotRadius, paint)
                }
                3 -> {
                    canvas.drawCircle(centerX - dotOffset, centerY - dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX, centerY, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY + dotOffset, dotRadius, paint)
                }
                4 -> {
                    canvas.drawCircle(centerX - dotOffset, centerY - dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY - dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX - dotOffset, centerY + dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY + dotOffset, dotRadius, paint)
                }
                5 -> {
                    canvas.drawCircle(centerX, centerY, dotRadius, paint)
                    canvas.drawCircle(centerX - dotOffset, centerY - dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY - dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX - dotOffset, centerY + dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY + dotOffset, dotRadius, paint)
                }
                6 -> {
                    canvas.drawCircle(centerX - dotOffset, centerY - dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX - dotOffset, centerY, dotRadius, paint)
                    canvas.drawCircle(centerX - dotOffset, centerY + dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY - dotOffset, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY, dotRadius, paint)
                    canvas.drawCircle(centerX + dotOffset, centerY + dotOffset, dotRadius, paint)
                }
            }
            canvas.restore()

            paint.color = palette.muted
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("ROLLS", cx, bottom + 52f, paint)

            paint.color = palette.primary
            paint.textSize = 28f
            canvas.drawText(rolls.toString().padStart(2, '0'), cx, bottom + 84f, paint)

            val segments = 6
            val segmentWidth = 18f
            val startX = cx - ((segments * segmentWidth) + ((segments - 1) * 6f)) / 2f
            for (i in 0 until segments) {
                paint.color = if (i < value) palette.primary else palette.border
                canvas.drawRect(startX + i * (segmentWidth + 6f), size - 54f, startX + i * (segmentWidth + 6f) + segmentWidth, size - 47f, paint)
            }

            paint.color = palette.primary
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            canvas.drawText("TAP TO ROLL", cx, size - 20f, paint)

            return bitmap
        }
    }
}

class DinoGameWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_DINO_JUMP") {
            animateRun(context.applicationContext, goAsync())
        }
    }

    companion object {
        private val DINO_PROVIDERS = arrayOf(DinoGameWidgetProvider::class.java)

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "games")
            views.setImageViewBitmap(R.id.widget_image, renderDinoLauncher(palette, appWidgetId))
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chromedino.com/")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderDinoLauncher(palette: WidgetTheme.Palette, appWidgetId: Int): Bitmap {
            val size = 400
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            canvas.drawColor(palette.background)
            drawDotGrid(canvas, paint, size, size, palette)
            drawLabel(canvas, paint, "DINO RUN", 24f, 42f, palette)

            val groundY = 260f
            paint.style = Paint.Style.STROKE
            paint.color = palette.border
            paint.strokeWidth = 3f
            canvas.drawLine(36f, groundY, 364f, groundY, paint)

            paint.color = palette.muted
            paint.strokeWidth = 2f
            for (x in 48 until 350 step 34) {
                canvas.drawLine(x.toFloat(), groundY + 14f, x + 12f, groundY + 14f, paint)
            }

            paint.style = Paint.Style.FILL
            paint.color = palette.primary
            drawPixelDino(canvas, 74f, 174f, paint, palette.background, false, false)

            paint.color = palette.secondary
            drawPixelCactus(canvas, 246f, 188f, paint)
            paint.color = palette.accent
            drawPixelCactus(canvas, 296f, 188f, paint)

            paint.color = palette.primary
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            paint.textSize = 24f
            canvas.drawText("PLAY", size / 2f, 324f, paint)
            paint.color = palette.muted
            paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            paint.textSize = 15f
            canvas.drawText("TAP TO START", size / 2f, 352f, paint)
            return bitmap
        }

        private fun animateRun(context: Context, pendingResult: android.content.BroadcastReceiver.PendingResult) {
            val prefs = context.getSharedPreferences("dino_game", Context.MODE_PRIVATE)
            val startScore = if (prefs.getInt("state", 0) == 0) 0 else prefs.getInt("score", 0)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val frameCount = 30

            fun drawFrame(frame: Int) {
                val progress = frame / frameCount.toFloat()
                val eased = easeOutCubic(progress)
                val hop = sin((progress * Math.PI).toDouble()).toFloat()
                val obstacleX = (380f - 460f * eased).toInt()
                val dinoY = (260f - 92f * hop).toInt()
                val score = startScore + (progress * 16f).toInt()
                val runningFrame = frame % 6

                prefs.edit()
                    .putInt("state", 1)
                    .putInt("score", score)
                    .putInt("obstacle_x", if (frame == frameCount) 420 + Random.nextInt(80) else obstacleX)
                    .putInt("dino_y", if (frame == frameCount) 260 else dinoY)
                    .putBoolean("is_jumping", frame != frameCount)
                    .putInt("jump_frame", runningFrame)
                    .apply()

                for (provider in DINO_PROVIDERS) {
                    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, provider))
                    for (id in ids) {
                        updateAppWidget(
                            context,
                            appWidgetManager,
                            id,
                            1,
                            score,
                            obstacleX,
                            dinoY,
                            frame != frameCount,
                            runningFrame,
                        )
                    }
                }

                if (frame < frameCount) {
                    gameAnimationHandler.postDelayed({ drawFrame(frame + 1) }, 38L)
                } else {
                    pendingResult.finish()
                }
            }

            drawFrame(0)
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            animatedState: Int?,
            animatedScore: Int?,
            animatedObstacleX: Int?,
            animatedDinoY: Int?,
            animatedJumping: Boolean,
            runningFrame: Int,
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val prefs = context.getSharedPreferences("dino_game", Context.MODE_PRIVATE)
            val state = animatedState ?: prefs.getInt("state", 0)
            val score = animatedScore ?: prefs.getInt("score", 0)
            val obstacleX = animatedObstacleX ?: prefs.getInt("obstacle_x", 400)
            val dinoY = animatedDinoY ?: prefs.getInt("dino_y", 260)
            val isJumping = animatedState != null && animatedJumping || prefs.getBoolean("is_jumping", false)
            val frame = if (animatedState != null) runningFrame else prefs.getInt("jump_frame", 0)
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "games")
            val bitmap = renderDinoGame(context, state, score, obstacleX, dinoY, isJumping, frame, palette, appWidgetId)
            views.setImageViewBitmap(R.id.widget_image, bitmap)
            val intent = Intent(context, DinoGameWidgetProvider::class.java).apply { action = "ACTION_DINO_JUMP" }
            val pendingIntent = PendingIntent.getBroadcast(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderDinoGame(context: Context, state: Int, score: Int, obstacleX: Int, dinoY: Int, isJumping: Boolean, runningFrame: Int, palette: WidgetTheme.Palette, appWidgetId: Int): Bitmap {
            val w = 400
            val h = 700
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint().apply { isAntiAlias = true }

            canvas.drawColor(palette.background)
            drawDotGrid(canvas, paint, w, h, palette)

            val groundY = 540f
            val groundOffset = if (state == 1) (runningFrame * 11) % 40 else 0

            paint.color = palette.border
            paint.strokeWidth = 2f
            canvas.drawLine(0f, groundY, w.toFloat(), groundY, paint)

            paint.color = palette.muted
            paint.strokeWidth = 1f
            for (x in -40 until w + 40 step 40) {
                val shiftedX = x - groundOffset
                canvas.drawLine(x.toFloat(), groundY, x.toFloat(), groundY + 20f, paint)
                canvas.drawLine(shiftedX.toFloat(), groundY + 10f, shiftedX + 12f, groundY + 10f, paint)
            }

            if (state == 1) {
                val dinoLeft = 28f
                val dinoTop = dinoY.toFloat() - 4f
                val isLeftUp = !isJumping && runningFrame < 3
                val isRightUp = !isJumping && runningFrame >= 3

                paint.color = palette.primary
                paint.style = Paint.Style.FILL

                drawPixelDino(canvas, dinoLeft, dinoTop, paint, palette.background, isLeftUp, isRightUp)

                val obsLeft = obstacleX.toFloat()
                val obsTop = groundY - 72f

                paint.color = palette.secondary
                drawPixelCactus(canvas, obsLeft, obsTop, paint)

                paint.color = palette.label
                paint.textSize = 18f
                paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${score}PTS", (w - 16).toFloat(), 36f, paint)
            } else if (state == 2) {
                paint.color = palette.accent
                paint.textSize = 28f
                paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("GAME OVER", w / 2f, h / 2f - 40f, paint)

                paint.color = palette.label
                paint.textSize = 36f
                paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                canvas.drawText("${score}pts", w / 2f, h / 2f + 20f, paint)

                paint.color = palette.muted
                paint.textSize = 16f
                canvas.drawText("TAP TO RESTART", w / 2f, h / 2f + 70f, paint)
            } else {
                paint.color = palette.primary
                paint.textSize = 36f
                paint.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("DINO", w / 2f, h / 2f - 20f, paint)

                paint.color = palette.muted
                paint.textSize = 16f
                canvas.drawText("TAP TO START", w / 2f, h / 2f + 30f, paint)

                paint.color = palette.border
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                val dinoSx = w / 2f - 16f
                val dinoSy = h / 2f + 50f
                paint.color = palette.primary
                paint.style = Paint.Style.FILL
                canvas.drawRoundRect(dinoSx, dinoSy, dinoSx + 32f, dinoSy + 40f, 4f, 4f, paint)
                canvas.drawRoundRect(dinoSx + 28f, dinoSy + 8f, dinoSx + 40f, dinoSy + 20f, 3f, 3f, paint)
                canvas.drawRoundRect(dinoSx - 6f, dinoSy + 36f, dinoSx + 6f, dinoSy + 48f, 2f, 2f, paint)
                canvas.drawRoundRect(dinoSx + 14f, dinoSy + 36f, dinoSx + 26f, dinoSy + 48f, 2f, 2f, paint)
            }

            paint.color = palette.label
            paint.textSize = 11f
            paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("DINO GAME", 16f, h - 16f, paint)

            return bitmap
        }
    }
}

class CoinFlipWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id, null)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_COIN_FLIP") {
            animateFlip(context.applicationContext, goAsync())
        }
    }

    companion object {
        private val COIN_PROVIDERS = arrayOf(CoinFlipWidgetProvider::class.java)

        private fun animateFlip(context: Context, pendingResult: PendingResult) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val prefs = context.getSharedPreferences("coin_flip", Context.MODE_PRIVATE)
            val isHeads = prefs.getBoolean("isHeads", true)
            val nextIsHeads = Math.random() < 0.5
            
            val frameCount = 20
            var frame = 0
            
            fun easeOutQuart(x: Float): Float = 1f - Math.pow(1f - x.toDouble(), 4.0).toFloat()

            fun drawFrame(currentFrame: Int) {
                if (currentFrame > frameCount) {
                    pendingResult.finish()
                    return
                }

                val progress = currentFrame / frameCount.toFloat()
                val eased = easeOutQuart(progress)
                // total rotation is 5 half-flips (5 * 180 = 900) + matching next target
                val flips = 5
                val targetAngle = if (nextIsHeads) 0f else 180f
                val rotation = (flips * 180f) * progress + (if(isHeads) 0f else 180f)

                prefs.edit()
                    .putBoolean("isHeads", nextIsHeads)
                    .putFloat("rotation", rotation)
                    .apply()

                for (provider in COIN_PROVIDERS) {
                    val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, provider))
                    for (id in ids) {
                        updateAppWidget(context, appWidgetManager, id, rotation)
                    }
                }

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    drawFrame(currentFrame + 1)
                }, 33L)
            }
            drawFrame(0)
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, animatedRotation: Float?) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "games")
            val prefs = context.getSharedPreferences("coin_flip", Context.MODE_PRIVATE)
            val rotation = animatedRotation ?: if (prefs.getBoolean("isHeads", true)) 0f else 180f
            
            views.setImageViewBitmap(R.id.widget_image, renderCoin(context, rotation, palette, appWidgetId))
            
            val intent = Intent(context, CoinFlipWidgetProvider::class.java).apply { action = "ACTION_COIN_FLIP" }
            val pendingIntent = PendingIntent.getBroadcast(
                context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderCoin(context: Context, rotationX: Float, palette: WidgetTheme.Palette, appWidgetId: Int): Bitmap {
            val w = 400
            val h = 400
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            canvas.drawColor(palette.background)
            drawDotGrid(canvas, paint, w, h, palette)
            drawLabel(canvas, paint, "COIN FLIP", 24f, 42f, palette)

            val text = if (Math.cos(Math.toRadians(rotationX.toDouble())) >= 0) "H" else "T"
            
            val scaleY = Math.abs(Math.cos(Math.toRadians(rotationX.toDouble()))).toFloat()
            
            canvas.save()
            canvas.translate(w / 2f, h / 2f + 16f)
            canvas.scale(1f, scaleY)
            
            // Outer ring
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 5f
            paint.color = palette.primary
            canvas.drawCircle(0f, 0f, 100f, paint)

            // Inner dotted ring
            paint.strokeWidth = 2f
            paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 10f), 0f)
            paint.color = palette.muted
            canvas.drawCircle(0f, 0f, 84f, paint)
            paint.pathEffect = null

            paint.style = Paint.Style.FILL
            paint.color = palette.primary
            paint.textSize = 90f
            paint.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            // adjust baseline
            canvas.drawText(text, 0f, 32f, paint)
            
            canvas.restore()

            return bitmap
        }
    }
}
