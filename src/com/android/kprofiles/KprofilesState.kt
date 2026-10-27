/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 TheParasiteProject
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import android.content.Context

/**
 * Persistent KProfiles state. Device-protected storage keeps the master switch,
 * auto mode and selected mode available before the user unlocks the device.
 */
object KprofilesState {
    private const val PREFS_NAME = "kprofiles_preferences"

    const val KEY_ENABLED = "kprofiles_enabled"
    const val KEY_AUTO = "kprofiles_auto"
    const val KEY_MODE = "kprofiles_modes"

    /*
     * Keep the initial boot inert. The user enables KProfiles explicitly; the
     * default mode is 0 and automatic mode is off.
     */
    const val DEFAULT_ENABLED = false

    private fun prefs(context: Context) =
        context.createDeviceProtectedStorageContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, DEFAULT_ENABLED)

    fun setEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()

    fun isAutoEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO, false)

    fun setAuto(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_AUTO, enabled).apply()

    fun getMode(context: Context): String =
        Kprofiles.sanitizeModeValue(
            prefs(context).getString(KEY_MODE, Kprofiles.Mode.NONE.value)
        )

    fun setMode(context: Context, mode: String) =
        prefs(context).edit()
            .putString(KEY_MODE, Kprofiles.sanitizeModeValue(mode))
            .apply()
}
