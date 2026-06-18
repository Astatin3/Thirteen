/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.astatin3.thirteen.models.FlowResult
import dev.astatin3.thirteen.models.FlowResult.Companion.asFlowResult
import dev.astatin3.thirteen.models.FlowResult.Companion.foldLatest
import dev.astatin3.thirteen.models.FlowResult.Companion.getOrNull
import dev.astatin3.thirteen.models.Playlist
import dev.astatin3.thirteen.models.PlaylistThumbnailCompositor

class PlaylistViewModel(application: Application) : ThirteenViewModel(application) {
    private val playlistUri = MutableStateFlow<Uri?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlist = playlistUri
        .filterNotNull()
        .flatMapLatest {
            mediaRepository.playlist(it)
        }
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    val playlistMetadataCanBeEdited = playlist
        .foldLatest(
            onSuccess = { it.first.type == Playlist.Type.PLAYLIST },
            onError = { _, _ -> false },
        )
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val compositeThumbnail = playlist
        .mapLatest { result ->
            when (result) {
                is FlowResult.Success -> {
                    val audios = result.data.second
                    val thumbnails = audios
                        .distinctBy { it.albumUri ?: it.albumTitle ?: it.uri }
                        .mapNotNull { it.thumbnail }
                        .take(4)
                    if (thumbnails.isNotEmpty()) {
                        try {
                            PlaylistThumbnailCompositor.composite(getApplication(), thumbnails)
                        } catch (_: Exception) {
                            null
                        }
                    } else null
                }
                else -> null
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )

    fun loadPlaylist(playlistUri: Uri) {
        this.playlistUri.value = playlistUri
    }

    suspend fun renamePlaylist(name: String) {
        playlistUri.value?.let { playlistUri ->
            withContext(Dispatchers.IO) {
                mediaRepository.renamePlaylist(playlistUri, name)
            }
        }
    }

    suspend fun deletePlaylist() {
        playlistUri.value?.let { playlistUri ->
            withContext(Dispatchers.IO) {
                mediaRepository.deletePlaylist(playlistUri)
            }
        }
    }

    private var reorderJob: Job? = null

    fun reorderPlaylist(audioUris: List<Uri>) {
        Log.d(LOG_TAG, "reorderPlaylist entered: ${audioUris.size} items, cancelling prev=$reorderJob")
        try {
            reorderJob?.cancel()
            reorderJob = viewModelScope.launch {
                try {
                    playlistUri.value?.let { playlistUri ->
                        Log.d(LOG_TAG, "Persisting reorder for $playlistUri, firstUri=${audioUris.firstOrNull()}")
                        val result = withContext(Dispatchers.IO) {
                            mediaRepository.reorderPlaylist(playlistUri, audioUris)
                        }
                        Log.d(LOG_TAG, "Reorder result: $result")
                    } ?: Log.w(LOG_TAG, "Cannot reorder: playlistUri is null")
                } catch (e: kotlinx.coroutines.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "reorder coroutine failed", e)
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "reorderPlaylist threw before launch", e)
        }
    }

    fun playPlaylist(position: Int = 0) {
        playlist.value.getOrNull()?.second?.takeUnless {
            it.isEmpty()
        }?.let {
            playAudio(it, position)
        }
    }

    fun shufflePlayPlaylist() {
        playlist.value.getOrNull()?.second?.takeUnless {
            it.isEmpty()
        }?.let {
            playAudio(it.shuffled(), 0)
        }
    }

    companion object {
        private const val LOG_TAG = "PlaylistViewModel"
    }
}
