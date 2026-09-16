/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles

import com.android.kprofiles.utils.FileUtils

object Kprofiles {
    const val INTENT_ACTION = "com.android.kprofiles.battery.KPROFILE_CHANGED"

    const val MODES_NODE = "/sys/kernel/kprofiles/kp_mode"
    const val AUTO_NODE = "/sys/module/kprofiles/parameters/auto_kp"

    const val AUTO_KEY = "kprofiles_auto"
    const val MODES_KEY = "kprofiles_modes"

    const val ON = "Y"
    const val OFF = "N"

    val IS_SUPPORTED: Boolean by lazy { FileUtils.fileExists(MODES_NODE) }

    enum class Mode(
        val value: String,
        val labelRes: Int,
        val descRes: Int,
        val iconRes: Int,
    ) {
        NONE(
            "0",
            R.string.kprofiles_modes_none,
            R.string.kprofiles_modes_none_description,
            R.drawable.ic_kprofiles,
        ),
        BATTERY(
            "1",
            R.string.kprofiles_modes_battery,
            R.string.kprofiles_modes_battery_description,
            R.drawable.ic_leaf,
        ),
        BALANCED(
            "2",
            R.string.kprofiles_modes_balanced,
            R.string.kprofiles_modes_balanced_description,
            R.drawable.ic_balance,
        ),
        PERFORMANCE(
            "3",
            R.string.kprofiles_modes_performance,
            R.string.kprofiles_modes_performance_description,
            R.drawable.ic_rocket,
        );

        companion object {
            fun from(value: String?): Mode {
                val v = value?.trim().orEmpty()
                if (v.isEmpty()) return NONE
                return entries.firstOrNull { it.value == v } ?: NONE
            }

            fun next(mode: Mode?): Mode {
                val m = mode ?: NONE
                return entries[(m.ordinal + 1) % entries.size]
            }
        }
    }

    fun sanitizeModeValue(raw: String?): String {
        val v = raw?.trim().orEmpty()
        return if (entriesContains(v)) v else NONE_VALUE
    }

    private fun entriesContains(v: String): Boolean =
        Mode.entries.any { it.value == v }

    private const val NONE_VALUE = "0"
}
