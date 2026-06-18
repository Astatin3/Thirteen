/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.jellyfin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QueryResult(
    @SerialName("Items") val items: List<Item>,
)
