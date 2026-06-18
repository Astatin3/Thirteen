/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.viewmodels

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.guava.await
import dev.astatin3.thirteen.ThirteenApplication
import dev.astatin3.thirteen.ext.applicationContext
import dev.astatin3.thirteen.ext.eventsFlow
import dev.astatin3.thirteen.ext.resources
import dev.astatin3.thirteen.ext.shuffleModeEnabled
import dev.astatin3.thirteen.ext.typedRepeatMode
import dev.astatin3.thirteen.models.Audio
import dev.astatin3.thirteen.models.RepeatMode
import dev.astatin3.thirteen.services.PlaybackService

/**
 * Base view model for all app view models.
 * Here we keep the shared stuff every fragment could use, like access to the repository and
 * the media controller to interact with the playback service.
 */
abstract class ThirteenViewModel(application: Application) : AndroidViewModel(application) {
    protected val mediaRepository = getApplication<ThirteenApplication>().mediaRepository
    protected val providersRepository = getApplication<ThirteenApplication>().providersRepository
    protected val outputConfigurationRepository =
        getApplication<ThirteenApplication>().outputConfigurationRepository

    @Suppress("EmptyMethod")
    final override fun <T : Application> getApplication() = super.getApplication<T>()

    protected val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(application)!!
    }

    private val sessionToken by lazy {
        SessionToken(
            applicationContext,
            ComponentName(applicationContext, PlaybackService::class.java)
        )
    }

    protected val mediaControllerFlow = channelFlow {
        val mediaController = MediaController.Builder(applicationContext, sessionToken)
            .buildAsync()
            .await()

        trySend(mediaController)

        awaitClose {
            mediaController.release()
        }
    }
        .flowOn(Dispatchers.Main)
        .shareIn(
            viewModelScope,
            SharingStarted.Eagerly,
            replay = 1
        )

    protected val mediaController = mediaControllerFlow
        .stateIn(
            viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    protected val eventsFlow = mediaControllerFlow
        .flatMapLatest { it.eventsFlow() }
        .shareIn(
            viewModelScope,
            SharingStarted.Eagerly,
            replay = 1
        )

    protected var shuffleModeEnabled: Boolean
        get() = mediaController.value?.shuffleModeEnabled ?: false
        set(value) {
            mediaController.value?.apply {
                shuffleModeEnabled = value
                sharedPreferences.shuffleModeEnabled = value
            }
        }

    protected var typedRepeatMode: RepeatMode
        get() = mediaController.value?.typedRepeatMode ?: RepeatMode.NONE
        set(value) {
            mediaController.value?.apply {
                typedRepeatMode = value
                sharedPreferences.typedRepeatMode = value
            }
        }

    fun playAudio(audio: List<Audio>, position: Int) {
        mediaController.value?.apply {
            // Initialize shuffle and repeat modes
            shuffleModeEnabled = sharedPreferences.shuffleModeEnabled
            typedRepeatMode = sharedPreferences.typedRepeatMode

            setMediaItems(audio.map { it.toMedia3MediaItem(resources) }, true)
            prepare()
            seekToDefaultPosition(position)
            play()
        }
    }
}
