/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeScreen(
    themeVm: AppThemeViewModel,
    onBack: () -> Unit,
) {
    val ui by themeVm.prefs.collectAsStateWithLifecycle()
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kprofiles_customize_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SettingsBrightness, contentDescription = null)
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = stringResource(R.string.kprofiles_theme_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeChip(
                            label = stringResource(R.string.kprofiles_theme_system),
                            selected = ui.themeMode == KprofilesPrefs.THEME_SYSTEM,
                            onClick = { themeVm.setThemeMode(KprofilesPrefs.THEME_SYSTEM) },
                        )
                        ThemeChip(
                            label = stringResource(R.string.kprofiles_theme_light),
                            icon = Icons.Filled.LightMode,
                            selected = ui.themeMode == KprofilesPrefs.THEME_LIGHT,
                            onClick = { themeVm.setThemeMode(KprofilesPrefs.THEME_LIGHT) },
                        )
                        ThemeChip(
                            label = stringResource(R.string.kprofiles_theme_dark),
                            icon = Icons.Filled.DarkMode,
                            selected = ui.themeMode == KprofilesPrefs.THEME_DARK,
                            onClick = { themeVm.setThemeMode(KprofilesPrefs.THEME_DARK) },
                        )
                    }
                }
            }

            ElevatedCard {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Palette, contentDescription = null)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.kprofiles_dynamic_color_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = if (supportsDynamic)
                                stringResource(R.string.kprofiles_dynamic_color_summary)
                            else
                                stringResource(R.string.kprofiles_dynamic_color_unsupported),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = ui.dynamicColor && supportsDynamic,
                        onCheckedChange = { themeVm.setDynamicColor(it) },
                        enabled = supportsDynamic,
                    )
                }
            }

            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.kprofiles_accent_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = stringResource(R.string.kprofiles_accent_summary),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        KprofilesPrefs.ACCENTS.forEachIndexed { index, argb ->
                            AccentDot(
                                color = Color(argb),
                                selected = ui.accentIndex == index,
                                enabled = !ui.dynamicColor || !supportsDynamic,
                                onClick = { themeVm.setAccentIndex(index) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = icon?.let { { Icon(it, null, Modifier.size(16.dp)) } },
    )
}

@Composable
private fun AccentDot(
    color: Color,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (enabled) color else color.copy(alpha = 0.35f))
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
            )
        }
    }
}
