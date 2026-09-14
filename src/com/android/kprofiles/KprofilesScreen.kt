/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.kprofiles.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KprofilesScreen(
    onBack: () -> Unit,
    onOpenCustomize: () -> Unit,
    vm: KprofilesViewModel = viewModel(),
) {
    val mode by vm.mode.collectAsStateWithLifecycle()
    val autoEnabled by vm.autoEnabled.collectAsStateWithLifecycle()
    val supported = Kprofiles.IS_SUPPORTED
    val autoSupported = remember { FileUtils.fileExists(Kprofiles.AUTO_NODE) }

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kprofiles_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenCustomize) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = stringResource(
                                R.string.kprofiles_customize_open
                            ),
                        )
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.kprofiles_auto_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = if (autoSupported)
                                stringResource(R.string.kprofiles_auto_summary)
                            else
                                stringResource(R.string.kprofiles_not_supported),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(
                        checked = autoEnabled,
                        onCheckedChange = { vm.setAuto(it) },
                        enabled = autoSupported,
                    )
                }
            }

            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.kprofiles_modes_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(12.dp))

                    Kprofiles.Mode.entries.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = mode == m,
                                onClick = { vm.setMode(m) },
                                enabled = supported,
                            )
                            Icon(
                                painter = painterResource(m.iconRes),
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            Text(
                                text = stringResource(m.labelRes),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .heightIn(min = 92.dp),
                ) {
                    Text(
                        text = stringResource(R.string.kprofiles_modes_description_title),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (supported)
                            stringResource(mode.descRes)
                        else
                            stringResource(R.string.kprofiles_not_supported),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }
    }
}
