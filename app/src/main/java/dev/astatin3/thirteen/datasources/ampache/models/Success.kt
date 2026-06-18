/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Success.
 *
 * @param success The success message
 */
@Serializable
data class Success(
    @SerialName("success") val success: String,
)
