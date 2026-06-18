/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import dev.astatin3.thirteen.R
import dev.astatin3.thirteen.ext.getViewProperty
import dev.astatin3.thirteen.ext.navigateSafe
import dev.astatin3.thirteen.ext.setProgressCompat
import dev.astatin3.thirteen.ext.toPx
import dev.astatin3.thirteen.models.FlowResult
import dev.astatin3.thirteen.models.Playlist
import dev.astatin3.thirteen.models.PlaylistItem
import dev.astatin3.thirteen.models.SortingStrategy
import dev.astatin3.thirteen.ui.recyclerview.DisplayAwareGridLayoutManager
import dev.astatin3.thirteen.ui.recyclerview.SimpleListAdapter
import dev.astatin3.thirteen.ui.views.PlaylistGridItemView
import dev.astatin3.thirteen.ui.views.SortingChip
import dev.astatin3.thirteen.utils.PermissionsChecker
import dev.astatin3.thirteen.utils.PermissionsUtils
import dev.astatin3.thirteen.viewmodels.PlaylistsViewModel

/**
 * View all music playlists.
 */
class PlaylistsFragment : Fragment(R.layout.fragment_playlists) {
    // View models
    private val viewModel by viewModels<PlaylistsViewModel>()

    // Views
    private val createNewPlaylistFab by getViewProperty<FloatingActionButton>(R.id.createNewPlaylistFab)
    private val createNewPlaylistButton by getViewProperty<Button>(R.id.createNewPlaylistButton)
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsLinearLayout by getViewProperty<LinearLayout>(R.id.noElementsLinearLayout)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val sortingChip by getViewProperty<SortingChip>(R.id.sortingChip)

    // Recyclerview
    private val adapter by lazy {
        object : SimpleListAdapter<PlaylistItem, PlaylistGridItemView>(
            playlistDiffCallback,
            ::PlaylistGridItemView,
        ) {
            override fun ViewHolder.onBindView(item: PlaylistItem) {
                view.setOnClickListener {
                    findNavController().navigateSafe(
                        R.id.action_mainFragment_to_fragment_playlist,
                        PlaylistFragment.createBundle(item.playlist.uri)
                    )
                }
                view.setOnLongClickListener {
                    findNavController().navigateSafe(
                        R.id.action_mainFragment_to_fragment_media_item_bottom_sheet_dialog,
                        MediaItemBottomSheetDialogFragment.createBundle(item.playlist.uri)
                    )
                    true
                }

                view.headlineText = item.playlist.name ?: getString(
                    when (item.playlist.type) {
                        Playlist.Type.PLAYLIST -> R.string.playlist_unknown
                        Playlist.Type.FAVORITES -> R.string.favorites_playlist
                    }
                )
                view.setThumbnail(
                    item.compositeBitmap?.bitmap,
                    when (item.playlist.type) {
                        Playlist.Type.PLAYLIST -> R.drawable.ic_playlist_play
                        Playlist.Type.FAVORITES -> R.drawable.ic_favorite
                    }
                )
            }
        }
    }

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )

            val padding = v.toPx(8)
            v.updatePadding(
                left = insets.left + padding,
                right = insets.right + padding,
                bottom = insets.bottom + padding,
            )

            windowInsets
        }

        sortingChip.setSortingStrategies(
            sortedMapOf(
                SortingStrategy.CREATION_DATE to R.string.sort_by_creation_date,
                SortingStrategy.MODIFICATION_DATE to R.string.sort_by_last_modified,
                SortingStrategy.NAME to R.string.sort_by_name,
                SortingStrategy.PLAY_COUNT to R.string.sort_by_play_count,
            )
        )
        sortingChip.setOnSortingRuleSelectedListener {
            viewModel.setSortingRule(it)
        }

        recyclerView.layoutManager = DisplayAwareGridLayoutManager(recyclerView.context, 2)
        recyclerView.adapter = adapter

        val navigateToCreatePlaylist = {
            findNavController().navigateSafe(
                R.id.action_mainFragment_to_fragment_create_playlist_dialog,
                CreatePlaylistDialogFragment.createBundle(
                    providerIdentifier = viewModel.navigationProvider.value?.identifier
                )
            )
        }

        createNewPlaylistFab.setOnClickListener { navigateToCreatePlaylist() }
        createNewPlaylistButton.setOnClickListener { navigateToCreatePlaylist() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter = null
        recyclerView.layoutManager = null

        super.onDestroyView()
    }

    private suspend fun loadData() {
        coroutineScope {
            launch {
                viewModel.playlistItems.collectLatest {
                    linearProgressIndicator.setProgressCompat(it)

                    when (it) {
                        is FlowResult.Loading -> {
                        }

                        is FlowResult.Success -> {
                            adapter.submitList(it.data)

                            val isEmpty = it.data.isEmpty()
                            recyclerView.isVisible = !isEmpty
                            noElementsLinearLayout.isVisible = isEmpty
                        }

                        is FlowResult.Failure -> {
                            Log.e(
                                LOG_TAG,
                                "Failed to load playlists, error: ${it.error}",
                                it.throwable
                            )

                            adapter.submitList(emptyList())

                            recyclerView.isVisible = false
                            noElementsLinearLayout.isVisible = true
                        }
                    }
                }
            }

            launch {
                viewModel.sortingRule.collectLatest {
                    sortingChip.setSortingRule(it)
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = PlaylistsFragment::class.simpleName!!

        private val playlistDiffCallback = object : DiffUtil.ItemCallback<PlaylistItem>() {
            override fun areItemsTheSame(
                oldItem: PlaylistItem,
                newItem: PlaylistItem,
            ) = oldItem.playlist.uri == newItem.playlist.uri

            override fun areContentsTheSame(
                oldItem: PlaylistItem,
                newItem: PlaylistItem,
            ) = oldItem.playlist.areContentsTheSame(newItem.playlist) &&
                oldItem.compositeBitmap?.bitmap == newItem.compositeBitmap?.bitmap
        }
    }
}
