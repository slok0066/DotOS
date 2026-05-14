package com.example.dotos.widgets

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.ComponentName
import android.content.Context
import android.media.session.MediaSessionManager
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean
import com.example.dotos.widgets.WidgetTheme.KEY_ARTIST
import com.example.dotos.widgets.WidgetTheme.KEY_DURATION
import com.example.dotos.widgets.WidgetTheme.KEY_PLAYING
import com.example.dotos.widgets.WidgetTheme.KEY_PROGRESS
import com.example.dotos.widgets.WidgetTheme.KEY_TRACK
import com.example.dotos.widgets.WidgetTheme.MUSIC_PREFS_NAME

/**
 * NotificationListenerService that monitors media notifications and updates
 * the music widget SharedPreferences whenever the playing track changes.
 *
 * This service requires the user to grant Notification Access in Settings.
 */
class MusicNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "MusicNotifListener"
        private const val PROGRESS_POLL_INTERVAL_MS = 1000L

        private val progressHandler = android.os.Handler(android.os.Looper.getMainLooper())
        private var progressPollRunnable: Runnable? = null

        /**
         * Re-entrancy guard. Prevents the cycle:
         *   refreshFromMediaSession → refreshWidget → updateAppWidget
         *   → (widget update fires notification) → onNotificationPosted
         *   → refreshFromMediaSession (infinite)
         */
        private val isRefreshing = AtomicBoolean(false)

        fun refreshFromMediaSession(context: Context) {
            // Bail immediately if a refresh is already in progress
            if (!isRefreshing.compareAndSet(false, true)) {
                Log.d(TAG, "refreshFromMediaSession skipped — already running")
                return
            }
            try {
                val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE)
                        as? MediaSessionManager ?: return

                val component = ComponentName(context, MusicNotificationListener::class.java)
                val sessions: List<MediaController> = try {
                    msm.getActiveSessions(component)
                } catch (e: SecurityException) {
                    Log.w(TAG, "Notification access not granted: ${e.message}")
                    stopProgressPolling()
                    return
                }

                if (sessions.isEmpty()) {
                    Log.d(TAG, "No active media sessions found")
                    stopProgressPolling()
                    return
                }

                val activeController = sessions.firstOrNull { ctrl ->
                    val state = ctrl.playbackState?.state
                    state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED
                } ?: sessions.first()

                applyControllerToPrefs(context, activeController)

            } catch (e: Exception) {
                Log.e(TAG, "refreshFromMediaSession failed", e)
            } finally {
                // Always release the guard so future calls can proceed
                isRefreshing.set(false)
            }
        }

        private fun applyControllerToPrefs(context: Context, controller: MediaController) {
            val meta = controller.metadata ?: return
            val state = controller.playbackState

            val track = (meta.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
                ?: "UNKNOWN TRACK").uppercase()

            val artist = (meta.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: meta.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
                ?: meta.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
                ?: "UNKNOWN ARTIST").uppercase()

            val duration = meta.getLong(MediaMetadata.METADATA_KEY_DURATION).takeIf { it > 0 } ?: 1L
            val position = estimateCurrentPosition(state)
            val progress = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
            val isPlaying = state?.state == PlaybackState.STATE_PLAYING

            Log.d(TAG, "Media update — track='$track' artist='$artist' playing=$isPlaying progress=$progress")

            val prefs = context.getSharedPreferences(MUSIC_PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString(KEY_TRACK, track)
                .putString(KEY_ARTIST, artist)
                .putBoolean(KEY_PLAYING, isPlaying)
                .putFloat(KEY_PROGRESS, progress)
                .putLong(KEY_DURATION, duration)
                .apply()

            // Widget refresh happens AFTER we release the guard via finally{} in refreshFromMediaSession.
            // We schedule it on the main thread so the guard is already released before any
            // downstream notification events fire.
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                MusicPlayerWidgetProvider.refreshWidget(context)
                MusicCompactWidgetProvider.refreshWidget(context)
                MusicTickerWidgetProvider.refreshWidget(context)
                MusicWaveWidgetProvider.refreshWidget(context)
            }

            if (isPlaying) {
                startProgressPolling(context)
            } else {
                stopProgressPolling()
            }
        }

        private fun estimateCurrentPosition(state: PlaybackState?): Long {
            if (state == null) return 0L

            val basePosition = state.position.coerceAtLeast(0L)
            val lastUpdateTime = state.lastPositionUpdateTime
            val playbackSpeed = state.playbackSpeed

            if (lastUpdateTime <= 0L || playbackSpeed == 0f) {
                return basePosition
            }

            val elapsed = (SystemClock.elapsedRealtime() - lastUpdateTime).coerceAtLeast(0L)
            val estimatedPosition = basePosition + (elapsed * playbackSpeed).toLong()
            return estimatedPosition.coerceAtLeast(0L)
        }

        private fun startProgressPolling(context: Context) {
            if (progressPollRunnable != null) return

            val appContext = context.applicationContext
            val runnable = object : Runnable {
                override fun run() {
                    progressPollRunnable?.let { progressHandler.postDelayed(it, PROGRESS_POLL_INTERVAL_MS) }
                    refreshFromMediaSession(appContext)
                }
            }

            progressPollRunnable = runnable
            progressHandler.postDelayed(runnable, PROGRESS_POLL_INTERVAL_MS)
        }

        private fun stopProgressPolling() {
            progressPollRunnable?.let { progressHandler.removeCallbacks(it) }
            progressPollRunnable = null
        }

        fun handleMediaControl(context: Context, action: String) {
            try {
                val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
                val component = ComponentName(context, MusicNotificationListener::class.java)
                val sessions = msm.getActiveSessions(component)
                if (sessions.isEmpty()) {
                    Log.d(TAG, "No active media sessions for control")
                    return
                }

                val activeController = sessions.firstOrNull { ctrl ->
                    val state = ctrl.playbackState?.state
                    state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_PAUSED
                } ?: sessions.first()

                when (action) {
                    "TOGGLE" -> {
                        val state = activeController.playbackState?.state
                        if (state == PlaybackState.STATE_PLAYING) {
                            activeController.transportControls.pause()
                        } else {
                            activeController.transportControls.play()
                        }
                    }
                    "NEXT" -> activeController.transportControls.skipToNext()
                    "PREV" -> activeController.transportControls.skipToPrevious()
                    else -> if (action.startsWith("SEEK_")) {
                        val pct = action.substringAfter("SEEK_").toFloatOrNull()
                        if (pct != null) {
                            val duration = activeController.metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
                            if (duration > 0) {
                                activeController.transportControls.seekTo((duration * (pct / 100f)).toLong())
                            }
                        }
                    }
                }

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    refreshFromMediaSession(context)
                }, 300)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle media control", e)
            }
        }
    }

    // Only fire for media-category notifications to reduce spurious refresh cycles.
    private fun isMediaNotification(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification?.extras ?: return false
        val category = sbn.notification?.category
        // android.app.Notification.CATEGORY_TRANSPORT = "transport"
        return category == "transport" || extras.containsKey("android.mediaSession")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn != null && isMediaNotification(sbn)) {
            refreshFromMediaSession(applicationContext)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn != null && isMediaNotification(sbn)) {
            refreshFromMediaSession(applicationContext)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener connected — initial media scan")
        refreshFromMediaSession(applicationContext)
    }
}
