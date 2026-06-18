/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.viewmodels

import android.app.Application
import android.media.MediaScannerConnection
import android.os.storage.StorageManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import dev.astatin3.thirteen.ext.Bundle
import dev.astatin3.thirteen.ext.applicationContext
import dev.astatin3.thirteen.services.PlaybackService
import dev.astatin3.thirteen.services.PlaybackService.CustomCommand.Companion.sendCustomCommand

class SettingsViewModel(application: Application) : ThirteenViewModel(application) {
    // System services
    private val storageManager by lazy {
        applicationContext.getSystemService(StorageManager::class.java)
    }

    @OptIn(UnstableApi::class)
    suspend fun toggleOffload(offload: Boolean) {
        withMediaController {
            sendCustomCommand(
                PlaybackService.CustomCommand.TOGGLE_OFFLOAD,
                Bundle {
                    putBoolean(PlaybackService.CustomCommand.ARG_VALUE, offload)
                }
            )
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun toggleSkipSilence(skipSilence: Boolean) {
        withMediaController {
            sendCustomCommand(
                PlaybackService.CustomCommand.TOGGLE_SKIP_SILENCE,
                Bundle {
                    putBoolean(PlaybackService.CustomCommand.ARG_VALUE, skipSilence)
                }
            )
        }
    }

    suspend fun resetLocalStats() {
        mediaRepository.resetLocalStats()
    }

    fun rescanMediaStore() {
        MediaScannerConnection.scanFile(
            applicationContext,
            storageManager.storageVolumes.mapNotNull { it.directory?.absolutePath }.toTypedArray(),
            null,
            null,
        )
    }

    private suspend fun withMediaController(block: suspend MediaController.() -> Unit) {
        mediaController.value?.let {
            block(it)
        }
    }
}
