package com.example.dotos.widgets

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Central broadcaster that refreshes ALL instances of EVERY widget type.
 * Call this whenever theme changes, counter updates, music updates, etc.
 * 
 * Usage from Flutter (via MethodChannel):
 *   WidgetUpdateBroadcaster.refreshAll(context)
 * 
 * Usage from a widget provider:
 *   WidgetUpdateBroadcaster.refreshAll(context)
 */
object WidgetUpdateBroadcaster {

    const val ACTION_REFRESH_ALL = "com.example.dotos.ACTION_REFRESH_ALL_WIDGETS"

    /**
     * Broadcast an intent that wakes up all widget providers and forces them to redraw.
     * This is the safest way — each provider handles its own re-render on receipt.
     */
    fun refreshAll(context: Context) {
        refreshClocks(context)
        refreshTapCounters(context)
        refreshBattery(context)
        refreshStorage(context)
        refreshMusic(context)
        refreshScreenTime(context)
        refreshSound(context)
        refreshGames(context)
    }

    fun refreshTheme(context: Context) {
        // Theme change = full refresh
        refreshAll(context)
    }

    fun refreshClocks(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        listOf(
            ClockWidgetProvider::class.java,
            ClockAnalogWidgetProvider::class.java,
            ClockBinaryWidgetProvider::class.java,
        ).forEach { cls ->
            val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    fun refreshTapCounters(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        listOf(
            TapCounterWidgetProvider::class.java,
            TapCounterDialWidgetProvider::class.java,
            TapCounterMatrixWidgetProvider::class.java,
        ).forEach { cls ->
            val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    fun refreshBattery(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        listOf(
            BatteryWidgetProvider::class.java,
            BatteryCircularWidgetProvider::class.java,
            BatteryBarsWidgetProvider::class.java,
            BatteryMinimalWidgetProvider::class.java,
            BatteryDetailedWidgetProvider::class.java,
        ).forEach { cls ->
            val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    fun refreshStorage(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        listOf(
            StorageWidgetProvider::class.java,
            StorageCircularWidgetProvider::class.java,
            StorageCompactWidgetProvider::class.java,
            StorageDetailedWidgetProvider::class.java,
            StorageAnalysisWidgetProvider::class.java,
        ).forEach { cls ->
            val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    fun refreshMusic(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        listOf(
            MusicVinylWidgetProvider::class.java,
            MusicPlayerWidgetProvider::class.java,
            MusicCompactWidgetProvider::class.java,
            MusicTickerWidgetProvider::class.java,
            MusicWaveWidgetProvider::class.java,
        ).forEach { cls ->
            val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    fun refreshScreenTime(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        listOf(
            ScreenTimeMinimalWidgetProvider::class.java,
            ScreenTimeRingWidgetProvider::class.java,
            ScreenTimeSplitWidgetProvider::class.java,
        ).forEach { cls ->
            val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    fun refreshSound(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        listOf(
            SoundWidgetProvider::class.java,
            SoundDialWidgetProvider::class.java,
            SoundSegmentsWidgetProvider::class.java,
        ).forEach { cls ->
            val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    fun refreshGames(context: Context) {
        val mgr = AppWidgetManager.getInstance(context)
        listOf(
            DinoGameWidgetProvider::class.java,
            CoinFlipWidgetProvider::class.java,
            DiceRollWidgetProvider::class.java,
            SpinnerWidgetProvider::class.java,
            BottleSpinWidgetProvider::class.java,
        ).forEach { cls ->
            val ids = mgr.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                val intent = Intent(context, cls).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }
}
