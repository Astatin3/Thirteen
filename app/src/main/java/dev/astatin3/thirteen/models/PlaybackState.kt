/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.models

/**
 * Player playback status.
 */
enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED,
}
