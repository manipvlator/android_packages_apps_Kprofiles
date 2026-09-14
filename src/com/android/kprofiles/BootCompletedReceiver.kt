/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.android.kprofiles.utils.FileUtils

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (FileUtils.fileExists(Kprofiles.AUTO_NODE)) {
            val auto = prefs.getBoolean(Kprofiles.AUTO_KEY, false)
            FileUtils.writeLine(
                Kprofiles.AUTO_NODE,
                if (auto) Kprofiles.ON else Kprofiles.OFF,
            )
        }

        if (Kprofiles.IS_SUPPORTED) {
            val mode = prefs.getString(
                Kprofiles.MODES_KEY,
                FileUtils.readOneLine(Kprofiles.MODES_NODE),
            ) ?: Kprofiles.Mode.NONE.value
            FileUtils.writeLine(Kprofiles.MODES_NODE, mode)
        }
    }
}
