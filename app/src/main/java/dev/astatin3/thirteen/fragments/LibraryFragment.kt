/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.astatin3.thirteen.fragments

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dev.astatin3.thirteen.R
import dev.astatin3.thirteen.ext.getViewProperty

/**
 * Music library.
 */
class LibraryFragment : Fragment(R.layout.fragment_library) {
    // Views
    private val tabLayout by getViewProperty<TabLayout>(R.id.tabLayout)
    private val viewPager2 by getViewProperty<ViewPager2>(R.id.viewPager2)

    // ViewPager2
    private enum class Menus(
        @StringRes val titleStringResId: Int,
        val fragment: () -> Fragment,
    ) {
        PLAYLISTS(
            R.string.library_fragment_menu_playlists,
            { PlaylistsFragment() },
        ),
        ALBUMS(
            R.string.library_fragment_menu_albums,
            { AlbumsFragment() },
        ),
        ARTISTS(
            R.string.library_fragment_menu_artists,
            { ArtistsFragment() },
        ),
        GENRES(
            R.string.library_fragment_menu_genres,
            { GenresFragment() },
        ),
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager2.adapter = object : FragmentStateAdapter(
            childFragmentManager, viewLifecycleOwner.lifecycle
        ) {
            override fun getItemCount() = Menus.entries.size
            override fun createFragment(position: Int) = Menus.entries[position].fragment()
        }
        viewPager2.offscreenPageLimit = Menus.entries.size

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            val menu = Menus.entries[position]

            tab.setText(menu.titleStringResId)
            tab.setContentDescription(menu.titleStringResId)
        }.attach()
    }

    override fun onDestroyView() {
        viewPager2.adapter = null

        super.onDestroyView()
    }
}
