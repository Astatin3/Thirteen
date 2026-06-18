/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Collections
import dev.astatin3.thirteen.R
import dev.astatin3.thirteen.ext.Bundle
import dev.astatin3.thirteen.ext.getParcelable
import dev.astatin3.thirteen.ext.getViewProperty
import dev.astatin3.thirteen.ext.loadThumbnail
import dev.astatin3.thirteen.ext.navigateSafe
import dev.astatin3.thirteen.ext.setProgressCompat
import dev.astatin3.thirteen.ext.updatePadding
import dev.astatin3.thirteen.models.Audio
import dev.astatin3.thirteen.models.Error
import dev.astatin3.thirteen.models.FlowResult
import dev.astatin3.thirteen.models.Playlist
import dev.astatin3.thirteen.ui.dialogs.EditTextMaterialAlertDialogBuilder
import dev.astatin3.thirteen.ui.recyclerview.SimpleListAdapter
import dev.astatin3.thirteen.ui.recyclerview.UniqueItemDiffCallback
import dev.astatin3.thirteen.ui.views.FullscreenLoadingProgressBar
import dev.astatin3.thirteen.ui.views.ListItem
import dev.astatin3.thirteen.ThirteenApplication
import dev.astatin3.thirteen.utils.PermissionsChecker
import dev.astatin3.thirteen.utils.PermissionsUtils
import dev.astatin3.thirteen.utils.TimestampFormatter
import dev.astatin3.thirteen.viewmodels.PlaylistViewModel

/**
 * Single playlist viewer.
 */
class PlaylistFragment : CollapsingToolbarLayoutFragment(R.layout.fragment_playlist) {
    // View models
    private val viewModel by viewModels<PlaylistViewModel>()

    // Views
    override val appBarLayout by getViewProperty<AppBarLayout>(R.id.appBarLayout)
    override val coordinatorLayout by getViewProperty<CoordinatorLayout>(R.id.coordinatorLayout)
    private val fullscreenLoadingProgressBar by getViewProperty<FullscreenLoadingProgressBar>(R.id.fullscreenLoadingProgressBar)
    private val infoNestedScrollView by getViewProperty<NestedScrollView?>(R.id.infoNestedScrollView)
    private val linearProgressIndicator by getViewProperty<LinearProgressIndicator>(R.id.linearProgressIndicator)
    private val noElementsNestedScrollView by getViewProperty<NestedScrollView>(R.id.noElementsNestedScrollView)
    private val playAllExtendedFloatingActionButton by getViewProperty<ExtendedFloatingActionButton>(
        R.id.playAllExtendedFloatingActionButton
    )
    private val playlistNameTextView by getViewProperty<TextView>(R.id.playlistNameTextView)
    private val playButtonsLinearLayout by getViewProperty<LinearLayout>(R.id.playButtonsLinearLayout)
    private val recyclerView by getViewProperty<RecyclerView>(R.id.recyclerView)
    private val shufflePlayExtendedFloatingActionButton by getViewProperty<ExtendedFloatingActionButton>(
        R.id.shufflePlayExtendedFloatingActionButton
    )
    private val thumbnailImageView by getViewProperty<ImageView>(R.id.thumbnailImageView)
    private val toolbar by getViewProperty<MaterialToolbar>(R.id.toolbar)
    private val tracksInfoTextView by getViewProperty<TextView>(R.id.tracksInfoTextView)

    // Menu items
    private val deletePlaylistMenuItem get() = toolbar.menu.findItem(R.id.deletePlaylist)
    private val renamePlaylistMenuItem get() = toolbar.menu.findItem(R.id.renamePlaylist)

    // Recyclerview
    private var currentPlaylistAudios = listOf<Audio>()
    private var canReorder = true

    private val adapter by lazy {
        object : SimpleListAdapter<Audio, ListItem>(
            UniqueItemDiffCallback(),
            ::ListItem,
        ) {
            override fun submitList(list: List<Audio>?) {
                val newList = list.orEmpty()
                Log.d(LOG_TAG, "submitList: ${newList.size} items, uri0=${newList.firstOrNull()?.uri}, fromDb=${newList.map { it.title?.take(15) }}")
                currentPlaylistAudios = newList
                super.submitList(list)
            }

            override fun ViewHolder.onPrepareView() {
                view.setLeadingIconImage(R.drawable.ic_music_note)
            }

            override fun ViewHolder.onBindView(item: Audio) {
                val trailingIconView = view.findViewById<ImageView>(R.id.trailingIconImageView)
                if (canReorder) {
                    view.setTrailingIconImage(R.drawable.ic_drag_handle)
                    trailingIconView.setOnTouchListener { v, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(this)
                        }
                        false
                    }
                } else {
                    view.trailingIconImage = null
                    trailingIconView.setOnTouchListener(null)
                }
                view.setOnClickListener {
                    viewModel.playPlaylist(bindingAdapterPosition)
                }
                view.setOnLongClickListener {
                    findNavController().navigateSafe(
                        R.id.action_playlistFragment_to_fragment_media_item_bottom_sheet_dialog,
                        MediaItemBottomSheetDialogFragment.createBundle(
                            item.uri,
                            playlistUri = playlistUri,
                        )
                    )
                    true
                }

                view.headlineText = item.title
                item.artistName?.also {
                    view.supportingText = it
                } ?: view.setSupportingText(R.string.artist_unknown)
                view.trailingSupportingText = item.durationMs?.let {
                    TimestampFormatter.formatTimestampMillis(it)
                }
            }
        }
    }
    private val itemTouchHelperCallback by lazy {
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0,
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition

                Log.d(LOG_TAG, "onMove($from -> $to), curSize=${currentPlaylistAudios.size}")

                Collections.swap(currentPlaylistAudios, from, to)
                recyclerView.adapter!!.notifyItemMoved(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

            override fun isLongPressDragEnabled() = false

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (!canReorder) return
                val uris = try {
                    currentPlaylistAudios.map { audio -> audio.uri }
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "clearView: failed to map URIs", e)
                    return
                }
                Log.d(LOG_TAG, "clearView: drag ended, vmClass=${viewModel::class.java.name}, uris=$uris")
                viewModel.reorderPlaylist(uris)
            }
        }
    }
    private val itemTouchHelper by lazy { ItemTouchHelper(itemTouchHelperCallback) }

    // Arguments
    private val playlistUri: Uri
        get() = requireArguments().getParcelable(ARG_PLAYLIST_URI, Uri::class)!!

    // Permissions
    private val permissionsChecker = PermissionsChecker(
        this, PermissionsUtils.mainPermissions
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())

            v.updatePadding(
                insets,
                start = true,
                end = true,
            )

            windowInsets
        }

        infoNestedScrollView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                v.updatePadding(
                    insets,
                    bottom = true,
                )

                windowInsets
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                insets,
                bottom = true,
            )

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(noElementsNestedScrollView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                insets,
                bottom = true,
            )

            windowInsets
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            playButtonsLinearLayout
        ) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                insets,
                bottom = true,
            )

            windowInsets
        }

        toolbar.setupWithNavController(findNavController())
        toolbar.inflateMenu(R.menu.fragment_playlist_toolbar)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.renamePlaylist -> {
                    showRenamePlaylistAlertDialog()
                    true
                }

                R.id.deletePlaylist -> {
                    showDeletePlaylistAlertDialog()
                    true
                }

                else -> false
            }
        }

        recyclerView.adapter = adapter

        playAllExtendedFloatingActionButton.setOnClickListener {
            viewModel.playPlaylist()
        }

        shufflePlayExtendedFloatingActionButton.setOnClickListener {
            viewModel.shufflePlayPlaylist()
        }

        viewModel.loadPlaylist(playlistUri)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                permissionsChecker.withPermissionsGranted {
                    loadData()
                }
            }
        }
    }

    override fun onDestroyView() {
        itemTouchHelper.attachToRecyclerView(null)
        recyclerView.adapter = null

        super.onDestroyView()
    }

    private fun CoroutineScope.loadData() {
        launch {
            viewModel.playlist.collectLatest {
                linearProgressIndicator.setProgressCompat(it)

                when (it) {
                    is FlowResult.Loading -> {
                        // Do nothing
                    }

                    is FlowResult.Success -> {
                        val (playlist, audios) = it.data
                        Log.d(LOG_TAG, "playlist Flow emitted Success: ${audios.size} items, first3=${audios.take(3).map { a -> a.title?.take(15) }}, type=${playlist.type}")

                        val isPlaylist = playlist.type == Playlist.Type.PLAYLIST
                        if (canReorder != isPlaylist) {
                            canReorder = isPlaylist
                            if (isPlaylist) {
                                itemTouchHelper.attachToRecyclerView(recyclerView)
                            } else {
                                itemTouchHelper.attachToRecyclerView(null)
                            }
                        }

                        val playlistName = playlist.name ?: getString(
                            when (playlist.type) {
                                Playlist.Type.PLAYLIST -> R.string.playlist_unknown
                                Playlist.Type.FAVORITES -> R.string.favorites_playlist
                            }
                        )
                        toolbar.title = playlistName
                        playlistNameTextView.text = playlistName

                        thumbnailImageView.loadThumbnail(
                            playlist.thumbnail,
                            placeholder = when (playlist.type) {
                                Playlist.Type.PLAYLIST -> R.drawable.ic_playlist_play
                                Playlist.Type.FAVORITES -> R.drawable.ic_favorite
                            }
                        )

                        val totalDurationMs = audios.sumOf { audio ->
                            audio.durationMs ?: 0L
                        }
                        val totalDurationMinutes = (totalDurationMs / 1000 / 60).toInt()

                        val tracksCount = resources.getQuantityString(
                            R.plurals.tracks_count,
                            audios.size,
                            audios.size
                        )
                        val tracksDuration = resources.getQuantityString(
                            R.plurals.tracks_duration,
                            totalDurationMinutes,
                            totalDurationMinutes
                        )
                        tracksInfoTextView.text = getString(
                            R.string.tracks_info,
                            tracksCount, tracksDuration
                        )

                        adapter.submitList(audios)

                        val isEmpty = audios.isEmpty()
                        recyclerView.isVisible = !isEmpty
                        noElementsNestedScrollView.isVisible = isEmpty
                        when (isEmpty) {
                            true -> {
                                playAllExtendedFloatingActionButton.hide()
                                shufflePlayExtendedFloatingActionButton.hide()
                            }

                            false -> {
                                playAllExtendedFloatingActionButton.show()
                                shufflePlayExtendedFloatingActionButton.show()
                            }
                        }
                    }

                    is FlowResult.Failure -> {
                        Log.e(
                            LOG_TAG,
                            "Error loading playlist, error: ${it.error}",
                            it.throwable
                        )

                        toolbar.title = ""
                        playlistNameTextView.text = ""

                        adapter.submitList(listOf())

                        recyclerView.isVisible = false
                        noElementsNestedScrollView.isVisible = true
                        playAllExtendedFloatingActionButton.isVisible = false

                        if (it.error == Error.NOT_FOUND) {
                            // Get out of here
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }

        launch {
            viewModel.playlistMetadataCanBeEdited.collectLatest { playlistMetadataCanBeEdited ->
                renamePlaylistMenuItem.isVisible = playlistMetadataCanBeEdited
                deletePlaylistMenuItem.isVisible = playlistMetadataCanBeEdited
            }
        }
    }

    private fun showRenamePlaylistAlertDialog() {
        EditTextMaterialAlertDialogBuilder(requireContext())
            .setText(toolbar.title.toString())
            .setPositiveButton(R.string.rename_playlist_positive) { text ->
                viewLifecycleOwner.lifecycleScope.launch {
                    fullscreenLoadingProgressBar.withProgress {
                        viewModel.renamePlaylist(text)
                    }
                }
            }
            .setTitle(R.string.rename_playlist)
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showDeletePlaylistAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_playlist)
            .setMessage(R.string.delete_playlist_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    fullscreenLoadingProgressBar.withProgress {
                        viewModel.deletePlaylist()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    companion object {
        private const val LOG_TAG = "PlaylistFragment"

        private const val ARG_PLAYLIST_URI = "playlist_uri"

        /**
         * Create a [Bundle] to use as the arguments for this fragment.
         * @param playlistUri The URI of the playlist to display
         */
        fun createBundle(
            playlistUri: Uri,
        ) = Bundle {
            putParcelable(ARG_PLAYLIST_URI, playlistUri)
        }
    }
}
