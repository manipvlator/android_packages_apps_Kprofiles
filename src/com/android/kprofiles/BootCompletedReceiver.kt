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
import android.util.Log
import androidx.preference.PreferenceManager
import com.android.kprofiles.utils.FileUtils

class BootCompletedReceiver : BroadcastReceiver() {

    private companion object {
        const val TAG = "KProfiles:Boot"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            if (FileUtils.fileExists(Kprofiles.AUTO_NODE)) {
                val auto = prefs.getBoolean(Kprofiles.AUTO_KEY, false)
                FileUtils.writeLine(
                    Kprofiles.AUTO_NODE,
                    if (auto) Kprofiles.ON else Kprofiles.OFF,
                )
            }

            if (Kprofiles.IS_SUPPORTED) {
                val saved: String? = prefs.getString(Kprofiles.MODES_KEY, null)
                val current: String? = FileUtils.readOneLine(Kprofiles.MODES_NODE)
                val mode = Kprofiles.sanitizeModeValue(saved ?: current)
                FileUtils.writeLine(Kprofiles.MODES_NODE, mode)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "onReceive failed", t)
        }
    }
}
