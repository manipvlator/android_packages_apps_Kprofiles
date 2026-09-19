/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun resolveDarkTheme(mode: Int): Boolean = when (mode) {
    KprofilesPrefs.THEME_LIGHT -> false
    KprofilesPrefs.THEME_DARK -> true
    else -> isSystemInDarkTheme()
}

@Composable
fun rememberKprofilesColorScheme(
    dark: Boolean,
    dynamicColor: Boolean,
    accentIndex: Int,
): ColorScheme {
    val ctx = LocalContext.current
    return remember(dark, dynamicColor, accentIndex, ctx) {
        val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        val base = when {
            dynamicColor && supportsDynamic ->
                if (dark) dynamicDarkColorScheme(ctx)
                else dynamicLightColorScheme(ctx)
            dark -> darkColorScheme()
            else -> lightColorScheme()
        }

        if (accentIndex in 1 until KprofilesPrefs.ACCENTS.size) {
            val accent = Color(KprofilesPrefs.ACCENTS[accentIndex])
            base.copy(
                primary = accent,
                onPrimary = Color.White,
                primaryContainer = accent.copy(alpha = 0.20f),
                onPrimaryContainer = accent,
            )
        } else {
            base
        }
    }
}

val LocalUiPrefs = staticCompositionLocalOf { UiPrefs() }

@Composable
fun ProvideUiPrefs(
    prefs: UiPrefs,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalUiPrefs provides prefs, content = content)
}
