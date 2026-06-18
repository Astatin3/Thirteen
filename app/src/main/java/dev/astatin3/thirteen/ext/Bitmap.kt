/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.ext

import android.graphics.Bitmap
import java.nio.ByteBuffer

fun Bitmap.toByteArray(): ByteArray = ByteBuffer.allocate(rowBytes * height).apply {
    copyPixelsToBuffer(this)
}.array()
