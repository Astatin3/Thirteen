/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:UseSerializers(UUIDSerializer::class)

package dev.astatin3.thirteen.datasources.jellyfin.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import dev.astatin3.thirteen.datasources.jellyfin.serializers.UUIDSerializer

@Serializable
data class LyricLine(
    @SerialName("Start") val start: Long,
    @SerialName("Text") val text: String
)
