/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 TheParasiteProject
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
import android.os.PowerManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.content.ContextCompat
import com.android.kprofiles.R
import com.android.kprofiles.Kprofiles
import com.android.kprofiles.KprofilesState
import com.android.kprofiles.utils.FileUtils

class KprofilesModesTileService : TileService() {

    private companion object {
        const val TAG = "KProfiles:Tile"
    }

    private lateinit var powerManager: PowerManager

    @Volatile
    private var receiverRegistered = false

    private val changeIntent: Intent = Intent(Kprofiles.INTENT_ACTION).apply {
        setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != Kprofiles.INTENT_ACTION) return
            updateTile()
        }
    }

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(PowerManager::class.java)

        if (!Kprofiles.IS_SUPPORTED) {
            qsTile?.apply {
                state = Tile.STATE_UNAVAILABLE
                updateTile()
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        if (!Kprofiles.IS_SUPPORTED) return

        if (!receiverRegistered) {
            try {
                val filter = IntentFilter(Kprofiles.INTENT_ACTION)
                val flags =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        Context.RECEIVER_EXPORTED
                    else 0
                ContextCompat.registerReceiver(this, receiver, filter, flags)
                receiverRegistered = true
            } catch (t: Throwable) {
                Log.w(TAG, "receiver registration failed", t)
            }
        }
        updateTile()
    }

    override fun onStopListening() {
        if (receiverRegistered) {
            runCatching { unregisterReceiver(receiver) }
            receiverRegistered = false
        }
        super.onStopListening()
    }

    override fun onDestroy() {
        if (receiverRegistered) {
            runCatching { unregisterReceiver(receiver) }
            receiverRegistered = false
        }
        super.onDestroy()
    }

    override fun onClick() {
        try {
            if (!Kprofiles.IS_SUPPORTED) return
            if (!KprofilesState.isEnabled(this) || powerManager.isPowerSaveMode) {
                updateTile()
                return
            }

            val next = Kprofiles.Mode.next(currentNodeMode())
            val ok = FileUtils.writeLine(Kprofiles.MODES_NODE, next.value)
            if (!ok) {
                Log.w(TAG, "onClick: write failed for ${next.value}")
                return
            }

            KprofilesState.setMode(this, next.value)
            sendBroadcast(changeIntent)
            updateTile(next)
        } catch (t: Throwable) {
            Log.e(TAG, "onClick failed", t)
        } finally {
            runCatching { super.onClick() }
        }
    }

    /** Read the mode the kernel currently has, falling back to the saved value. */
    private fun currentNodeMode(): Kprofiles.Mode =
        Kprofiles.Mode.from(
            FileUtils.readOneLine(Kprofiles.MODES_NODE)
                ?: KprofilesState.getMode(this)
        )

    private fun updateTile(mode: Kprofiles.Mode? = null) {
        try {
            val tile = qsTile ?: return
            if (!Kprofiles.IS_SUPPORTED) {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.updateTile()
                return
            }

            if (!KprofilesState.isEnabled(this) || powerManager.isPowerSaveMode) {
                tile.state = Tile.STATE_UNAVAILABLE
                tile.subtitle =
                    if (powerManager.isPowerSaveMode) {
                        getString(R.string.kprofiles_battery_saver_on)
                    } else {
                        null
                    }
                tile.contentDescription = getString(R.string.kprofiles_title)
                tile.updateTile()
                return
            }

            val current = mode ?: currentNodeMode()
            tile.state =
                if (current != Kprofiles.Mode.NONE) Tile.STATE_ACTIVE
                else Tile.STATE_INACTIVE

            val label = try {
                getString(current.labelRes)
            } catch (t: Throwable) {
                current.name
            }

            tile.subtitle = label
            tile.contentDescription = label
            tile.icon = Icon.createWithResource(this, current.iconRes)
            tile.updateTile()
        } catch (t: Throwable) {
            Log.w(TAG, "updateTile failed", t)
        }
    }
}
