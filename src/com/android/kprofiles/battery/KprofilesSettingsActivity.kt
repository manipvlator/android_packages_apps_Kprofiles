/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles.battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.kprofiles.AppThemeViewModel
import com.android.kprofiles.KprofilesApp
import com.android.kprofiles.ProvideUiPrefs
import com.android.kprofiles.rememberKprofilesColorScheme
import com.android.kprofiles.resolveDarkTheme
import androidx.activity.SystemBarStyle

class KprofilesSettingsActivity : ComponentActivity() {

    private val themeVm: AppThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        setContent {
            val ui by themeVm.prefs.collectAsStateWithLifecycle()

            val dark = resolveDarkTheme(ui.themeMode)
            val scheme = rememberKprofilesColorScheme(
                dark = dark,
                dynamicColor = ui.dynamicColor,
                accentIndex = ui.accentIndex,
            )

            ProvideUiPrefs(ui) {
                MaterialTheme(colorScheme = scheme) {
                    Surface {
                        KprofilesApp(
                            themeVm = themeVm,
                            onBack = { finish() },
                        )
                    }
                }
            }
        }
    }
}
