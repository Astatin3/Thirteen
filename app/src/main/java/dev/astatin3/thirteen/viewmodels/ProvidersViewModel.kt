/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import dev.astatin3.thirteen.models.Provider
import dev.astatin3.thirteen.models.areItemsTheSame

class ProvidersViewModel(application: Application) : ThirteenViewModel(application) {
    val navigationProvider = mediaRepository.navigationProvider
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null,
        )

    val providersToIsCurrent = combine(
        providersRepository.allProviders,
        navigationProvider,
    ) { allVisibleProviders, navigationProvider ->
        allVisibleProviders.map { provider ->
            provider to provider.areItemsTheSame(navigationProvider)
        }
    }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            listOf(),
        )

    fun setNavigationProvider(provider: Provider) {
        mediaRepository.setNavigationProvider(provider.identifier)
    }
}
