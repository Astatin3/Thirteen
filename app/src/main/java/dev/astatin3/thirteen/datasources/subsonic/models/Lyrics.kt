/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * Lyrics.
 */
@Serializable
data class Lyrics(
    val value: String,
    val artist: String? = null,
    val title: String? = null,
)
