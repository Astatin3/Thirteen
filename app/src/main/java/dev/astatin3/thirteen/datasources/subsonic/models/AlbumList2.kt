/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Serializable
data class AlbumList2(
    val album: List<AlbumID3>,
)
