/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.datasources.subsonic.models

import kotlinx.serialization.Serializable

/**
 * A genre returned in list of genres for an item.
 *
 * Note: OpenSubsonic only.
 *
 * @param name Genre name
 */
@Serializable
data class ItemGenre(
    val name: String,
)
