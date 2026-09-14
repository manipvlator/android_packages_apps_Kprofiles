/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.android.kprofiles.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KprofilesViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    private val appContext = app.applicationContext

    private val _mode = MutableStateFlow(Kprofiles.Mode.NONE)
    val mode: StateFlow<Kprofiles.Mode> = _mode.asStateFlow()

    private val _autoEnabled = MutableStateFlow(false)
    val autoEnabled: StateFlow<Boolean> = _autoEnabled.asStateFlow()

    private var selfChange = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Kprofiles.INTENT_ACTION) return
            if (selfChange) {
                selfChange = false
                return
            }
            refresh()
        }
    }

    init {
        val filter = IntentFilter(Kprofiles.INTENT_ACTION)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Context.RECEIVER_NOT_EXPORTED
            else 0
        ContextCompat.registerReceiver(appContext, receiver, filter, flags)
        refresh()
    }

    fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        if (Kprofiles.IS_SUPPORTED) {
            _mode.value = Kprofiles.Mode.from(FileUtils.readOneLine(Kprofiles.MODES_NODE))
        }
        if (FileUtils.fileExists(Kprofiles.AUTO_NODE)) {
            _autoEnabled.value =
                FileUtils.readOneLine(Kprofiles.AUTO_NODE) == Kprofiles.ON
        }
    }

    fun setMode(mode: Kprofiles.Mode) = viewModelScope.launch(Dispatchers.IO) {
        if (!Kprofiles.IS_SUPPORTED) return@launch
        FileUtils.writeLine(Kprofiles.MODES_NODE, mode.value)
        prefs.edit { putString(Kprofiles.MODES_KEY, mode.value) }
        _mode.value = mode
        broadcastSelfChange()
    }

    fun cycleMode() = setMode(Kprofiles.Mode.next(_mode.value))

    fun setAuto(enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        if (!FileUtils.fileExists(Kprofiles.AUTO_NODE)) return@launch
        FileUtils.writeLine(
            Kprofiles.AUTO_NODE,
            if (enabled) Kprofiles.ON else Kprofiles.OFF,
        )
        prefs.edit { putBoolean(Kprofiles.AUTO_KEY, enabled) }
        _autoEnabled.value = enabled
    }

    private fun broadcastSelfChange() {
        selfChange = true
        val intent = Intent(Kprofiles.INTENT_ACTION).apply {
            setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
        }
        appContext.sendBroadcast(intent)
    }

    override fun onCleared() {
        appContext.unregisterReceiver(receiver)
        super.onCleared()
    }
}
