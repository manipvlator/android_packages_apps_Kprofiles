/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 TheParasiteProject
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
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.kprofiles.battery.KProfilesService
import com.android.kprofiles.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class KprofilesViewModel(app: Application) : AndroidViewModel(app) {

    private companion object {
        const val TAG = "KProfiles:VM"
    }

    private val appContext = app.applicationContext
    private val powerManager = appContext.getSystemService(PowerManager::class.java)

    private val _mode = MutableStateFlow(Kprofiles.Mode.NONE)
    val mode: StateFlow<Kprofiles.Mode> = _mode.asStateFlow()

    private val _autoEnabled = MutableStateFlow(false)
    val autoEnabled: StateFlow<Boolean> = _autoEnabled.asStateFlow()

    private val _enabled = MutableStateFlow(KprofilesState.isEnabled(appContext))
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _powerSave = MutableStateFlow(powerManager.isPowerSaveMode)
    val powerSave: StateFlow<Boolean> = _powerSave.asStateFlow()

    @Volatile
    private var receiverRegistered = false

    private val changeIntent: Intent = Intent(Kprofiles.INTENT_ACTION).apply {
        setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Kprofiles.INTENT_ACTION -> refresh()

                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                    _powerSave.value = powerManager.isPowerSaveMode
                    viewModelScope.launch {
                        // The service applies the power-save state first.
                        delay(250)
                        refresh()
                    }
                }
            }
        }
    }

    init {
        try {
            val filter = IntentFilter().apply {
                addAction(Kprofiles.INTENT_ACTION)
                addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
            }
            val flags =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Context.RECEIVER_EXPORTED
                else 0
            ContextCompat.registerReceiver(appContext, receiver, filter, flags)
            receiverRegistered = true
        } catch (t: Throwable) {
            Log.e(TAG, "receiver registration failed", t)
        }
        refresh()
    }

    fun refresh() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val enabled = KprofilesState.isEnabled(appContext)
            _enabled.value = enabled
            _powerSave.value = powerManager.isPowerSaveMode

            if (!enabled) {
                _mode.value = Kprofiles.Mode.NONE
                _autoEnabled.value = false
                return@launch
            }

            if (Kprofiles.IS_SUPPORTED) {
                val raw = FileUtils.readOneLine(Kprofiles.MODES_NODE)
                _mode.value = Kprofiles.Mode.from(raw)
            }
            if (FileUtils.fileExists(Kprofiles.AUTO_NODE)) {
                val raw = FileUtils.readOneLine(Kprofiles.AUTO_NODE)
                _autoEnabled.value = raw == Kprofiles.ON
            }
        } catch (t: Throwable) {
            Log.e(TAG, "refresh failed", t)
        }
    }

    fun setEnabled(enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        if (!Kprofiles.IS_SUPPORTED) return@launch

        KprofilesState.setEnabled(appContext, enabled)
        _enabled.value = enabled

        if (!enabled) {
            FileUtils.writeLine(Kprofiles.AUTO_NODE, Kprofiles.OFF)
            FileUtils.writeLine(Kprofiles.MODES_NODE, Kprofiles.Mode.NONE.value)
            _autoEnabled.value = false
            _mode.value = Kprofiles.Mode.NONE
            runCatching {
                appContext.stopService(Intent(appContext, KProfilesService::class.java))
            }
        } else {
            runCatching {
                appContext.startService(Intent(appContext, KProfilesService::class.java))
            }
            delay(150)
            refresh()
        }

        broadcastChange()
    }

    fun setMode(mode: Kprofiles.Mode?) = viewModelScope.launch(Dispatchers.IO) {
        val target = mode ?: Kprofiles.Mode.NONE
        if (!Kprofiles.IS_SUPPORTED ||
            !KprofilesState.isEnabled(appContext) ||
            powerManager.isPowerSaveMode
        ) {
            return@launch
        }

        try {
            val value = Kprofiles.sanitizeModeValue(target.value)
            val ok = FileUtils.writeLine(Kprofiles.MODES_NODE, value)
            if (!ok) {
                Log.w(TAG, "write failed for mode=$value")
                return@launch
            }
            KprofilesState.setMode(appContext, value)
            _mode.value = target
            broadcastChange()
        } catch (t: Throwable) {
            Log.e(TAG, "setMode failed", t)
        }
    }

    fun setAuto(enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        if (!Kprofiles.IS_SUPPORTED ||
            !KprofilesState.isEnabled(appContext) ||
            powerManager.isPowerSaveMode
        ) {
            return@launch
        }

        try {
            if (!FileUtils.fileExists(Kprofiles.AUTO_NODE)) return@launch
            val value = if (enabled) Kprofiles.ON else Kprofiles.OFF
            val ok = FileUtils.writeLine(Kprofiles.AUTO_NODE, value)
            if (!ok) {
                Log.w(TAG, "write failed for auto=$enabled")
                return@launch
            }
            KprofilesState.setAuto(appContext, enabled)
            _autoEnabled.value = enabled
            broadcastChange()
        } catch (t: Throwable) {
            Log.e(TAG, "setAuto failed", t)
        }
    }

    private fun broadcastChange() {
        try {
            appContext.sendBroadcast(changeIntent)
        } catch (t: Throwable) {
            Log.w(TAG, "broadcast failed", t)
        }
    }

    override fun onCleared() {
        if (receiverRegistered) {
            runCatching { appContext.unregisterReceiver(receiver) }
            receiverRegistered = false
        }
        super.onCleared()
    }
}
