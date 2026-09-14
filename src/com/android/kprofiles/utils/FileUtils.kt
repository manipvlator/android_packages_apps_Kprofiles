/*
 * Copyright (C) 2016-2026 The CyanogenMod Project
 *               2017-2026 The LineageOS Project
 *               2026 YAAP
 *               2026 haloUI
 *               2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles.utils

import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException

object FileUtils {
    private const val TAG = "FileUtils"

    fun readOneLine(fileName: String): String? = try {
        BufferedReader(FileReader(fileName), 512).use { it.readLine() }
    } catch (e: FileNotFoundException) {
        Log.w(TAG, "No such file $fileName for reading", e)
        null
    } catch (e: IOException) {
        Log.e(TAG, "Could not read from file $fileName", e)
        null
    }

    fun writeLine(fileName: String, value: String): Boolean = try {
        BufferedWriter(FileWriter(fileName)).use { it.write(value) }
        true
    } catch (e: FileNotFoundException) {
        Log.w(TAG, "No such file $fileName for writing", e)
        false
    } catch (e: IOException) {
        Log.e(TAG, "Could not write to file $fileName", e)
        false
    }

    fun fileExists(fileName: String): Boolean = File(fileName).exists()

    fun isFileReadable(fileName: String): Boolean =
        File(fileName).let { it.exists() && it.canRead() }

    fun isFileWritable(fileName: String): Boolean =
        File(fileName).let { it.exists() && it.canWrite() }

    fun delete(fileName: String): Boolean = try {
        File(fileName).delete()
    } catch (e: SecurityException) {
        Log.w(TAG, "SecurityException trying to delete $fileName", e)
        false
    }

    fun rename(srcPath: String, dstPath: String): Boolean = try {
        File(srcPath).renameTo(File(dstPath))
    } catch (e: SecurityException) {
        Log.w(TAG, "SecurityException trying to rename $srcPath to $dstPath", e)
        false
    } catch (e: NullPointerException) {
        Log.e(TAG, "NullPointerException trying to rename $srcPath to $dstPath", e)
        false
    }
}
