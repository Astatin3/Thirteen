/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.fragments

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dev.astatin3.thirteen.R
import dev.astatin3.thirteen.ext.Bundle
import dev.astatin3.thirteen.ext.getParcelable
import dev.astatin3.thirteen.ext.getViewProperty
import dev.astatin3.thirteen.ext.selectItem
import dev.astatin3.thirteen.models.ProviderIdentifier
import dev.astatin3.thirteen.ui.views.FullscreenLoadingProgressBar
import dev.astatin3.thirteen.viewmodels.CreatePlaylistViewModel

class CreatePlaylistDialogFragment : MaterialDialogFragment(
    R.layout.fragment_create_playlist_dialog
) {
    // View models
    private val viewModel by viewModels<CreatePlaylistViewModel>()

    // Views
    private val cancelMaterialButton by getViewProperty<MaterialButton>(R.id.cancelMaterialButton)
    private val createMaterialButton by getViewProperty<MaterialButton>(R.id.createMaterialButton)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val playlistNameTextInputLayout by getViewProperty<TextInputLayout>(R.id.playlistNameTextInputLayout)
    private val providerAutoCompleteTextView by getViewProperty<MaterialAutoCompleteTextView>(R.id.providerAutoCompleteTextView)
    private val providerTextInputLayout by getViewProperty<TextInputLayout>(R.id.providerTextInputLayout)

    // Arguments
    private val providerIdentifier: ProviderIdentifier?
        get() = arguments?.getParcelable(ARG_PROVIDER_IDENTIFIER, ProviderIdentifier::class)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setProviderIdentifier(providerIdentifier)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        providerAutoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            viewModel.setProviderPosition(position)
        }

        playlistNameTextInputLayout.editText!!.apply {
            setText(viewModel.getPlaylistName())
            doOnTextChanged { text, _, _, _ ->
                playlistNameTextInputLayout.error = null
                viewModel.setPlaylistName(text?.toString() ?: "")
            }
        }

        cancelMaterialButton.setOnClickListener {
            findNavController().navigateUp()
        }

        createMaterialButton.setOnClickListener {
            if (viewModel.isPlaylistNameEmpty()) {
                playlistNameTextInputLayout.error = getString(
                    R.string.create_playlist_error_empty_name
                )
                return@setOnClickListener
            }

            playlistNameTextInputLayout.error = null

            viewLifecycleOwner.lifecycleScope.launch {
                fullscreenLoadingProgressBar.withProgress {
                    viewModel.createPlaylist()
                    findNavController().navigateUp()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadData()
            }
        }
    }

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.providersWithSelection.collectLatest { providersWithSelection ->
                val (providers, position) = providersWithSelection

                providerAutoCompleteTextView.setSimpleItems(
                    providers.map { provider ->
                        getString(
                            R.string.provider_format,
                            provider.name,
                            getString(provider.type.nameStringResId),
                        )
                    }.toTypedArray()
                )

                position?.also {
                    val provider = providers[it]

                    providerAutoCompleteTextView.selectItem(it)
                    providerTextInputLayout.setStartIconDrawable(
                        provider.type.iconDrawableResId
                    )
                }
            }
        }
    }

    companion object {
        private const val ARG_PROVIDER_IDENTIFIER = "provider_identifier"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param providerIdentifier A [ProviderIdentifier] to pre-fill the provider field
         */
        fun createBundle(
            providerIdentifier: ProviderIdentifier? = null,
        ) = Bundle {
            putParcelable(ARG_PROVIDER_IDENTIFIER, providerIdentifier)
        }
    }
}
