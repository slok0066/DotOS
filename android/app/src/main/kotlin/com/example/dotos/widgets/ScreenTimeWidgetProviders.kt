package com.example.dotos.widgets

import android.app.AppOpsManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.provider.Settings
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dotos.MainActivity
import com.example.dotos.R
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class ScreenTimeMinimalWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { ScreenTimeWidgetUpdater.updateWidget(context, appWidgetManager, it, ScreenTimeDesign.MINIMAL) }
        ScreenTimeWidgetUpdater.schedule(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ScreenTimeWidgetUpdater.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ScreenTimeWidgetUpdater.cancelIfNoWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (ScreenTimeWidgetUpdater.shouldRefresh(intent.action)) {
            ScreenTimeWidgetUpdater.updateProvider(context, ScreenTimeMinimalWidgetProvider::class.java, ScreenTimeDesign.MINIMAL)
        }
    }
}

class ScreenTimeRingWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { ScreenTimeWidgetUpdater.updateWidget(context, appWidgetManager, it, ScreenTimeDesign.RING) }
        ScreenTimeWidgetUpdater.schedule(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ScreenTimeWidgetUpdater.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ScreenTimeWidgetUpdater.cancelIfNoWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (ScreenTimeWidgetUpdater.shouldRefresh(intent.action)) {
            ScreenTimeWidgetUpdater.updateProvider(context, ScreenTimeRingWidgetProvider::class.java, ScreenTimeDesign.RING)
        }
    }
}

class ScreenTimeSplitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { ScreenTimeWidgetUpdater.updateWidget(context, appWidgetManager, it, ScreenTimeDesign.SPLIT) }
        ScreenTimeWidgetUpdater.schedule(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        ScreenTimeWidgetUpdater.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ScreenTimeWidgetUpdater.cancelIfNoWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (ScreenTimeWidgetUpdater.shouldRefresh(intent.action)) {
            ScreenTimeWidgetUpdater.updateProvider(context, ScreenTimeSplitWidgetProvider::class.java, ScreenTimeDesign.SPLIT)
        }
    }
}

class ScreenTimeRefreshWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : Worker(context, workerParameters) {
    override fun doWork(): Result {
        ScreenTimeWidgetUpdater.refreshAll(applicationContext)
        return Result.success()
    }
}

class ScreenTimeRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ScreenTimeWidgetUpdater.shouldRefresh(intent.action)) {
            ScreenTimeWidgetUpdater.refreshAll(context)
        }
    }
}

enum class ScreenTimeDesign {
    MINIMAL,
    RING,
    SPLIT,
}

data class ScreenTimeStats(
    val hasPermission: Boolean,
    val totalMillis: Long,
    val topAppLabel: String?,
)

object ScreenTimeWidgetUpdater {
    const val ACTION_REFRESH = "com.example.dotos.widgets.ACTION_REFRESH_SCREEN_TIME_WIDGETS"

    private const val UNIQUE_WORK_NAME = "screen_time_widget_refresh"
    private const val REQUEST_CODE_BASE = 5200
    private const val REFRESH_ALARM_REQUEST_CODE = 5299
    private const val REFRESH_INTERVAL_MS = 60_000L
    private const val DAILY_GOAL_MILLIS = 8L * 60L * 60L * 1000L

    private val providers = arrayOf(
        ScreenTimeMinimalWidgetProvider::class.java to ScreenTimeDesign.MINIMAL,
        ScreenTimeRingWidgetProvider::class.java to ScreenTimeDesign.RING,
        ScreenTimeSplitWidgetProvider::class.java to ScreenTimeDesign.SPLIT,
    )

    fun shouldRefresh(action: String?): Boolean {
        return action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == ACTION_REFRESH ||
            action == Intent.ACTION_SCREEN_ON ||
            action == Intent.ACTION_SCREEN_OFF ||
            action == Intent.ACTION_USER_PRESENT ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_BOOT_COMPLETED
    }

    fun schedule(context: Context) {
        scheduleAlarm(context)

        val request = PeriodicWorkRequestBuilder<ScreenTimeRefreshWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancelIfNoWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val hasWidgets = providers.any { (providerClass, _) ->
            appWidgetManager.getAppWidgetIds(ComponentName(context, providerClass)).isNotEmpty()
        }

        if (!hasWidgets) {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
            cancelAlarm(context)
        }
    }

    fun refreshAll(context: Context) {
        providers.forEach { (providerClass, design) ->
            updateProvider(context, providerClass, design)
        }
        cancelIfNoWidgets(context)
    }

    fun updateProvider(
        context: Context,
        providerClass: Class<out AppWidgetProvider>,
        design: ScreenTimeDesign,
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, providerClass))
        ids.forEach { updateWidget(context, appWidgetManager, it, design) }
        if (ids.isNotEmpty()) {
            schedule(context)
        }
    }

    fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        design: ScreenTimeDesign,
    ) {
        val stats = ScreenTimeUsageReader.readToday(context)
        val views = RemoteViews(context.packageName, R.layout.widget_layout)
        val bitmap = when (design) {
            ScreenTimeDesign.MINIMAL -> ScreenTimeRenderer.renderMinimal(context, appWidgetId, stats)
            ScreenTimeDesign.RING -> ScreenTimeRenderer.renderRing(context, appWidgetId, stats, DAILY_GOAL_MILLIS)
            ScreenTimeDesign.SPLIT -> ScreenTimeRenderer.renderSplit(context, appWidgetId, stats, DAILY_GOAL_MILLIS)
        }

        views.setImageViewBitmap(R.id.widget_image, bitmap)
        views.setOnClickPendingIntent(R.id.widget_image, clickIntent(context, appWidgetId, stats.hasPermission))
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun clickIntent(context: Context, appWidgetId: Int, hasPermission: Boolean): PendingIntent {
        val intent = if (hasPermission) {
            Intent(context, MainActivity::class.java)
        } else {
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_BASE + appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun scheduleAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = SystemClock.elapsedRealtime() + REFRESH_INTERVAL_MS
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerAt,
            refreshPendingIntent(context),
        )
    }

    private fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(refreshPendingIntent(context))
    }

    private fun refreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ScreenTimeRefreshReceiver::class.java).apply {
            action = ACTION_REFRESH
        }

        return PendingIntent.getBroadcast(
            context,
            REFRESH_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

object ScreenTimeUsageReader {
    fun hasPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun readToday(context: Context): ScreenTimeStats {
        if (!hasPermission(context)) {
            return ScreenTimeStats(hasPermission = false, totalMillis = 0L, topAppLabel = null)
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        val durations = mutableMapOf<String, Long>()
        var statsTotal = 0L

        fun collectUsageStats(statsList: Iterable<android.app.usage.UsageStats>) {
            for (s in statsList) {
                if (s.totalTimeInForeground > 0) {
                    durations[s.packageName] = s.totalTimeInForeground
                    statsTotal += s.totalTimeInForeground
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val aggregatedStats = usageStatsManager.queryAndAggregateUsageStats(startOfDay, now)
            if (!aggregatedStats.isNullOrEmpty()) {
                collectUsageStats(aggregatedStats.values)
            } else {
                val statsList = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startOfDay,
                    now,
                ) ?: emptyList()
                collectUsageStats(statsList)
            }
        } else {
            val statsList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startOfDay,
                now,
            ) ?: emptyList()
            collectUsageStats(statsList)
        }

        // Try to refine with events if possible (more accurate for current session)
        val events = usageStatsManager.queryEvents(startOfDay, now)
        val event = UsageEvents.Event()
        var lastResumedTime = -1L
        var lastPackage: String? = null
        var eventRefinement = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED, UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    lastResumedTime = event.timeStamp
                    lastPackage = event.packageName
                }
                UsageEvents.Event.ACTIVITY_PAUSED, UsageEvents.Event.ACTIVITY_STOPPED, UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    if (lastResumedTime > 0 && event.packageName == lastPackage) {
                        val duration = event.timeStamp - lastResumedTime
                        if (duration > 0) {
                            // We don't replace queryUsageStats totally as it's more stable, 
                            // but we ensure the current app is accounted for.
                            durations[lastPackage!!] = (durations[lastPackage!!] ?: 0L).coerceAtLeast(duration)
                        }
                        lastResumedTime = -1L
                        lastPackage = null
                    }
                }
            }
        }

        // If an app is currently in foreground
        if (lastResumedTime > 0 && lastPackage != null) {
            val duration = now - lastResumedTime
            durations[lastPackage] = max(durations[lastPackage] ?: 0L, duration)
        }

        val finalTotal = durations.values.sum().coerceAtLeast(statsTotal)
        val topPackage = durations.maxByOrNull { it.value }?.key

        return ScreenTimeStats(
            hasPermission = true,
            totalMillis = finalTotal,
            topAppLabel = topPackage?.let { labelFor(context, it) },
        )
    }

    private fun labelFor(context: Context, packageName: String): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            packageName.substringAfterLast('.')
        }
    }
}

object ScreenTimeRenderer {
    private const val SIZE = 400
    private const val PADDING = 34f
    private const val CORNER_RADIUS = 48f

    fun renderMinimal(context: Context, appWidgetId: Int, stats: ScreenTimeStats): Bitmap {
        val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "screen_time")
        val bitmap = createBitmap()
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        drawCoreBackground(canvas, paint, palette, drawGrid = true)
        if (!stats.hasPermission) {
            drawPermissionState(canvas, paint, palette)
            return bitmap
        }

        val time = formatTime(stats.totalMillis)
        val textWidth = DotMatrixRenderer.measureDotMatrixText(time.matrix, 7.5f, 2.5f)
        DotMatrixRenderer.drawDotMatrixText(
            canvas = canvas,
            text = time.matrix,
            x = (SIZE - textWidth) / 2f,
            y = 132f,
            dotSize = 7.5f,
            dotSpacing = 2.5f,
            color = palette.primary,
        )

        paint.apply {
            color = palette.label
            textSize = 16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.12f
        }
        canvas.drawText("SCREEN TIME", SIZE / 2f, 292f, paint)

        drawTopApp(canvas, paint, palette, stats.topAppLabel, SIZE / 2f, 330f, Paint.Align.CENTER)
        return bitmap
    }

    fun renderRing(context: Context, appWidgetId: Int, stats: ScreenTimeStats, goalMillis: Long): Bitmap {
        val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "screen_time")
        val bitmap = createBitmap()
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        drawCoreBackground(canvas, paint, palette, drawGrid = false)
        if (!stats.hasPermission) {
            drawPermissionState(canvas, paint, palette)
            return bitmap
        }

        val center = SIZE / 2f
        val radius = 122f
        val ring = RectF(center - radius, center - radius, center + radius, center + radius)
        val progress = min(1f, stats.totalMillis / goalMillis.toFloat())

        paint.apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 14f
            color = palette.border
        }
        canvas.drawCircle(center, center, radius, paint)

        paint.apply {
            color = palette.primary
            strokeWidth = 12f
        }
        canvas.drawArc(ring, -90f, 360f * progress, false, paint)

        paint.apply {
            color = palette.grid
            strokeWidth = 2f
            strokeCap = Paint.Cap.BUTT
        }
        canvas.drawCircle(center, center, radius + 18f, paint)
        paint.style = Paint.Style.FILL

        val time = formatTime(stats.totalMillis)
        paint.apply {
            color = palette.primary
            textSize = 44f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = -0.04f
        }
        canvas.drawText(time.compact, center, center + 12f, paint)

        paint.apply {
            color = palette.label
            textSize = 15f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.14f
        }
        canvas.drawText("TODAY", center, SIZE - 48f, paint)
        return bitmap
    }

    fun renderSplit(context: Context, appWidgetId: Int, stats: ScreenTimeStats, goalMillis: Long): Bitmap {
        val palette = WidgetTheme.paletteForWidget(context, appWidgetId, "screen_time")
        val bitmap = createBitmap()
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        drawCoreBackground(canvas, paint, palette, drawGrid = false)
        if (!stats.hasPermission) {
            drawPermissionState(canvas, paint, palette)
            return bitmap
        }

        paint.apply {
            color = palette.border
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(18f, 18f, SIZE - 18f, SIZE - 18f, 26f, 26f, paint)
        canvas.drawLine(SIZE / 2f, 88f, SIZE / 2f, 286f, paint)
        paint.style = Paint.Style.FILL

        val time = formatTime(stats.totalMillis)
        paint.apply {
            color = palette.primary
            textSize = 94f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = -0.08f
        }
        canvas.drawText(time.hours.toString(), 106f, 198f, paint)

        paint.apply {
            color = palette.label
            textSize = 18f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.12f
        }
        canvas.drawText("HOURS", 106f, 235f, paint)

        paint.apply {
            color = palette.primary
            textSize = 50f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = -0.04f
        }
        canvas.drawText("${time.minutes}M", 296f, 174f, paint)

        paint.apply {
            color = palette.label
            textSize = 17f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.14f
        }
        canvas.drawText("TODAY", 296f, 212f, paint)

        drawUsageBar(canvas, paint, palette, stats.totalMillis, goalMillis)
        drawTopApp(canvas, paint, palette, stats.topAppLabel, PADDING, 342f, Paint.Align.LEFT)
        return bitmap
    }

    private fun createBitmap(): Bitmap {
        return Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
    }

    private fun drawCoreBackground(
        canvas: Canvas,
        paint: Paint,
        palette: WidgetTheme.Palette,
        drawGrid: Boolean,
    ) {
        paint.apply {
            color = palette.background
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(0f, 0f, SIZE.toFloat(), SIZE.toFloat(), CORNER_RADIUS, CORNER_RADIUS, paint)

        if (!drawGrid) return

        paint.color = palette.grid
        paint.alpha = 95
        val spacing = 28f
        for (x in 0..(SIZE / spacing).toInt()) {
            for (y in 0..(SIZE / spacing).toInt()) {
                canvas.drawCircle(x * spacing, y * spacing, 2.2f, paint)
            }
        }
        paint.alpha = 255
    }

    private fun drawPermissionState(
        canvas: Canvas,
        paint: Paint,
        palette: WidgetTheme.Palette,
    ) {
        paint.apply {
            color = palette.primary
            textSize = 24f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.05f
            style = Paint.Style.FILL
        }
        canvas.drawText("ENABLE", SIZE / 2f, 172f, paint)
        canvas.drawText("USAGE ACCESS", SIZE / 2f, 205f, paint)

        paint.apply {
            color = palette.label
            textSize = 13f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            letterSpacing = 0.12f
        }
        canvas.drawText("TAP TO OPEN SETTINGS", SIZE / 2f, 246f, paint)
    }

    private fun drawTopApp(
        canvas: Canvas,
        paint: Paint,
        palette: WidgetTheme.Palette,
        topAppLabel: String?,
        x: Float,
        y: Float,
        align: Paint.Align,
    ) {
        if (topAppLabel.isNullOrBlank()) return

        paint.apply {
            color = palette.muted
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = align
            letterSpacing = 0.08f
            style = Paint.Style.FILL
        }
        canvas.drawText("TOP ${fitLabel(topAppLabel.uppercase(), 18)}", x, y, paint)
    }

    private fun drawUsageBar(
        canvas: Canvas,
        paint: Paint,
        palette: WidgetTheme.Palette,
        totalMillis: Long,
        goalMillis: Long,
    ) {
        val x = PADDING
        val y = 304f
        val width = SIZE - PADDING * 2
        val progress = min(1f, totalMillis / goalMillis.toFloat())

        paint.apply {
            color = palette.border
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(x, y, x + width, y + 8f, 4f, 4f, paint)

        paint.color = palette.primary
        canvas.drawRoundRect(x, y, x + max(8f, width * progress), y + 8f, 4f, 4f, paint)
    }

    private fun fitLabel(label: String, maxChars: Int): String {
        return if (label.length <= maxChars) label else "${label.take(maxChars - 1)}."
    }

    private fun formatTime(totalMillis: Long): ScreenTimeText {
        val totalMinutes = max(0L, totalMillis / 60_000L)
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        return ScreenTimeText(
            hours = hours,
            minutes = minutes,
            matrix = "${hours}H ${minutes}M",
            compact = "${hours}h ${minutes}m",
        )
    }

    private data class ScreenTimeText(
        val hours: Long,
        val minutes: Long,
        val matrix: String,
        val compact: String,
    )
}
