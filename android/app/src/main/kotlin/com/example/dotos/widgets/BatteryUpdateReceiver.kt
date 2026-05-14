package com.example.dotos.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetProvider
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class BatteryUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_CHANGED &&
            intent.action != Intent.ACTION_POWER_CONNECTED &&
            intent.action != Intent.ACTION_POWER_DISCONNECTED &&
            intent.action != Intent.ACTION_BATTERY_LOW &&
            intent.action != Intent.ACTION_BATTERY_OKAY &&
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != BatteryWidgetUpdater.ACTION_REFRESH) {
            return
        }

        BatteryWidgetUpdater.refresh(context)
    }
}

object BatteryWidgetUpdater {
    const val ACTION_REFRESH = "com.example.dotos.widgets.ACTION_REFRESH_BATTERY_WIDGETS"
    private const val REQUEST_CODE = 4105
    private const val REFRESH_INTERVAL_MS = 60_000L

    private val providers = arrayOf(
        BatteryWidgetProvider::class.java,
        BatteryCircularWidgetProvider::class.java,
        BatteryBarsWidgetProvider::class.java,
        BatteryMinimalWidgetProvider::class.java,
        BatteryDetailedWidgetProvider::class.java,
    )

    fun refresh(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        var hasWidgets = false

        for (providerClass in providers) {
            hasWidgets = updateWidgets(context, appWidgetManager, providerClass) || hasWidgets
        }

        if (hasWidgets) {
            schedule(context)
        } else {
            cancel(context)
        }
    }

    fun schedule(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = refreshPendingIntent(context)
        val firstRefreshAt = System.currentTimeMillis() + REFRESH_INTERVAL_MS

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            firstRefreshAt,
            REFRESH_INTERVAL_MS,
            pendingIntent,
        )
    }

    fun cancelIfNoBatteryWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val hasWidgets = providers.any { providerClass ->
            appWidgetManager.getAppWidgetIds(ComponentName(context, providerClass)).isNotEmpty()
        }

        if (!hasWidgets) {
            cancel(context)
        }
    }

    private fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(refreshPendingIntent(context))
    }

    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        providerClass: Class<out AppWidgetProvider>,
    ): Boolean {
        val componentName = ComponentName(context, providerClass)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isNotEmpty()) {
            val provider = providerClass.getDeclaredConstructor().newInstance()
            provider.onUpdate(context, appWidgetManager, appWidgetIds)
            return true
        }

        return false
    }

    private fun refreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, BatteryUpdateReceiver::class.java).apply {
            action = ACTION_REFRESH
        }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
