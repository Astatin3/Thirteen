/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import dev.astatin3.thirteen.models.Error
import dev.astatin3.thirteen.models.FlowResult
import dev.astatin3.thirteen.models.FlowResult.Companion.asFlowResult
import dev.astatin3.thirteen.models.FlowResult.Companion.flatMapLatestData
import dev.astatin3.thirteen.models.FlowResult.Companion.foldLatest
import dev.astatin3.thirteen.models.ProviderIdentifier

open class ProviderViewModel(application: Application) : ThirteenViewModel(application) {
    private val _providerIdentifier = MutableStateFlow<ProviderIdentifier?>(null)

    /**
     * The provider identifiers to manage.
     */
    protected val providerIdentifier = _providerIdentifier.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val provider = providerIdentifier
        .flatMapLatest {
            it?.let { providerIdentifier ->
                providersRepository.provider(providerIdentifier).mapLatest { maybeProvider ->
                    maybeProvider?.let { provider ->
                        FlowResult.Success(provider)
                    } ?: FlowResult.Failure(Error.NOT_FOUND)
                }
            } ?: flowOf(FlowResult.Loading)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    val canBeManaged = provider
        .foldLatest(
            onSuccess = {
                it.type.canBeManaged
            },
            onError = { _, _ ->
                false
            },
        )
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    val status = provider
        .flatMapLatestData {
            mediaRepository.status(it.identifier).asFlowResult()
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            FlowResult.Loading
        )

    fun setProviderIdentifier(providerIdentifier: ProviderIdentifier?) {
        _providerIdentifier.value = providerIdentifier
    }

    /**
     * Delete the provider.
     */
    suspend fun deleteProvider() {
        val providerIdentifier = providerIdentifier.value ?: return

        withContext(Dispatchers.IO) {
            providersRepository.deleteProvider(providerIdentifier)
        }
    }
}
