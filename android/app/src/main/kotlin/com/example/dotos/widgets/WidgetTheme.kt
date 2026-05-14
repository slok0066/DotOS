package com.example.dotos.widgets

import android.content.Context
import android.graphics.Color

object WidgetTheme {
    private const val PREFS_NAME = "widget_prefs"
    private const val KEY_WIDGET_THEME = "widget_theme"

    // Music widget constants
    const val MUSIC_PREFS_NAME = "music_widget"
    const val KEY_PLAYING = "playing"
    const val KEY_TRACK = "track"
    const val KEY_ARTIST = "artist"
    const val KEY_PROGRESS = "progress"
    const val KEY_DURATION = "duration"

    const val THEME_DARK = "dark"
    const val THEME_LIGHT = "light"

    data class Palette(
        val background: Int,
        val grid: Int,
        val label: Int,
        val primary: Int,
        val secondary: Int,
        val muted: Int,
        val border: Int,
        val success: Int,
        val accent: Int,
    )

    private val darkPalette = Palette(
        background = Color.parseColor("#111111"),
        grid = Color.parseColor("#333333"),
        label = Color.parseColor("#999999"),
        primary = Color.WHITE,
        secondary = Color.parseColor("#E8E8E8"),
        muted = Color.parseColor("#666666"),
        border = Color.parseColor("#333333"),
        success = Color.parseColor("#4CAF50"),
        accent = Color.parseColor("#D71921"),
    )

    private val lightPalette = Palette(
        background = Color.parseColor("#F5F5F0"),
        grid = Color.parseColor("#D8D8D8"),
        label = Color.parseColor("#666666"),
        primary = Color.BLACK,
        secondary = Color.parseColor("#1A1A1A"),
        muted = Color.parseColor("#777777"),
        border = Color.parseColor("#CCCCCC"),
        success = Color.parseColor("#2E7D32"),
        accent = Color.parseColor("#D71921"),
    )

    fun currentTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return normalize(prefs.getString(KEY_WIDGET_THEME, THEME_DARK))
    }

    fun palette(context: Context): Palette {
        return if (isDark(context)) darkPalette else lightPalette
    }

    fun saveTheme(context: Context, theme: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WIDGET_THEME, normalize(theme))
            .apply()
    }

    fun isDark(context: Context): Boolean {
        return currentTheme(context) == THEME_DARK
    }

    private fun normalize(theme: String?): String {
        return when (theme) {
            THEME_LIGHT -> THEME_LIGHT
            else -> THEME_DARK
        }
    }
}