/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UiPrefs(
    val themeMode: Int = KprofilesPrefs.THEME_SYSTEM,
    val dynamicColor: Boolean = true,
    val accentIndex: Int = 0,
)

class AppThemeViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app.applicationContext

    private val _prefs = MutableStateFlow(
        UiPrefs(
            themeMode = KprofilesPrefs.themeMode(ctx),
            dynamicColor = KprofilesPrefs.dynamicColor(ctx),
            accentIndex = KprofilesPrefs.accentIndex(ctx),
        )
    )
    val prefs: StateFlow<UiPrefs> = _prefs.asStateFlow()

    fun setThemeMode(value: Int) {
        KprofilesPrefs.setThemeMode(ctx, value)
        _prefs.value = _prefs.value.copy(themeMode = value)
    }

    fun setDynamicColor(value: Boolean) {
        KprofilesPrefs.setDynamicColor(ctx, value)
        _prefs.value = _prefs.value.copy(dynamicColor = value)
    }

    fun setAccentIndex(value: Int) {
        KprofilesPrefs.setAccentIndex(ctx, value)
        _prefs.value = _prefs.value.copy(accentIndex = value)
    }
}
