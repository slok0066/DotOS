package com.example.dotos.widgets

import android.content.Context
import android.graphics.Color

object WidgetTheme {
    private const val PREFS_NAME = "widget_prefs"
    private const val KEY_WIDGET_THEME = "widget_theme"
    private const val KEY_WIDGET_THEME_PREFIX = "widget_theme_id_"
    private const val KEY_CATEGORY_THEME_PREFIX = "widget_theme_cat_"

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

    /** Read the global theme (used as default for new widgets). */
    fun currentTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return normalize(prefs.getString(KEY_WIDGET_THEME, THEME_DARK))
    }

    /** Save theme for a specific widget category (e.g. "clock", "battery"). */
    fun saveCategoryTheme(context: Context, category: String, theme: String) {
        val key = KEY_CATEGORY_THEME_PREFIX + category
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(key, normalize(theme))
            .apply()
    }

    /** Read the theme for a specific widget category. Falls back to global theme. */
    fun categoryTheme(context: Context, category: String): String {
        val key = KEY_CATEGORY_THEME_PREFIX + category
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(key)) {
            normalize(prefs.getString(key, THEME_DARK))
        } else {
            currentTheme(context)
        }
    }

    /**
     * Read the theme locked to a SPECIFIC widget instance.
     * If this widget has no saved theme yet, it initialises from the category theme.
     */
    fun widgetTheme(context: Context, appWidgetId: Int, category: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = KEY_WIDGET_THEME_PREFIX + appWidgetId
        return if (prefs.contains(key)) {
            normalize(prefs.getString(key, THEME_DARK))
        } else {
            // First time this widget is rendered — lock it to the category theme
            val catTheme = categoryTheme(context, category)
            prefs.edit().putString(key, catTheme).apply()
            catTheme
        }
    }

    /**
     * Read the theme locked to a SPECIFIC widget instance (legacy, uses global fallback).
     */
    fun widgetTheme(context: Context, appWidgetId: Int): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = KEY_WIDGET_THEME_PREFIX + appWidgetId
        return if (prefs.contains(key)) {
            normalize(prefs.getString(key, THEME_DARK))
        } else {
            val global = currentTheme(context)
            prefs.edit().putString(key, global).apply()
            global
        }
    }

    /**
     * Returns the palette locked to this specific widget instance.
     * Changing the global theme will NOT affect already-added widgets.
     */
    fun paletteForWidget(context: Context, appWidgetId: Int): Palette {
        return if (widgetTheme(context, appWidgetId) == THEME_DARK) darkPalette else lightPalette
    }

    /**
     * Returns the palette for a widget instance, initialized from category theme.
     */
    fun paletteForWidget(context: Context, appWidgetId: Int, category: String): Palette {
        return if (widgetTheme(context, appWidgetId, category) == THEME_DARK) darkPalette else lightPalette
    }

    /** Returns the global palette — use only for new/unkeyed renders. */
    fun palette(context: Context): Palette {
        return if (isDark(context)) darkPalette else lightPalette
    }

    /** Returns the palette for a category (useful for previews). */
    fun paletteForCategory(context: Context, category: String): Palette {
        return if (categoryTheme(context, category) == THEME_DARK) darkPalette else lightPalette
    }

    /** Save the global theme (does NOT touch per-widget themes). */
    fun saveTheme(context: Context, theme: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WIDGET_THEME, normalize(theme))
            .apply()
    }

    /** Remove per-widget theme when a widget is deleted to avoid stale prefs. */
    fun removeWidgetTheme(context: Context, appWidgetId: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_WIDGET_THEME_PREFIX + appWidgetId)
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