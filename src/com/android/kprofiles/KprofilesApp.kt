/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

private enum class Screen { Main, Customize }

@Composable
fun KprofilesApp(
    themeVm: AppThemeViewModel,
    onBack: () -> Unit,
) {
    var screen by rememberSaveable { mutableStateOf(Screen.Main) }

    when (screen) {
        Screen.Main -> KprofilesScreen(
            onBack = onBack,
            onOpenCustomize = { screen = Screen.Customize },
        )

        Screen.Customize -> {
            BackHandler { screen = Screen.Main }
            CustomizeScreen(
                themeVm = themeVm,
                onBack = { screen = Screen.Main },
            )
        }
    }
}
