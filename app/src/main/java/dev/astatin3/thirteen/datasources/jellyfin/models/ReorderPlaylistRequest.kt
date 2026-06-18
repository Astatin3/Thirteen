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
import java.util.UUID

@Serializable
data class ReorderPlaylistRequest(
    @SerialName("PlaylistItemIds") val playlistItemIds: List<UUID>,
)
