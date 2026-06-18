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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import dev.astatin3.thirteen.ext.ARTISTS_SORTING_REVERSE_KEY
import dev.astatin3.thirteen.ext.ARTISTS_SORTING_STRATEGY_KEY
import dev.astatin3.thirteen.ext.artistsSortingRule
import dev.astatin3.thirteen.ext.preferenceFlow
import dev.astatin3.thirteen.models.FlowResult
import dev.astatin3.thirteen.models.FlowResult.Companion.asFlowResult
import dev.astatin3.thirteen.models.SortingRule

class ArtistsViewModel(application: Application) : ThirteenViewModel(application) {
    val sortingRule = sharedPreferences.preferenceFlow(
        ARTISTS_SORTING_STRATEGY_KEY,
        ARTISTS_SORTING_REVERSE_KEY,
        getter = SharedPreferences::artistsSortingRule,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val artists = sortingRule
        .flatMapLatest { mediaRepository.artists(it) }
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    fun setSortingRule(sortingRule: SortingRule) {
        sharedPreferences.artistsSortingRule = sortingRule
    }
}
