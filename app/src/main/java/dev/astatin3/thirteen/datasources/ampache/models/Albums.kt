/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.ampache.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Albums.
 *
 * @param totalCount The total count
 * @param md5 The md5
 * @param album The albums
 */
@Serializable
data class Albums(
    @SerialName("total_count") val totalCount: Int? = null,
    @SerialName("md5") val md5: String? = null,
    @SerialName("album") val album: List<Album>,
)
