/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult3(
    val artist: List<ArtistID3>? = null,
    val album: List<AlbumID3>? = null,
    val song: List<Child>? = null,
)
