/*
 * Copyright (C) 2026 YAAP
 * Copyright (C) 2026 haloUI
 * Copyright (C) 2026 zenin1504
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.kprofiles.utils

import android.util.Log
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object FileUtils {
    private const val TAG = "KProfiles:FileUtils"

    fun readOneLine(fileName: String?): String? {
        if (fileName.isNullOrBlank()) return null
        return try {
            BufferedReader(FileReader(fileName), 512).use { reader ->
                val line = reader.readLine()
                line?.takeIf { it.isNotBlank() }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "readOneLine($fileName) failed: ${t.javaClass.simpleName}")
            null
        }
    }

    fun writeLine(fileName: String?, value: String?): Boolean {
        if (fileName.isNullOrBlank() || value == null) return false
        return try {
            BufferedWriter(FileWriter(fileName)).use { it.write(value) }
            true
        } catch (t: Throwable) {
            Log.w(TAG, "writeLine($fileName, $value) failed: ${t.javaClass.simpleName}")
            false
        }
    }

    fun fileExists(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        return try {
            File(fileName).exists()
        } catch (t: Throwable) {
            Log.w(TAG, "fileExists($fileName) failed")
            false
        }
    }

    fun isFileReadable(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        return try {
            val f = File(fileName)
            f.exists() && f.canRead() && f.isFile
        } catch (t: Throwable) {
            false
        }
    }

    fun isFileWritable(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        return try {
            val f = File(fileName)
            f.exists() && f.canWrite() && f.isFile
        } catch (t: Throwable) {
            false
        }
    }

    fun delete(fileName: String?): Boolean {
        if (fileName.isNullOrBlank()) return false
        return try {
            File(fileName).delete()
        } catch (t: Throwable) {
            Log.w(TAG, "delete($fileName) failed")
            false
        }
    }

    fun rename(srcPath: String?, dstPath: String?): Boolean {
        if (srcPath.isNullOrBlank() || dstPath.isNullOrBlank()) return false
        return try {
            File(srcPath).renameTo(File(dstPath))
        } catch (t: Throwable) {
            Log.w(TAG, "rename($srcPath, $dstPath) failed")
            false
        }
    }
}
