/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.android.kprofiles.Kprofiles
import com.android.kprofiles.utils.FileUtils

class KprofilesModesTileService : TileService() {

    private companion object {
        const val TAG = "KProfiles:Tile"
    }

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
            updateTile()
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            if (!Kprofiles.IS_SUPPORTED) {
                qsTile?.apply {
                    state = Tile.STATE_UNAVAILABLE
                    updateTile()
                }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "onCreate failed", t)
        }
    }

    override fun onStartListening() {
        try {
            if (!Kprofiles.IS_SUPPORTED) return
            super.onStartListening()

            if (!receiverRegistered) {
                val filter = IntentFilter(Kprofiles.INTENT_ACTION)
                val flags =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        Context.RECEIVER_EXPORTED
                    else 0
                registerReceiver(receiver, filter, flags)
                receiverRegistered = true
            }
            updateTile()
        } catch (t: Throwable) {
            Log.w(TAG, "onStartListening failed", t)
        }
    }

    override fun onStopListening() {
        try {
            if (receiverRegistered) {
                runCatching { unregisterReceiver(receiver) }
                receiverRegistered = false
            }
        } catch (t: Throwable) {
            Log.w(TAG, "onStopListening failed", t)
        }
        super.onStopListening()
    }

    override fun onClick() {
        try {
            if (!Kprofiles.IS_SUPPORTED) return

            val next = Kprofiles.Mode.next(currentMode())
            val value = Kprofiles.sanitizeModeValue(next.value)

            val ok = FileUtils.writeLine(Kprofiles.MODES_NODE, value)
            if (!ok) {
                Log.w(TAG, "onClick: write failed for $value")
                return
            }

            PreferenceManager.getDefaultSharedPreferences(this)
                .edit { putString(Kprofiles.MODES_KEY, value) }

            selfChange = true
            sendBroadcast(changeIntent)

            updateTile(value)
        } catch (t: Throwable) {
            Log.e(TAG, "onClick failed", t)
        } finally {
            runCatching { super.onClick() }
        }
    }

    private fun currentMode(): Kprofiles.Mode {
        return try {
            Kprofiles.Mode.from(FileUtils.readOneLine(Kprofiles.MODES_NODE))
        } catch (t: Throwable) {
            Log.w(TAG, "currentMode failed", t)
            Kprofiles.Mode.NONE
        }
    }

    private fun updateTile(modeValue: String? = null) {
        try {
            if (!Kprofiles.IS_SUPPORTED) return
            val tile = qsTile ?: return
            val mode = if (modeValue != null) {
                Kprofiles.Mode.from(modeValue)
            } else {
                currentMode()
            }

            tile.state =
                if (mode != Kprofiles.Mode.NONE) Tile.STATE_ACTIVE
                else Tile.STATE_INACTIVE

            val label = try {
                getString(mode.labelRes)
            } catch (t: Throwable) {
                mode.name
            }

            tile.subtitle = label
            tile.contentDescription = label
            tile.icon = Icon.createWithResource(this, mode.iconRes)
            tile.updateTile()
        } catch (t: Throwable) {
            Log.w(TAG, "updateTile failed", t)
        }
    }
}
