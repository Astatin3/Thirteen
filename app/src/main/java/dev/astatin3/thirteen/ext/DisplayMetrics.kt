/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.ext

import android.util.DisplayMetrics
import kotlin.math.roundToInt

/**
 * Convert a device independent pixel (dp) value to a pixel (px) value.
 */
fun DisplayMetrics.toPx(dp: Int): Int = dp.times(density).roundToInt()
