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
import dev.astatin3.thirteen.ext.GENRES_SORTING_REVERSE_KEY
import dev.astatin3.thirteen.ext.GENRES_SORTING_STRATEGY_KEY
import dev.astatin3.thirteen.ext.genresSortingRule
import dev.astatin3.thirteen.ext.preferenceFlow
import dev.astatin3.thirteen.models.FlowResult
import dev.astatin3.thirteen.models.FlowResult.Companion.asFlowResult
import dev.astatin3.thirteen.models.SortingRule

class GenresViewModel(application: Application) : ThirteenViewModel(application) {
    val sortingRule = sharedPreferences.preferenceFlow(
        GENRES_SORTING_STRATEGY_KEY,
        GENRES_SORTING_REVERSE_KEY,
        getter = SharedPreferences::genresSortingRule,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val genres = sortingRule
        .flatMapLatest { mediaRepository.genres(it) }
        .asFlowResult()
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    fun setSortingRule(sortingRule: SortingRule) {
        sharedPreferences.genresSortingRule = sortingRule
    }
}
