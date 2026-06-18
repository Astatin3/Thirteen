/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.models

import android.net.Uri

data class PlaylistItem(
    val playlist: Playlist,
    val compositeBitmap: Thumbnail?,
)
