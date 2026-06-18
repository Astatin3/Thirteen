/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.ext

import com.google.android.material.progressindicator.LinearProgressIndicator
import dev.astatin3.thirteen.models.FlowResult

/**
 * @see LinearProgressIndicator.setProgressCompat
 */
fun <T, E> LinearProgressIndicator.setProgressCompat(status: FlowResult<T, E>) {
    when (status) {
        is FlowResult.Loading -> {
            if (!isIndeterminate) {
                hide()
                isIndeterminate = true
            }

            show()
        }

        else -> {
            hide()
        }
    }
}
