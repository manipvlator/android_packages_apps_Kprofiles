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
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.android.kprofiles.Kprofiles
import com.android.kprofiles.utils.FileUtils

class KprofilesModesTileService : TileService() {

    private var selfChange = false

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
        if (!Kprofiles.IS_SUPPORTED) {
            qsTile?.apply {
                state = Tile.STATE_UNAVAILABLE
                updateTile()
            }
        }
    }

    override fun onStartListening() {
        if (!Kprofiles.IS_SUPPORTED) return
        super.onStartListening()

        val filter = IntentFilter(Kprofiles.INTENT_ACTION)
        val flags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Context.RECEIVER_EXPORTED
            else 0
        registerReceiver(receiver, filter, flags)

        updateTile()
    }

    override fun onStopListening() {
        if (Kprofiles.IS_SUPPORTED) {
            runCatching { unregisterReceiver(receiver) }
        }
        super.onStopListening()
    }

    override fun onClick() {
        if (!Kprofiles.IS_SUPPORTED) return

        val next = Kprofiles.Mode.next(currentMode())
        FileUtils.writeLine(Kprofiles.MODES_NODE, next.value)
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit { putString(Kprofiles.MODES_KEY, next.value) }

        selfChange = true
        sendBroadcast(
            Intent(Kprofiles.INTENT_ACTION).apply {
                setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY)
            },
        )

        updateTile(next.value)
        super.onClick()
    }

    private fun currentMode(): Kprofiles.Mode =
        Kprofiles.Mode.from(FileUtils.readOneLine(Kprofiles.MODES_NODE))

    private fun updateTile(modeValue: String? = null) {
        if (!Kprofiles.IS_SUPPORTED) return
        val tile = qsTile ?: return
        val mode = modeValue?.let { Kprofiles.Mode.from(it) } ?: currentMode()

        tile.state =
            if (mode != Kprofiles.Mode.NONE) Tile.STATE_ACTIVE
            else Tile.STATE_INACTIVE
        tile.subtitle = getString(mode.labelRes)
        tile.contentDescription = getString(mode.labelRes)
        tile.icon = Icon.createWithResource(this, mode.iconRes)
        tile.updateTile()
    }
}
