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
import android.util.Log
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

    private companion object {
        const val TAG = "KProfiles:VM"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    private val appContext = app.applicationContext

    private val _mode = MutableStateFlow(Kprofiles.Mode.NONE)
    val mode: StateFlow<Kprofiles.Mode> = _mode.asStateFlow()

    private val _autoEnabled = MutableStateFlow(false)
    val autoEnabled: StateFlow<Boolean> = _autoEnabled.asStateFlow()

    @Volatile
    private var selfChange = false

    @Volatile
    private var receiverRegistered = false

    private val changeIntent: Intent = Intent(Kprofiles.INTENT_ACTION).apply {
        setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
    }

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
        try {
            val filter = IntentFilter(Kprofiles.INTENT_ACTION)
            val flags =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Context.RECEIVER_NOT_EXPORTED
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

    fun setMode(mode: Kprofiles.Mode?) = viewModelScope.launch(Dispatchers.IO) {
        val target = mode ?: Kprofiles.Mode.NONE
        if (!Kprofiles.IS_SUPPORTED) return@launch
        try {
            val value = Kprofiles.sanitizeModeValue(target.value)
            val ok = FileUtils.writeLine(Kprofiles.MODES_NODE, value)
            if (!ok) {
                Log.w(TAG, "write failed for mode=$value")
                return@launch
            }
            prefs.edit { putString(Kprofiles.MODES_KEY, value) }
            _mode.value = target
            broadcastSelfChange()
        } catch (t: Throwable) {
            Log.e(TAG, "setMode failed", t)
        }
    }

    fun cycleMode() {
        setMode(Kprofiles.Mode.next(_mode.value))
    }

    fun setAuto(enabled: Boolean) = viewModelScope.launch(Dispatchers.IO) {
        try {
            if (!FileUtils.fileExists(Kprofiles.AUTO_NODE)) return@launch
            val value = if (enabled) Kprofiles.ON else Kprofiles.OFF
            val ok = FileUtils.writeLine(Kprofiles.AUTO_NODE, value)
            if (!ok) {
                Log.w(TAG, "write failed for auto=$enabled")
                return@launch
            }
            prefs.edit { putBoolean(Kprofiles.AUTO_KEY, enabled) }
            _autoEnabled.value = enabled
        } catch (t: Throwable) {
            Log.e(TAG, "setAuto failed", t)
        }
    }

    private fun broadcastSelfChange() {
        selfChange = true
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
