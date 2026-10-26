/*
 * Copyright (C) 2026 The CyanogenMod Project
 * Copyright (C) 2026 The LineageOS Project
 * Copyright (C) 2026 TheParasiteProject
 * Copyright (C) 2026 haloUI
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.kprofiles.battery.KProfilesService
import com.android.kprofiles.utils.FileUtils

class BootCompletedReceiver : BroadcastReceiver() {

    private companion object {
        const val TAG = "KProfiles:Boot"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val ctx = context ?: return
        if (intent == null) return
        if (!Kprofiles.IS_SUPPORTED) return

        if (!KprofilesState.isEnabled(ctx)) {
            FileUtils.writeLine(Kprofiles.AUTO_NODE, Kprofiles.OFF)
            FileUtils.writeLine(Kprofiles.MODES_NODE, Kprofiles.Mode.NONE.value)
            return
        }

        try {
            ctx.startService(Intent(ctx, KProfilesService::class.java))
        } catch (t: Throwable) {
            Log.e(TAG, "failed to start KProfilesService", t)
        }
    }
}
