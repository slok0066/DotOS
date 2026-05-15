package com.example.dotos

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dotos.widgets.*
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val WIDGET_CHANNEL = "widget_channel"
    private val PERMISSION_CHANNEL = "permission_channel"
    private val PERMISSION_REQUEST_CODE = 1001
    
    private var pendingPermissionResult: MethodChannel.Result? = null
    private var pendingPermissionType: String? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // Widget channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, WIDGET_CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "addWidget") {
                val type = call.argument<String>("type")
                val theme = call.argument<String>("theme") ?: call.argument<String>("themeMode")
                if (theme != null && type != null) {
                    WidgetTheme.saveCategoryTheme(this, type, theme)
                }
                val success = requestPinWidget(type)
                result.success(success)
            } else if (call.method == "setWidgetTheme") {
                val theme = call.argument<String>("theme")
                if (theme != null) {
                    WidgetTheme.saveTheme(this, theme)
                    refreshAllWidgets()
                }
                result.success(true)
            } else if (call.method == "getScreenTimeMillis") {
                result.success(readTodayScreenTimeStats().totalMillis)
            } else if (call.method == "getScreenTimeStats") {
                val stats = readTodayScreenTimeStats()
                val totalMinutes = (stats.totalMillis / 60_000L).toInt()
                result.success(
                    mapOf(
                        "hasPermission" to stats.hasPermission,
                        "totalMillis" to stats.totalMillis,
                        "hours" to (totalMinutes / 60),
                        "minutes" to (totalMinutes % 60),
                        "topAppLabel" to stats.topAppLabel,
                    )
                )
            } else {
                result.notImplemented()
            }
        }
        
        // Permission channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PERMISSION_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "requestPermission" -> {
                    val type = call.argument<String>("type")
                    requestPermission(type, result)
                }
                "checkPermission" -> {
                    val type = call.argument<String>("type")
                    checkPermission(type, result)
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun refreshAllWidgets() {
        // Delegate to the central broadcaster — updates ALL instances of ALL widget types
        WidgetUpdateBroadcaster.refreshAll(this)
        // Also refresh screen time via its dedicated updater
        ScreenTimeWidgetUpdater.refreshAll(this)
    }

    override fun onResume() {
        super.onResume()
        refreshScreenTimeWidgetsIfAllowed()
    }

    private fun readTodayScreenTimeStats(): ScreenTimeStats {
        return ScreenTimeUsageReader.readToday(this)
    }

    private fun refreshScreenTimeWidgetsIfAllowed() {
        if (!ScreenTimeUsageReader.hasPermission(this)) {
            return
        }

        ScreenTimeWidgetUpdater.refreshAll(this)
    }

    private fun checkPermission(type: String?, result: MethodChannel.Result) {
        when (type) {
            "battery" -> {
                // Battery stats permission is automatically granted
                result.success(true)
            }
            "storage" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ doesn't need storage permission for app-specific storage
                    result.success(true)
                } else {
                    val granted = ContextCompat.checkSelfPermission(
                        this, 
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                    result.success(granted)
                }
            }
            "calendar" -> {
                val granted = ContextCompat.checkSelfPermission(
                    this, 
                    Manifest.permission.READ_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
                result.success(granted)
            }
            "media" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        this, 
                        Manifest.permission.READ_MEDIA_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    result.success(granted)
                } else {
                    // Android 12 and below use READ_EXTERNAL_STORAGE
                    val granted = ContextCompat.checkSelfPermission(
                        this, 
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                    result.success(granted)
                }
            }
            "notification" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        this, 
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    result.success(granted)
                } else {
                    // Notifications don't need permission on Android 12 and below
                    result.success(true)
                }
            }
            "usage_stats" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
                    val mode = appOps.checkOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        packageName
                    )
                    result.success(mode == android.app.AppOpsManager.MODE_ALLOWED)
                } else {
                    result.success(true)
                }
            }
            "sound" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    result.success(notificationManager.isNotificationPolicyAccessGranted)
                } else {
                    result.success(true)
                }
            }
            "notification_listener" -> {
                // Check if our NotificationListenerService is enabled
                val enabledListeners = android.provider.Settings.Secure.getString(
                    contentResolver,
                    "enabled_notification_listeners"
                ) ?: ""
                result.success(enabledListeners.contains(packageName))
            }
            else -> {
                result.success(false)
            }
        }
    }

    private fun requestPermission(type: String?, result: MethodChannel.Result) {
        when (type) {
            "battery" -> {
                // Battery stats permission is automatically granted
                result.success(true)
            }
            "storage" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ doesn't need storage permission for app-specific storage
                    result.success(true)
                } else {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                        == PackageManager.PERMISSION_GRANTED) {
                        result.success(true)
                    } else {
                        pendingPermissionResult = result
                        pendingPermissionType = "storage"
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }
            "calendar" -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) 
                    == PackageManager.PERMISSION_GRANTED) {
                    result.success(true)
                } else {
                    pendingPermissionResult = result
                    pendingPermissionType = "calendar"
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_CALENDAR),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
            "media" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) 
                        == PackageManager.PERMISSION_GRANTED) {
                        result.success(true)
                    } else {
                        pendingPermissionResult = result
                        pendingPermissionType = "media"
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                } else {
                    // Android 12 and below use READ_EXTERNAL_STORAGE
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                        == PackageManager.PERMISSION_GRANTED) {
                        result.success(true)
                    } else {
                        pendingPermissionResult = result
                        pendingPermissionType = "media"
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                }
            }
            "notification" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                        == PackageManager.PERMISSION_GRANTED) {
                        result.success(true)
                    } else {
                        pendingPermissionResult = result
                        pendingPermissionType = "notification"
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                            PERMISSION_REQUEST_CODE
                        )
                    }
                } else {
                    // Notifications don't need permission on Android 12 and below
                    result.success(true)
                }
            }
            "usage_stats" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
                    val mode = appOps.checkOpNoThrow(
                        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                        android.os.Process.myUid(),
                        packageName
                    )
                    if (mode == android.app.AppOpsManager.MODE_ALLOWED) {
                        result.success(true)
                    } else {
                        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        result.success(false)
                    }
                } else {
                    result.success(true)
                }
            }
            "sound" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    if (notificationManager.isNotificationPolicyAccessGranted) {
                        result.success(true)
                    } else {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        result.success(false)
                    }
                } else {
                    result.success(true)
                }
            }
            "notification_listener" -> {
                // Open Notification Access settings so user can enable our listener
                val enabledListeners = android.provider.Settings.Secure.getString(
                    contentResolver,
                    "enabled_notification_listeners"
                ) ?: ""
                if (enabledListeners.contains(packageName)) {
                    result.success(true)
                } else {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    result.success(false)
                }
            }
            else -> {
                result.success(false)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            pendingPermissionResult?.success(granted)
            pendingPermissionResult = null
            pendingPermissionType = null
        }
    }

    private fun requestPinWidget(type: String?): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        
        if (!appWidgetManager.isRequestPinAppWidgetSupported) {
            return false
        }

        // Debug logging
        android.util.Log.d("DotOS", "Requesting widget type: $type")

        val providerClass = when (type) {
            "clock" -> ClockWidgetProvider::class.java
            "clock_02" -> ClockAnalogWidgetProvider::class.java
            "clock_03" -> ClockBinaryWidgetProvider::class.java
            "calendar", "calendar_01" -> CalendarWidgetProvider::class.java
            "calendar_02" -> CalendarWeekWidgetProvider::class.java
            "calendar_03" -> CalendarEventListWidgetProvider::class.java
            "calendar_04" -> CalendarMonthWidgetProvider::class.java
            "calendar_05" -> CalendarAgendaWidgetProvider::class.java
            "battery", "battery_01" -> BatteryWidgetProvider::class.java
            "battery_02" -> BatteryCircularWidgetProvider::class.java
            "battery_03" -> BatteryBarsWidgetProvider::class.java
            "battery_04" -> BatteryMinimalWidgetProvider::class.java
            "battery_05" -> BatteryDetailedWidgetProvider::class.java
            "storage", "storage_01" -> StorageWidgetProvider::class.java
            "storage_02" -> StorageCircularWidgetProvider::class.java
            "storage_03" -> StorageCompactWidgetProvider::class.java
            "storage_04" -> StorageAnalysisWidgetProvider::class.java
            "storage_05" -> StorageDetailedWidgetProvider::class.java
            "calculator", "calculator_01" -> CalculatorWidgetProvider::class.java
            "tap_counter", "tap_counter_01" -> TapCounterWidgetProvider::class.java
            "tap_counter_02" -> TapCounterDialWidgetProvider::class.java
            "tap_counter_03" -> TapCounterMatrixWidgetProvider::class.java
            "screen_time", "screen_time_01" -> ScreenTimeMinimalWidgetProvider::class.java
            "screen_time_02" -> ScreenTimeRingWidgetProvider::class.java
            "screen_time_03" -> ScreenTimeSplitWidgetProvider::class.java
            "sound", "sound_01" -> SoundWidgetProvider::class.java
            "sound_02" -> SoundSegmentsWidgetProvider::class.java
            "sound_03" -> SoundDialWidgetProvider::class.java
            "spinner", "spinner_01" -> SpinnerWidgetProvider::class.java
            "bottle_spin", "bottle_spin_01" -> BottleSpinWidgetProvider::class.java
            "dice_roll", "dice_roll_01" -> DiceRollWidgetProvider::class.java
            "dino_game", "dino_game_01" -> DinoGameWidgetProvider::class.java
            "coin_flip", "coin_flip_01" -> CoinFlipWidgetProvider::class.java
            "music_01", "music_player" -> MusicPlayerWidgetProvider::class.java
            "music_02" -> MusicVinylWidgetProvider::class.java
            "music_03" -> MusicCompactWidgetProvider::class.java
            "music_04" -> MusicTickerWidgetProvider::class.java
            "music_05" -> MusicWaveWidgetProvider::class.java
            else -> {
                android.util.Log.w("DotOS", "Unknown widget type: $type, defaulting to ClockWidgetProvider")
                ClockWidgetProvider::class.java
            }
        }

        android.util.Log.d("DotOS", "Selected provider class: ${providerClass.simpleName}")

        val componentName = ComponentName(this, providerClass)
        val pinnedWidgetCallbackIntent = if (type?.startsWith("screen_time") == true) {
            Intent(this, ScreenTimeRefreshReceiver::class.java).apply {
                action = ScreenTimeWidgetUpdater.ACTION_REFRESH
            }
        } else {
            Intent(this, javaClass)
        }
        val successCallback = PendingIntent.getBroadcast(
            this, 0, pinnedWidgetCallbackIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return appWidgetManager.requestPinAppWidget(componentName, null, successCallback)
    }
}
