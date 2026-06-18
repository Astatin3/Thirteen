/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import dev.astatin3.thirteen.ext.PLAYLISTS_SORTING_REVERSE_KEY
import dev.astatin3.thirteen.ext.PLAYLISTS_SORTING_STRATEGY_KEY
import dev.astatin3.thirteen.ext.playlistsSortingRule
import dev.astatin3.thirteen.ext.preferenceFlow
import dev.astatin3.thirteen.models.Error
import dev.astatin3.thirteen.models.FlowResult
import dev.astatin3.thirteen.models.FlowResult.Companion.asFlowResult
import dev.astatin3.thirteen.models.FlowResult.Companion.mapLatestData
import dev.astatin3.thirteen.models.Playlist
import dev.astatin3.thirteen.models.PlaylistItem
import dev.astatin3.thirteen.models.PlaylistThumbnailCompositor
import dev.astatin3.thirteen.models.SortingRule
import dev.astatin3.thirteen.models.Thumbnail
import dev.astatin3.thirteen.models.Result

class PlaylistsViewModel(application: Application) : ThirteenViewModel(application) {
    val navigationProvider = mediaRepository.navigationProvider
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null,
        )

    val sortingRule = sharedPreferences.preferenceFlow(
        PLAYLISTS_SORTING_STRATEGY_KEY,
        PLAYLISTS_SORTING_REVERSE_KEY,
        getter = SharedPreferences::playlistsSortingRule,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists = sortingRule
        .flatMapLatest { mediaRepository.playlists(it) }
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlistItems: StateFlow<FlowResult<List<PlaylistItem>, Error>> = playlists
        .mapLatestData { result ->
            coroutineScope {
                result.map { playlist ->
                    async {
                        try {
                            loadPlaylistItem(playlist)
                        } catch (_: Exception) {
                            PlaylistItem(
                                playlist = playlist,
                                compositeBitmap = null
                            )
                        }
                    }
                }.awaitAll()
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    fun setSortingRule(sortingRule: SortingRule) {
        sharedPreferences.playlistsSortingRule = sortingRule
    }

    private suspend fun loadPlaylistItem(playlist: Playlist): PlaylistItem {
        val playlistData = mediaRepository.playlist(playlist.uri).first()
        val thumbnails = if (playlistData is Result.Success) {
            playlistData.data.second
                .distinctBy { it.albumUri ?: it.albumTitle ?: it.uri }
                .mapNotNull { it.thumbnail }
                .take(4)
        } else emptyList()

        val composite = if (thumbnails.isNotEmpty()) {
            PlaylistThumbnailCompositor.composite(
                getApplication(), thumbnails
            )
        } else null

        return PlaylistItem(
            playlist = playlist,
            compositeBitmap = composite?.let {
                Thumbnail.Builder().setBitmap(it).build()
            },
        )
    }
}
