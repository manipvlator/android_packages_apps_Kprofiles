/*
 * Copyright (C) 2026 TheParasiteProject
 * Copyright (C) 2026 haloUI
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles.battery

import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.content.ContextCompat
import com.android.kprofiles.Kprofiles
import com.android.kprofiles.KprofilesState
import com.android.kprofiles.utils.FileUtils

class KProfilesService : Service() {

    private companion object {
        const val TAG = "KProfiles:Service"
    }

    private val powerManager by lazy {
        getSystemService(PowerManager::class.java)
    }

    private val tileComponent by lazy {
        ComponentName(this, KprofilesModesTileService::class.java)
    }

    private val intentFilter = IntentFilter().apply {
        addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        addAction(Kprofiles.INTENT_ACTION)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Kprofiles.INTENT_ACTION,
                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> applyState()
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!Kprofiles.IS_SUPPORTED) {
            stopSelf()
            return
        }

        try {
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_EXPORTED
            } else 0
            ContextCompat.registerReceiver(
                this,
                receiver,
                intentFilter,
                flags,
            )
        } catch (t: Throwable) {
            Log.e(TAG, "receiver registration failed", t)
        }
        applyState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Kprofiles.IS_SUPPORTED) {
            stopSelf()
            return START_NOT_STICKY
        }
        applyState()
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(receiver) }
        super.onDestroy()
    }

    private fun applyState() {
        if (!Kprofiles.IS_SUPPORTED) return

        val enabled = KprofilesState.isEnabled(this)
        val powerSave = powerManager.isPowerSaveMode

        val mode = when {
            !enabled -> Kprofiles.Mode.NONE
            powerSave -> Kprofiles.Mode.BATTERY
            else -> Kprofiles.Mode.from(KprofilesState.getMode(this))
        }
        val auto = enabled && !powerSave && KprofilesState.isAutoEnabled(this)

        FileUtils.writeLine(Kprofiles.MODES_NODE, mode.value)
        if (FileUtils.fileExists(Kprofiles.AUTO_NODE)) {
            FileUtils.writeLine(
                Kprofiles.AUTO_NODE,
                if (auto) Kprofiles.ON else Kprofiles.OFF,
            )
        }

        runCatching {
            TileService.requestListeningState(this, tileComponent)
        }
    }
}
