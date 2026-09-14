/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

object KprofilesPrefs {
    const val KEY_THEME = "ui_theme"
    const val KEY_DYNAMIC_COLOR = "ui_dynamic"
    const val KEY_ACCENT = "ui_accent"

    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    val ACCENTS = listOf(
        0xFF6750A4.toInt(),
        0xFF0061A4.toInt(),
        0xFF006E1C.toInt(),
        0xFF8B5000.toInt(),
        0xFF9A4058.toInt(),
        0xFF5D5F79.toInt(),
    )

    fun themeMode(ctx: Context): Int =
        prefs(ctx).getInt(KEY_THEME, THEME_SYSTEM)

    fun setThemeMode(ctx: Context, value: Int) =
        prefs(ctx).edit { putInt(KEY_THEME, value) }

    fun dynamicColor(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_DYNAMIC_COLOR, true)

    fun setDynamicColor(ctx: Context, value: Boolean) =
        prefs(ctx).edit { putBoolean(KEY_DYNAMIC_COLOR, value) }

    fun accentIndex(ctx: Context): Int =
        prefs(ctx).getInt(KEY_ACCENT, 0)

    fun setAccentIndex(ctx: Context, value: Int) =
        prefs(ctx).edit { putInt(KEY_ACCENT, value) }

    private fun prefs(ctx: Context) =
        PreferenceManager.getDefaultSharedPreferences(ctx)
}
