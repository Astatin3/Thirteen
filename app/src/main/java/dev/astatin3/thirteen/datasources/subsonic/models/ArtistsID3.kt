/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.subsonic.models

import kotlinx.serialization.Serializable

@Serializable
data class ArtistsID3(
    val index: List<IndexID3>,
    val ignoredArticles: String,

    // Navidrome
    val lastModified: Long? = null, // TODO
)
